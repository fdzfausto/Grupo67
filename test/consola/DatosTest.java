package consola;

import excepcion.ReglaNegocioException;
import modelo.Arbitro;
import modelo.Club;
import modelo.Entrenador;
import modelo.Estadio;
import modelo.Jugador;
import modelo.Partido;
import modelo.TernaArbitral;
import modelo.Torneo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;

// CU01 (baja): solo se da de baja lo que no está en uso. Se parte de los datos de demostración.
class DatosTest {

    private Datos datos;

    @BeforeEach
    void preparar() throws ReglaNegocioException {
        datos = new Datos();
        datos.cargarDemostracion();
    }

    private Torneo torneo() {
        return datos.getTorneos().get(0);
    }

    // Programa el primer partido del fixture con los tres primeros árbitros y el primer estadio.
    private Partido programarPrimerPartido() throws ReglaNegocioException {
        Partido partido = torneo().getPartidos().get(0);
        TernaArbitral terna = new TernaArbitral(datos.getArbitros().get(0), datos.getArbitros().get(1),
                datos.getArbitros().get(2));
        torneo().programarPartido(partido, LocalDateTime.of(2026, 4, 1, 18, 0), datos.getEstadios().get(0), terna);
        return partido;
    }

    // ---------- Datos de demostración ----------

    @Test
    void laDemostracionCargaTodoLoNecesarioParaProbarElSistema() {
        assertEquals(1, datos.getTorneos().size());
        assertEquals(4, datos.getClubes().size());
        assertEquals(80, datos.getJugadores().size());
        assertEquals(4, datos.getEntrenadores().size());
        assertEquals(9, datos.getArbitros().size());
        assertEquals(3, datos.getEstadios().size());
        assertEquals(1, datos.getSponsors().size());
        assertEquals(1, datos.getSuscriptores().size());
        assertEquals(6, torneo().getPartidos().size()); // 4 clubes, todos contra todos, una rueda
    }

    @Test
    void jugadoresDeUnClubSonLosQueTienenContratoVigente() {
        Club river = datos.getClubes().get(0);

        assertEquals(20, datos.jugadoresDe(river).size());
    }

    @Test
    void unJugadorSinContratoNoFiguraEnNingunPlantel() throws ReglaNegocioException {
        Jugador libre = datos.getJugadores().get(0);
        libre.rescindirContrato(LocalDate.of(2026, 2, 1));

        assertEquals(19, datos.jugadoresDe(datos.getClubes().get(0)).size());
    }

    @Test
    void existeDniBuscaEnJugadoresEntrenadoresYArbitros() {
        assertTrue(datos.existeDni("30000000"));  // jugador
        assertTrue(datos.existeDni("10000000"));  // entrenador
        assertTrue(datos.existeDni("20000000"));  // árbitro
        assertFalse(datos.existeDni("1"));
    }

    // ---------- Bajas ----------

