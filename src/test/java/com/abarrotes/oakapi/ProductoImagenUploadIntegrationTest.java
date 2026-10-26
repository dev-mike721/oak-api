package com.abarrotes.oakapi;

import com.abarrotes.oakapi.dto.response.ProductoImagenUploadResponseDto;
import com.abarrotes.oakapi.exception.ResourceNotFoundException;
import com.abarrotes.oakapi.exception.StorageException;
import com.abarrotes.oakapi.model.Categoria;
import com.abarrotes.oakapi.model.Producto;
import com.abarrotes.oakapi.model.enums.UnidadMedida;
import com.abarrotes.oakapi.repository.CategoriaRepository;
import com.abarrotes.oakapi.repository.ProductoRepository;
import com.abarrotes.oakapi.service.ImagenStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(TestDatabaseConfig.class)
class ProductoImagenUploadIntegrationTest {

    @DynamicPropertySource
    static void testProperties(DynamicPropertyRegistry registry) {
        registry.add("app.upload.dir", () -> "/tmp/uploads/productos");
    }

    private static final String SUFIJO = String.valueOf(System.nanoTime());

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ImagenStorageService imagenStorageService;

    private MockMvc mockMvc;
    private Integer productoId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        Categoria categoria = categoriaRepository.findAll().get(0);

