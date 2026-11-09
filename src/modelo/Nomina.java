package modelo;

import excepcion.ReglaNegocioException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Titulares y suplentes de un club para un partido (S04: 11 titulares y hasta 7 suplentes).
public class Nomina implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int CANTIDAD_TITULARES = 11;
    public static final int MAX_SUPLENTES = 7;

    private Club club;
    private List<Jugador> titulares;
    private List<Jugador> suplentes;

    public Nomina(Club club) throws ReglaNegocioException {
        if (club == null) {
            throw new ReglaNegocioException("La nómina necesita un club");
        }
        this.club = club;
        this.titulares = new ArrayList<>();
        this.suplentes = new ArrayList<>();
    }

    public void agregarTitular(Jugador jugador) throws ReglaNegocioException {
        verificarNuevoJugador(jugador);
        if (titulares.size() >= CANTIDAD_TITULARES) {
            throw new ReglaNegocioException("La nómina ya tiene los " + CANTIDAD_TITULARES + " titulares");
        }
        titulares.add(jugador);
    }

    public void agregarSuplente(Jugador jugador) throws ReglaNegocioException {
        verificarNuevoJugador(jugador);
        if (suplentes.size() >= MAX_SUPLENTES) {
            throw new ReglaNegocioException("La nómina ya tiene los " + MAX_SUPLENTES + " suplentes permitidos");
        }
        suplentes.add(jugador);
    }

    // Una nómina está completa con 11 titulares y hasta 7 suplentes.
    public boolean validarNomina() {
        return titulares.size() == CANTIDAD_TITULARES && suplentes.size() <= MAX_SUPLENTES;
    }

    // Experto: la nómina sabe si un jugador está en ella (se compara por DNI, igual que Inscripcion).
    public boolean contiene(Jugador jugador) {
        for (Jugador j : getJugadores()) {
            if (j.getDni().equals(jugador.getDni())) {
                return true;
            }
        }
        return false;
    }

    // Experto: la nómina sabe quiénes son sus titulares (se compara por DNI, igual que contiene).
    public boolean esTitular(Jugador jugador) {
        for (Jugador j : titulares) {
            if (j.getDni().equals(jugador.getDni())) {
                return true;
            }
        }
        return false;
    }

    // Titulares y suplentes juntos.
    public List<Jugador> getJugadores() {
        List<Jugador> todos = new ArrayList<>(titulares);
        todos.addAll(suplentes);
        return todos;
    }

    private void verificarNuevoJugador(Jugador jugador) throws ReglaNegocioException {
        if (jugador == null) {
            throw new ReglaNegocioException("El jugador no puede ser nulo");
        }
        if (contiene(jugador)) {
            throw new ReglaNegocioException(jugador + " ya está en la nómina");
        }
    }

    public Club getClub() {
        return club;
    }

    public List<Jugador> getTitulares() {
        return new ArrayList<>(titulares);
    }

    public List<Jugador> getSuplentes() {
        return new ArrayList<>(suplentes);
    }

    @Override
    public String toString() {
        return "Nómina de " + club + " (" + titulares.size() + " titulares, " + suplentes.size() + " suplentes)";
    }
}