    @Test
    void noSeDaDeBajaUnClubInscriptoEnUnTorneo() {
        Club inscripto = datos.getClubes().get(0);

        try {
            datos.bajaClub(inscripto);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
        assertTrue(datos.getClubes().contains(inscripto));
    }

    @Test
    void noSeDaDeBajaUnClubConJugadoresConContratoVigente() throws ReglaNegocioException {
        Club conContratos = new Club("Estudiantes", LocalDate.of(1905, 8, 4), "La Plata");
        datos.getClubes().add(conContratos);
        Jugador jugador = new Jugador("55555555", "Juan", "Perez", LocalDate.of(1999, 1, 1), "Arquero", 1, 1000, 0);
        datos.getJugadores().add(jugador);
        jugador.registrarContrato(conContratos, LocalDate.of(2026, 1, 1));

        try {
            datos.bajaClub(conContratos);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
    }


    @Test
    void seDaDeBajaUnClubSinTorneosNiJugadores() throws ReglaNegocioException {
        Club libre = new Club("Estudiantes", LocalDate.of(1905, 8, 4), "La Plata");
        datos.getClubes().add(libre);

        datos.bajaClub(libre);

        assertFalse(datos.getClubes().contains(libre));
        assertEquals(4, datos.getClubes().size());
    }

    @Test
    void noSeDaDeBajaUnJugadorConContratoVigente() {
        Jugador conContrato = datos.getJugadores().get(0);

        try {
            datos.bajaJugador(conContrato);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
        assertTrue(datos.getJugadores().contains(conContrato));
    }

    @Test
    void noSeDaDeBajaUnJugadorInscriptoAunqueHayaRescindido() throws ReglaNegocioException {
        Jugador inscripto = datos.getJugadores().get(0);
        inscripto.rescindirContrato(LocalDate.of(2026, 2, 1));

        try {
            datos.bajaJugador(inscripto); // sigue en la inscripción
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
    }

    @Test
    void seDaDeBajaUnJugadorLibreQueNoEstaInscripto() throws ReglaNegocioException {
        Jugador libre = new Jugador("55555555", "Juan", "Perez", LocalDate.of(1999, 1, 1), "Arquero", 1, 1000, 0);
        datos.getJugadores().add(libre);

        datos.bajaJugador(libre);

        assertFalse(datos.getJugadores().contains(libre));
    }

    @Test
    void noSeDaDeBajaUnEntrenadorQueDirigeUnClub() {
        Entrenador dirigiendo = datos.getEntrenadores().get(0);

        try {
            datos.bajaEntrenador(dirigiendo);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
        assertTrue(datos.getEntrenadores().contains(dirigiendo));
    }

    @Test
    void seDaDeBajaUnEntrenadorSinClub() throws ReglaNegocioException {
        Entrenador libre = new Entrenador("66666666", "Marcelo", "Gallardo", LocalDate.of(1976, 1, 1),
                "Director técnico", "LIC-1");
        datos.getEntrenadores().add(libre);

        datos.bajaEntrenador(libre);

        assertFalse(datos.getEntrenadores().contains(libre));
    }

    @Test
    void noSeDaDeBajaUnArbitroAsignadoAUnPartido() throws ReglaNegocioException {
        programarPrimerPartido();
        Arbitro asignado = datos.getArbitros().get(0);

        try {
            datos.bajaArbitro(asignado);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
        assertTrue(datos.getArbitros().contains(asignado));
    }

    @Test
    void seDaDeBajaUnArbitroSinPartidos() throws ReglaNegocioException {
        programarPrimerPartido();
        Arbitro libre = datos.getArbitros().get(8); // no está en la terna

        datos.bajaArbitro(libre);

        assertFalse(datos.getArbitros().contains(libre));
    }

    @Test
    void noSeDaDeBajaUnEstadioAsignadoAUnPartido() throws ReglaNegocioException {
        programarPrimerPartido();
        Estadio usado = datos.getEstadios().get(0);

        try {
            datos.bajaEstadio(usado);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
        assertTrue(datos.getEstadios().contains(usado));
    }

    @Test
    void seDaDeBajaUnEstadioSinPartidos() throws ReglaNegocioException {
        programarPrimerPartido();
        Estadio libre = datos.getEstadios().get(2);

        datos.bajaEstadio(libre);

        assertFalse(datos.getEstadios().contains(libre));
        assertEquals(2, datos.getEstadios().size());
    }

    @Test
    void seDaDeBajaUnTorneoPlanificado() throws ReglaNegocioException {
        datos.bajaTorneo(torneo());

        assertTrue(datos.getTorneos().isEmpty());
    }

    @Test
    void noSeDaDeBajaUnTorneoEnCurso() throws ReglaNegocioException {
        Torneo enCurso = torneo();
        enCurso.avanzarEstado();

        try {
            datos.bajaTorneo(enCurso);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
        assertEquals(List.of(enCurso), datos.getTorneos());
    }
}
