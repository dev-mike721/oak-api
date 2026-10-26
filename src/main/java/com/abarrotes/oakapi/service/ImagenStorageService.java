package com.abarrotes.oakapi.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface ImagenStorageService {

    /**
     * Guarda una imagen en el almacenamiento local y retorna la ruta relativa.
     *
     * @param archivo archivo de imagen a guardar
     * @param plu     PLU del producto para organizar por directorio
     * @return ruta relativa de la imagen guardada (ej: productos/PLU/timestamp-uuid.jpg)
     */
    String guardarImagen(org.springframework.web.multipart.MultipartFile archivo, String plu);

    /**
     * Elimina una imagen del almacenamiento.
     *
     * @param rutaRelativa ruta relativa de la imagen a eliminar
     * @return true si se eliminó correctamente, false si no existía
     */
    boolean eliminarImagen(String rutaRelativa);

    /**
     * Obtiene una imagen del almacenamiento como Resource de Spring.
     *
     * @param rutaRelativa ruta relativa de la imagen
     * @return Resource de Spring listo para streaming, o null si no existe
     */
    org.springframework.core.io.Resource obtenerImagen(String rutaRelativa);

    /**
     * Obtiene el Content-Type de una imagen almacenada basándose en su extensión.
     *
     * @param rutaRelativa ruta relativa de la imagen
     * @return Content-Type MIME (ej: image/jpeg, image/png, image/webp)
     */
    String getContentType(String rutaRelativa);
}