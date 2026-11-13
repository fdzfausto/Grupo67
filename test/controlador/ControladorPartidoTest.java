package controlador;

import eventos.ColorTarjeta;
import eventos.Evento;
import eventos.Falta;
import eventos.Gol;
import eventos.Remate;
import eventos.Tarjeta;
import eventos.TipoGol;
import eventos.ZonaRemate;
import excepcion.ReglaNegocioException;
import modelo.Club;
import modelo.EstadoPartido;
import modelo.Jugador;
import modelo.Partido;
import notificaciones.CanalNotificacion;
import notificaciones.Notificacion;
import notificaciones.Suscriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;

// CU07/CU08/CU09: el controlador registra los eventos y avisa a los suscriptores (gol, roja y final).
class ControladorPartidoTest {

    // Canal de prueba: guarda el texto de cada aviso que recibe.
    private static class CanalRegistrador implements CanalNotificacion {
        private static final long serialVersionUID = 1L;
        final List<String> avisos = new ArrayList<>();

        @Override
        public boolean enviar(Notificacion notificacion, Suscriptor suscriptor) {
            avisos.add(notificacion.getHecho());
            return true;
        }
    }

    // Canal de prueba que siempre falla.
    private static class CanalRoto implements CanalNotificacion {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean enviar(Notificacion notificacion, Suscriptor suscriptor) {
            throw new IllegalStateException("Canal caído");
        }
    }

    private EscenarioControlador escenario;
    private ControladorPartido controlador;
    private Club river;
    private Club boca;
    private Partido partido;
    private CanalRegistrador canal;
    private List<Suscriptor> suscriptores;

    @BeforeEach
    void preparar() throws ReglaNegocioException {
        escenario = new EscenarioControlador();
        escenario.iniciarTorneo();
        controlador = new ControladorPartido();
        river = escenario.clubes.get(0);
        boca = escenario.clubes.get(1);
        partido = escenario.programarConNominas(escenario.partidoEntre(river, boca));
        canal = new CanalRegistrador();
        suscriptores = new ArrayList<>();
        suscriptores.add(suscriptor("Seguidor de River", river, canal));
    }

    private Suscriptor suscriptor(String nombre, Club clubSeguido, CanalNotificacion... canales)
            throws ReglaNegocioException {
        Suscriptor s = new Suscriptor(nombre, "mail@mail.com", "1155");
        Set<CanalNotificacion> set = new LinkedHashSet<>();
        for (CanalNotificacion c : canales) {
            set.add(c);
        }
        s.configurarCanales(set);
        s.seguir(clubSeguido);
        return s;
    }

    private Jugador titular(Club club, int indice) {
        return escenario.jugador(club, indice);
    }

    // ---------- Ciclo de vida ----------

    @Test
    void iniciarElPartidoLoPoneEnJuego() throws ReglaNegocioException {
        controlador.iniciarPartido(partido);

        assertEquals(EstadoPartido.EN_JUEGO, partido.getEstado());
    }

    @Test
    void sePuedeSuspenderUnPartidoEnJuego() throws ReglaNegocioException {
        controlador.iniciarPartido(partido);

        controlador.suspenderPartido(partido);

        assertEquals(EstadoPartido.SUSPENDIDO, partido.getEstado());
    }

