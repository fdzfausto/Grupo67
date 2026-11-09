package eventos;

import excepcion.ReglaNegocioException;
import modelo.Club;
import modelo.Jugador;

public class Falta extends Evento {
    private static final long serialVersionUID = 1L;

    private String tipoFalta;

    public Falta(int minuto, Jugador jugador, Club equipo, String tipoFalta) throws ReglaNegocioException {
        super(minuto, jugador, equipo);
        if (tipoFalta == null || tipoFalta.isBlank()) {
            throw new ReglaNegocioException("La falta debe indicar su tipo");
        }
        this.tipoFalta = tipoFalta;
    }

    public String getTipoFalta() {
        return tipoFalta;
    }

    @Override
    public String obtenerDescripcion() {
        return "Falta (" + tipoFalta + ") de " + getJugador().getNombre() + " " + getJugador().getApellido()
                + " (min " + getMinuto() + ")";
    }
}
