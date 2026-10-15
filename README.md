# Reflexión y Máquinas de Interpretación

## Objetivo

Esta aplicación académica demuestra cómo crear figuras geométricas en tiempo de ejecución mediante la API de reflexión de Java. También permite compilar y registrar nuevas figuras escritas en Groovy mientras Spring Boot está ejecutándose, sin agregar un archivo Java ni reiniciar la aplicación.

## Tecnologías

- Java 17
- Spring Boot 4.1.1
- Maven y Maven Wrapper
- Java Reflection API
- Apache Groovy 4.0.28

## Arquitectura

Para crear una figura nativa o previamente registrada, el flujo es:

```text
Cliente
  ↓
FiguraController
  ↓
FiguraFactory
  ↓
Reflection
  ↓
Figura
```

Para cargar una figura nueva durante la ejecución:

```text
Código Groovy
  ↓
GroovyClassLoader
  ↓
Compilación en tiempo de ejecución
  ↓
Verificación e inclusión en el registro dinámico
  ↓
FiguraFactory
  ↓
Figura
```

Los modelos están en `model`, los servicios de creación y compilación en `service`, los endpoints en `controller` y los objetos de entrada/salida en `dto`.

## Figuras nativas

La aplicación incluye `Circulo`, `Cuadrado`, `Rectangulo` y `TrianguloEquilatero`. Todas implementan:

```java
public interface Figura {
    double calcularArea();
    double calcularPerimetro();
}
```

## Ejecución

En Windows, desde la carpeta del proyecto:

```powershell
.\mvnw.cmd spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

## Ejemplos de uso

Crear un círculo:

```http
POST http://localhost:8080/api/figuras/crear
Content-Type: application/json

{
  "clase": "Circulo",
  "parametros": {
    "radio": 5
  }
}
```

Respuesta aproximada:

```json
{
  "figura": "Circulo",
  "area": 78.53981633974483,
  "perimetro": 31.41592653589793
}
```

Crear un rectángulo:

```http
POST http://localhost:8080/api/figuras/crear
Content-Type: application/json

{
  "clase": "Rectangulo",
  "parametros": {
    "base": 10,
    "altura": 5
  }
}
```

Cargar un pentágono con Groovy:

```http
POST http://localhost:8080/api/figuras/cargar-groovy
Content-Type: application/json

{
  "codigo": "import com.arquitectura.figuras.model.Figura\n\nclass Pentagono implements Figura {\n    double lado\n\n    double calcularArea() {\n        return (Math.sqrt(5 * (5 + 2 * Math.sqrt(5))) / 4) * lado * lado\n    }\n\n    double calcularPerimetro() {\n        return 5 * lado\n    }\n}"
}
```

Respuesta:

```json
{
  "mensaje": "Clase cargada correctamente",
  "clase": "Pentagono"
}
```

Sin reiniciar Spring, crear el pentágono inmediatamente:

```http
POST http://localhost:8080/api/figuras/crear
Content-Type: application/json

{
  "clase": "Pentagono",
  "parametros": {
    "lado": 5
  }
}
```

También se puede comprobar que la API está activa con `GET /api/figuras`.

## Cómo funciona Reflection

`Class.forName(...)` localiza una clase a partir de su nombre completo. `getDeclaredConstructor().newInstance()` obtiene y ejecuta su constructor sin parámetros, sin escribir un `new` específico para cada figura. Para cada parámetro, la fábrica busca un `Field`, llama a `setAccessible(true)` para permitir el acceso a campos privados y usa `field.set(...)` después de convertir el valor al tipo declarado. Se soportan tipos numéricos comunes, booleanos y `String`.

## Cómo funciona Groovy en tiempo de ejecución

`GroovyClassLoader` recibe el texto enviado por el cliente y `parseClass(codigo)` lo compila durante la ejecución. La clase resultante se valida con `Figura.class.isAssignableFrom(clase)`. Si cumple el contrato, `GroovyCompilerService` la registra en `FiguraFactory`; por eso puede solicitarse por nombre en la siguiente petición sin reiniciar Spring.

## Pruebas

Ejecutar todas las pruebas con:

```powershell
.\mvnw.cmd clean test
```

Las pruebas cubren reflexión para círculo y rectángulo, conversión y asignación de parámetros, errores de figuras inexistentes, compilación de `Pentagono`, registro dinámico, creación posterior y cálculos.

## Seguridad

Compilar y ejecutar código Groovy arbitrario permite ejecutar instrucciones con los permisos del proceso. Esta funcionalidad se implementa únicamente con fines académicos y demostrativos; en producción se requerirían aislamiento, validación estricta y una política de seguridad independiente.
