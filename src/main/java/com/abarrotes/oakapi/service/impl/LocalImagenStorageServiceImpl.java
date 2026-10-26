package com.abarrotes.oakapi.service.impl;

import com.abarrotes.oakapi.exception.StorageException;
import com.abarrotes.oakapi.service.ImagenStorageService;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class LocalImagenStorageServiceImpl implements ImagenStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalImagenStorageServiceImpl.class);

    private final Path rootDir;
    private final Tika tika = new Tika();

    public LocalImagenStorageServiceImpl(@Value("${app.upload.dir:./uploads/productos}") String uploadDir) {
        this.rootDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        log.info("Directorio de uploads configurado: {}", this.rootDir);
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.rootDir);
        } catch (IOException e) {
            throw new StorageException("No se pudo crear el directorio de uploads: " + this.rootDir, e);
        }
    }

    @Override
    public String guardarImagen(MultipartFile archivo, String plu) {
        validarArchivo(archivo);

        try {
            Path directorioPlu = this.rootDir.resolve(plu).normalize();
            if (!Files.exists(directorioPlu)) {
                Files.createDirectories(directorioPlu);
            }

            String extension = obtenerExtension(archivo.getOriginalFilename());
            String nombreUnico = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
                    + "-" + UUID.randomUUID().toString().substring(0, 8) + "." + extension;

            Path destino = this.rootDir.resolve(plu).resolve(nombreUnico).normalize();

            if (!destino.startsWith(this.rootDir)) {
                throw new StorageException("Intento de path traversal detectado");
            }

            try (InputStream inputStream = archivo.getInputStream()) {
                Files.copy(inputStream, destino, StandardCopyOption.REPLACE_EXISTING);
            }

            validarMimeReal(destino);

            return this.rootDir.relativize(destino).toString();

        } catch (IOException e) {
            throw new StorageException("Error al guardar la imagen: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new StorageException("Error inesperado al guardar la imagen: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean eliminarImagen(String rutaRelativa) {
        try {
            Path archivo = this.rootDir.resolve(rutaRelativa).normalize();

            if (!archivo.startsWith(this.rootDir)) {
                throw new StorageException("Intento de path traversal detectado");
            }

            return Files.deleteIfExists(archivo);
        } catch (IOException e) {
            throw new StorageException("Error al eliminar la imagen: " + rutaRelativa, e);
        }
    }

    @Override
    public Resource obtenerImagen(String rutaRelativa) {
        try {
            Path archivo = this.rootDir.resolve(rutaRelativa).normalize();

            if (!archivo.startsWith(this.rootDir)) {
                throw new StorageException("Intento de path traversal detectado");
            }

            if (!Files.exists(archivo)) {
                return null;
            }

            Resource resource = new UrlResource(archivo.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                return null;
            }
        } catch (MalformedURLException e) {
            throw new StorageException("Error al leer la imagen: " + rutaRelativa, e);
        }
    }

    @Override
    public String getContentType(String rutaRelativa) {
        try {
            Path archivo = this.rootDir.resolve(rutaRelativa).normalize();

            if (!archivo.startsWith(this.rootDir)) {
                throw new StorageException("Intento de path traversal detectado");
            }

            if (!Files.exists(archivo)) {
                return "application/octet-stream";
            }

            try {
                String mimeType = tika.detect(Files.newInputStream(archivo));
                if (mimeType != null && !mimeType.equals("application/octet-stream")) {
                    return mimeType;
                }
            } catch (IOException ignored) {
            }

            String extension = obtenerExtension(rutaRelativa).toLowerCase();
            return switch (extension) {
                case "jpg", "jpeg" -> "image/jpeg";
                case "png" -> "image/png";
                case "webp" -> "image/webp";
                default -> "application/octet-stream";
            };
        } catch (Exception e) {
            return "application/octet-stream";
        }
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        long maxSize = 5 * 1024 * 1024;
        if (archivo.getSize() > maxSize) {
            throw new IllegalArgumentException("El archivo supera el tamaño máximo permitido de 5MB");
        }

        String contentType = archivo.getContentType();
        if (contentType == null || !esTipoPermitido(contentType)) {
            throw new IllegalArgumentException("Tipo de archivo no permitido. Solo se permiten: image/jpeg, image/png, image/webp");
        }
    }

    private void validarMimeReal(Path archivo) {
        try (InputStream is = Files.newInputStream(archivo)) {
            String mimeReal = tika.detect(is);
            String extension = obtenerExtension(archivo.getFileName().toString()).toLowerCase();

            String mimeEsperado = switch (extension) {
                case "jpg", "jpeg" -> "image/jpeg";
                case "png" -> "image/png";
                case "webp" -> "image/webp";
                default -> null;
            };

            if (mimeEsperado != null && !mimeReal.equalsIgnoreCase(mimeEsperado)) {
                Files.deleteIfExists(archivo);
                throw new IllegalArgumentException(
                    "El tipo de archivo no coincide con su extensión. Se esperaba " + mimeEsperado + " pero se detectó " + mimeReal
                );
            }
        } catch (IOException e) {
            try {
                Files.deleteIfExists(archivo);
            } catch (IOException ignored) {
            }
            throw new StorageException("Error al validar MIME real del archivo: " + e.getMessage(), e);
        }
    }

    private boolean esTipoPermitido(String contentType) {
        return contentType != null && (contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/webp"));
    }

    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isEmpty()) {
            return "jpg";
        }
        int lastDot = nombreArchivo.lastIndexOf('.');
        if (lastDot == -1) {
            return "jpg";
        }
        String ext = nombreArchivo.substring(lastDot + 1).toLowerCase();
        return switch (ext) {
            case "jpg", "jpeg" -> "jpg";
            case "png" -> "png";
            case "webp" -> "webp";
            default -> "jpg";
        };
    }
}