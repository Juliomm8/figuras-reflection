package com.arquitectura.figuras.dto;

import java.util.Map;

public record CrearFiguraRequest(String clase, Map<String, Object> parametros) {
}
