package com.arquitectura.figuras.service;

import com.arquitectura.figuras.model.Figura;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FiguraFactory {

	private static final String PAQUETE_FIGURAS_NATIVAS = "com.arquitectura.figuras.model.";

	private final Map<String, Class<? extends Figura>> clasesDinamicas = new ConcurrentHashMap<>();

	public Figura crearFigura(String nombreClase, Map<String, Object> parametros) {
		if (nombreClase == null || nombreClase.isBlank()) {
			throw new FiguraException("El nombre de la clase es obligatorio");
		}

		Class<? extends Figura> clase = localizarClase(nombreClase.trim());
		Map<String, Object> valores = parametros == null ? Collections.emptyMap() : parametros;

		try {
			Constructor<? extends Figura> constructor = clase.getDeclaredConstructor();
			constructor.setAccessible(true);
			Figura figura = constructor.newInstance();
			asignarParametros(figura, clase, valores);
			return figura;
		} catch (NoSuchMethodException exception) {
			throw new FiguraException("La clase " + nombreClase + " debe tener un constructor sin parámetros", exception);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
			throw new FiguraException("No se pudo instanciar la figura " + nombreClase, exception);
		}
	}

	public void registrarClaseDinamica(Class<?> clase) {
		if (clase == null || !Figura.class.isAssignableFrom(clase)) {
			throw new FiguraException("La clase dinámica debe implementar Figura");
		}
		if (clase.isInterface() || Modifier.isAbstract(clase.getModifiers())) {
			throw new FiguraException("La clase dinámica debe ser concreta");
		}

		@SuppressWarnings("unchecked")
		Class<? extends Figura> figuraClass = (Class<? extends Figura>) clase;
		clasesDinamicas.put(clase.getSimpleName(), figuraClass);
	}

	public Map<String, Class<? extends Figura>> obtenerClasesDinamicas() {
		return Collections.unmodifiableMap(clasesDinamicas);
	}

	private Class<? extends Figura> localizarClase(String nombreClase) {
		Class<? extends Figura> dinamica = clasesDinamicas.get(nombreClase);
		if (dinamica != null) {
			return dinamica;
		}

		try {
			String nombreCompleto = nombreClase.contains(".")
					? nombreClase
					: PAQUETE_FIGURAS_NATIVAS + nombreClase;
			Class<?> claseEncontrada = Class.forName(nombreCompleto);
			if (!Figura.class.isAssignableFrom(claseEncontrada)) {
				throw new FiguraException("La clase " + nombreClase + " no implementa Figura");
			}
			@SuppressWarnings("unchecked")
			Class<? extends Figura> figuraClass = (Class<? extends Figura>) claseEncontrada;
			return figuraClass;
		} catch (ClassNotFoundException exception) {
			throw new FiguraException("No existe una figura llamada " + nombreClase, exception);
		}
	}

	private void asignarParametros(Figura figura, Class<?> clase, Map<String, Object> parametros) {
		for (Map.Entry<String, Object> parametro : parametros.entrySet()) {
			Field campo = buscarCampo(clase, parametro.getKey());
			if (campo == null) {
				throw new FiguraException("El parámetro '" + parametro.getKey()
						+ "' no existe en la figura " + clase.getSimpleName());
			}
			if (Modifier.isStatic(campo.getModifiers())) {
				throw new FiguraException("El parámetro '" + parametro.getKey() + "' no es asignable");
			}

			try {
				campo.setAccessible(true);
				campo.set(figura, convertirValor(parametro.getValue(), campo.getType(), parametro.getKey()));
			} catch (IllegalAccessException | IllegalArgumentException exception) {
				throw new FiguraException("El valor del parámetro '" + parametro.getKey()
						+ "' no es compatible con " + campo.getType().getSimpleName(), exception);
			}
		}
	}

	private Field buscarCampo(Class<?> clase, String nombreCampo) {
		Class<?> actual = clase;
		while (actual != null) {
			try {
				return actual.getDeclaredField(nombreCampo);
			} catch (NoSuchFieldException exception) {
				actual = actual.getSuperclass();
			}
		}
		return null;
	}

	private Object convertirValor(Object valor, Class<?> tipoDestino, String nombreParametro) {
		if (valor == null) {
			if (tipoDestino.isPrimitive()) {
				throw new FiguraException("El parámetro '" + nombreParametro + "' no puede ser null");
			}
			return null;
		}
		if (tipoDestino.isInstance(valor)) {
			return valor;
		}

		try {
			if (tipoDestino == double.class || tipoDestino == Double.class) {
				return valor instanceof Number ? ((Number) valor).doubleValue() : Double.parseDouble(valor.toString());
			}
			if (tipoDestino == int.class || tipoDestino == Integer.class) {
				return valor instanceof Number ? ((Number) valor).intValue() : Integer.parseInt(valor.toString());
			}
			if (tipoDestino == float.class || tipoDestino == Float.class) {
				return valor instanceof Number ? ((Number) valor).floatValue() : Float.parseFloat(valor.toString());
			}
			if (tipoDestino == long.class || tipoDestino == Long.class) {
				return valor instanceof Number ? ((Number) valor).longValue() : Long.parseLong(valor.toString());
			}
			if (tipoDestino == short.class || tipoDestino == Short.class) {
				return valor instanceof Number ? ((Number) valor).shortValue() : Short.parseShort(valor.toString());
			}
			if (tipoDestino == byte.class || tipoDestino == Byte.class) {
				return valor instanceof Number ? ((Number) valor).byteValue() : Byte.parseByte(valor.toString());
			}
			if (tipoDestino == boolean.class || tipoDestino == Boolean.class) {
				return Boolean.parseBoolean(valor.toString());
			}
			if (tipoDestino == String.class) {
				return valor.toString();
			}
		} catch (NumberFormatException exception) {
			throw new FiguraException("El valor del parámetro '" + nombreParametro + "' no es válido", exception);
		}

		throw new FiguraException("No se puede convertir el parámetro '" + nombreParametro
				+ "' al tipo " + tipoDestino.getSimpleName());
	}
}
