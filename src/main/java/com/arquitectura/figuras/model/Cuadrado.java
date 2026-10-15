package com.arquitectura.figuras.model;

import java.beans.ConstructorProperties;

public class Cuadrado implements Figura {

	private double lado;

	public Cuadrado() {
	}

	@ConstructorProperties({"lado"})
	public Cuadrado(double lado) {
		this.lado = lado;
	}

	@Override
	public double calcularArea() {
		return lado * lado;
	}

	@Override
	public double calcularPerimetro() {
		return 4 * lado;
	}
}
