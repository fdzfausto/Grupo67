package modelo;

import excepcion.ReglaNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

// CU01 (modificación): los datos se pueden cambiar solo si siguen siendo válidos.
class ModificacionDatosTest {

    private Torneo torneo;

    @BeforeEach
    void preparar() throws ReglaNegocioException {
        torneo = new Torneo("Liga Profesional 2026", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 12, 15),
                TipoTorneo.NACIONAL, 2, 30);
    }

    // ---------- Torneo ----------

    @Test
    void unTorneoPlanificadoPuedeCambiarNombreYFechas() throws ReglaNegocioException {
        torneo.modificarDatos("Copa Nacional", LocalDate.of(2026, 4, 1), LocalDate.of(2026, 11, 30));

        assertEquals("Copa Nacional", torneo.getNombre());
        assertEquals(LocalDate.of(2026, 4, 1), torneo.getFechaInicio());
        assertEquals(LocalDate.of(2026, 11, 30), torneo.getFechaFin());
    }

    @Test
    void unTorneoEnCursoNoSePuedeModificar() throws ReglaNegocioException {
        for (int i = 0; i < 4; i++) {
            torneo.inscribirClub(DatosPrueba.club("Club" + i), DatosPrueba.plantel(i + 1));
        }
        torneo.avanzarEstado();

        try {
            torneo.modificarDatos("Otro", LocalDate.of(2026, 4, 1), LocalDate.of(2026, 11, 30));
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: solo se modifica un torneo planificado
        }
        assertEquals("Liga Profesional 2026", torneo.getNombre());
    }

    @Test
    void laModificacionRechazaNombreVacioOFechasInvertidas() {
        try {
            torneo.modificarDatos(" ", LocalDate.of(2026, 4, 1), LocalDate.of(2026, 11, 30));
            fail("Debía rechazar el nombre vacío");
        } catch (ReglaNegocioException e) {
            // correcto
        }
        try {
            torneo.modificarDatos("Copa", LocalDate.of(2026, 11, 30), LocalDate.of(2026, 4, 1));
            fail("Debía rechazar las fechas invertidas");
        } catch (ReglaNegocioException e) {
            // correcto
        }
        assertEquals("Liga Profesional 2026", torneo.getNombre()); // no queda a medio modificar
        assertEquals(LocalDate.of(2026, 3, 1), torneo.getFechaInicio());
    }

    @Test
    void laModificacionRechazaFechasNulasConReglaDeNegocio() {
        try {
            torneo.modificarDatos("Copa", null, LocalDate.of(2026, 11, 30));
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
        try {
            torneo.modificarDatos("Copa", LocalDate.of(2026, 4, 1), null);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
        assertEquals("Liga Profesional 2026", torneo.getNombre());
    }

    // ---------- Jugador ----------

    @Test
    void elNumeroDeCamisetaDebeEstarEntreUnoYNoventaYNueve() throws ReglaNegocioException {
        Jugador jugador = DatosPrueba.plantel(1).get(0);

        jugador.setNumeroCamiseta(99);
        assertEquals(99, jugador.getNumeroCamiseta());
        jugador.setNumeroCamiseta(1);
        assertEquals(1, jugador.getNumeroCamiseta());
        try {
            jugador.setNumeroCamiseta(0);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
        try {
            jugador.setNumeroCamiseta(100);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
        assertEquals(1, jugador.getNumeroCamiseta());
    }

    @Test
    void elValorDeMercadoNoPuedeSerNegativo() throws ReglaNegocioException {
        Jugador jugador = DatosPrueba.plantel(1).get(0);

        jugador.setValorMercado(0);
        jugador.setValorMercado(2500000);
        assertEquals(2500000, jugador.getValorMercado());
        try {
            jugador.setValorMercado(-1);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
        assertEquals(2500000, jugador.getValorMercado());
    }

    // ---------- Club, entrenador, árbitro y estadio ----------

    @Test
    void laSedeSocialNoPuedeQuedarVacia() throws ReglaNegocioException {
        Club club = DatosPrueba.club("River");

        club.setSedeSocial("Núñez");
        assertEquals("Núñez", club.getSedeSocial());
        try {
            club.setSedeSocial("");
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
        try {
            club.setSedeSocial(null);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
        assertEquals("Núñez", club.getSedeSocial());
    }

    @Test
    void laLicenciaDelEntrenadorNoPuedeQuedarVacia() throws ReglaNegocioException {
        Entrenador entrenador = new Entrenador("111", "Marcelo", "Gallardo", LocalDate.of(1976, 1, 1),
                "Director técnico", "LIC-1");

        entrenador.setLicencia("LIC-2");
        assertEquals("LIC-2", entrenador.getLicencia());
        try {
            entrenador.setLicencia("  ");
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
        assertEquals("LIC-2", entrenador.getLicencia());
    }

    @Test
    void laCategoriaDelArbitroNoPuedeQuedarVacia() throws ReglaNegocioException {
        Arbitro arbitro = DatosPrueba.arbitro(1, "nacional");

        arbitro.setCategoria("internacional");
        assertEquals("internacional", arbitro.getCategoria());
        try {
            arbitro.setCategoria(null);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
        assertEquals("internacional", arbitro.getCategoria());
    }

    @Test
    void laCapacidadDelEstadioDebeSerMayorACero() throws ReglaNegocioException {
        Estadio estadio = DatosPrueba.estadio("Monumental");

        estadio.setCapacidad(80000);
        assertEquals(80000, estadio.getCapacidad());
        try {
            estadio.setCapacidad(0);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
        try {
            estadio.setCapacidad(-5);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
        assertEquals(80000, estadio.getCapacidad());
    }

    @Test
    void elEstadioConservaElTerrenoOUsaUnValorPorDefecto() {
        Estadio sinTerreno = new Estadio("A", 1000, "Calle 1", "Templado");
        Estadio conTerreno = new Estadio("B", 1000, "Calle 2", "Templado", "Césped sintético");

        assertEquals("No informado", sinTerreno.getTerreno());
        assertEquals("Césped sintético", conTerreno.getTerreno()); // corrección del constructor de 5 parámetros
    }
}
