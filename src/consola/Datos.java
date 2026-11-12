package consola;

import excepcion.ReglaNegocioException;
import modelo.Arbitro;
import modelo.Club;
import modelo.Entrenador;
import modelo.EstadoTorneo;
import modelo.Estadio;
import modelo.Inscripcion;
import modelo.Jugador;
import modelo.Partido;
import modelo.Sponsor;
import modelo.TernaArbitral;
import modelo.Torneo;
import modelo.TipoTorneo;
import notificaciones.AppMovil;
import notificaciones.CanalNotificacion;
import notificaciones.CorreoPrensa;
import notificaciones.Sms;
import notificaciones.Suscriptor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Todo lo que el sistema tiene cargado en memoria. Es Serializable para poder guardarlo en un archivo.
public class Datos implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Torneo> torneos = new ArrayList<>();
    private List<Club> clubes = new ArrayList<>();
    private List<Jugador> jugadores = new ArrayList<>();
    private List<Entrenador> entrenadores = new ArrayList<>();
    private List<Arbitro> arbitros = new ArrayList<>();
    private List<Estadio> estadios = new ArrayList<>();
    private List<Suscriptor> suscriptores = new ArrayList<>();
    private List<Sponsor> sponsors = new ArrayList<>();

    public List<Torneo> getTorneos() {
        return torneos;
    }

    public List<Club> getClubes() {
        return clubes;
    }

    public List<Jugador> getJugadores() {
        return jugadores;
    }

    public List<Entrenador> getEntrenadores() {
        return entrenadores;
    }

    public List<Arbitro> getArbitros() {
        return arbitros;
    }

    public List<Estadio> getEstadios() {
        return estadios;
    }

    public List<Suscriptor> getSuscriptores() {
        return suscriptores;
    }

    public List<Sponsor> getSponsors() {
        return sponsors;
    }

    // Jugadores con contrato vigente en el club: son los que se pueden inscribir en un torneo.
    public List<Jugador> jugadoresDe(Club club) {
        List<Jugador> plantel = new ArrayList<>();
        for (Jugador jugador : jugadores) {
            if (club.equals(jugador.getClubActual())) {
                plantel.add(jugador);
            }
        }
        return plantel;
    }

    public boolean existeDni(String dni) {
        for (Jugador j : jugadores) {
            if (j.getDni().equals(dni)) {
                return true;
            }
        }
        for (Entrenador e : entrenadores) {
            if (e.getDni().equals(dni)) {
                return true;
            }
        }
        for (Arbitro a : arbitros) {
            if (a.getDni().equals(dni)) {
                return true;
            }
        }
        return false;
    }

    // CU01 (baja): solo se da de baja lo que no esta en uso

    public void bajaClub(Club club) throws ReglaNegocioException {
        for (int i = 0; i < torneos.size(); i++) {
            if (torneos.get(i).estaInscripto(club)) {
                throw new ReglaNegocioException(club + " esta inscripto en un torneo");
            }
        }
        if (!jugadoresDe(club).isEmpty()) {
            throw new ReglaNegocioException(club + " tiene jugadores con contrato vigente");
        }
        clubes.remove(club);
    }

    public void bajaJugador(Jugador jugador) throws ReglaNegocioException {
        if (jugador.getContratoVigente() != null) {
            throw new ReglaNegocioException(jugador + " tiene un contrato vigente");
        }
        for (int i = 0; i < torneos.size(); i++) {
            List<Inscripcion> inscripciones = torneos.get(i).getInscripciones();
            for (int j = 0; j < inscripciones.size(); j++) {
                if (inscripciones.get(j).contiene(jugador)) {
                    throw new ReglaNegocioException(jugador + " esta inscripto en el torneo " + torneos.get(i).getNombre());
                }
            }
        }
        jugadores.remove(jugador);
    }

    public void bajaEntrenador(Entrenador entrenador) throws ReglaNegocioException {
        for (int i = 0; i < clubes.size(); i++) {
            if (clubes.get(i).getEntrenador() == entrenador) {
                throw new ReglaNegocioException(entrenador + " dirige a " + clubes.get(i));
            }
        }
        entrenadores.remove(entrenador);
    }

    public void bajaArbitro(Arbitro arbitro) throws ReglaNegocioException {
        List<Partido> partidos = todosLosPartidos();
        for (int i = 0; i < partidos.size(); i++) {
            TernaArbitral terna = partidos.get(i).getTerna();
            if (terna != null && terna.getArbitros().contains(arbitro)) {
                throw new ReglaNegocioException(arbitro + " esta asignado a un partido");
            }
        }
        arbitros.remove(arbitro);
    }

    public void bajaEstadio(Estadio estadio) throws ReglaNegocioException {
        List<Partido> partidos = todosLosPartidos();
        for (int i = 0; i < partidos.size(); i++) {
            if (partidos.get(i).getEstadio() == estadio) {
                throw new ReglaNegocioException(estadio + " esta asignado a un partido");
            }
        }
        estadios.remove(estadio);
    }

    // Junta los partidos de todos los torneos (lo usan bajaArbitro y bajaEstadio)
    private List<Partido> todosLosPartidos() {
        List<Partido> partidos = new ArrayList<>();
        for (int i = 0; i < torneos.size(); i++) {
            partidos.addAll(torneos.get(i).getPartidos());
        }
        return partidos;
    }

    public void bajaTorneo(Torneo torneo) throws ReglaNegocioException {
        if (torneo.getEstado() != EstadoTorneo.PLANIFICADO) {
            throw new ReglaNegocioException("Solo se puede dar de baja un torneo PLANIFICADO");
        }
        torneos.remove(torneo);
    }

    // Carga un torneo de ejemplo listo para usar: 4 clubes con 20 jugadores cada uno, fixture generado,
    // estadios, árbitros, un sponsor y un suscriptor. Sirve para probar y demostrar el sistema.
    public void cargarDemostracion() throws ReglaNegocioException {
        String[] nombres = {"River Plate", "Boca Juniors", "Racing Club", "Independiente"};
        Torneo torneo = new Torneo("Liga Profesional 2026", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 12, 15),
                TipoTorneo.NACIONAL, 2, 30);
        for (int c = 0; c < nombres.length; c++) {
            Club club = new Club(nombres[c], LocalDate.of(1901 + c, 5, 25), "Sede social de " + nombres[c]);
            club.asignarEntrenador(crearEntrenador(c));
            clubes.add(club);
            List<Jugador> plantel = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                String dni = String.valueOf(30000000 + c * 100 + i);
                Jugador jugador = new Jugador(dni, "Jugador" + (i + 1), "de " + nombres[c].split(" ")[0],
                        LocalDate.of(1995 + i % 8, 1 + i % 12, 10), posicion(i), i + 1, 500000 + i * 100000, 0);
                jugador.registrarContrato(club, LocalDate.of(2026, 1, 1));
                jugadores.add(jugador);
                plantel.add(jugador);
            }
            torneo.inscribirClub(club, plantel);
        }
        torneo.generarFixture();
        torneos.add(torneo);

        estadios.add(new Estadio("Monumental", 84000, "Av. Pres. Figueroa Alcorta 7597, CABA, Buenos Aires", "Templado"));
        estadios.add(new Estadio("La Bombonera", 54000, "Brandsen 805, CABA, Buenos Aires", "Templado"));
        estadios.add(new Estadio("Cilindro de Avellaneda", 51000, "Av. Mitre 934, Avellaneda, Buenos Aires", "Húmedo"));

        for (int i = 0; i < 9; i++) {
            arbitros.add(new Arbitro(String.valueOf(20000000 + i), "Arbitro" + (i + 1), "Gomez",
                    LocalDate.of(1980 + i, 3, 12), "nacional", "Colegio de Árbitros de Buenos Aires"));
        }

        Sponsor sponsor = new Sponsor("Deportes SA", "Sportline", "sportline.png");
        sponsors.add(sponsor);

        Suscriptor suscriptor = new Suscriptor("Marcos Prensa", "marcos@prensa.com", "1155551234");
        Set<CanalNotificacion> canales = new LinkedHashSet<>();
        canales.add(new AppMovil());
        canales.add(new CorreoPrensa());
        canales.add(new Sms());
        suscriptor.configurarCanales(canales);
        suscriptor.seguir(clubes.get(0));
        suscriptor.seguir(clubes.get(1));
        suscriptores.add(suscriptor);
    }

    private Entrenador crearEntrenador(int numero) {
        Entrenador entrenador = new Entrenador(String.valueOf(10000000 + numero), "Entrenador" + (numero + 1),
                "Perez", LocalDate.of(1970 + numero, 8, 2), "Director técnico", "LIC-" + (100 + numero));
        entrenadores.add(entrenador);
        return entrenador;
    }

    private String posicion(int indice) {
        if (indice == 0 || indice == 11) {
            return "Arquero";
        }
        if (indice < 5 || indice == 12) {
            return "Defensor";
        }
        if (indice < 9 || indice == 13) {
            return "Mediocampista";
        }
        return "Delantero";
    }
}