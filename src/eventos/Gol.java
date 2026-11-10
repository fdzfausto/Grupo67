package eventos;

import excepcion.ReglaNegocioException;
import modelo.Club;
import modelo.Jugador;

public class Gol extends Evento {
    private static final long serialVersionUID = 1L;

    private TipoGol tipoGol;

    public Gol(int minuto, Jugador jugador, Club equipo, TipoGol tipoGol) throws ReglaNegocioException {
        super(minuto, jugador, equipo);
        if (tipoGol == null) {
            throw new ReglaNegocioException("El gol debe indicar su tipo (a favor, en contra o penal)");
        }
        this.tipoGol = tipoGol;
    }

    public TipoGol getTipoGol() {
        return tipoGol;
    }

    // RN14: el gol en contra suma al marcador del rival; los demás, al club del jugador.
    @Override
    public int golesPara(Club club) {
        boolean esDelEquipo = getEquipo().equals(club);
        if (tipoGol == TipoGol.EN_CONTRA) {
            if (esDelEquipo) {
                return 0;
            } else {
                return 1;
            }
        }
        if (esDelEquipo) {
            return 1;
        } else {
            return 0;
        }
    }

    // RN26: el gol de penal cuenta para el goleador; el gol en contra no.
    @Override
    public void actualizarEstadisticas() {
        if (tipoGol != TipoGol.EN_CONTRA) {
            getJugador().sumarGol();
        }
    }

    @Override
    public String obtenerDescripcion() {
        String tipo;
        switch (tipoGol) {
            case EN_CONTRA:
                tipo = "Gol en contra";
                break;
            case PENAL:
                tipo = "Gol de penal";
                break;
            default:
                tipo = "Gol";
                break;
        }
        return tipo + " de " + getJugador().getNombre() + " " + getJugador().getApellido() + " (min " + getMinuto() + ")";
    }
}
