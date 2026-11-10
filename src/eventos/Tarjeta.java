package eventos;

import excepcion.ReglaNegocioException;
import modelo.Club;
import modelo.Jugador;

public class Tarjeta extends Evento {
    private static final long serialVersionUID = 1L;

    private ColorTarjeta tipoTarjeta;

    public Tarjeta(int minuto, Jugador jugador, Club equipo, ColorTarjeta tipoTarjeta) throws ReglaNegocioException {
        super(minuto, jugador, equipo);
        if (tipoTarjeta == null) {
            throw new ReglaNegocioException("La tarjeta debe indicar su color");
        }
        this.tipoTarjeta = tipoTarjeta;
    }

    public ColorTarjeta getTipoTarjeta() {
        return tipoTarjeta;
    }

    public boolean esRoja() {
        return tipoTarjeta == ColorTarjeta.ROJA;
    }

    @Override
    public void actualizarEstadisticas() {
        getJugador().sumarTarjeta();
    }

    @Override
    public boolean expulsa(Jugador jugador) {
        return esRoja() && esElMismo(getJugador(), jugador);
    }

    @Override
    public boolean amonesta(Jugador jugador) {
        return !esRoja() && esElMismo(getJugador(), jugador);
    }

    @Override
    public String obtenerDescripcion() {
        return "Tarjeta " + tipoTarjeta.name().toLowerCase() + " para " + getJugador().getNombre() + " "
                + getJugador().getApellido() + " (min " + getMinuto() + ")";
    }
}
