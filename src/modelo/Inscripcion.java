package modelo;

import excepcion.ReglaNegocioException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Registro de un club en un torneo, con su plantel (RN05).
public class Inscripcion implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MIN_JUGADORES = 18;
    public static final int MAX_JUGADORES = 30;

    private Club club;
    private List<Jugador> plantel; // agregación: los jugadores existen aunque no estén inscriptos

    // Constructor de paquete: solo Torneo crea inscripciones (GRASP Creador).
    Inscripcion(Club club, List<Jugador> plantel) throws ReglaNegocioException {
        if (club == null) {
            throw new ReglaNegocioException("La inscripción necesita un club");
        }
        if (plantel == null) {
            throw new ReglaNegocioException("La inscripción necesita un plantel");
        }
        if (plantel.size() < MIN_JUGADORES || plantel.size() > MAX_JUGADORES) {
            throw new ReglaNegocioException("El plantel debe tener entre " + MIN_JUGADORES + " y "
                    + MAX_JUGADORES + " jugadores (tiene " + plantel.size() + ")");
        }
        Set<String> dnis = new HashSet<>();
        for (Jugador jugador : plantel) {
            if (jugador == null) {
                throw new ReglaNegocioException("El plantel no puede tener jugadores nulos");
            }
            if (!dnis.add(jugador.getDni())) {
                throw new ReglaNegocioException("El jugador " + jugador + " está repetido en el plantel");
            }
        }
        this.club = club;
        this.plantel = new ArrayList<>(plantel);
    }

    // Experto: el plantel sabe si un jugador forma parte de él (se compara por DNI).
    public boolean contiene(Jugador jugador) {
        for (Jugador j : plantel) {
            if (j.getDni().equals(jugador.getDni())) {
                return true;
            }
        }
        return false;
    }

    public Club getClub() {
        return club;
    }

    public List<Jugador> getPlantel() {
        return new ArrayList<>(plantel);
    }

    @Override
    public String toString() {
        return club + " (" + plantel.size() + " jugadores)";
    }
}
