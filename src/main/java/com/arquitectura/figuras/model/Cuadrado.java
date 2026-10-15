package com.arquitectura.figuras.model;

public class Cuadrado implements Figura {

	private double lado;

	@Override
	public double calcularArea() {
		return lado * lado;
	}

	@Override
	public double calcularPerimetro() {
		return 4 * lado;
	}
}
