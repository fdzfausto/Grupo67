package controlador;

import excepcion.ReglaNegocioException;
import modelo.*;

import java.time.LocalDateTime;
import java.util.List;

public class ControladorTorneo {
    public Inscripcion inscribirClub(Torneo torneo, Club club, List<Jugador> plantel) throws ReglaNegocioException {
        return torneo.inscribirClub(club, plantel);
    }

    public Fixture generarFixture(Torneo torneo) throws ReglaNegocioException {
        return torneo.generarFixture();
    }

    public void avanzarEstado(Torneo torneo) throws ReglaNegocioException {
        torneo.avanzarEstado();
    }


    public void programarPartido(Torneo torneo, Partido partido, LocalDateTime fechaHora, Estadio estadio,
                                 TernaArbitral terna) throws ReglaNegocioException {
        torneo.programarPartido(partido, fechaHora, estadio, terna);
    }

    public void cargarNomina(Torneo torneo, Partido partido, Nomina nomina) throws ReglaNegocioException {
        torneo.cargarNomina(partido, nomina);
    }
}
