# Ciudad y conducción con OpenGL

Simulador de conducción en una ciudad 3D hecho con **Java 17, Maven, LWJGL 3.3.3 y OpenGL 3.3 Core** (shaders GLSL, VAO y VBO). El jugador maneja un auto por una ciudad de 11 × 11 celdas, con ciclo de día y noche, farolas, faros, semáforos y un minimapa, y tiene que completar un recorrido de tres entregas.

Toda la geometría se genera con código a partir de un único cubo: no usa imágenes, texturas ni modelos externos.

**Autor:** Gerson Enrique Villarroel Poma

---

## Ejecución

### Requisitos
- JDK 17 o superior (`java -version`).
- Maven (`mvn -version`).
- Una pantalla y una placa de video compatible con OpenGL 3.3.

### Comando para ejecutar la versión final

Abrir una terminal **en la carpeta del proyecto** (donde está `pom.xml`) y ejecutar:

```sh
mvn compile exec:exec
```

Abre `clase4`, que es la versión final con todas las funciones. La primera vez Maven descarga las dependencias, así que puede tardar un poco. **ESC** cierra la ventana.

### Otros comandos

```sh
mvn test                                                  # pruebas automáticas
mvn compile exec:exec -DmainClass=com.graphics.clase1     # solo la ciudad
mvn compile exec:exec -DmainClass=com.graphics.clase2     # ciudad + auto
mvn compile exec:exec -DmainClass=com.graphics.clase3     # ciudad + auto + iluminación
```

También se puede ejecutar desde IntelliJ: abrir `clase4.java` y ejecutar su método `main`.

**Notas:**
- Hay que usar `exec:exec` (no `exec:java`).
- En macOS el `pom.xml` agrega `-XstartOnFirstThread` automáticamente. Si se ejecuta desde un IDE en Mac, hay que agregar ese argumento a la JVM.
- Incluye las bibliotecas nativas para Windows x64, Linux x64/ARM64 y macOS Intel/Apple Silicon.
- Con versiones de Java más nuevas que 17 pueden aparecer advertencias de LWJGL en la consola (acceso nativo, Unsafe). No afectan al funcionamiento.

---

## Controles

| Tecla | Acción |
|---|---|
| W / ↑ | Acelerar |
| S / ↓ | Frenar y retroceder |
| A / ← y D / → | Girar (el giro depende de la velocidad) |
| Espacio | Freno fuerte |
| C | Cambiar entre cámara de seguimiento y vista aérea |
| N | Día / noche |
| F | Encender / apagar los faros |
| M | Mostrar / ocultar el minimapa |
| R | Reiniciar el auto y el recorrido de entregas |
| ESC | Salir |

En `clase1` (solo la ciudad), las flechas izquierda/derecha giran la cámara alrededor de la ciudad.

**Indicador:** el título de la ventana muestra en todo momento la velocidad en km/h, si es de día o de noche, el estado de los faros, el progreso de las entregas y el tiempo. Si la ventana es muy angosta el título puede cortarse; en ese caso conviene maximizarla.

---

## Diseño de la ciudad

La ciudad sale de una matriz de 11 × 11 (`MAPA` en `clase1`), donde `0` es calle, `1` es edificio y `2` es parque. Cada celda mide 10 unidades, así que el mundo ocupa 110 × 110.

```
fila 0   0 0 0 0 0 0 0 0 0 0 0   ← calle del borde norte
fila 1   0 1 0 1 0 2 0 1 0 1 0
fila 2   0 0 0 0 0 0 0 0 0 0 0   ← avenida
fila 3   0 2 0 1 0 1 0 1 0 2 0
fila 4   0 0 0 0 0 0 0 0 0 0 0
fila 5   0 1 0 2 0 1 0 2 0 1 0   ← centro
fila 6   0 0 0 0 0 0 0 0 0 0 0
fila 7   0 1 0 1 0 2 0 1 0 1 0
fila 8   0 0 0 0 0 0 0 0 0 0 0
fila 9   0 2 0 1 0 1 0 1 0 2 0
fila 10  0 0 0 0 0 0 0 0 0 0 0   ← calle del borde sur
```

- **Cuadrícula:** las filas y columnas pares son siempre calle y las manzanas van en las impares. Cada manzana queda rodeada de calle, así que toda la red vial está conectada y se puede llegar a cualquier punto.
- **25 manzanas:** 17 con edificio y 8 parques.
- **Edificios:** cada uno tiene una altura (entre 5 y 13) y un color distintos según su posición, con techo y ventanas en las cuatro fachadas.
- **Parques:** césped, cuatro árboles y un banco.
- **Calles:** asfalto, líneas amarillas entre los cruces, un paso de cebra y un semáforo junto a cada manzana.
- **Iluminación:** 25 farolas, una en la esquina de cada manzana.

---

## Funciones implementadas

### Conducción y colisiones
- Aceleración, freno, reversa y giro proporcional a la velocidad. Todo el movimiento usa el tiempo entre cuadros (`deltaTime`), así que la velocidad no depende de los FPS.
- Colisiones contra las manzanas y los bordes del mapa: el auto se modela como un círculo y se detiene al chocar.
- Cámara de seguimiento detrás del auto y vista aérea de toda la ciudad (tecla C).

