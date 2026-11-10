package reportes;

import eventos.Evento;
import modelo.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ServicioReportes {

    public List<FilaTabla> calcularTabla(Torneo torneo) {
        List<FilaTabla> tabla = new ArrayList<>();
        List<Inscripcion> inscripciones = torneo.getInscripciones();
        for (int i = 0; i < inscripciones.size(); i++) {
            tabla.add(new FilaTabla(inscripciones.get(i).getClub()));
        }
        List<Partido> partidos = torneo.getPartidos();
        for (int i = 0; i < partidos.size(); i++) {
            Partido p = partidos.get(i);
            if (p.getEstado() == EstadoPartido.FINALIZADO){
                int golesLocal = p.getGoles(p.getLocal());
                int golesVisitante = p.getGoles(p.getVisitante());
                buscarFila(tabla, p.getLocal()).registrarResultado(golesLocal, golesVisitante);
                buscarFila(tabla, p.getVisitante()).registrarResultado(golesVisitante, golesLocal);
            }
        }
        Collections.sort(tabla, new ComparadorTabla(partidos)); // RN25
        return tabla;
    }

    private FilaTabla buscarFila(List<FilaTabla> tabla, Club club) {
        for (int i = 0; i < tabla.size(); i++) {
            if (tabla.get(i).getClub() == club) {
                return tabla.get(i);
            }
        }
        return null;
    }

    public String generarTablaPosiciones(Torneo torneo) {
        List<FilaTabla> tabla = calcularTabla(torneo);
        String texto = "Tabla de posiciones - " + torneo.getNombre() + "\n";
        for (int i = 0; i < tabla.size(); i++) {
            texto = texto + (i + 1) + ". " + tabla.get(i) + "\n";
        }
        return texto;
    }

    public List<Goleador> obtenerGoleadores(Torneo torneo) {
        List<Goleador> goleadores = new ArrayList<>();
        List<Partido> partidos = torneo.getPartidos();
        for (int i = 0; i < partidos.size(); i++) {
            Partido p = partidos.get(i);
            if (p.getEstado() == EstadoPartido.FINALIZADO) {
                List<Evento> eventos = p.getEventos();
                for (int j = 0; j < eventos.size(); j++) {
                    Evento e = eventos.get(j);
                    if (!p.estaAnulado(e) && e.golesPara(e.getEquipo()) > 0){
                        Goleador goleador = buscarGoleador(goleadores, e.getJugador());
                        if (goleador == null) {
                            goleador = new Goleador(e.getJugador(), e.getEquipo());
                            goleadores.add(goleador);
                        }
                        goleador.sumarGol();
                    }
                }
            }
        }
        Collections.sort(goleadores);
        return goleadores;
    }

    private Goleador buscarGoleador(List<Goleador> goleadores, Jugador jugador) {
        for (int i = 0; i < goleadores.size(); i++) {
            if (goleadores.get(i).getJugador().getDni().equals(jugador.getDni())) {
                return goleadores.get(i);
            }
        }
        return null;
    }

    public Club obtenerVallaMenosVencida(Torneo torneo) {
        List<FilaTabla> tabla = calcularTabla(torneo);
        Club mejor = null;
        double mejorPromedio = 0;
        for (int i = 0; i < tabla.size(); i++) {
            FilaTabla fila = tabla.get(i);
            if (fila.getJugados() > 0){
                double promedio = (double) fila.getGolesEnContra() / fila.getJugados();
                if (mejor == null || promedio < mejorPromedio) {
                    mejor = fila.getClub();
                    mejorPromedio = promedio;
                }
            }
        }
        return mejor;
    }

    public double calcularProbabilidadClasificacion(Torneo torneo, Club club, MetodoProyeccion metodo) {
        return metodo.calcular(torneo, club);
    }
}
