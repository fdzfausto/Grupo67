package controlador;

import eventos.Evento;
import excepcion.ReglaNegocioException;
import modelo.Partido;
import modelo.Torneo;
import notificaciones.Notificacion;
import notificaciones.ServicioNotificaciones;
import notificaciones.Suscriptor;

import java.util.ArrayList;
import java.util.List;

public class ControladorPartido {
    public static final int MINUTO_FINAL = 90;
    private ServicioNotificaciones servicioNotificaciones;

    public ControladorPartido(){
        this.servicioNotificaciones = new ServicioNotificaciones();
    }

    public void iniciarPartido (Partido partido) throws ReglaNegocioException {
        partido.iniciar();
    }

    public void suspenderPartido (Partido partido) throws ReglaNegocioException {
        partido.suspender();
    }

    public List<Evento> registrarEvento(Partido partido, Evento evento, List<Suscriptor> suscriptores) throws ReglaNegocioException {
        int cantidadAntes = partido.getEventos().size();
        partido.registrarEvento(evento);
        List<Evento> todos = partido.getEventos();
        List<Evento> nuevos = new ArrayList<>();
        for (int i = cantidadAntes; i < todos.size(); i++) {
            nuevos.add(todos.get(i));
            notificarSiCorresponde(partido, todos.get(i), suscriptores);
        }
        return nuevos;
    }

    public void anularEvento(Partido partido, Evento evento, String motivo) throws ReglaNegocioException {
        partido.anularEvento(evento, motivo);
    }


    public void finalizarPartido(Torneo torneo, Partido partido, List<Suscriptor> suscriptores)
            throws ReglaNegocioException {
        partido.finalizar();
        torneo.registrarSanciones(partido);
        notificar(partido, MINUTO_FINAL, "Final del partido: " + partido, suscriptores);
    }


    private void notificarSiCorresponde(Partido partido, Evento evento, List<Suscriptor> suscriptores)
            throws ReglaNegocioException {
        int goles = evento.golesPara(partido.getLocal()) + evento.golesPara(partido.getVisitante());
        if (goles > 0) {
            notificar(partido, evento.getMinuto(), "Gol: " + evento, suscriptores);
        } else if (evento.expulsa(evento.getJugador())) {
            notificar(partido, evento.getMinuto(), "Tarjeta roja: " + evento, suscriptores);
        }
    }

    private void notificar(Partido partido, int minuto, String hecho, List<Suscriptor> suscriptores)
            throws ReglaNegocioException {
        List<Suscriptor> destinatarios = new ArrayList<>();
        for (int i = 0; i < suscriptores.size(); i++) {
            Suscriptor s = suscriptores.get(i);
            if (s.sigueA(partido.getLocal()) || s.sigueA(partido.getVisitante())) {
                destinatarios.add(s);
            }
        }
        servicioNotificaciones.notificar(new Notificacion(partido, minuto, hecho), destinatarios);
    }
}
