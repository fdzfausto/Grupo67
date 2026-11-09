package modelo;

import eventos.Anulacion;
import eventos.Evento;
import excepcion.ReglaNegocioException;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Partido implements Serializable {
    private static final long serialVersionUID = 1L;

    private Club local;
    private Club visitante;
    private LocalDateTime fechaHora;
    private Estadio estadio;
    private TernaArbitral terna;
    private EstadoPartido estado;
    private List<Evento> eventos; // composición: los eventos pertenecen al partido

    // El fixture crea los partidos sin fecha, estadio ni terna (supuesto S03).
    public Partido(Club local, Club visitante) throws ReglaNegocioException {
        if (local == null || visitante == null) {
            throw new ReglaNegocioException("El partido necesita un club local y uno visitante");
        }
        if (local.equals(visitante)) {
            throw new ReglaNegocioException("Un partido debe enfrentar a dos clubes distintos");
        }
        this.local = local;
        this.visitante = visitante;
        this.estado = EstadoPartido.SIN_PROGRAMAR;
        this.eventos = new ArrayList<>();
    }

    // Las validaciones que dependen del torneo (RN04, CU14) las hace Torneo antes de llamar a este método.
    public void programar(LocalDateTime fechaHora, Estadio estadio, TernaArbitral terna) throws ReglaNegocioException {
        verificarEstado(EstadoPartido.SIN_PROGRAMAR, "programar el partido");
        if (fechaHora == null || estadio == null || terna == null) {
            throw new ReglaNegocioException("Para programar un partido se necesitan fecha y hora, estadio y terna arbitral");
        }
        this.fechaHora = fechaHora;
        this.estadio = estadio;
        this.terna = terna;
        this.estado = EstadoPartido.PROGRAMADO;
    }

    public void iniciar() throws ReglaNegocioException {
        verificarEstado(EstadoPartido.PROGRAMADO, "iniciar el partido");
        this.estado = EstadoPartido.EN_JUEGO;
    }

    public void registrarEvento(Evento evento) throws ReglaNegocioException {
        if (evento == null) {
            throw new ReglaNegocioException("El evento no puede ser nulo");
        }
        verificarEstado(EstadoPartido.EN_JUEGO, "registrar un evento"); // RN12
        eventos.add(evento);
    }

    public void anularEvento(Evento evento, String motivo) throws ReglaNegocioException {
        if (!eventos.contains(evento)) {
            throw new ReglaNegocioException("El evento no pertenece a este partido");
        }
        for (int i = 0; i < eventos.size(); i++) {
            if (eventos.get(i) instanceof Anulacion) {
                Anulacion anulacion = (Anulacion) eventos.get(i);
                if (anulacion.getEventoAnulado() == evento) {
                    throw new ReglaNegocioException("Ese evento ya fue anulado");
                }
            }
        }
        registrarEvento(new Anulacion(evento, motivo));
    }

    public void finalizar() throws ReglaNegocioException {
        verificarEstado(EstadoPartido.EN_JUEGO, "finalizar el partido");
        this.estado = EstadoPartido.FINALIZADO;
    }

    public void suspender() throws ReglaNegocioException {
        if (estado != EstadoPartido.PROGRAMADO && estado != EstadoPartido.EN_JUEGO) {
            throw new ReglaNegocioException("Solo se puede suspender un partido programado o en juego (estado actual: " + estado + ")");
        }
        this.estado = EstadoPartido.SUSPENDIDO;
    }

    public int getGoles(Club club) {
        int goles = 0;
        for (Evento evento : eventos) {
            goles += evento.golesPara(club);
        }
        return goles;
    }

    public boolean participa(Club club) {
        return local.equals(club) || visitante.equals(club);
    }

    private void verificarEstado(EstadoPartido esperado, String accion) throws ReglaNegocioException {
        if (estado != esperado) {
            throw new ReglaNegocioException("No se puede " + accion + ": está " + estado + " y debe estar " + esperado);
        }
    }

    public Club getLocal() {
        return local;
    }

    public Club getVisitante() {
        return visitante;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public Estadio getEstadio() {
        return estadio;
    }

    public TernaArbitral getTerna() {
        return terna;
    }

    public EstadoPartido getEstado() {
        return estado;
    }

    public List<Evento> getEventos() {
        return new ArrayList<>(eventos);
    }

    @Override
    public String toString() {
        return local + " " + getGoles(local) + " - " + getGoles(visitante) + " " + visitante + " (" + estado + ")";
    }
}
