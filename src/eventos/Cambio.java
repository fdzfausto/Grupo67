package eventos;

import excepcion.ReglaNegocioException;
import modelo.Club;
import modelo.Jugador;

// El jugador heredado de Evento es el que sale; jugadorEntrante es el que ingresa.
public class Cambio extends Evento {
    private static final long serialVersionUID = 1L;

    private Jugador jugadorEntrante;

    public Cambio(int minuto, Jugador jugadorSaliente, Jugador jugadorEntrante, Club equipo) throws ReglaNegocioException {
        super(minuto, jugadorSaliente, equipo);
        if (jugadorEntrante == null) {
            throw new ReglaNegocioException("El cambio debe indicar el jugador que entra");
        }
        if (jugadorEntrante.equals(jugadorSaliente)) {
            throw new ReglaNegocioException("El jugador que entra debe ser distinto del que sale");
        }
        this.jugadorEntrante = jugadorEntrante;
    }

    public Jugador getJugadorSaliente() {
        return getJugador();
    }

    @Override
    public Jugador getJugadorEntrante() {
        return jugadorEntrante;
    }

    @Override
    public boolean sustituye(Jugador jugador) {
        return esElMismo(getJugadorSaliente(), jugador);
    }

    @Override
    public boolean haceEntrar(Jugador jugador) {
        return esElMismo(jugadorEntrante, jugador);
    }

    @Override
    public String obtenerDescripcion() {
        return "Sale " + getJugador().getApellido() + ", entra " + jugadorEntrante.getApellido()
                + " (min " + getMinuto() + ")";
    }
}
