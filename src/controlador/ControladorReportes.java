package controlador;

import modelo.Club;
import modelo.Torneo;
import reportes.Goleador;
import reportes.MetodoProyeccion;
import reportes.ServicioReportes;

import java.util.List;

public class ControladorReportes {
    private ServicioReportes servicioReportes;

    public ControladorReportes() {
        servicioReportes = new ServicioReportes();
    }

    public String generarTablaPosiciones(Torneo torneo){
        return servicioReportes.generarTablaPosiciones(torneo);
    }

    public List<Goleador> obtenerGoleadores(Torneo torneo){
        return servicioReportes.obtenerGoleadores(torneo);
    }

    public Club obtenervallaMenosVencida(Torneo torneo){
        return servicioReportes.obtenerVallaMenosVencida(torneo);
    }

    public double calcularProbabilidadClasificacion(Torneo torneo, Club club, MetodoProyeccion metodo){
        return servicioReportes.calcularProbabilidadClasificacion(torneo, club, metodo);
    }
}
