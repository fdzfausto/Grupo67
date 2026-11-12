package reportes;

import eventos.Gol;
import eventos.Remate;
import eventos.TipoGol;
import eventos.ZonaRemate;
import excepcion.ReglaNegocioException;
import modelo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServicioReportesTest {
    private Torneo torneo;
    private Club river;
    private Club boca;
    private Club racing;
    private Club independiente;
    private List<Club> clubes;
    private List<List<Jugador>> planteles;
    private ServicioReportes servicio;
    private int numeroPartido; // cada partido se juega una semana despues del anterior

    @BeforeEach
    void preparar() throws ReglaNegocioException {
        torneo = new Torneo("Liga 2026", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 12, 15),
                TipoTorneo.NACIONAL, 2, 30);
        river = new Club("River", LocalDate.of(1901, 5, 25), "Nunez");
        boca = new Club("Boca", LocalDate.of(1905, 4, 3), "La Boca");
        racing = new Club("Racing", LocalDate.of(1903, 3, 25), "Avellaneda");
        independiente = new Club("Independiente", LocalDate.of(1905, 1, 1), "Avellaneda");
        clubes = new ArrayList<>();
        clubes.add(river);
        clubes.add(boca);
        clubes.add(racing);
        clubes.add(independiente);
        planteles = new ArrayList<>();
        for (int c = 0; c < clubes.size(); c++) {
            List<Jugador> plantel = new ArrayList<>();
            for (int i = 0; i < 18; i++) {
                String dni = String.valueOf((c + 1) * 100 + i); // DNI distintos en cada club
                plantel.add(new Jugador(dni, "Jugador" + dni, "Apellido", LocalDate.of(1998, 1, 1), "Delantero", i + 1, 1000, 0));
            }
            planteles.add(plantel);
            torneo.inscribirClub(clubes.get(c), plantel);
        }
        torneo.generarFixture();
        torneo.avanzarEstado(); // EN_CURSO
        servicio = new ServicioReportes();
        numeroPartido = 0;
    }

    // Ayuda: busca el partido entre a y b, lo programa, carga las nominas y lo deja EN_JUEGO
    private Partido empezarPartido(Club a, Club b) throws ReglaNegocioException {
        Partido partido = null;
        for (int i = 0; i < torneo.getPartidos().size(); i++) {
            if (torneo.getPartidos().get(i).participa(a) && torneo.getPartidos().get(i).participa(b)) {
                partido = torneo.getPartidos().get(i);
            }
        }
        numeroPartido++;
        String n = String.valueOf(numeroPartido);
        TernaArbitral terna = new TernaArbitral(
                new Arbitro("9" + n + "1", "Arbitro", "Uno", LocalDate.of(1980, 1, 1), "nacional", "Colegio"),
                new Arbitro("9" + n + "2", "Arbitro", "Dos", LocalDate.of(1980, 1, 1), "nacional", "Colegio"),
                new Arbitro("9" + n + "3", "Arbitro", "Tres", LocalDate.of(1980, 1, 1), "nacional", "Colegio"));
        LocalDateTime fecha = LocalDateTime.of(2026, 4, 1, 18, 0).plusDays(7 * numeroPartido);
        torneo.programarPartido(partido, fecha, new Estadio("Estadio" + n, 40000, "Calle 1", "Templado"), terna);
        cargarNomina(partido, partido.getLocal());
        cargarNomina(partido, partido.getVisitante());
        partido.iniciar();
        return partido;
    }

    // Ayuda: 11 titulares y 5 suplentes del plantel del club
    private void cargarNomina(Partido partido, Club club) throws ReglaNegocioException {
        List<Jugador> plantel = planteles.get(clubes.indexOf(club));
        Nomina nomina = new Nomina(club);
        for (int i = 0; i < 11; i++) {
            nomina.agregarTitular(plantel.get(i));
        }
        for (int i = 11; i < 16; i++) {
            nomina.agregarSuplente(plantel.get(i));
        }
        torneo.cargarNomina(partido, nomina);
    }

    // Ayuda: juega el partido con ese resultado. El que hace los goles es el jugador numero 9 de cada club.
    private void jugar(Club a, int golesA, Club b, int golesB) throws ReglaNegocioException {
        Partido partido = empezarPartido(a, b);
        for (int i = 0; i < golesA; i++) {
            partido.registrarEvento(new Gol(10 + i, goleador(a), a, TipoGol.A_FAVOR));
        }
        for (int i = 0; i < golesB; i++) {
            partido.registrarEvento(new Gol(50 + i, goleador(b), b, TipoGol.A_FAVOR));
        }
        partido.finalizar();
    }

    private Jugador goleador(Club club) {
        return planteles.get(clubes.indexOf(club)).get(8);
    }

    // ---------- Pruebas ----------

    @Test
    void rn24LaVictoriaDaTresPuntosYElGanadorQuedaPrimero() throws ReglaNegocioException {
        jugar(river, 2, boca, 0);

        List<FilaTabla> tabla = servicio.calcularTabla(torneo);

        assertEquals(river, tabla.get(0).getClub());
        assertEquals(3, tabla.get(0).getPuntos());
        assertEquals(2, tabla.get(0).getDiferenciaGol());
        assertEquals(boca, tabla.get(3).getClub()); // perdio: queda ultimo con 0 puntos
    }

    @Test
    void rn25ConIgualesPuntosVaPrimeroElDeMejorDiferenciaDeGol() throws ReglaNegocioException {
        jugar(river, 1, boca, 0);           // River: 3 puntos, diferencia +1
        jugar(racing, 4, independiente, 0); // Racing: 3 puntos, diferencia +4

        List<FilaTabla> tabla = servicio.calcularTabla(torneo);

        assertEquals(racing, tabla.get(0).getClub());
        assertEquals(river, tabla.get(1).getClub());
    }

    @Test
    void rn26ElPenalCuentaComoGolPeroElGolEnContraYElAnuladoNo() throws ReglaNegocioException {
        Partido partido = empezarPartido(river, boca);
        partido.registrarEvento(new Gol(10, goleador(river), river, TipoGol.PENAL));
        partido.registrarEvento(new Gol(20, goleador(boca), boca, TipoGol.EN_CONTRA));
        Gol golAnulado = new Gol(30, goleador(river), river, TipoGol.A_FAVOR);
        partido.registrarEvento(golAnulado);
        partido.anularEvento(golAnulado, "Offside");
        partido.finalizar();

        List<Goleador> goleadores = servicio.obtenerGoleadores(torneo);

        assertEquals(1, goleadores.size());
        assertEquals(goleador(river), goleadores.get(0).getJugador());
        assertEquals(1, goleadores.get(0).getGoles());
    }

    @Test
    void rn26LaVallaMenosVencidaEsLaQueRecibeMenosGolesPorPartido() throws ReglaNegocioException {
        jugar(river, 0, boca, 0);           // Boca: 0 goles recibidos
        jugar(racing, 2, independiente, 1); // Racing: 1, Independiente: 2

        assertEquals(boca, servicio.obtenerVallaMenosVencida(torneo));
    }

    @Test
    void s07LaProyeccionPorPromedioComparaLosPuntosProyectadosConElCorte() throws ReglaNegocioException {
        jugar(river, 2, boca, 0); //

        double probabilidad = servicio.calcularProbabilidadClasificacion(torneo, river, new ProyeccionPorPromedio());

        assertEquals(30.0, probabilidad, 0.001);
    }

    @Test
    void rn27ElXGDelClubEsLaSumaDeSusRematesYLaSimulacionDaEntre0Y100() throws ReglaNegocioException {
        Partido partido = empezarPartido(river, boca);
        partido.registrarEvento(new Remate(10, goleador(river), river, ZonaRemate.PENAL));
        partido.registrarEvento(new Remate(20, goleador(river), river, ZonaRemate.AREA_CHICA));
        partido.finalizar();

        assertEquals(1.16, partido.calcularXGClub(river), 0.001);
        double probabilidad = servicio.calcularProbabilidadClasificacion(torneo, boca, new ProyeccionPorXG());
        assertTrue(probabilidad >= 0 && probabilidad <= 100);
    }
}
