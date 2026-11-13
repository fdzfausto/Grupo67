package modelo;

import eventos.ColorTarjeta;
import eventos.Tarjeta;
import excepcion.ReglaNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TorneoTest {

    private Torneo torneo;
    private List<Club> clubes;
    private List<List<Jugador>> planteles;

    @BeforeEach
    void preparar() throws ReglaNegocioException {
        torneo = new Torneo("Liga Profesional 2026", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 12, 15),
                TipoTorneo.NACIONAL, 2, 30);
        clubes = new ArrayList<>();
        planteles = new ArrayList<>();
        String[] nombres = {"River", "Boca", "Racing", "Independiente"};
        for (int i = 0; i < nombres.length; i++) {
            clubes.add(DatosPrueba.club(nombres[i]));
            planteles.add(DatosPrueba.plantel(i + 1));
        }
    }

    private void inscribirClubes(int cantidad) throws ReglaNegocioException {
        for (int i = 0; i < cantidad; i++) {
            torneo.inscribirClub(clubes.get(i), planteles.get(i));
        }
    }

    // Con 4 clubes inscriptos genera el fixture y devuelve el primer partido.
    private Partido fixtureConPrimerPartido() throws ReglaNegocioException {
        inscribirClubes(4);
        torneo.generarFixture();
        return torneo.getPartidos().get(0);
    }

    // ---------- Estados e inscripciones ----------

    @Test
    void unTorneoNuevoEstaPlanificado() {
        assertEquals(EstadoTorneo.PLANIFICADO, torneo.getEstado());
    }

    @Test
    void elTorneoNoPuedeTenerFechaDeFinAnteriorALaDeInicio() {
        try {
            new Torneo("Copa", LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 4, 1), TipoTorneo.NACIONAL, 1, 0);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
    }

    @Test
    void noSePuedeIniciarConMenosDeCuatroClubes() throws ReglaNegocioException {
        inscribirClubes(3);
        try {
            torneo.avanzarEstado();
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
        assertEquals(EstadoTorneo.PLANIFICADO, torneo.getEstado());
    }

    @Test
    void elTorneoAvanzaSoloAlEstadoSiguienteYNoPasaDeFinalizado() throws ReglaNegocioException {
        inscribirClubes(4);
        torneo.avanzarEstado();
        assertEquals(EstadoTorneo.EN_CURSO, torneo.getEstado());
        torneo.avanzarEstado();
        assertEquals(EstadoTorneo.FINALIZADO, torneo.getEstado());
        try {
            torneo.avanzarEstado();
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
    }

    @Test
    void unTorneoEnCursoNoAdmiteNuevasInscripciones() throws ReglaNegocioException {
        inscribirClubes(4);
        torneo.avanzarEstado();
        Club nuevo = DatosPrueba.club("San Lorenzo");
        List<Jugador> plantel = DatosPrueba.plantel(9);
        try {
            torneo.inscribirClub(nuevo, plantel);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
    }

    @Test
    void unClubSoloSePuedeInscribirUnaVezEnElTorneo() throws ReglaNegocioException {
        inscribirClubes(1);
        List<Jugador> otroPlantel = DatosPrueba.plantel(8);
        try {
            torneo.inscribirClub(clubes.get(0), otroPlantel);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
    }

    @Test
    void elPlantelDebeTenerEntre18Y30Jugadores() {
        List<Jugador> corto = DatosPrueba.plantel(7).subList(0, 17);
        try {
            torneo.inscribirClub(clubes.get(0), corto);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
        assertTrue(torneo.getInscripciones().isEmpty());
    }

    @Test
    void unJugadorNoPuedeEstarEnDosClubesDelMismoTorneo() throws ReglaNegocioException {
        inscribirClubes(1);
        // El plantel de Boca incluye a un jugador que ya está inscripto con River.
        List<Jugador> plantelConRepetido = new ArrayList<>(planteles.get(1));
        plantelConRepetido.set(0, planteles.get(0).get(0));
        try {
            torneo.inscribirClub(clubes.get(1), plantelConRepetido);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
    }

    // ---------- Fixture ----------

    @Test
    void noSeGeneraElFixtureConMenosDeCuatroClubes() throws ReglaNegocioException {
        inscribirClubes(3);
        try {
            torneo.generarFixture();
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
    }

    @Test
    void elFixtureEnfrentaATodosContraTodosUnaVez() throws ReglaNegocioException {
        inscribirClubes(4);
        torneo.generarFixture();
        assertEquals(3, torneo.getFixture().getFechas().size());
        assertEquals(6, torneo.getPartidos().size());
    }

    // ---------- Programación de partidos (CU05 y CU14) ----------

    @Test
    void sePuedeProgramarUnPartidoDentroDelPeriodo() throws ReglaNegocioException {
        Partido partido = fixtureConPrimerPartido();
        torneo.programarPartido(partido, LocalDateTime.of(2026, 5, 10, 18, 0),
                DatosPrueba.estadio("Monumental"), DatosPrueba.terna(10, "nacional"));
        assertEquals(EstadoPartido.PROGRAMADO, partido.getEstado());
        assertNotNull(partido.getEstadio());
    }

    @Test
    void laFechaDelPartidoDebeEstarDentroDelPeriodoDelTorneo() throws ReglaNegocioException {
        Partido partido = fixtureConPrimerPartido();
        Estadio estadio = DatosPrueba.estadio("Monumental");
        TernaArbitral terna = DatosPrueba.terna(10, "nacional");
        try {
            torneo.programarPartido(partido,
                LocalDateTime.of(2027, 1, 10, 18, 0), estadio, terna);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
        assertEquals(EstadoPartido.SIN_PROGRAMAR, partido.getEstado());
    }

    @Test
    void unEstadioNoPuedeAlojarDosPartidosConMenosDeTresHorasDeDiferencia() throws ReglaNegocioException {
        inscribirClubes(4);
        torneo.generarFixture();
        Partido primero = torneo.getFixture().getFechas().get(0).get(0);
        Partido segundo = torneo.getFixture().getFechas().get(0).get(1);
        Estadio estadio = DatosPrueba.estadio("Monumental");
        torneo.programarPartido(primero, LocalDateTime.of(2026, 5, 10, 18, 0), estadio, DatosPrueba.terna(10, "nacional"));
        TernaArbitral otraTerna = DatosPrueba.terna(20, "nacional");
        try {
            torneo.programarPartido(segundo,
                LocalDateTime.of(2026, 5, 10, 20, 0), estadio, otraTerna);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
        // Con 3 horas de diferencia ya es posible.
        torneo.programarPartido(segundo, LocalDateTime.of(2026, 5, 10, 21, 0), estadio, otraTerna);
        assertEquals(EstadoPartido.PROGRAMADO, segundo.getEstado());
    }

    @Test
    void unClubNoPuedeJugarDosPartidosConMenosDe48HorasDeDiferencia() throws ReglaNegocioException {
        inscribirClubes(4);
        torneo.generarFixture();
        Partido primero = torneo.getFixture().getFechas().get(0).get(0);
        Club repetido = primero.getLocal();
        Partido segundo = null;
        for (Partido p : torneo.getPartidos()) {
            if (p != primero && p.participa(repetido)) {
                segundo = p;
                break;
            }
        }
        final Partido otro = segundo;
        torneo.programarPartido(primero, LocalDateTime.of(2026, 5, 10, 18, 0),
                DatosPrueba.estadio("Monumental"), DatosPrueba.terna(10, "nacional"));
        Estadio otroEstadio = DatosPrueba.estadio("Bombonera");
        TernaArbitral otraTerna = DatosPrueba.terna(20, "nacional");
        try {
            torneo.programarPartido(otro,
                LocalDateTime.of(2026, 5, 11, 18, 0), otroEstadio, otraTerna);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
        torneo.programarPartido(otro, LocalDateTime.of(2026, 5, 12, 18, 0), otroEstadio, otraTerna);
        assertEquals(EstadoPartido.PROGRAMADO, otro.getEstado());
    }

    @Test
    void unArbitroNoPuedeDirigirDosPartidosElMismoDia() throws ReglaNegocioException {
        inscribirClubes(4);
        torneo.generarFixture();
        Partido primero = torneo.getFixture().getFechas().get(0).get(0);
        Partido segundo = torneo.getFixture().getFechas().get(0).get(1);
        TernaArbitral terna = DatosPrueba.terna(10, "nacional");
        torneo.programarPartido(primero, LocalDateTime.of(2026, 5, 10, 12, 0), DatosPrueba.estadio("Monumental"), terna);
        Estadio otroEstadio = DatosPrueba.estadio("Bombonera");
        try {
            torneo.programarPartido(segundo,
                LocalDateTime.of(2026, 5, 10, 20, 0), otroEstadio, terna);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
    }

    @Test
    void unArbitroNoPuedeDirigirAUnClubDelQueEsHincha() throws ReglaNegocioException {
        Partido partido = fixtureConPrimerPartido();
        TernaArbitral terna = DatosPrueba.terna(10, "nacional");
        terna.getPrincipal().agregarClubHincha(partido.getLocal());
        Estadio estadio = DatosPrueba.estadio("Monumental");
        try {
            torneo.programarPartido(partido,
                LocalDateTime.of(2026, 5, 10, 18, 0), estadio, terna);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
    }

    @Test
    void unTorneoInternacionalExigeTernaInternacional() throws ReglaNegocioException {
        Torneo copa = new Torneo("Copa Libertadores", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 12, 15),
                TipoTorneo.INTERNACIONAL, 2, 30);
        for (int i = 0; i < 4; i++) {
            copa.inscribirClub(clubes.get(i), planteles.get(i));
        }
        copa.generarFixture();
        Partido partido = copa.getPartidos().get(0);
        Estadio estadio = DatosPrueba.estadio("Monumental");
        try {
            copa.programarPartido(partido,
                LocalDateTime.of(2026, 5, 10, 18, 0), estadio, DatosPrueba.terna(10, "nacional"));
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
        copa.programarPartido(partido, LocalDateTime.of(2026, 5, 10, 18, 0), estadio,
                DatosPrueba.terna(10, "internacional"));
        assertEquals(EstadoPartido.PROGRAMADO, partido.getEstado());
    }

    // ---------- Sanciones (RN18) ----------

    // Programa el primer partido, carga las dos nóminas, lo inicia y devuelve el partido en juego.
    private Partido partidoEnJuego() throws ReglaNegocioException {
        Partido partido = fixtureConPrimerPartido();
        torneo.programarPartido(partido, LocalDateTime.of(2026, 5, 10, 18, 0),
                DatosPrueba.estadio("Monumental"), DatosPrueba.terna(10, "nacional"));
        torneo.cargarNomina(partido, DatosPrueba.nomina(partido.getLocal(), plantelDe(partido.getLocal())));
        torneo.cargarNomina(partido, DatosPrueba.nomina(partido.getVisitante(), plantelDe(partido.getVisitante())));
        partido.iniciar();
        return partido;
    }

    private List<Jugador> plantelDe(Club club) {
        return planteles.get(clubes.indexOf(club));
    }

    @Test
    void unaRojaDirectaSuspendeAlJugadorParaLaFechaSiguiente() throws ReglaNegocioException {
        Partido partido = partidoEnJuego();
        Jugador expulsado = plantelDe(partido.getLocal()).get(5);
        partido.registrarEvento(new Tarjeta(33, expulsado, partido.getLocal(), ColorTarjeta.ROJA));
        partido.finalizar();
        torneo.registrarSanciones(partido);
        assertTrue(torneo.estaSuspendido(expulsado));
        assertEquals(1, torneo.getSanciones().size());
    }

    @Test
    void unJugadorSuspendidoNoPuedeIncluirseEnLaNominaDelPartidoSiguiente() throws ReglaNegocioException {
        Partido partido = partidoEnJuego();
        Club club = partido.getLocal();
        Jugador expulsado = plantelDe(club).get(5);
        partido.registrarEvento(new Tarjeta(33, expulsado, club, ColorTarjeta.ROJA));
        partido.finalizar();
        torneo.registrarSanciones(partido);

        Partido siguiente = null;
        for (Partido p : torneo.getPartidos()) {
            if (p != partido && p.participa(club)) {
                siguiente = p;
                break;
            }
        }
        final Partido proximo = siguiente;
        torneo.programarPartido(proximo, LocalDateTime.of(2026, 5, 20, 18, 0),
                DatosPrueba.estadio("Bombonera"), DatosPrueba.terna(30, "nacional"));
        Nomina nomina = DatosPrueba.nomina(club, plantelDe(club)); // incluye al suspendido
        try {
            torneo.cargarNomina(proximo, nomina);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
    }

    @Test
    void laDobleAmarillaGeneraSuspensionPorLaRojaPeroNoSumaParaElLimiteDeCincoAmarillas() throws ReglaNegocioException {
        Partido partido = partidoEnJuego();
        Jugador jugador = plantelDe(partido.getLocal()).get(6);
        partido.registrarEvento(new Tarjeta(20, jugador, partido.getLocal(), ColorTarjeta.AMARILLA));
        partido.registrarEvento(new Tarjeta(70, jugador, partido.getLocal(), ColorTarjeta.AMARILLA));
        partido.finalizar();
        torneo.registrarSanciones(partido);
        assertEquals(1, torneo.getSanciones().size());
        assertEquals(Sancion.MOTIVO_ROJA, torneo.getSanciones().get(0).getMotivo());
    }

    @Test
    void unaAmarillaSueltaNoGeneraSancion() throws ReglaNegocioException {
        Partido partido = partidoEnJuego();
        Jugador jugador = plantelDe(partido.getLocal()).get(6);
        partido.registrarEvento(new Tarjeta(20, jugador, partido.getLocal(), ColorTarjeta.AMARILLA));
        partido.finalizar();
        torneo.registrarSanciones(partido);
        assertFalse(torneo.estaSuspendido(jugador));
        assertTrue(torneo.getSanciones().isEmpty());
    }

    @Test
    void lasSancionesDeUnPartidoSeRegistranUnaSolaVez() throws ReglaNegocioException {
        Partido partido = partidoEnJuego();
        partido.finalizar();
        torneo.registrarSanciones(partido);
        try {
            torneo.registrarSanciones(partido);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
    }
}
