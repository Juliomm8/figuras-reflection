package com.arquitectura.figuras.service;

import com.arquitectura.figuras.model.Figura;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FiguraExternaServiceTest {

	private HttpServer servidorFalso;
	private FiguraFactory figuraFactory;
	private FiguraExternaService figuraExternaService;

	@BeforeEach
	void iniciarProveedorFalso() throws IOException {
		servidorFalso = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		servidorFalso.createContext("/api/figuras", this::responderFigura);
		servidorFalso.start();

		figuraFactory = new FiguraFactory();
		GroovyCompilerService groovyCompilerService = new GroovyCompilerService(figuraFactory);
		String url = "http://localhost:" + servidorFalso.getAddress().getPort();
		figuraExternaService = new FiguraExternaService(RestClient.builder(), url, groovyCompilerService);
	}

	@AfterEach
	void detenerProveedorFalso() {
		servidorFalso.stop(0);
	}

	@Test
	void obtieneCompilaRegistraYPermiteCrearPentagono() {
		Class<? extends Figura> clase = figuraExternaService.cargarFigura("Pentagono");
		Figura pentagono = figuraFactory.crearFigura("Pentagono", Map.of("lado", 5));
		double areaEsperada = (Math.sqrt(5 * (5 + 2 * Math.sqrt(5))) / 4) * 25;

		assertEquals("Pentagono", clase.getSimpleName());
		assertEquals(areaEsperada, pentagono.calcularArea(), 0.0000001);
		assertEquals(25, pentagono.calcularPerimetro(), 0.0000001);
	}

	@Test
	void informaCuandoLaFiguraNoExisteEnElProveedor() {
		FiguraException exception = assertThrows(FiguraException.class,
				() -> figuraExternaService.cargarFigura("Inexistente"));

		assertEquals("La figura Inexistente no existe en el proveedor externo", exception.getMessage());
	}

	private void responderFigura(HttpExchange exchange) throws IOException {
		String path = exchange.getRequestURI().getPath();
		if (path.endsWith("/Pentagono")) {
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
			responder(exchange, 200, "{\"nombre\":\"Pentagono\",\"codigo\":\""
					+ escaparJson(codigo) + "\"}");
			return;
		}
		responder(exchange, 404, "{\"error\":\"No existe\"}");
	}

	private void responder(HttpExchange exchange, int estado, String cuerpo) throws IOException {
		byte[] contenido = cuerpo.getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().add("Content-Type", "application/json");
		exchange.sendResponseHeaders(estado, contenido.length);
		try (var salida = exchange.getResponseBody()) {
			salida.write(contenido);
		}
	}

	private String escaparJson(String valor) {
		return valor.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\r", "\\r")
				.replace("\n", "\\n");
	}
}
