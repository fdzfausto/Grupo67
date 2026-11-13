package controlador;

import excepcion.ReglaNegocioException;
import modelo.EstadoPartido;
import modelo.EstadoTorneo;
import modelo.Fixture;
import modelo.Inscripcion;
import modelo.Jugador;
import modelo.Nomina;
import modelo.Partido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;

// El controlador delega en Torneo (GRASP Controlador): las reglas siguen viviendo en el modelo.
class ControladorTorneoTest {

    private EscenarioControlador escenario;
    private ControladorTorneo controlador;

    @BeforeEach
    void preparar() throws ReglaNegocioException {
        escenario = new EscenarioControlador(); // ya inscribió los 4 clubes
        controlador = escenario.controladorTorneo;
    }

    @Test
    void inscribirUnClubDevuelveLaInscripcionYElTorneoLaRecuerda() throws ReglaNegocioException {
        // El escenario ya inscribió 4 clubes; se prueba con un torneo nuevo vacío.
        modelo.Torneo vacio = new modelo.Torneo("Copa", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 12, 15),
                modelo.TipoTorneo.NACIONAL, 1, 0);

        Inscripcion inscripcion = controlador.inscribirClub(vacio, escenario.clubes.get(0), escenario.planteles.get(0));

        assertNotNull(inscripcion);
        assertSame(escenario.clubes.get(0), inscripcion.getClub());
        assertTrue(vacio.estaInscripto(escenario.clubes.get(0)));
    }

    @Test
    void inscribirDosVecesElMismoClubSePropagaComoReglaDeNegocio() {
        try {
            controlador.inscribirClub(escenario.torneo, escenario.clubes.get(0), escenario.planteles.get(0));
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: el club ya estaba inscripto
        }
    }

    @Test
    void elPlantelDebeTenerAlMenosDieciochoJugadores() throws ReglaNegocioException {
        modelo.Torneo vacio = new modelo.Torneo("Copa", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 12, 15),
                modelo.TipoTorneo.NACIONAL, 1, 0);
        List<Jugador> corto = new ArrayList<>(escenario.planteles.get(0).subList(0, 17));

        try {
            controlador.inscribirClub(vacio, escenario.clubes.get(0), corto);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
    }

    @Test
    void generarElFixtureCreaTodosLosPartidosDeUnaRueda() throws ReglaNegocioException {
        Fixture fixture = controlador.generarFixture(escenario.torneo);

        assertEquals(6, fixture.getPartidos().size()); // 4 clubes: 4 * 3 / 2
        assertEquals(3, fixture.getFechas().size());
        assertSame(fixture, escenario.torneo.getFixture());
    }

    @Test
    void noSePuedeGenerarElFixtureDosVeces() throws ReglaNegocioException {
        controlador.generarFixture(escenario.torneo);

        try {
            controlador.generarFixture(escenario.torneo);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
    }

    @Test
    void avanzarElEstadoLlevaAlSiguienteYNoSePasaDeFinalizado() throws ReglaNegocioException {
        controlador.avanzarEstado(escenario.torneo);
        assertEquals(EstadoTorneo.EN_CURSO, escenario.torneo.getEstado());
        controlador.avanzarEstado(escenario.torneo);
        assertEquals(EstadoTorneo.FINALIZADO, escenario.torneo.getEstado());

        try {
            controlador.avanzarEstado(escenario.torneo);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
    }

    @Test
    void programarUnPartidoLoDejaProgramadoConSuEstadioYTerna() throws ReglaNegocioException {
        escenario.iniciarTorneo();
        Partido partido = escenario.torneo.getPartidos().get(0);

        escenario.programarConNominas(partido);

        assertEquals(EstadoPartido.PROGRAMADO, partido.getEstado());
        assertNotNull(partido.getEstadio());
        assertNotNull(partido.getTerna());
    }

    @Test
    void programarUnPartidoFueraDelPeriodoDelTorneoSePropagaComoError() throws ReglaNegocioException {
        escenario.iniciarTorneo();
        Partido partido = escenario.torneo.getPartidos().get(0);

        try {
            controlador.programarPartido(escenario.torneo, partido, LocalDateTime.of(2027, 1, 10, 18, 0),
                    new modelo.Estadio("E", 1000, "Calle", "Templado"), escenario.terna(1));
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la fecha está fuera del período del torneo
        }
        assertEquals(EstadoPartido.SIN_PROGRAMAR, partido.getEstado());
    }

    @Test
    void cargarLaNominaLaDejaDisponibleEnElPartido() throws ReglaNegocioException {
        escenario.iniciarTorneo();
        Partido partido = escenario.programarConNominas(escenario.torneo.getPartidos().get(0));

        Nomina nomina = partido.getNomina(partido.getLocal());

        assertNotNull(nomina);
        assertEquals(11, nomina.getTitulares().size());
        assertEquals(5, nomina.getSuplentes().size());
    }

    @Test
    void cargarUnaNominaIncompletaSePropagaComoError() throws ReglaNegocioException {
        escenario.iniciarTorneo();
        Partido partido = escenario.torneo.getPartidos().get(0);
        controlador.programarPartido(escenario.torneo, partido, LocalDateTime.of(2026, 4, 8, 18, 0),
                new modelo.Estadio("E", 1000, "Calle", "Templado"), escenario.terna(1));
        Nomina incompleta = new Nomina(partido.getLocal());
        incompleta.agregarTitular(escenario.jugador(partido.getLocal(), 0));

        try {
            controlador.cargarNomina(escenario.torneo, partido, incompleta);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
    }
}
