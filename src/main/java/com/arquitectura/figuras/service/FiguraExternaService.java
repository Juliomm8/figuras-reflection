package com.arquitectura.figuras.service;

import com.arquitectura.figuras.dto.FiguraExternaResponse;
import com.arquitectura.figuras.model.Figura;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class FiguraExternaService {

	private final RestClient restClient;
	private final GroovyCompilerService groovyCompilerService;

	public FiguraExternaService(RestClient.Builder restClientBuilder,
			@Value("${figuras.provider.url}") String providerUrl,
			GroovyCompilerService groovyCompilerService) {
		if (providerUrl == null || providerUrl.isBlank()) {
			throw new IllegalArgumentException("La URL del proveedor de figuras es obligatoria");
		}
		this.restClient = restClientBuilder.baseUrl(providerUrl).build();
		this.groovyCompilerService = groovyCompilerService;
	}

	public Class<? extends Figura> cargarFigura(String nombre) {
		if (nombre == null || nombre.isBlank()) {
			throw new FiguraException("El nombre de la figura externa es obligatorio");
		}

		FiguraExternaResponse respuesta;
		try {
			respuesta = restClient.get()
					.uri("/api/figuras/{nombre}", nombre.trim())
					.retrieve()
					.body(FiguraExternaResponse.class);
		} catch (HttpClientErrorException.NotFound exception) {
			throw new FiguraException("La figura " + nombre + " no existe en el proveedor externo", exception);
		} catch (RestClientException exception) {
			throw new FiguraException("No se pudo consultar el proveedor externo de figuras", exception);
		}

		if (respuesta == null || respuesta.codigo() == null || respuesta.codigo().isBlank()) {
			throw new FiguraException("El proveedor externo devolvió una respuesta sin código Groovy");
		}

		try {
			return groovyCompilerService.compilarYRegistrar(respuesta.codigo());
		} catch (FiguraException exception) {
			throw new FiguraException("El código de la figura externa no es válido: " + exception.getMessage(), exception);
		}
	}
}
