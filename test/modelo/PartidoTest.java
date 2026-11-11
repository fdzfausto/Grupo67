package modelo;

import eventos.Cambio;
import eventos.ColorTarjeta;
import eventos.Evento;
import eventos.Gol;
import eventos.Remate;
import eventos.Tarjeta;
import eventos.TipoGol;
import eventos.ZonaRemate;
import excepcion.ReglaNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PartidoTest {

    private Club local;
    private Club visitante;
    private List<Jugador> plantelLocal;
    private List<Jugador> plantelVisitante;
    private Partido partido;

    @BeforeEach
    void preparar() throws ReglaNegocioException {
        local = DatosPrueba.club("River");
        visitante = DatosPrueba.club("Boca");
        plantelLocal = DatosPrueba.plantel(1);
        plantelVisitante = DatosPrueba.plantel(2);
        partido = new Partido(local, visitante);
        partido.programar(LocalDateTime.of(2026, 5, 10, 18, 0), DatosPrueba.estadio("Monumental"),
                DatosPrueba.terna(10, "nacional"));
    }

    // Carga las dos nóminas e inicia el partido.
    private void iniciarPartido() throws ReglaNegocioException {
        partido.cargarNomina(DatosPrueba.nomina(local, plantelLocal), new Inscripcion(local, plantelLocal));
        partido.cargarNomina(DatosPrueba.nomina(visitante, plantelVisitante), new Inscripcion(visitante, plantelVisitante));
        partido.iniciar();
    }

    @Test
    void unPartidoNoPuedeEnfrentarAUnClubConsigoMismo() {
        assertThrows(ReglaNegocioException.class, () -> new Partido(local, local));
    }

    @Test
    void noSeRegistranEventosSiElPartidoNoEstaEnJuego() throws ReglaNegocioException {
        Gol gol = new Gol(10, plantelLocal.get(0), local, TipoGol.A_FAVOR);
        assertThrows(ReglaNegocioException.class, () -> partido.registrarEvento(gol));
    }

    @Test
    void noSePuedeIniciarSinLasDosNominas() throws ReglaNegocioException {
        partido.cargarNomina(DatosPrueba.nomina(local, plantelLocal), new Inscripcion(local, plantelLocal));
        assertThrows(ReglaNegocioException.class, () -> partido.iniciar());
    }

    @Test
    void unaNominaIncompletaEsRechazada() throws ReglaNegocioException {
        Nomina incompleta = new Nomina(local);
        incompleta.agregarTitular(plantelLocal.get(0));
        Inscripcion inscripcion = new Inscripcion(local, plantelLocal);
        assertThrows(ReglaNegocioException.class, () -> partido.cargarNomina(incompleta, inscripcion));
    }

    @Test
    void elGolAFavorSumaAlClubDelGoleador() throws ReglaNegocioException {
        iniciarPartido();
        partido.registrarEvento(new Gol(10, plantelLocal.get(0), local, TipoGol.A_FAVOR));
        assertEquals(1, partido.getGoles(local));
        assertEquals(0, partido.getGoles(visitante));
    }

    @Test
    void elGolEnContraSumaAlRival() throws ReglaNegocioException {
        iniciarPartido();
        partido.registrarEvento(new Gol(30, plantelLocal.get(1), local, TipoGol.EN_CONTRA));
        assertEquals(0, partido.getGoles(local));
        assertEquals(1, partido.getGoles(visitante));
    }

    @Test
    void elGolDePenalSumaAlMarcador() throws ReglaNegocioException {
        iniciarPartido();
        partido.registrarEvento(new Gol(55, plantelVisitante.get(9), visitante, TipoGol.PENAL));
        assertEquals(1, partido.getGoles(visitante));
    }

    @Test
    void unJugadorFueraDeLaNominaNoPuedeProtagonizarEventos() throws ReglaNegocioException {
        iniciarPartido();
        Jugador fueraDeNomina = plantelLocal.get(18);
        Gol gol = new Gol(20, fueraDeNomina, local, TipoGol.A_FAVOR);
        assertThrows(ReglaNegocioException.class, () -> partido.registrarEvento(gol));
    }

    @Test
    void dosAmarillasGeneranUnaRojaYElJugadorQuedaExpulsado() throws ReglaNegocioException {
        iniciarPartido();
        Jugador jugador = plantelLocal.get(4);
        partido.registrarEvento(new Tarjeta(20, jugador, local, ColorTarjeta.AMARILLA));
        assertFalse(partido.estaExpulsado(jugador));
        partido.registrarEvento(new Tarjeta(60, jugador, local, ColorTarjeta.AMARILLA));
        assertTrue(partido.estaExpulsado(jugador));
    }

    @Test
    void unJugadorExpulsadoNoPuedeParticiparDeMasEventos() throws ReglaNegocioException {
        iniciarPartido();
        Jugador jugador = plantelLocal.get(4);
        partido.registrarEvento(new Tarjeta(20, jugador, local, ColorTarjeta.ROJA));
        Remate remate = new Remate(40, jugador, local, ZonaRemate.AREA_GRANDE);
        assertThrows(ReglaNegocioException.class, () -> partido.registrarEvento(remate));
    }

    @Test
    void unJugadorExpulsadoNoPuedeSerSustituido() throws ReglaNegocioException {
        iniciarPartido();
        Jugador expulsado = plantelLocal.get(4);
        partido.registrarEvento(new Tarjeta(20, expulsado, local, ColorTarjeta.ROJA));
        Cambio cambio = new Cambio(50, expulsado, plantelLocal.get(11), local);
        assertThrows(ReglaNegocioException.class, () -> partido.registrarEvento(cambio));
    }

    @Test
    void unJugadorSustituidoNoPuedeVolverAJugar() throws ReglaNegocioException {
        iniciarPartido();
        Jugador sale = plantelLocal.get(3);
        partido.registrarEvento(new Cambio(46, sale, plantelLocal.get(11), local));
        Remate remate = new Remate(70, sale, local, ZonaRemate.AREA_CHICA);
        assertThrows(ReglaNegocioException.class, () -> partido.registrarEvento(remate));
    }

    @Test
    void cadaClubHaceComoMaximoCincoCambios() throws ReglaNegocioException {
        iniciarPartido();
        for (int i = 0; i < Partido.MAX_CAMBIOS; i++) {
            partido.registrarEvento(new Cambio(60 + i, plantelLocal.get(i), plantelLocal.get(11 + i), local));
        }
        Cambio sexto = new Cambio(80, plantelLocal.get(5), plantelLocal.get(16), local);
        assertThrows(ReglaNegocioException.class, () -> partido.registrarEvento(sexto));
    }

    @Test
    void unEventoAnuladoDejaDeContarEnElMarcador() throws ReglaNegocioException {
        iniciarPartido();
        Gol gol = new Gol(10, plantelLocal.get(0), local, TipoGol.A_FAVOR);
        partido.registrarEvento(gol);
        partido.anularEvento(gol, "Error de carga");
        assertTrue(partido.estaAnulado(gol));
        assertEquals(0, partido.getGoles(local));
    }

    @Test
    void elEventoAnuladoSigueRegistradoEnElPartido() throws ReglaNegocioException {
        iniciarPartido();
        Gol gol = new Gol(10, plantelLocal.get(0), local, TipoGol.A_FAVOR);
        partido.registrarEvento(gol);
        partido.anularEvento(gol, "Error de carga");
        List<Evento> eventos = partido.getEventos();
        assertTrue(eventos.contains(gol));
        assertEquals(2, eventos.size());
    }

    @Test
    void unEventoNoSePuedeAnularDosVeces() throws ReglaNegocioException {
        iniciarPartido();
        Gol gol = new Gol(10, plantelLocal.get(0), local, TipoGol.A_FAVOR);
        partido.registrarEvento(gol);
        partido.anularEvento(gol, "Error de carga");
        assertThrows(ReglaNegocioException.class, () -> partido.anularEvento(gol, "Otra vez"));
    }

    @Test
    void alFinalizarSeActualizanLasEstadisticasDeLosJugadores() throws ReglaNegocioException {
        iniciarPartido();
        Jugador goleador = plantelLocal.get(9);
        Jugador suplenteQueEntra = plantelLocal.get(11);
        Jugador suplenteQueNoEntra = plantelLocal.get(12);
        partido.registrarEvento(new Gol(15, goleador, local, TipoGol.A_FAVOR));
        partido.registrarEvento(new Cambio(70, plantelLocal.get(2), suplenteQueEntra, local));
        partido.finalizar();
        assertEquals(EstadoPartido.FINALIZADO, partido.getEstado());
        assertEquals(1, goleador.getGoles());
        assertEquals(1, goleador.getPartidosJugados());
        assertEquals(1, suplenteQueEntra.getPartidosJugados());
        assertEquals(0, suplenteQueNoEntra.getPartidosJugados());
    }

    @Test
    void elXGDeUnClubEsLaSumaDeLosXGDeSusRemates() throws ReglaNegocioException {
        iniciarPartido();
        partido.registrarEvento(new Remate(12, plantelLocal.get(9), local, ZonaRemate.AREA_CHICA));
        partido.registrarEvento(new Remate(33, plantelLocal.get(8), local, ZonaRemate.FUERA_DEL_AREA));
        partido.registrarEvento(new Remate(51, plantelVisitante.get(9), visitante, ZonaRemate.PENAL));
        assertEquals(0.44, partido.calcularXGClub(local), 0.0001);
        assertEquals(0.76, partido.calcularXGClub(visitante), 0.0001);
    }

    @Test
    void soloSePuedeSuspenderUnPartidoProgramadoOEnJuego() throws ReglaNegocioException {
        iniciarPartido();
        partido.finalizar();
        assertThrows(ReglaNegocioException.class, () -> partido.suspender());
    }
}
