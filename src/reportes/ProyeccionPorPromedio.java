package reportes;

import modelo.Club;
import modelo.EstadoPartido;
import modelo.Partido;
import modelo.Torneo;

import java.util.ArrayList;
import java.util.List;

public class ProyeccionPorPromedio implements MetodoProyeccion{
    @Override
    public double calcular (Torneo torneo, Club club) {
        FilaTabla fila = new FilaTabla(club);
        int partidosTotales = 0;
        List< Partido> partidos = torneo.getPartidos();
        for (int i = 0; i < partidos.size(); i++) {
            Partido p =  partidos.get(i);
            if (p.participa(club)){
                partidosTotales++;
                if (p.getEstado() == EstadoPartido.FINALIZADO){
                    fila.registrarResultado(p.getGoles(club), p.getGoles(rival(p, club)));
                }
            }
        }
        if (fila.getJugados() == 0){
            return 0;
        }
        double promedio = (double) fila.getPuntos() / fila.getJugados();
        double puntosProyectados = promedio * partidosTotales;
        if (puntosProyectados >= torneo.getPuntajeCorte()){
            return 100;
        }
        return puntosProyectados / torneo.getPuntajeCorte();
    }

    private Club rival (Partido p, Club club){
        if (p.getLocal() == club){
            return p.getVisitante();
        } else {
            return p.getLocal();
        }
    }
}
