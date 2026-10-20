package com.arquitectura.figurasprovider.controller;

import com.arquitectura.figurasprovider.dto.FiguraDefinicionResponse;
import com.arquitectura.figurasprovider.service.FiguraProviderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/figuras")
public class FiguraProviderController {

	private final FiguraProviderService figuraProviderService;

	public FiguraProviderController(FiguraProviderService figuraProviderService) {
		this.figuraProviderService = figuraProviderService;
	}

	@GetMapping("/{nombre}")
	public FiguraDefinicionResponse obtenerFigura(@PathVariable String nombre) {
		return figuraProviderService.obtenerFigura(nombre);
	}
}
