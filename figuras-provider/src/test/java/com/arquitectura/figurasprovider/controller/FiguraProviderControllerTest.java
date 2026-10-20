package com.arquitectura.figurasprovider.controller;

import com.arquitectura.figurasprovider.service.FiguraProviderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FiguraProviderControllerTest {

	private MockMvc mockMvc;

	@BeforeEach
	void configurarMockMvc() {
		mockMvc = MockMvcBuilders
				.standaloneSetup(new FiguraProviderController(new FiguraProviderService()))
				.setControllerAdvice(new FiguraProviderExceptionHandler())
				.build();
	}

	@Test
	void devuelvePentagonoConSuCodigoGroovy() throws Exception {
		mockMvc.perform(get("/api/figuras/Pentagono"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("Pentagono"))
				.andExpect(jsonPath("$.codigo").value(containsString("class Pentagono")));
	}

	@Test
	void devuelve404CuandoLaFiguraNoExiste() throws Exception {
		mockMvc.perform(get("/api/figuras/Inexistente"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("La figura Inexistente no existe en el proveedor"));
	}
}