    @Test
    void noSePuedeIniciarUnPartidoSinSusNominas() throws ReglaNegocioException {
        Partido sinNominas = escenario.partidoEntre(escenario.clubes.get(2), escenario.clubes.get(3));
        // Se programa por el controlador de torneo pero no se cargan las nóminas.
        escenario.controladorTorneo.programarPartido(escenario.torneo, sinNominas,
                java.time.LocalDateTime.of(2026, 9, 1, 18, 0), new modelo.Estadio("E", 1000, "Calle", "Templado"),
                escenario.terna(50));

        try {
            controlador.iniciarPartido(sinNominas);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
    }

    @Test
    void noSePuedenRegistrarEventosSiElPartidoNoEstaEnJuego() throws ReglaNegocioException {
        Gol gol = new Gol(10, titular(river, 8), river, TipoGol.A_FAVOR);

        try {
            controlador.registrarEvento(partido, gol, suscriptores);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
        assertTrue(canal.avisos.isEmpty());
    }

    // ---------- Notificaciones por evento ----------

    @Test
    void unGolSeRegistraYSeNotificaALosSeguidoresDelClub() throws ReglaNegocioException {
        controlador.iniciarPartido(partido);
        Gol gol = new Gol(10, titular(river, 8), river, TipoGol.A_FAVOR);

        List<Evento> nuevos = controlador.registrarEvento(partido, gol, suscriptores);

        assertEquals(List.of(gol), nuevos);
        assertEquals(1, partido.getGoles(river));
        assertEquals(1, canal.avisos.size());
        assertTrue(canal.avisos.get(0).startsWith("Gol"));
    }

    @Test
    void quienNoSigueANingunoDeLosDosClubesNoRecibeElAviso() throws ReglaNegocioException {
        CanalRegistrador otro = new CanalRegistrador();
        suscriptores.add(suscriptor("Seguidor de Racing", escenario.clubes.get(2), otro));
        controlador.iniciarPartido(partido);

        controlador.registrarEvento(partido, new Gol(10, titular(river, 8), river, TipoGol.A_FAVOR), suscriptores);

        assertEquals(1, canal.avisos.size());
        assertTrue(otro.avisos.isEmpty());
    }

    @Test
    void tambienSeNotificaAlSeguidorDelClubVisitante() throws ReglaNegocioException {
        CanalRegistrador delVisitante = new CanalRegistrador();
        suscriptores.add(suscriptor("Seguidor de Boca", boca, delVisitante));
        controlador.iniciarPartido(partido);

        controlador.registrarEvento(partido, new Gol(10, titular(river, 8), river, TipoGol.A_FAVOR), suscriptores);

        assertEquals(1, delVisitante.avisos.size()); // sigue a uno de los dos clubes del partido
    }

    @Test
    void unaTarjetaRojaSeNotifica() throws ReglaNegocioException {
        controlador.iniciarPartido(partido);

        controlador.registrarEvento(partido, new Tarjeta(30, titular(river, 3), river, ColorTarjeta.ROJA), suscriptores);

        assertEquals(1, canal.avisos.size());
        assertTrue(canal.avisos.get(0).startsWith("Tarjeta roja"));
    }

    @Test
    void unaAmarillaUnRemateOUnaFaltaNoSeNotifican() throws ReglaNegocioException {
        controlador.iniciarPartido(partido);

        controlador.registrarEvento(partido, new Tarjeta(20, titular(river, 3), river, ColorTarjeta.AMARILLA), suscriptores);
        controlador.registrarEvento(partido, new Remate(25, titular(river, 9), river, ZonaRemate.AREA_GRANDE), suscriptores);
        controlador.registrarEvento(partido, new Falta(28, titular(river, 4), river, "Mano"), suscriptores);

        assertTrue(canal.avisos.isEmpty());
        assertEquals(3, partido.getEventos().size());
    }

    @Test
    void laSegundaAmarillaGeneraLaRojaAutomaticaYSeNotificaComoRoja() throws ReglaNegocioException {
        controlador.iniciarPartido(partido);
        Jugador jugador = titular(river, 3);
        controlador.registrarEvento(partido, new Tarjeta(20, jugador, river, ColorTarjeta.AMARILLA), suscriptores);

        List<Evento> nuevos = controlador.registrarEvento(partido,
                new Tarjeta(60, jugador, river, ColorTarjeta.AMARILLA), suscriptores);

        assertEquals(2, nuevos.size()); // la amarilla y la roja automática
        assertTrue(partido.estaExpulsado(jugador));
        assertEquals(1, canal.avisos.size());
        assertTrue(canal.avisos.get(0).startsWith("Tarjeta roja"));
    }

    @Test
    void unEventoInvalidoNoSeRegistraNiSeNotifica() throws ReglaNegocioException {
        controlador.iniciarPartido(partido);
        Jugador fueraDeNomina = titular(river, 19); // el plantel tiene 20: solo 16 están en la nómina

        try {
            controlador.registrarEvento(partido, new Gol(10, fueraDeNomina, river, TipoGol.A_FAVOR), suscriptores);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: el jugador no está en la nómina
        }
        assertTrue(partido.getEventos().isEmpty());
        assertTrue(canal.avisos.isEmpty());
    }

    @Test
    void siUnCanalFallaElEventoIgualQuedaRegistradoYLosOtrosCanalesAvisan() throws ReglaNegocioException {
        suscriptores.add(suscriptor("Con canal roto", river, new CanalRoto()));
        controlador.iniciarPartido(partido);

        controlador.registrarEvento(partido, new Gol(10, titular(river, 8), river, TipoGol.A_FAVOR), suscriptores);

        assertEquals(1, partido.getGoles(river)); // RN21: no frena la carga del partido
        assertEquals(1, canal.avisos.size());
    }

    // ---------- Anulación y cierre ----------

    @Test
    void anularUnGolLoDescuentaDelMarcador() throws ReglaNegocioException {
        controlador.iniciarPartido(partido);
        Gol gol = new Gol(10, titular(river, 8), river, TipoGol.A_FAVOR);
        controlador.registrarEvento(partido, gol, suscriptores);

        controlador.anularEvento(partido, gol, "Fuera de juego");

        assertEquals(0, partido.getGoles(river));
        assertTrue(partido.estaAnulado(gol));
        assertEquals(2, partido.getEventos().size()); // el evento original no se elimina
    }

    @Test
    void noSePuedeAnularDosVecesElMismoEvento() throws ReglaNegocioException {
        controlador.iniciarPartido(partido);
        Gol gol = new Gol(10, titular(river, 8), river, TipoGol.A_FAVOR);
        controlador.registrarEvento(partido, gol, suscriptores);
        controlador.anularEvento(partido, gol, "Fuera de juego");

        try {
            controlador.anularEvento(partido, gol, "Otra vez");
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
    }

    @Test
    void finalizarElPartidoLoCierraYNotificaElFinal() throws ReglaNegocioException {
        controlador.iniciarPartido(partido);

        controlador.finalizarPartido(escenario.torneo, partido, suscriptores);

        assertEquals(EstadoPartido.FINALIZADO, partido.getEstado());
        assertEquals(1, canal.avisos.size());
        assertTrue(canal.avisos.get(0).startsWith("Final del partido"));
    }

    @Test
    void alFinalizarSeActualizanLasEstadisticasDeLosJugadores() throws ReglaNegocioException {
        controlador.iniciarPartido(partido);
        Jugador goleador = titular(river, 8);
        controlador.registrarEvento(partido, new Gol(10, goleador, river, TipoGol.A_FAVOR), suscriptores);

        controlador.finalizarPartido(escenario.torneo, partido, suscriptores);

        assertEquals(1, goleador.getGoles());
        assertEquals(1, goleador.getPartidosJugados());
    }

    @Test
    void alFinalizarSeRegistraLaSuspensionPorUnaRojaDirecta() throws ReglaNegocioException {
        controlador.iniciarPartido(partido);
        Jugador expulsado = titular(boca, 3);
        controlador.registrarEvento(partido, new Tarjeta(40, expulsado, boca, ColorTarjeta.ROJA), suscriptores);

        controlador.finalizarPartido(escenario.torneo, partido, suscriptores);

        assertTrue(escenario.torneo.estaSuspendido(expulsado));
        assertEquals(1, escenario.torneo.getSanciones().size());
    }

    @Test
    void noSePuedeFinalizarUnPartidoQueNoEstaEnJuego() {
        try {
            controlador.finalizarPartido(escenario.torneo, partido, suscriptores);
            fail("Debía lanzar ReglaNegocioException");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó la operación
        }
        assertEquals(EstadoPartido.PROGRAMADO, partido.getEstado());
        assertTrue(canal.avisos.isEmpty());
    }
}
