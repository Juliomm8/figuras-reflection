package com.arquitectura.figuras.model;

public class Rectangulo implements Figura {

	private double base;
	private double altura;

	@Override
	public double calcularArea() {
		return base * altura;
	}

	@Override
	public double calcularPerimetro() {
		return 2 * (base + altura);
	}
}
