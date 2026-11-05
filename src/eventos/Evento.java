package eventos;

import excepcion.ReglaNegocioException;
import modelo.Club;
import modelo.Jugador;

import java.io.Serializable;

// Clase base de Gol, Remate, Tarjeta, Cambio, Falta y Anulacion.
public abstract class Evento implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MINUTO_MAXIMO = 120; // 90 + adicional (RN13)

    private int minuto;
    private Jugador jugador;
    private Club equipo;

    protected Evento(int minuto, Jugador jugador, Club equipo) throws ReglaNegocioException {
        if (minuto < 0 || minuto > MINUTO_MAXIMO) {
            throw new ReglaNegocioException("El minuto debe estar entre 0 y " + MINUTO_MAXIMO);
        }
        if (jugador == null || equipo == null) {
            throw new ReglaNegocioException("Todo evento debe indicar el jugador y su club");
        }
        this.minuto = minuto;
        this.jugador = jugador;
        this.equipo = equipo;
    }

    public int getMinuto() {
        return minuto;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public Club getEquipo() {
        return equipo;
    }

    public abstract String obtenerDescripcion();

    // Por defecto un evento no suma goles. Gol lo sobrescribe (polimorfismo),
    // así Partido calcula el marcador sin preguntar el tipo de cada evento.
    public int golesPara(Club club) {
        return 0;
    }

    @Override
    public String toString() {
        return obtenerDescripcion();
    }
}