        Producto producto = productoRepository.save(Producto.builder()
                .plu("IMG" + SUFIJO)
                .nombre("Producto Imagen Test")
                .categoria(categoria)
                .unidadMedida(UnidadMedida.PIEZA)
                .precioBase(new BigDecimal("10.00"))
                .esCompuesto(false)
                .activo(true)
                .build());
        productoId = producto.getId();
    }

    private MockMultipartFile imagenValida() {
        // Minimal valid JPEG header (SOI marker + APP0)
        byte[] jpegContent = new byte[]{
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10,
                (byte) 0x4A, (byte) 0x46, (byte) 0x49, (byte) 0x46, 0x00, 0x01,
                0x01, 0x01, 0x00, (byte) 0x48, 0x00, (byte) 0x48, 0x00, 0x00
        };
        return new MockMultipartFile("archivo", "test.jpg", MediaType.IMAGE_JPEG_VALUE, jpegContent);
    }

    private MockMultipartFile imagenPng() {
        // Minimal valid PNG header
        byte[] pngContent = new byte[]{
                (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47, 0x0D, 0x0A,
                0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D, (byte) 0x49, (byte) 0x48,
                (byte) 0x44, (byte) 0x52, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00,
                0x00, 0x01, 0x08, 0x02, 0x00, 0x00, 0x00, (byte) 0x90, (byte) 0x77,
                (byte) 0x53, (byte) 0xDE, 0x00, 0x00, 0x00, 0x0C, (byte) 0x49,
                (byte) 0x44, (byte) 0x41, (byte) 0x54, 0x08, (byte) 0xD7, (byte) 0x63,
                (byte) 0xF8, 0x0F, 0x00, 0x01, 0x01, 0x00, 0x05, 0x00, 0x1D,
                (byte) 0xE8, 0x00, 0x00, 0x00, 0x00, (byte) 0x49, (byte) 0x45,
                (byte) 0x4E, (byte) 0x44, (byte) 0xAE, (byte) 0x42, (byte) 0x60,
                (byte) 0x82
        };
        return new MockMultipartFile("archivo", "test.png", MediaType.IMAGE_PNG_VALUE, pngContent);
    }

    private MockMultipartFile imagenWebp() {
        // Minimal valid WebP header (RIFF + WEBP)
        byte[] webpContent = new byte[]{
                (byte) 0x52, (byte) 0x49, (byte) 0x46, (byte) 0x46, 0x2A, 0x00,
                0x00, 0x00, (byte) 0x57, (byte) 0x45, (byte) 0x42, (byte) 0x50,
                (byte) 0x56, (byte) 0x50, (byte) 0x38, 0x20, 0x2A, 0x00, 0x00,
                0x00, 0x2F, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        };
        return new MockMultipartFile("archivo", "test.webp", "image/webp", webpContent);
    }

    private MockMultipartFile archivoInvalidoTipo(String contentType, String originalFilename, byte[] content) {
        return new MockMultipartFile("archivo", originalFilename, contentType, content);
    }

    // ==================== POST /productos/{id}/foto ====================

    @Test
    void subirImagenJpegExitosa() throws Exception {
        mockMvc.perform(multipart("/productos/{id}/foto", productoId)
                        .file(imagenValida())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imagenUrl").value("/api/v1/productos/" + productoId + "/foto/archivo"))
                .andExpect(jsonPath("$.nombreOriginal").value("test.jpg"))
                .andExpect(jsonPath("$.tamañoBytes").value(20))
                .andExpect(jsonPath("$.contentType").value("image/jpeg"));

        Producto producto = productoRepository.findById(productoId).orElseThrow();
        assertNotNull(producto.getImagenStorageKey());
        assertEquals("image/jpeg", producto.getImagenContentType());
        assertEquals("test.jpg", producto.getImagenNombreOriginal());
        assertEquals(20L, producto.getImagenSizeBytes());
        assertNotNull(producto.getImagenUpdatedAt());
    }

    @Test
    void subirImagenPngExitosa() throws Exception {
        mockMvc.perform(multipart("/productos/{id}/foto", productoId)
                        .file(imagenPng())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contentType").value("image/png"));
    }

    @Test
    void subirImagenWebpExitosa() throws Exception {
        mockMvc.perform(multipart("/productos/{id}/foto", productoId)
                        .file(imagenWebp())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contentType").value("image/webp"));
    }

    @Test
    void rechazaTipoNoSoportado() throws Exception {
        MockMultipartFile gif = new MockMultipartFile(
                "archivo", "test.gif", "image/gif", "gif content".getBytes());

        mockMvc.perform(multipart("/productos/{id}/foto", productoId)
                        .file(gif)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rechazaTamanioExcesivo() throws Exception {
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB > 5MB limit
        MockMultipartFile largeFile = new MockMultipartFile(
                "archivo", "large.jpg", MediaType.IMAGE_JPEG_VALUE, largeContent);

        mockMvc.perform(multipart("/productos/{id}/foto", productoId)
                        .file(largeFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isPayloadTooLarge());
    }

    @Test
    void rechazaMimeRealVsExtension() throws Exception {
        // Archivo con extensión .jpg pero contenido PNG (MIME real image/png)
        MockMultipartFile fakeJpeg = new MockMultipartFile(
                "archivo", "fake.jpg", MediaType.IMAGE_JPEG_VALUE,
                "PNG fake content".getBytes());

        mockMvc.perform(multipart("/productos/{id}/foto", productoId)
                        .file(fakeJpeg)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rechazaArchivoVacio() throws Exception {
        MockMultipartFile empty = new MockMultipartFile(
                "archivo", "empty.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[0]);

        mockMvc.perform(multipart("/productos/{id}/foto", productoId)
                        .file(empty)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void productoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(multipart("/productos/{id}/foto", 99999)
                        .file(imagenValida())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isNotFound());
    }

    // ==================== PUT /productos/{id}/foto/archivo ====================

    @Test
    void actualizarImagenArchivo() throws Exception {
        // Primero subimos una imagen
        mockMvc.perform(multipart("/productos/{id}/foto", productoId)
                        .file(imagenValida())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());

        String storageKeyAntes = productoRepository.findById(productoId).orElseThrow().getImagenStorageKey();

        // Ahora actualizamos con PNG
        mockMvc.perform(multipart("/productos/{id}/foto/archivo", productoId)
                        .file(imagenPng())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contentType").value("image/png"))
                .andExpect(jsonPath("$.nombreOriginal").value("test.png"));

        Producto producto = productoRepository.findById(productoId).orElseThrow();
        assertNotEquals(storageKeyAntes, producto.getImagenStorageKey());
        assertEquals("image/png", producto.getImagenContentType());
    }

    // ==================== GET /productos/{id}/foto/archivo ====================

    @Test
    void descargarImagenArchivo() throws Exception {
        mockMvc.perform(multipart("/productos/{id}/foto", productoId)
                        .file(imagenValida())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());

        byte[] expectedJpeg = new byte[]{
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10,
                (byte) 0x4A, (byte) 0x46, (byte) 0x49, (byte) 0x46, 0x00, 0x01,
                0x01, 0x01, 0x00, (byte) 0x48, 0x00, (byte) 0x48, 0x00, 0x00
        };

        mockMvc.perform(get("/productos/{id}/foto/archivo", productoId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"test.jpg\""))
                .andExpect(content().bytes(expectedJpeg));
    }

    @Test
    void descargarImagenInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/productos/{id}/foto/archivo", productoId))
                .andExpect(status().isNotFound());
    }

    @Test
    void descargarImagenProductoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/productos/{id}/foto/archivo", 99999))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETE /productos/{id}/foto/archivo ====================

    @Test
    void eliminarImagenArchivo() throws Exception {
        mockMvc.perform(multipart("/productos/{id}/foto", productoId)
                        .file(imagenValida())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());

        Producto producto = productoRepository.findById(productoId).orElseThrow();
        assertNotNull(producto.getImagenStorageKey());

        mockMvc.perform(delete("/productos/{id}/foto/archivo", productoId))
                .andExpect(status().isNoContent());

        producto = productoRepository.findById(productoId).orElseThrow();
        assertNull(producto.getImagenUrl());
        assertNull(producto.getImagenStorageKey());
        assertNull(producto.getImagenContentType());
        assertNull(producto.getImagenNombreOriginal());
        assertNull(producto.getImagenSizeBytes());
        assertNull(producto.getImagenUpdatedAt());
    }

    @Test
    void eliminarImagenInexistenteNoFalla() throws Exception {
        mockMvc.perform(delete("/productos/{id}/foto/archivo", productoId))
                .andExpect(status().isNoContent());
    }

    // ==================== Regresión: edición de producto preserva la imagen ====================

    @Test
    void editarProductoSinImagenUrlPreservaImagenSubida() throws Exception {
        mockMvc.perform(multipart("/productos/{id}/foto", productoId)
                        .file(imagenValida())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());

        Producto antes = productoRepository.findById(productoId).orElseThrow();
        assertNotNull(antes.getImagenUrl());
        String storageKey = antes.getImagenStorageKey();
        String imagenUrlEsperada = "/api/v1/productos/" + productoId + "/foto/archivo";

        String payload = """
                {"plu":"%s","nombre":"Producto Editado","descripcion":"editado",
                 "categoriaId":%d,"unidadMedida":"PIEZA","permiteDecimales":false,
                 "precioBase":12.00,"esCompuesto":false}
                """.formatted(antes.getPlu(), antes.getCategoria().getId());

        mockMvc.perform(put("/productos/{id}", productoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagenUrl").value(imagenUrlEsperada));

        Producto despues = productoRepository.findById(productoId).orElseThrow();
        assertEquals(imagenUrlEsperada, despues.getImagenUrl());
        assertEquals(storageKey, despues.getImagenStorageKey());
    }

    // ==================== Compatibilidad Legacy (URL externa) ====================

    @Test
    void legacyActualizarImagenUrlExterna() throws Exception {
        String urlExterna = "https://example.com/imagen.jpg";

        mockMvc.perform(put("/productos/{id}/foto", productoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"imagenUrl\":\"" + urlExterna + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagenUrl").value(urlExterna));
    }

    @Test
    void legacyObtenerImagenUrlExterna() throws Exception {
        String urlExterna = "https://example.com/imagen.jpg";
        productoRepository.findById(productoId).ifPresent(p -> {
            p.setImagenUrl(urlExterna);
            productoRepository.save(p);
        });

        mockMvc.perform(get("/productos/{id}/foto", productoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagenUrl").value(urlExterna));
    }

    @Test
    void legacyEliminarImagenUrlExterna() throws Exception {
        String urlExterna = "https://example.com/imagen.jpg";
        productoRepository.findById(productoId).ifPresent(p -> {
            p.setImagenUrl(urlExterna);
            productoRepository.save(p);
        });

        mockMvc.perform(delete("/productos/{id}/foto", productoId))
                .andExpect(status().isNoContent());

        assertNull(productoRepository.findById(productoId).orElseThrow().getImagenUrl());
    }
}