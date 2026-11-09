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

    // Polimorfismo: igual que golesPara, cada tipo de evento responde a su manera y por defecto no aplica.
    // Así Partido consulta los eventos sin preguntar de qué clase es cada uno.

    // Anulacion lo sobrescribe: devuelve el evento que anula (null si este evento no es una anulación).
    public Evento getEventoAnulado() {
        return null;
    }

    // Cambio lo sobrescribe: devuelve el jugador que entra (null si este evento no es un cambio).
    public Jugador getJugadorEntrante() {
        return null;
    }

    // Anulacion lo sobrescribe: ¿este evento anula a "otro"?
    public boolean anula(Evento otro) {
        return false;
    }

    // Cambio lo sobrescribe: ¿este evento hace salir al jugador?
    public boolean sustituye(Jugador jugador) {
        return false;
    }

    // Cambio lo sobrescribe: ¿este evento hace entrar al jugador?
    public boolean haceEntrar(Jugador jugador) {
        return false;
    }

    // Tarjeta lo sobrescribe: ¿este evento expulsa al jugador (tarjeta roja)?
    public boolean expulsa(Jugador jugador) {
        return false;
    }

    // Tarjeta lo sobrescribe: ¿este evento es una amarilla para el jugador?
    public boolean amonesta(Jugador jugador) {
        return false;
    }

    // Los jugadores se identifican por DNI, igual que en Nomina e Inscripcion.
    protected static boolean esElMismo(Jugador a, Jugador b) {
        return a.getDni().equals(b.getDni());
    }

    @Override
    public String toString() {
        return obtenerDescripcion();
    }
}
