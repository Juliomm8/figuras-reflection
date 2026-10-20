package com.arquitectura.figuras.controller;

import com.arquitectura.figuras.dto.CargarGroovyRequest;
import com.arquitectura.figuras.dto.CargarGroovyResponse;
import com.arquitectura.figuras.dto.CrearFiguraRequest;
import com.arquitectura.figuras.dto.FigurasDisponiblesResponse;
import com.arquitectura.figuras.dto.FiguraResponse;
import com.arquitectura.figuras.dto.ProveedorEstadoResponse;
import com.arquitectura.figuras.model.Figura;
import com.arquitectura.figuras.service.FiguraFactory;
import com.arquitectura.figuras.service.FiguraExternaService;
import com.arquitectura.figuras.service.GroovyCompilerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/figuras")
public class FiguraController {

	private final FiguraFactory figuraFactory;
	private final GroovyCompilerService groovyCompilerService;
	private final FiguraExternaService figuraExternaService;

	public FiguraController(FiguraFactory figuraFactory, GroovyCompilerService groovyCompilerService,
			FiguraExternaService figuraExternaService) {
		this.figuraFactory = figuraFactory;
		this.groovyCompilerService = groovyCompilerService;
		this.figuraExternaService = figuraExternaService;
	}

	@GetMapping
	public ResponseEntity<Map<String, String>> estado() {
		return ResponseEntity.ok(Map.of("mensaje", "API de figuras funcionando"));
	}

	@GetMapping("/disponibles")
	public FigurasDisponiblesResponse disponibles() {
		return new FigurasDisponiblesResponse(
				figuraFactory.obtenerNombresFigurasNativas(),
				figuraFactory.obtenerClasesDinamicas().keySet().stream().sorted().collect(Collectors.toList()));
	}

	@GetMapping("/proveedor/estado")
	public ProveedorEstadoResponse estadoProveedor() {
		return new ProveedorEstadoResponse(figuraExternaService.proveedorDisponible());
	}

	@PostMapping("/crear")
	public FiguraResponse crear(@RequestBody CrearFiguraRequest request) {
		if (request == null || request.clase() == null || request.clase().isBlank()) {
			throw new IllegalArgumentException("El campo 'clase' es obligatorio");
		}

		Figura figura = figuraFactory.crearFigura(request.clase(), request.parametros());
		return new FiguraResponse(request.clase().trim(), figura.calcularArea(), figura.calcularPerimetro());
	}

	@PostMapping("/cargar-groovy")
	public CargarGroovyResponse cargarGroovy(@RequestBody CargarGroovyRequest request) {
		if (request == null || request.codigo() == null || request.codigo().isBlank()) {
			throw new IllegalArgumentException("El campo 'codigo' es obligatorio");
		}

		Class<? extends Figura> clase = groovyCompilerService.compilarYRegistrar(request.codigo());
		return new CargarGroovyResponse("Clase cargada correctamente", clase.getSimpleName());
	}

	@PostMapping("/cargar-externa/{nombre}")
	public CargarGroovyResponse cargarExterna(@PathVariable String nombre) {
		Class<? extends Figura> clase = figuraExternaService.cargarFigura(nombre);
		return new CargarGroovyResponse("Figura externa cargada correctamente", clase.getSimpleName());
	}
}
