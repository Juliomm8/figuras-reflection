package com.arquitectura.figuras.controller;

import com.arquitectura.figuras.dto.ErrorResponse;
import com.arquitectura.figuras.service.FiguraException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class FiguraExceptionHandler {

	@ExceptionHandler({FiguraException.class, IllegalArgumentException.class, HttpMessageNotReadableException.class})
	public org.springframework.http.ResponseEntity<ErrorResponse> manejarSolicitudInvalida(Exception exception) {
		String mensaje = exception instanceof HttpMessageNotReadableException
				? "El cuerpo de la solicitud no tiene un JSON válido"
				: exception.getMessage();
		return org.springframework.http.ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ErrorResponse(mensaje == null ? "Solicitud inválida" : mensaje));
	}
}
