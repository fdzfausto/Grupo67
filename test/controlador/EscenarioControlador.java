package controlador;

import excepcion.ReglaNegocioException;
import modelo.Arbitro;
import modelo.Club;
import modelo.Estadio;
import modelo.Jugador;
import modelo.Nomina;
import modelo.Partido;
import modelo.TernaArbitral;
import modelo.TipoTorneo;
import modelo.Torneo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Datos de apoyo para probar los controladores: un torneo con 4 clubes inscriptos y su fixture.
// Usa solo los controladores para armar el torneo, igual que el menú.
class EscenarioControlador {

    final ControladorTorneo controladorTorneo = new ControladorTorneo();
    final Torneo torneo;
    final List<Club> clubes = new ArrayList<>();
    final List<List<Jugador>> planteles = new ArrayList<>();
    private int numeroPartido = 0; // cada partido se programa una semana después del anterior

    EscenarioControlador() throws ReglaNegocioException {
        torneo = new Torneo("Liga 2026", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 12, 15),
                TipoTorneo.NACIONAL, 2, 30);
        String[] nombres = {"River", "Boca", "Racing", "Independiente"};
        for (int c = 0; c < nombres.length; c++) {
            Club club = new Club(nombres[c], LocalDate.of(1901 + c, 5, 25), "Sede de " + nombres[c]);
            List<Jugador> plantel = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                String dni = String.valueOf((c + 1) * 100 + i);
                plantel.add(new Jugador(dni, "Jugador" + dni, "Apellido", LocalDate.of(1998, 1, 1),
                        "Delantero", i + 1, 1000, 0));
            }
            clubes.add(club);
            planteles.add(plantel);
            controladorTorneo.inscribirClub(torneo, club, plantel);
        }
    }

    // Genera el fixture y pone el torneo en curso.
    void iniciarTorneo() throws ReglaNegocioException {
        controladorTorneo.generarFixture(torneo);
        controladorTorneo.avanzarEstado(torneo);
    }

    Jugador jugador(Club club, int indice) {
        return planteles.get(clubes.indexOf(club)).get(indice);
    }

    Partido partidoEntre(Club a, Club b) {
        for (Partido p : torneo.getPartidos()) {
            if (p.participa(a) && p.participa(b)) {
                return p;
            }
        }
        throw new IllegalStateException("No hay partido entre " + a + " y " + b);
    }

    TernaArbitral terna(int numero) throws ReglaNegocioException {
        String n = String.valueOf(numero);
        return new TernaArbitral(
                new Arbitro("9" + n + "1", "Arbitro", "Uno", LocalDate.of(1980, 1, 1), "nacional", "Colegio"),
                new Arbitro("9" + n + "2", "Arbitro", "Dos", LocalDate.of(1980, 1, 1), "nacional", "Colegio"),
                new Arbitro("9" + n + "3", "Arbitro", "Tres", LocalDate.of(1980, 1, 1), "nacional", "Colegio"));
    }

    // Programa el partido y carga las dos nóminas (11 titulares y 5 suplentes). Todavía no lo inicia.
    Partido programarConNominas(Partido partido) throws ReglaNegocioException {
        numeroPartido++;
        LocalDateTime fecha = LocalDateTime.of(2026, 4, 1, 18, 0).plusDays(7L * numeroPartido);
        controladorTorneo.programarPartido(torneo, partido, fecha,
                new Estadio("Estadio" + numeroPartido, 40000, "Calle 1", "Templado"), terna(numeroPartido));
        cargarNomina(partido, partido.getLocal());
        cargarNomina(partido, partido.getVisitante());
        return partido;
    }

    private void cargarNomina(Partido partido, Club club) throws ReglaNegocioException {
        List<Jugador> plantel = planteles.get(clubes.indexOf(club));
        Nomina nomina = new Nomina(club);
        for (int i = 0; i < 11; i++) {
            nomina.agregarTitular(plantel.get(i));
        }
        for (int i = 11; i < 16; i++) {
            nomina.agregarSuplente(plantel.get(i));
        }
        controladorTorneo.cargarNomina(torneo, partido, nomina);
    }
}
