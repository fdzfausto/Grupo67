package reportes;

import modelo.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProyeccionPorXG implements MetodoProyeccion{
    public static final int SIMULACIONES = 1000;
    public static final int JUGADAS_POR_PARTIDO = 10;

    @Override
    public double calcular(Torneo torneo, Club club) {
        List<Partido> partidos = torneo.getPartidos();
        int vecesClasificado = 0;
        for (int s = 0; s < SIMULACIONES; s++) {
            List<FilaTabla> tabla = crearTablaVacia(torneo);
            for (int i = 0; i < partidos.size(); i++) {
                Partido p = partidos.get(i);
                int golesLocal;
                int golesVisitante;
                if (p.getEstado() == EstadoPartido.FINALIZADO) {
                    // los partidos ya jugados cuentan con su resultado real
                    golesLocal = p.getGoles(p.getLocal());
                    golesVisitante = p.getGoles(p.getVisitante());
                } else {
                    // los que faltan se simulan con el xG promedio de cada club
                    golesLocal = simularGoles(xgPromedio(torneo, p.getLocal()));
                    golesVisitante = simularGoles(xgPromedio(torneo, p.getVisitante()));
                }
                buscarFila(tabla, p.getLocal()).registrarResultado(golesLocal, golesVisitante);
                buscarFila(tabla, p.getVisitante()).registrarResultado(golesVisitante, golesLocal);
            }
            Collections.sort(tabla, new ComparadorTabla(partidos)); // mismos criterios que la tabla real (RN25)
            if (posicion(tabla, club) < torneo.getCuposClasificacion()) {
                vecesClasificado++;
            }
        }
        return (double) vecesClasificado / SIMULACIONES * 100;
    }

    private double xgPromedio(Torneo torneo, Club club) {
        double totalXG = 0;
        int jugados = 0;
        List<Partido> partidos = torneo.getPartidos();
        for (int i = 0; i < partidos.size(); i++) {
            Partido p = partidos.get(i);
            if (p.getEstado() == EstadoPartido.FINALIZADO && p.participa(club)) {
                totalXG = totalXG + p.calcularXGClub(club);
                jugados++;
            }
        }
        if (jugados == 0) {
            return 0;
        }
        return totalXG / jugados;
    }

    private int simularGoles(double xg) {
        int goles = 0;
        for (int i = 0; i < JUGADAS_POR_PARTIDO; i++) {
            if (Math.random() < xg / JUGADAS_POR_PARTIDO) {
                goles++;
            }
        }
        return goles;
    }

    private List<FilaTabla> crearTablaVacia(Torneo torneo) {
        List<FilaTabla> tabla = new ArrayList<>();
        List<Inscripcion> inscripciones = torneo.getInscripciones();
        for (int i = 0; i < inscripciones.size(); i++) {
            tabla.add(new FilaTabla(inscripciones.get(i).getClub()));
        }
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

    // Posicion del club en la tabla: 0 = primero, 1 = segundo, etc.
    private int posicion(List<FilaTabla> tabla, Club club) {
        for (int i = 0; i < tabla.size(); i++) {
            if (tabla.get(i).getClub() == club) {
                return i;
            }
        }
        return tabla.size();
    }
}
