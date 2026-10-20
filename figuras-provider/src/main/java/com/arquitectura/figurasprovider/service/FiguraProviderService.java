package com.arquitectura.figurasprovider.service;

import com.arquitectura.figurasprovider.dto.FiguraDefinicionResponse;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FiguraProviderService {

	private final Map<String, String> definiciones = Map.of(
			"Pentagono", """
					import com.arquitectura.figuras.model.Figura

					class Pentagono implements Figura {
					    double lado

					    double calcularArea() {
					        return (Math.sqrt(5 * (5 + 2 * Math.sqrt(5))) / 4) * lado * lado
					    }

					    double calcularPerimetro() {
					        return 5 * lado
					    }
					}
					""",
			"Hexagono", """
					import com.arquitectura.figuras.model.Figura

					class Hexagono implements Figura {
					    double lado

					    double calcularArea() {
					        return (3 * Math.sqrt(3) / 2) * lado * lado
					    }

					    double calcularPerimetro() {
					        return 6 * lado
					    }
					}
					""");

	public FiguraDefinicionResponse obtenerFigura(String nombre) {
		String codigo = definiciones.get(nombre);
		if (codigo == null) {
			throw new FiguraProviderException("La figura " + nombre + " no existe en el proveedor");
		}
		return new FiguraDefinicionResponse(nombre, codigo);
	}
}
