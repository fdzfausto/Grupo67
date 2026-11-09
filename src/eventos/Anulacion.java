package eventos;

import excepcion.ReglaNegocioException;
import modelo.Club;

// RN17: un evento no se elimina; se corrige con una Anulacion asociada al original.
public class Anulacion extends Evento {
    private static final long serialVersionUID = 1L;

    private Evento eventoAnulado;
    private String motivo;

    // Toma el minuto, jugador y club del evento original.
    public Anulacion(Evento eventoAnulado, String motivo) throws ReglaNegocioException {
        super(requerir(eventoAnulado).getMinuto(), eventoAnulado.getJugador(), eventoAnulado.getEquipo());
        if (eventoAnulado instanceof Anulacion) {
            throw new ReglaNegocioException("No se puede anular una anulación");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new ReglaNegocioException("La anulación debe indicar el motivo");
        }
        this.eventoAnulado = eventoAnulado;
        this.motivo = motivo;
    }

    private static Evento requerir(Evento evento) throws ReglaNegocioException {
        if (evento == null) {
            throw new ReglaNegocioException("Se debe indicar el evento a anular");
        }
        return evento;
    }

    @Override
    public Evento getEventoAnulado() {
        return eventoAnulado;
    }

    @Override
    public boolean anula(Evento otro) {
        return eventoAnulado == otro;
    }

    public String getMotivo() {
        return motivo;
    }

    // Compensa los goles del evento original, así el marcador sigue calculándose desde la lista de eventos.
    @Override
    public int golesPara(Club club) {
        return -eventoAnulado.golesPara(club);
    }

    @Override
    public String obtenerDescripcion() {
        return "Anulado: " + eventoAnulado.obtenerDescripcion() + " - Motivo: " + motivo;
    }
}
