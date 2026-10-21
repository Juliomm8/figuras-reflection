package com.arquitectura.figuras.model;

import java.beans.ConstructorProperties;

public class Triangulo implements Figura {

	private double ladoA;
	private double ladoB;
	private double ladoC;

	public Triangulo() {
	}

	@ConstructorProperties({"ladoA", "ladoB", "ladoC"})
	public Triangulo(double ladoA, double ladoB, double ladoC) {
		this.ladoA = ladoA;
		this.ladoB = ladoB;
		this.ladoC = ladoC;
	}

	@Override
	public double calcularArea() {
		double semiperimetro = calcularPerimetro() / 2;
		return Math.sqrt(semiperimetro
				* (semiperimetro - ladoA)
				* (semiperimetro - ladoB)
				* (semiperimetro - ladoC));
	}

	@Override
	public double calcularPerimetro() {
		return ladoA + ladoB + ladoC;
	}
}
