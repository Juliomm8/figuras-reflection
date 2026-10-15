package com.arquitectura.figuras.model;

import java.beans.ConstructorProperties;

public class Rectangulo implements Figura {

	private double base;
	private double altura;

	public Rectangulo() {
	}

	@ConstructorProperties({"base", "altura"})
	public Rectangulo(double base, double altura) {
		this.base = base;
		this.altura = altura;
	}

	@Override
	public double calcularArea() {
		return base * altura;
	}

	@Override
	public double calcularPerimetro() {
		return 2 * (base + altura);
	}
}
