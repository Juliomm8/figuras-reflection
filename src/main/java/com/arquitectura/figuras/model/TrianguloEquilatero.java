package com.arquitectura.figuras.model;

import java.beans.ConstructorProperties;

public class TrianguloEquilatero implements Figura {

	private double lado;

	public TrianguloEquilatero() {
	}

	@ConstructorProperties({"lado"})
	public TrianguloEquilatero(double lado) {
		this.lado = lado;
	}

	@Override
	public double calcularArea() {
		return (Math.sqrt(3) / 4) * lado * lado;
	}

	@Override
	public double calcularPerimetro() {
		return 3 * lado;
	}
}
