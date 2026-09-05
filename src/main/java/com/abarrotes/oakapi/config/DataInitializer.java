package com.abarrotes.oakapi.config;

import com.abarrotes.oakapi.model.Categoria;
import com.abarrotes.oakapi.model.Producto;
import com.abarrotes.oakapi.model.ProductoCompuesto;
import com.abarrotes.oakapi.model.ProductoCompuestoId;
import com.abarrotes.oakapi.model.enums.UnidadMedida;
import com.abarrotes.oakapi.repository.CategoriaRepository;
import com.abarrotes.oakapi.repository.ProductoCompuestoRepository;
import com.abarrotes.oakapi.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final ProductoCompuestoRepository productoCompuestoRepository;

    @Override
    public void run(String... args) {
        if (categoriaRepository.count() == 0) {

            Categoria botanas = Categoria.builder()
                    .nombre("Botanas")
                    .descripcion("Frituras, papas y botanas variadas")
                    .build();
            Categoria bebidas = Categoria.builder()
                    .nombre("Bebidas")
                    .descripcion("Refrescos, jugos y aguas")
                    .build();
            Categoria abarrotes = Categoria.builder()
                    .nombre("Abarrotes General")
                    .descripcion("Productos básicos del hogar y cocina")
                    .build();
            Categoria combos = Categoria.builder()
                    .nombre("Combos y Paquetes")
                    .descripcion("Productos armados a partir de otros productos")
                    .build();

            categoriaRepository.saveAll(List.of(botanas, bebidas, abarrotes, combos));

            if (productoRepository.count() == 0) {

                // --- 10 productos simples ---
                Producto sabritas = Producto.builder()
                        .plu("75010001")
                        .nombre("Sabritas Sal 45g")
                        .descripcion("Papas fritas con sal")
                        .categoria(botanas)
                        .unidadMedida(UnidadMedida.PIEZA)
                        .permiteDecimales(false)
                        .precioBase(new BigDecimal("18.50"))
                        .esCompuesto(false)
                        .activo(true)
                        .build();

                Producto doritos = Producto.builder()
                        .plu("75010003")
                        .nombre("Doritos Nacho 62g")
                        .descripcion("Tortillas de maíz sabor nacho")
                        .categoria(botanas)
                        .unidadMedida(UnidadMedida.PIEZA)
                        .permiteDecimales(false)
                        .precioBase(new BigDecimal("19.00"))
                        .esCompuesto(false)
                        .activo(true)
                        .build();

                Producto cocaCola = Producto.builder()
                        .plu("75010002")
                        .nombre("Coca Cola 600ml")
                        .descripcion("Refresco de cola no retornable")
                        .categoria(bebidas)
                        .unidadMedida(UnidadMedida.PIEZA)
                        .permiteDecimales(false)
                        .precioBase(new BigDecimal("20.00"))
                        .esCompuesto(false)
                        .activo(true)
                        .build();

                Producto aguaCiel = Producto.builder()
                        .plu("75010004")
                        .nombre("Agua Ciel 1L")
                        .descripcion("Agua purificada")
                        .categoria(bebidas)
                        .unidadMedida(UnidadMedida.PIEZA)
                        .permiteDecimales(false)
                        .precioBase(new BigDecimal("15.00"))
                        .esCompuesto(false)
                        .activo(true)
                        .build();

                Producto arroz = Producto.builder()
                        .plu("75020001")
                        .nombre("Arroz a Granel")
                        .descripcion("Arroz blanco, precio por kilogramo")
                        .categoria(abarrotes)
                        .unidadMedida(UnidadMedida.KG)
                        .permiteDecimales(true)
                        .precioBase(new BigDecimal("24.00"))
                        .esCompuesto(false)
                        .activo(true)
                        .build();

                Producto frijol = Producto.builder()
                        .plu("75020002")
                        .nombre("Frijol a Granel")
                        .descripcion("Frijol negro, precio por kilogramo")
                        .categoria(abarrotes)
                        .unidadMedida(UnidadMedida.KG)
                        .permiteDecimales(true)
                        .precioBase(new BigDecimal("32.00"))
                        .esCompuesto(false)
                        .activo(true)
                        .build();

                Producto azucar = Producto.builder()
                        .plu("75020003")
                        .nombre("Azúcar Estándar 1kg")
                        .descripcion("Azúcar refinada, bolsa de 1kg")
                        .categoria(abarrotes)
                        .unidadMedida(UnidadMedida.PIEZA)
                        .permiteDecimales(false)
                        .precioBase(new BigDecimal("22.00"))
                        .esCompuesto(false)
                        .activo(true)
                        .build();

                Producto bolillo = Producto.builder()
                        .plu("75020004")
                        .nombre("Bolillo")
                        .descripcion("Pan blanco individual")
                        .categoria(abarrotes)
                        .unidadMedida(UnidadMedida.PIEZA)
                        .permiteDecimales(false)
                        .precioBase(new BigDecimal("3.50"))
                        .esCompuesto(false)
                        .activo(true)
                        .build();

                Producto jamon = Producto.builder()
                        .plu("75020005")
                        .nombre("Jamón de Pavo a Granel")
                        .descripcion("Jamón de pavo rebanado, precio por gramo")
                        .categoria(abarrotes)
                        .unidadMedida(UnidadMedida.GRAMO)
                        .permiteDecimales(true)
                        .precioBase(new BigDecimal("0.15")) // equivale a $150.00/kg
                        .esCompuesto(false)
                        .activo(true)
                        .build();

                Producto queso = Producto.builder()
                        .plu("75020006")
                        .nombre("Queso Amarillo a Granel")
                        .descripcion("Queso amarillo rebanado, precio por gramo")
                        .categoria(abarrotes)
                        .unidadMedida(UnidadMedida.GRAMO)
                        .permiteDecimales(true)
                        .precioBase(new BigDecimal("0.18")) // equivale a $180.00/kg
                        .esCompuesto(false)
                        .activo(true)
                        .build();

                // --- 3 productos compuestos (cabecera; los componentes van abajo) ---
                Producto comboRefrescoPapas = Producto.builder()
                        .plu("80010001")
                        .nombre("Combo Refresco y Papas")
                        .descripcion("Coca Cola 600ml + Sabritas Sal 45g")
                        .categoria(combos)
                        .unidadMedida(UnidadMedida.PIEZA)
                        .permiteDecimales(false)
                        .precioBase(new BigDecimal("35.00"))
                        .esCompuesto(true)
                        .activo(true)
                        .build();

                Producto bolsaBotanera = Producto.builder()
                        .plu("80010002")
                        .nombre("Bolsa Botanera Mixta")
                        .descripcion("2 Sabritas Sal 45g + 2 Doritos Nacho 62g")
                        .categoria(combos)
                        .unidadMedida(UnidadMedida.PIEZA)
                        .permiteDecimales(false)
                        .precioBase(new BigDecimal("70.00"))
                        .esCompuesto(true)
                        .activo(true)
                        .build();

                Producto tortaJamonQueso = Producto.builder()
                        .plu("80010003")
                        .nombre("Torta de Jamón y Queso")
                        .descripcion("Bolillo + jamón de pavo + queso amarillo")
                        .categoria(combos)
                        .unidadMedida(UnidadMedida.PIEZA)
                        .permiteDecimales(false)
                        .precioBase(new BigDecimal("32.00"))
                        .esCompuesto(true)
                        .activo(true)
                        .build();

                productoRepository.saveAll(List.of(
                        sabritas, doritos, cocaCola, aguaCiel,
                        arroz, frijol, azucar, bolillo, jamon, queso,
                        comboRefrescoPapas, bolsaBotanera, tortaJamonQueso
                ));

                if (productoCompuestoRepository.count() == 0) {

                    // Combo Refresco y Papas: 1 Coca Cola + 1 Sabritas
                    ProductoCompuesto comp1a = ProductoCompuesto.builder()
                            .id(new ProductoCompuestoId(comboRefrescoPapas.getId(), cocaCola.getId()))
                            .productoPadre(comboRefrescoPapas)
                            .productoHijo(cocaCola)
                            .cantidad(new BigDecimal("1"))
                            .build();

                    ProductoCompuesto comp1b = ProductoCompuesto.builder()
                            .id(new ProductoCompuestoId(comboRefrescoPapas.getId(), sabritas.getId()))
                            .productoPadre(comboRefrescoPapas)
                            .productoHijo(sabritas)
                            .cantidad(new BigDecimal("1"))
                            .build();

                    // Bolsa Botanera Mixta: 2 Sabritas + 2 Doritos
                    ProductoCompuesto comp2a = ProductoCompuesto.builder()
                            .id(new ProductoCompuestoId(bolsaBotanera.getId(), sabritas.getId()))
                            .productoPadre(bolsaBotanera)
                            .productoHijo(sabritas)
                            .cantidad(new BigDecimal("2"))
                            .build();

                    ProductoCompuesto comp2b = ProductoCompuesto.builder()
                            .id(new ProductoCompuestoId(bolsaBotanera.getId(), doritos.getId()))
                            .productoPadre(bolsaBotanera)
                            .productoHijo(doritos)
                            .cantidad(new BigDecimal("2"))
                            .build();

                    // Torta de Jamón y Queso: 1 bolillo + 80g jamón + 40g queso
                    ProductoCompuesto comp3a = ProductoCompuesto.builder()
                            .id(new ProductoCompuestoId(tortaJamonQueso.getId(), bolillo.getId()))
                            .productoPadre(tortaJamonQueso)
                            .productoHijo(bolillo)
                            .cantidad(new BigDecimal("1"))
                            .build();

                    ProductoCompuesto comp3b = ProductoCompuesto.builder()
                            .id(new ProductoCompuestoId(tortaJamonQueso.getId(), jamon.getId()))
                            .productoPadre(tortaJamonQueso)
                            .productoHijo(jamon)
                            .cantidad(new BigDecimal("80")) // 80 gramos
                            .build();

                    ProductoCompuesto comp3c = ProductoCompuesto.builder()
                            .id(new ProductoCompuestoId(tortaJamonQueso.getId(), queso.getId()))
                            .productoPadre(tortaJamonQueso)
                            .productoHijo(queso)
                            .cantidad(new BigDecimal("40")) // 40 gramos
                            .build();

                    productoCompuestoRepository.saveAll(List.of(
                            comp1a, comp1b, comp2a, comp2b, comp3a, comp3b, comp3c
                    ));
                }
            }
        }
    }
}