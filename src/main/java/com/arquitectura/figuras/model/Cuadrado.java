package com.arquitectura.figuras.model;

import java.beans.ConstructorProperties;

public class Cuadrado extends Rectangulo {

	public Cuadrado() {
		super();
	}

	@ConstructorProperties({"lado"})
	public Cuadrado(double lado) {
		super(lado, lado);
	}
}
