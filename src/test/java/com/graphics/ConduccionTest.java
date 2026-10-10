package com.graphics; // Permite probar los métodos protegidos del juego desde el mismo paquete.

import junit.framework.TestCase; // Proporciona las comprobaciones de JUnit usadas por Maven.

/** Comprueba las reglas de conducción sin abrir una ventana OpenGL. */
public class ConduccionTest extends TestCase {

    /** Comprueba que las calles del recorrido conectan el inicio y las entregas. */
    public void testCallesYDestinosTransitables() {
        clase2 juego = new clase2(); // Crea el estado del juego sin ejecutar run().
        // El recorrido sale de (-50, 50), va a (50, -50), después a (-50, -50) y vuelve. Todo por las calles del borde.
        for (float z = -50; z <= 50; z += 0.25f) { // Recorre posiciones a lo largo de la calle del borde oeste.
            assertTrue(juego.puedeCircular(-50, z)); // Conecta la salida con la segunda entrega.
        }
        for (float x = -50; x <= 50; x += 0.25f) { // Recorre la calle del borde norte.
            assertTrue(juego.puedeCircular(x, -50)); // Conecta las dos primeras entregas.
        }
        for (float z = -30; z <= 30; z += 0.25f) { // Una calle de la parte vieja de la ciudad.
            assertTrue(juego.puedeCircular(-30, z)); // Tiene que seguir libre después de agrandar el mapa.
        }
        assertTrue(juego.puedeCircular(-10, -10)); // Verifica que un cruce interior permite circular.
        assertTrue(juego.puedeCircular(30, 30)); // Verifica un cruce de la zona nueva.
        assertFalse(juego.puedeCircular(0, 0)); // Verifica que la manzana del origen bloquea al vehículo.
    }

    /** Comprueba edificios, parques, aceras y bordes del mapa. */
    public void testObstaculosYLimites() {
        clase2 juego = new clase2(); // Crea un vehículo con su radio de colisión inicial.
        assertFalse(juego.puedeCircular(-20, -20)); // Rechaza el centro de un edificio (fila 3, columna 3).
        assertFalse(juego.puedeCircular(0, -40)); // Rechaza el centro de un parque (fila 1, columna 5).
        assertFalse(juego.puedeCircular(40, 40)); // Rechaza un parque de la zona nueva (fila 9, columna 9).
        assertFalse(juego.puedeCircular(-25.5f, -20)); // Rechaza una posición cuyo círculo invade la acera.
        assertTrue(juego.puedeCircular(-27, -20)); // Acepta una posición con separación suficiente de la acera.
        // El borde está en 55, pero como el auto tiene radio 1.65 solo puede llegar hasta 53.35.
        assertFalse(juego.puedeCircular(54, 0)); // Rechaza una posición demasiado cercana al borde este.
        assertFalse(juego.puedeCircular(0, -56)); // Rechaza una posición fuera del borde norte.
        assertTrue(juego.puedeCircular(53, -50)); // Acepta una posición dentro del margen del borde este.
    }

    /** Comprueba que el reinicio restaura la posición y detiene el vehículo. */
    public void testReinicio() {
        clase4 juego = new clase4(); // Crea la versión final sin inicializar OpenGL.
        juego.autoX = 12; // Simula que el auto se desplazó horizontalmente.
        juego.autoZ = 4; // Simula un desplazamiento en profundidad.
        juego.velocidad = 10; // Simula que el auto está avanzando.
        juego.angulo = 2; // Simula que el vehículo cambió de orientación.
        juego.reiniciar(); // Ejecuta el mismo reinicio que se activa con R.
        assertEquals(clase2.INICIO_X, juego.autoX, 0f); // Exige recuperar la X inicial.
        assertEquals(clase2.INICIO_Z, juego.autoZ, 0f); // Exige recuperar la Z inicial.
        assertEquals(0f, juego.velocidad, 0f); // Exige que el vehículo quede detenido.
        assertEquals(0f, juego.angulo, 0f); // Exige recuperar la dirección frontal inicial.
    }
}
