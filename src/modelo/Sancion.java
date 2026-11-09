package modelo;

import java.io.Serializable;

// RN18 y S05: suspensión de un jugador por una fecha, que es el próximo partido de su club en el torneo.
public class Sancion implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String MOTIVO_ROJA = "Tarjeta roja";
    public static final String MOTIVO_AMARILLAS = "Acumulación de 5 amarillas";

    private Jugador jugador;
    private Club club;
    private Partido partidoOrigen; // el partido donde se ganó la sanción
    private String motivo;
    private boolean cumplida;

    // Constructor de paquete: solo Torneo crea sanciones (GRASP Creador).
    Sancion(Jugador jugador, Club club, Partido partidoOrigen, String motivo) {
        this.jugador = jugador;
        this.club = club;
        this.partidoOrigen = partidoOrigen;
        this.motivo = motivo;
        this.cumplida = false;
    }

    // Se cumple cuando el club juega su siguiente partido sin el jugador.
    void cumplir() {
        this.cumplida = true;
    }

    public boolean estaPendiente() {
        return !cumplida;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public Club getClub() {
        return club;
    }

    public Partido getPartidoOrigen() {
        return partidoOrigen;
    }

    public String getMotivo() {
        return motivo;
    }

    @Override
    public String toString() {
        String estado;
        if (cumplida) {
            estado = "cumplida";
        } else {
            estado = "pendiente";
        }
        return jugador + " (" + club + "): " + motivo + " - " + estado;
    }
}
