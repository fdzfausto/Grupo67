package modelo;

import excepcion.ReglaNegocioException;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Torneo implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MIN_CLUBES = 4; // RN02

    private String nombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoTorneo estado;
    private TipoTorneo tipo;             // S02
    private int cuposClasificacion;      // S02
    private int puntajeCorte;            // S02: corte de años anteriores
    private List<Inscripcion> inscripciones; // composición: no existen sin el torneo
    private Fixture fixture;             // null hasta que se genera

    public Torneo(String nombre, LocalDate fechaInicio, LocalDate fechaFin, TipoTorneo tipo,
                  int cuposClasificacion, int puntajeCorte) throws ReglaNegocioException {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException("El torneo necesita un nombre");
        }
        if (fechaInicio == null || fechaFin == null || fechaFin.isBefore(fechaInicio)) {
            throw new ReglaNegocioException("El torneo necesita fechas válidas (el fin no puede ser anterior al inicio)");
        }
        if (tipo == null) {
            throw new ReglaNegocioException("El torneo necesita un tipo (nacional o internacional)");
        }
        if (cuposClasificacion < 1 || puntajeCorte < 0) {
            throw new ReglaNegocioException("Los cupos de clasificación deben ser al menos 1 y el puntaje de corte no puede ser negativo");
        }
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.tipo = tipo;
        this.cuposClasificacion = cuposClasificacion;
        this.puntajeCorte = puntajeCorte;
        this.estado = EstadoTorneo.PLANIFICADO;
        this.inscripciones = new ArrayList<>();
    }

    // Creador: Torneo crea sus inscripciones y verifica las reglas antes (CU03).
    public Inscripcion inscribirClub(Club club, List<Jugador> plantel) throws ReglaNegocioException {
        if (estado != EstadoTorneo.PLANIFICADO) { // RN03
            throw new ReglaNegocioException("Solo se puede inscribir en un torneo PLANIFICADO (estado actual: " + estado + ")");
        }
        if (club != null && estaInscripto(club)) { // RN03
            throw new ReglaNegocioException(club + " ya está inscripto en " + nombre);
        }
        Inscripcion nueva = new Inscripcion(club, plantel); // valida RN05
        for (Jugador jugador : nueva.getPlantel()) { // RN06
            for (Inscripcion existente : inscripciones) {
                if (existente.contiene(jugador)) {
                    throw new ReglaNegocioException(jugador + " ya pertenece a " + existente.getClub() + " en este torneo");
                }
            }
        }
        inscripciones.add(nueva);
        return nueva;
    }

    // RN01 y RN02: solo se avanza al estado siguiente.
    public void avanzarEstado() throws ReglaNegocioException {
        switch (estado) {
            case PLANIFICADO:
                if (inscripciones.size() < MIN_CLUBES) {
                    throw new ReglaNegocioException("El torneo necesita al menos " + MIN_CLUBES
                            + " clubes inscriptos para comenzar (tiene " + inscripciones.size() + ")");
                }
                estado = EstadoTorneo.EN_CURSO;
                break;
            case EN_CURSO:
                estado = EstadoTorneo.FINALIZADO;
                break;
            default:
                throw new ReglaNegocioException("El torneo ya está FINALIZADO y no puede avanzar de estado");
        }
    }

    // CU04: Torneo crea el fixture con los clubes inscriptos.
    public Fixture generarFixture() throws ReglaNegocioException {
        if (estado != EstadoTorneo.PLANIFICADO) {
            throw new ReglaNegocioException("El fixture se genera con el torneo PLANIFICADO (estado actual: " + estado + ")");
        }
        if (fixture != null) {
            throw new ReglaNegocioException("El fixture de " + nombre + " ya fue generado");
        }
        if (inscripciones.size() < MIN_CLUBES) {
            throw new ReglaNegocioException("Se necesitan al menos " + MIN_CLUBES + " clubes inscriptos para generar el fixture");
        }
        List<Club> clubes = new ArrayList<>();
        for (Inscripcion inscripcion : inscripciones) {
            clubes.add(inscripcion.getClub());
        }
        fixture = new Fixture(clubes);
        return fixture;
    }

    public boolean estaInscripto(Club club) {
        for (Inscripcion inscripcion : inscripciones) {
            if (inscripcion.getClub().equals(club)) {
                return true;
            }
        }
        return false;
    }

    public List<Partido> getPartidos() {
        if (fixture == null) {
            return new ArrayList<>();
        }
        return fixture.getPartidos();
    }

    public List<Inscripcion> getInscripciones() {
        return new ArrayList<>(inscripciones);
    }

    public Fixture getFixture() {
        return fixture;
    }

    public String getNombre() {
        return nombre;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public EstadoTorneo getEstado() {
        return estado;
    }

    public TipoTorneo getTipo() {
        return tipo;
    }

    public int getCuposClasificacion() {
        return cuposClasificacion;
    }

    public int getPuntajeCorte() {
        return puntajeCorte;
    }

    @Override
    public String toString() {
        return nombre + " (" + estado + ")";
    }
}
