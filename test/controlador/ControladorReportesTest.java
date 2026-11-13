package controlador;

import eventos.Gol;
import eventos.Remate;
import eventos.TipoGol;
import eventos.ZonaRemate;
import excepcion.ReglaNegocioException;
import modelo.Club;
import modelo.Partido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reportes.Goleador;
import reportes.ProyeccionPorPromedio;
import reportes.ServicioReportes;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// El controlador de reportes solo delega: debe dar lo mismo que ServicioReportes.
class ControladorReportesTest {

    private EscenarioControlador escenario;
    private ControladorReportes controlador;
    private ServicioReportes servicio;
    private Club river;
    private Club boca;
    private Partido partido;

    @BeforeEach
    void preparar() throws ReglaNegocioException {
        escenario = new EscenarioControlador();
        escenario.iniciarTorneo();
        controlador = new ControladorReportes();
        servicio = new ServicioReportes();
        river = escenario.clubes.get(0);
        boca = escenario.clubes.get(1);

        // River le gana 2 a 0 a Boca. Los goles los hace el jugador 9 de River (índice 8).
        ControladorPartido controladorPartido = new ControladorPartido();
        partido = escenario.programarConNominas(escenario.partidoEntre(river, boca));
        controladorPartido.iniciarPartido(partido);
        controladorPartido.registrarEvento(partido, new Gol(10, escenario.jugador(river, 8), river, TipoGol.A_FAVOR), List.of());
        controladorPartido.registrarEvento(partido, new Gol(50, escenario.jugador(river, 8), river, TipoGol.A_FAVOR), List.of());
        controladorPartido.registrarEvento(partido, new Remate(60, escenario.jugador(boca, 9), boca, ZonaRemate.AREA_CHICA), List.of());
        controladorPartido.registrarEvento(partido, new Remate(70, escenario.jugador(boca, 9), boca, ZonaRemate.PENAL), List.of());
        controladorPartido.finalizarPartido(escenario.torneo, partido, List.of());
    }

    @Test
    void laTablaDePosicionesCoincideConLaDelServicioYPonePrimeroAlGanador() {
        String tabla = controlador.generarTablaPosiciones(escenario.torneo);

        assertEquals(servicio.generarTablaPosiciones(escenario.torneo), tabla);
        assertTrue(tabla.contains("River"));
        assertTrue(tabla.indexOf("River") < tabla.indexOf("Boca")); // River tiene 3 puntos y Boca 0
    }

    @Test
    void losGoleadoresCoincidenConLosDelServicio() {
        List<Goleador> goleadores = controlador.obtenerGoleadores(escenario.torneo);

        assertEquals(1, goleadores.size());
        assertEquals(2, goleadores.get(0).getGoles());
        assertEquals(escenario.jugador(river, 8), goleadores.get(0).getJugador());
        assertEquals(servicio.obtenerGoleadores(escenario.torneo).size(), goleadores.size());
    }

    @Test
    void laVallaMenosVencidaEsElClubQueNoRecibioGoles() {
        Club valla = controlador.obtenervallaMenosVencida(escenario.torneo);

        assertEquals(river, valla); // River recibió 0 goles en su único partido
        assertEquals(servicio.obtenerVallaMenosVencida(escenario.torneo), valla);
    }

    @Test
    void elXGDeUnClubEsLaSumaDeLosXGDeSusRemates() {
        double xg = controlador.calcularXGClub(partido, boca);

        assertEquals(0.40 + 0.76, xg, 1e-9); // área chica + penal
        assertEquals(0.0, controlador.calcularXGClub(partido, river), 1e-9); // River no tuvo remates
    }

    @Test
    void laProbabilidadDeClasificacionCoincideConLaDelServicio() {
        ProyeccionPorPromedio metodo = new ProyeccionPorPromedio();

        double probabilidad = controlador.calcularProbabilidadClasificacion(escenario.torneo, river, metodo);

        assertEquals(servicio.calcularProbabilidadClasificacion(escenario.torneo, river, metodo), probabilidad, 1e-9);
        assertTrue(probabilidad >= 0 && probabilidad <= 100);
    }
}
