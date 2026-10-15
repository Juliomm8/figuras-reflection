package com.arquitectura.figuras.service;

public class FiguraException extends RuntimeException {

	public FiguraException(String message) {
		super(message);
	}

	public FiguraException(String message, Throwable cause) {
		super(message, cause);
	}
}
