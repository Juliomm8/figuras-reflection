package com.arquitectura.figuras.model;

public class Circulo implements Figura {

	private double radio;

	@Override
	public double calcularArea() {
		return Math.PI * radio * radio;
	}

	@Override
	public double calcularPerimetro() {
		return 2 * Math.PI * radio;
	}
}
