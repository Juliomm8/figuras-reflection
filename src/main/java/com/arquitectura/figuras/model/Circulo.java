package com.arquitectura.figuras.model;

import java.beans.ConstructorProperties;

public class Circulo implements Figura {

	private double radio;

	public Circulo() {
	}

	@ConstructorProperties({"radio"})
	public Circulo(double radio) {
		this.radio = radio;
	}

	@Override
	public double calcularArea() {
		return Math.PI * radio * radio;
	}

	@Override
	public double calcularPerimetro() {
		return 2 * Math.PI * radio;
	}
}
