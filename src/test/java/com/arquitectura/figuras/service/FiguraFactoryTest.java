package com.arquitectura.figuras.service;

import com.arquitectura.figuras.model.Figura;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FiguraFactoryTest {

	private FiguraFactory figuraFactory;
	private GroovyCompilerService groovyCompilerService;

	@BeforeEach
	void configurarServicios() {
		figuraFactory = new FiguraFactory();
		groovyCompilerService = new GroovyCompilerService(figuraFactory);
	}

	@Test
	void creaCirculoMedianteReflectionYAsignaRadio() {
		Figura circulo = figuraFactory.crearFigura("Circulo", Map.of("radio", 5));

		assertInstanceOf(Figura.class, circulo);
		assertEquals(Math.PI * 25, circulo.calcularArea(), 0.0000001);
		assertEquals(2 * Math.PI * 5, circulo.calcularPerimetro(), 0.0000001);
	}

	@Test
	void creaRectanguloMedianteReflection() {
		Figura rectangulo = figuraFactory.crearFigura("Rectangulo", Map.of("base", 10, "altura", 5));

		assertEquals(50, rectangulo.calcularArea(), 0.0000001);
		assertEquals(30, rectangulo.calcularPerimetro(), 0.0000001);
	}

	@Test
	void rechazaFiguraInexistente() {
		FiguraException exception = assertThrows(FiguraException.class,
				() -> figuraFactory.crearFigura("PentagonoNoRegistrado", Map.of("lado", 5)));

		assertEquals("No existe una figura llamada PentagonoNoRegistrado", exception.getMessage());
	}

	@Test
	void compilaRegistraYCreaPentagonoSinReiniciar() {
		String codigo = """
				import com.arquitectura.figuras.model.Figura

				class Pentagono implements Figura {
				    double lado

				    double calcularArea() {
				        return (Math.sqrt(5 * (5 + 2 * Math.sqrt(5))) / 4) * lado * lado
				    }

				    double calcularPerimetro() {
				        return 5 * lado
				    }
				}
				""";

				Class<? extends Figura> clase = groovyCompilerService.compilarYRegistrar(codigo);
				Figura pentagono = figuraFactory.crearFigura("Pentagono", Map.of("lado", 5));
				double areaEsperada = (Math.sqrt(5 * (5 + 2 * Math.sqrt(5))) / 4) * 25;

				assertEquals("Pentagono", clase.getSimpleName());
				assertEquals(areaEsperada, pentagono.calcularArea(), 0.0000001);
				assertEquals(25, pentagono.calcularPerimetro(), 0.0000001);
	}
}
