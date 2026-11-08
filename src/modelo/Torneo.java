package modelo;

import excepcion.ReglaNegocioException;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class Torneo implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MIN_CLUBES = 4;

    private String nombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoTorneo estado; // asociacion con ENUM
    private int cantidadClasificados; // S02: cuántos clubes clasifican (lo usa la proyección por xG, S08)
    private int puntajeCorte; // S02: corte de años anteriores (lo usa la proyección por promedio, S07)
    private List<Inscripcion> inscripciones; // composicion
    private List<Partido> partidos; // composicion

    // El torneo siempre arranca PLANIFICADO (RN01: es el primer estado)
    public Torneo(String nombre, LocalDate fechaInicio, LocalDate fechaFin, int cantidadClasificados, int puntajeCorte) throws ReglaNegocioException {
        if (fechaFin.isBefore(fechaInicio)) {
            throw new ReglaNegocioException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = EstadoTorneo.PLANIFICADO;
        this.cantidadClasificados = cantidadClasificados;
        this.puntajeCorte = puntajeCorte;
        this.inscripciones = new ArrayList<>();
        this.partidos = new ArrayList<>();
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

    public int getCantidadClasificados() {
        return cantidadClasificados;
    }

    public int getPuntajeCorte() {
        return puntajeCorte;
    }

    public List<Club> getClubes() {
        List<Club> clubes = new ArrayList<>();
        for (int i = 0; i < inscripciones.size(); i++) {
            clubes.add(inscripciones.get(i).getClub());
        }
        return clubes;
    }

    public Inscripcion inscribirClub(Club club, List<Jugador> plantel) throws ReglaNegocioException {
        if (estado != EstadoTorneo.PLANIFICADO){ // RN03
            throw new ReglaNegocioException("Solo se puede inscribir con el torneo PLANIFICADO (esta " + estado + ")");
        }
        if (getInscripcion(club) != null){ // RN03
            throw new ReglaNegocioException(club + " ya esta inscripto en " + nombre);
        }
        if (plantel.size() < Inscripcion.MIN_JUGADORES || plantel.size() > Inscripcion.MAX_JUGADORES) { // RN05
            throw new ReglaNegocioException("El plantel debe tener entre " + Inscripcion.MIN_JUGADORES + " y " + Inscripcion.MAX_JUGADORES + " jugadores (Tiene " +  plantel.size() + " jugadores)");
        }
        for (int i = 0; i < plantel.size(); i++) { // RN06
            Inscripcion otra = getInscripcionDeJugador(plantel.get(i));
            if (otra != null) {
                throw new ReglaNegocioException(plantel.get(i) + " ya esta inscripto en " + otra.getClub());
            }
        }
        Inscripcion inscripcion =  new Inscripcion(club);
        for (int i = 0; i < plantel.size(); i++) {
            inscripcion.agregarJugador(plantel.get(i));
        }
        inscripciones.add(inscripcion);
        return inscripcion;
    }

    public void iniciar() throws ReglaNegocioException {
        if (estado != EstadoTorneo.PLANIFICADO){
            throw new ReglaNegocioException("Solo se puede iniciar un torneo PLANIFICADO (esta " + estado + ")");
        }
        if (inscripciones.size() < MIN_CLUBES) {
            throw new ReglaNegocioException("El torneo necesita al menos " +  MIN_CLUBES + " clubes inscriptos (Tiene " +  inscripciones.size() + " clubes)");
        }
        estado = EstadoTorneo.EN_CURSO;
    }

    public void finalizar() throws ReglaNegocioException {
        if (estado != EstadoTorneo.EN_CURSO) {
            throw new ReglaNegocioException("Solo se puede finalizar un torneo EN_CURSO (esta " + estado + ")");
        }
        estado = EstadoTorneo.FINALIZADO;
    }

    public List<Partido> generarFixture() throws ReglaNegocioException {
        if (inscripciones.size() < MIN_CLUBES){
            throw new ReglaNegocioException("Para generar el fixture se necesitan al menos " + MIN_CLUBES + " clubes (Por ahora hay " + inscripciones.size() + " clubes)");
        }
        if (!partidos.isEmpty()){
            throw new ReglaNegocioException("El fixture de " + nombre + " ya fue genereado");
        }
        List<Club> clubes = getClubes();
        for (int i = 0; i < clubes.size(); i++) {
            for (int j = i + 1; j < clubes.size(); j++) {
                if ((i + j) % 2 == 0){
                    partidos.add(new Partido(clubes.get(i), clubes.get(j)));
                } else {
                    partidos.add(new Partido(clubes.get(j), clubes.get(i)));
                }
            }
        }
        return getPartidos();
    }

    public static final int HORAS_ENTRE_PARTIDOS_ESTADIO = 3; //RN08
    public static final int HORAS_ENTRE_PARTIDOS_CLUB = 48;

    public void programarPartido(Partido partido, LocalDateTime fechaHora, Estadio estadio, TernaArbitral terna) throws ReglaNegocioException {
        if (!partidos.contains(partido)) {
            throw new ReglaNegocioException("El partido no pertenece al torneo " + partido);
        }
        // RN04
        LocalDate dia = fechaHora.toLocalDate();
        if (dia.isBefore(fechaInicio) || dia.isAfter(fechaFin)) {
            throw new ReglaNegocioException("La fecha " + dia + " se encuentra fuera del periodo del torneo");
        }
        verificarDisponibilidad(partido, fechaHora, estadio, terna);
        partido.programar(fechaHora, estadio, terna);
    }

    private void verificarDisponibilidad(Partido partido, LocalDateTime fechaHora, Estadio estadio, TernaArbitral terna) throws ReglaNegocioException {
        List<Arbitro> arbitros = terna.getArbitros();
        for (int i = 0; i < arbitros.size(); i++) {
            Arbitro arbitro = arbitros.get(i);
            if (arbitro.esHinchaDe(partido.getLocal()) || arbitro.esHinchaDe(partido.getVisitante())) {
                throw new ReglaNegocioException(arbitro + " no puede dirigir porque es hincha de uno de los clubes");
            }
        }
        for (int i = 0; i < partidos.size(); i++) {
            Partido otro = partidos.get(i);
            if (otro == partido || otro.getFechaHora() == null) {
                continue;
            }
            if (otro.getEstadio().equals(estadio) && estaCerca(otro.getFechaHora(),fechaHora,HORAS_ENTRE_PARTIDOS_ESTADIO)) { //RN08
                throw new ReglaNegocioException(estadio.getNombre() + " ya tiene un partido a menos de " + HORAS_ENTRE_PARTIDOS_ESTADIO + " horas: " + otro);
            }
            if ((otro.participa(partido.getLocal()) || otro.participa(partido.getVisitante())) && estaCerca(otro.getFechaHora(), fechaHora, HORAS_ENTRE_PARTIDOS_CLUB)) { //RN09
                throw new ReglaNegocioException("Uno de los clubes ya juega a menos de " + HORAS_ENTRE_PARTIDOS_CLUB + " horas: " + otro);
            }
            if (otro.getFechaHora().toLocalDate().equals(fechaHora.toLocalDate())) {
                for (int j = 0; j < arbitros.size(); j++){
                    if (otro.getTerna().getArbitros().contains(arbitros.get(j))) {
                        throw new ReglaNegocioException(arbitros.get(j) + " ya dirige otro partido ese dia: " + otro);
                    }
                }
            }
        }
    }

    private boolean estaCerca(LocalDateTime otra, LocalDateTime fecha, int horas) {
        LocalDateTime desde = fecha.minusHours(horas);
        LocalDateTime hasta = fecha.plusHours(horas);
        return otra.isAfter(desde) && otra.isBefore(hasta);
    }

    //BUSQUEDAS
    public Inscripcion getInscripcion(Club club) {
        for (int i = 0; i < inscripciones.size(); i++) {
            if (inscripciones.get(i).getClub().equals(club)) {
                return inscripciones.get(i);
            }
        }
        return null;
    }

    public Inscripcion getInscripcionDeJugador(Jugador jugador) {
        for (int i = 0; i < inscripciones.size(); i++) {
            if (inscripciones.get(i).contieneJugador(jugador)) {
                return inscripciones.get(i);
            }
        }
        return null;
    }

    public List<Inscripcion> getInscripciones() {
        return new ArrayList<>(inscripciones);
    }

    public List<Partido> getPartidos() {
        return new ArrayList<>(partidos);
    }

    @Override
    public String toString() {
        return nombre + " (" + estado + ", " + inscripciones.size() + " clubes)";
    }
}