### Iluminación
- **Día y noche** (tecla N): luz ambiental y luz direccional (sol) distintas en cada modo.
- **Luz difusa con normales** (modelo de Lambert): cada cara recibe más o menos luz según hacia dónde mira, así que techos y paredes se ven distintos.
- **25 farolas** que iluminan el suelo y los edificios cercanos de noche, con una intensidad que baja con la distancia.
- **Faros del auto** (tecla F): dos focos que siguen la posición y la orientación del auto, con forma de cono y un alcance máximo de unas 28 unidades.
- **Emisión separada de la iluminación:** las bombillas, los faros encendidos y las ventanas de noche "brillan" con su color, pero eso no ilumina nada; la luz que reciben las superficies se calcula aparte en el shader.

### Funciones de la ciudad
- **Decoración:** árboles y bancos en los parques, ventanas en los edificios (encendidas de noche) y pasos de cebra junto a las manzanas.
- **Semáforos animados:** secuencia rojo (5 s) → verde (5 s) → amarillo (2 s) que se repite siempre. Son solo visuales.
- **Minimapa** (tecla M): vista superior en la esquina superior derecha con toda la ciudad, el auto (posición y orientación) y el destino activo, con el norte arriba. Mantiene su forma y su lugar al redimensionar la ventana y no altera la vista principal.
- **Entregas:** tres destinos marcados con una plataforma dorada y una baliza. Para completar cada uno hay que llegar y frenar encima. Al terminar el título muestra "GANASTE" con el tiempo.
- **Reinicio** (tecla R): devuelve el auto al punto de partida, detenido y mirando al norte, y reinicia las entregas y el cronómetro para volver a jugar.

---

## Organización del código

El proyecto se organiza por responsabilidad en cuatro clases que se heredan una a otra. Cada una agrega un área y reutiliza la anterior con `super`:

```text
clase1                → base: ventana, ciclo principal, shaders, cubo (VAO/VBO), mapa y cámara
  └── clase2          → conducción: física del auto, teclado, colisiones, cámaras, modelo del auto
        └── clase3    → iluminación: día/noche, farolas, faros, shader con luz
              └── clase4  → funciones: entregas, decoración, semáforos, minimapa y reinicio
```

Cada clase tiene su propio `main`, así que se puede ejecutar cada etapa por separado.

Archivos:
- `src/main/java/com/graphics/clase1.java` … `clase4.java`: código fuente.
- `src/test/java/com/graphics/ConduccionTest.java`: pruebas de calles transitables, colisiones, bordes y reinicio (se ejecutan sin abrir la ventana).
- `pom.xml`: dependencias y configuración de Maven.

---

## Limitaciones

- **Sin sombras:** la iluminación es local, así que la luz de una farola o de los faros puede "atravesar" un edificio.
- **Semáforos solo visuales:** no detienen al auto, y todos cambian de color al mismo tiempo.
- **Sin tráfico ni peatones:** el único vehículo es el del jugador. Tampoco hay sonido.
- **Choques simples:** al chocar el auto se detiene, sin rebote ni daño. La colisión usa un círculo, así que en las esquinas de las manzanas el choque se siente un poco antes de tocar la acera.
- **Indicador en el título:** no hay texto dibujado dentro de la ventana; la información está en el título.
- **Gráficos con cajas:** todo se arma con cubos de colores, sin texturas ni modelos.
- **Vista aérea fija:** en la versión con auto la vista aérea no se puede girar, porque las flechas se usan para manejar.
- **FPS muy bajos:** si el juego baja de 20 FPS, el movimiento se ralentiza a propósito (el paso de tiempo se limita a 0.05 s para que el auto no atraviese manzanas tras una pausa).

---

## Recursos externos

- **Proyecto base del curso:** este trabajo parte del proyecto de referencia entregado en la materia (las clases `clase1` a `clase4` originales, basadas en `AppCamara.java` y `AppLaberinto.java`). Sobre esa base se hicieron las modificaciones pedidas en el enunciado:
  - ampliar la ciudad de 7 × 7 a 11 × 11 y adaptar el suelo, los límites, las cámaras, el minimapa, los destinos y las pruebas;
  - llevar las farolas de 9 a 25, limitar el alcance de los faros y hacer que los faros brillen al encenderlos;
  - corregir que los semáforos se detuvieran al terminar la partida y rehacer los pasos peatonales.
- **[LWJGL 3.3.3](https://www.lwjgl.org/)** (GLFW y OpenGL): biblioteca para crear la ventana, leer el teclado y usar OpenGL desde Java.
- **[JUnit 3.8.1](https://junit.org/)**: framework de las pruebas automáticas.
- **Asistente de IA:** parte del análisis del código, de las modificaciones y de la documentación se hizo con ayuda de un asistente de inteligencia artificial (Claude, de Anthropic). Los cambios fueron revisados y probados por el autor.

No se usaron imágenes, texturas, modelos 3D ni otros fragmentos de código externos.
