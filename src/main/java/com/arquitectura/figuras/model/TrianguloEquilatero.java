package com.arquitectura.figuras.model;

public class TrianguloEquilatero implements Figura {

	private double lado;

	@Override
	public double calcularArea() {
		return (Math.sqrt(3) / 4) * lado * lado;
	}

	@Override
	public double calcularPerimetro() {
		return 3 * lado;
	}
}
