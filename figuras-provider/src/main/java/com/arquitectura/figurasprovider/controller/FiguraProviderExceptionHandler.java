package com.arquitectura.figurasprovider.controller;

import com.arquitectura.figurasprovider.dto.ErrorResponse;
import com.arquitectura.figurasprovider.service.FiguraProviderException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class FiguraProviderExceptionHandler {

	@ExceptionHandler(FiguraProviderException.class)
	public ResponseEntity<ErrorResponse> manejarFiguraInexistente(FiguraProviderException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(exception.getMessage()));
	}
}
