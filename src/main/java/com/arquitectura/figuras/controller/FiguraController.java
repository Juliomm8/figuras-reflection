package com.arquitectura.figuras.controller;

import com.arquitectura.figuras.dto.CargarGroovyRequest;
import com.arquitectura.figuras.dto.CargarGroovyResponse;
import com.arquitectura.figuras.dto.CrearFiguraRequest;
import com.arquitectura.figuras.dto.FiguraResponse;
import com.arquitectura.figuras.model.Figura;
import com.arquitectura.figuras.service.FiguraFactory;
import com.arquitectura.figuras.service.GroovyCompilerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/figuras")
public class FiguraController {

	private final FiguraFactory figuraFactory;
	private final GroovyCompilerService groovyCompilerService;

	public FiguraController(FiguraFactory figuraFactory, GroovyCompilerService groovyCompilerService) {
		this.figuraFactory = figuraFactory;
		this.groovyCompilerService = groovyCompilerService;
	}

	@GetMapping
	public ResponseEntity<Map<String, String>> estado() {
		return ResponseEntity.ok(Map.of("mensaje", "API de figuras funcionando"));
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
}
