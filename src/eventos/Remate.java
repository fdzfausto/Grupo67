package eventos;

import excepcion.ReglaNegocioException;
import modelo.Club;
import modelo.Jugador;

public class Remate extends Evento {
    private static final long serialVersionUID = 1L;

    private ZonaRemate zonaRemate;

    public Remate(int minuto, Jugador jugador, Club equipo, ZonaRemate zonaRemate) throws ReglaNegocioException {
        super(minuto, jugador, equipo);
        if (zonaRemate == null) {
            throw new ReglaNegocioException("El remate debe indicar la zona desde la que se patea");
        }
        this.zonaRemate = zonaRemate;
    }

    public ZonaRemate getZonaRemate() {
        return zonaRemate;
    }

    // RN27: el xG del remate depende de la zona.
    public double getXG() {
        return zonaRemate.getValorXG();
    }

    @Override
    public String obtenerDescripcion() {
        return "Remate de " + getJugador().getNombre() + " " + getJugador().getApellido()
                + " desde " + zonaRemate + " (min " + getMinuto() + ")";
    }
}
