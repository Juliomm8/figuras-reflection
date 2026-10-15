package com.arquitectura.figuras.service;

import com.arquitectura.figuras.model.Figura;
import groovy.lang.GroovyClassLoader;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class GroovyCompilerService {

	private final GroovyClassLoader groovyClassLoader;
	private final FiguraFactory figuraFactory;

	public GroovyCompilerService(FiguraFactory figuraFactory) {
		this.figuraFactory = figuraFactory;
		this.groovyClassLoader = new GroovyClassLoader(getClass().getClassLoader());
	}

	public synchronized Class<? extends Figura> compilarYRegistrar(String codigo) {
		if (codigo == null || codigo.isBlank()) {
			throw new FiguraException("El código Groovy es obligatorio");
		}

		final Class<?> claseCompilada;
		try {
			claseCompilada = groovyClassLoader.parseClass(codigo);
		} catch (Exception exception) {
			throw new FiguraException("No se pudo compilar el código Groovy: " + obtenerMensaje(exception), exception);
		}

		if (!Figura.class.isAssignableFrom(claseCompilada)) {
			throw new FiguraException("La clase Groovy " + claseCompilada.getSimpleName()
					+ " debe implementar Figura");
		}

		figuraFactory.registrarClaseDinamica(claseCompilada);
		@SuppressWarnings("unchecked")
		Class<? extends Figura> figuraClass = (Class<? extends Figura>) claseCompilada;
		return figuraClass;
	}

	private String obtenerMensaje(Exception exception) {
		Throwable causa = exception;
		while (causa.getCause() != null && (causa.getMessage() == null || causa.getMessage().isBlank())) {
			causa = causa.getCause();
		}
		return Objects.toString(causa.getMessage(), causa.getClass().getSimpleName());
	}
}
