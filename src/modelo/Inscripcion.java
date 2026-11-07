package modelo;

import excepcion.ReglaNegocioException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Inscripcion implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MIN_JUGADORES = 18;
    public static final int MAX_JUGADORES = 30;

    private Club club; // asociacion con CLUB
    private List<Jugador> jugadores; // agregacion con JUGADOR


    // sin public: solo la puede crear el Torneo (Patron creador)
    Inscripcion(Club club) {
        this.club = club;
        this.jugadores = new ArrayList<Jugador>();
    }

    public List<Jugador> getJugadores() {
        return new ArrayList<>(jugadores);
    }

    public boolean contieneJugador(Jugador jugador) {
        for (int i = 0; i < jugadores.size(); i++) {
           if (jugadores.get(i).getDni().equals(jugador.getDni())) {
               return true;
           }
        }
        return false;
    }

    public boolean validarPlantel(){
        return jugadores.size() >= MIN_JUGADORES && jugadores.size() <= MAX_JUGADORES;
    }

    public void agregarJugador(Jugador jugador) throws ReglaNegocioException {
        if (contieneJugador(jugador)) {
            throw new ReglaNegocioException(jugador + " ya se encuentra en en plantel de " + club);
        }
        if (jugadores.size() >= MAX_JUGADORES) {
            throw new ReglaNegocioException(jugador + " no puede ser  ingresado al plantel debido a que supera el maximo numero de jugadores");
        }
        jugadores.add(jugador);
    }

    public void quitarJugador(Jugador jugador) throws ReglaNegocioException {
        for (int i = 0; i < jugadores.size(); i++) {
            if (jugadores.get(i).getDni().equals(jugador.getDni())) {
                jugadores.remove(i);
                return;
            }
        }
    }

    public Club getClub() {
        return club;
    }

    @Override
    public String toString() {
        return club + " (" + jugadores.size() + " jugadores)";
    }
}
