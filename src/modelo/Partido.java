package modelo;

import eventos.Anulacion;
import eventos.Cambio;
import eventos.ColorTarjeta;
import eventos.Evento;
import eventos.Tarjeta;
import excepcion.ReglaNegocioException;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Partido implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MAX_CAMBIOS = 5; // RN16

    private Club local;
    private Club visitante;
    private LocalDateTime fechaHora;
    private Estadio estadio;
    private TernaArbitral terna;
    private EstadoPartido estado;
    private List<Evento> eventos; // composición: los eventos pertenecen al partido
    private List<Nomina> nominas; // composición: una nómina por club (0..2)

    // El fixture crea los partidos sin fecha, estadio ni terna (supuesto S03).
    public Partido(Club local, Club visitante) throws ReglaNegocioException {
        if (local == null || visitante == null) {
            throw new ReglaNegocioException("El partido necesita un club local y uno visitante");
        }
        if (local.equals(visitante)) {
            throw new ReglaNegocioException("Un partido debe enfrentar a dos clubes distintos");
        }
        this.local = local;
        this.visitante = visitante;
        this.estado = EstadoPartido.SIN_PROGRAMAR;
        this.eventos = new ArrayList<>();
        this.nominas = new ArrayList<>();
    }

    // Las validaciones que dependen del torneo (RN04, CU14) las hace Torneo antes de llamar a este método.
    public void programar(LocalDateTime fechaHora, Estadio estadio, TernaArbitral terna) throws ReglaNegocioException {
        verificarEstado(EstadoPartido.SIN_PROGRAMAR, "programar el partido");
        if (fechaHora == null || estadio == null || terna == null) {
            throw new ReglaNegocioException("Para programar un partido se necesitan fecha y hora, estadio y terna arbitral");
        }
        this.fechaHora = fechaHora;
        this.estadio = estadio;
        this.terna = terna;
        this.estado = EstadoPartido.PROGRAMADO;
    }

    // CU06: la nómina se carga con el partido PROGRAMADO y con jugadores del plantel inscripto (S04).
    // Si el club ya había cargado una nómina, se reemplaza (permite corregirla antes de iniciar).
    public void cargarNomina(Nomina nomina, Inscripcion inscripcion) throws ReglaNegocioException {
        verificarEstado(EstadoPartido.PROGRAMADO, "cargar la nómina");
        if (nomina == null || inscripcion == null) {
            throw new ReglaNegocioException("Se necesitan la nómina y la inscripción del club");
        }
        Club club = nomina.getClub();
        if (!participa(club)) {
            throw new ReglaNegocioException(club + " no juega este partido");
        }
        if (!inscripcion.getClub().equals(club)) {
            throw new ReglaNegocioException("La inscripción no corresponde a " + club);
        }
        if (!nomina.validarNomina()) {
            throw new ReglaNegocioException("La nómina debe tener " + Nomina.CANTIDAD_TITULARES
                    + " titulares y hasta " + Nomina.MAX_SUPLENTES + " suplentes");
        }
        for (Jugador jugador : nomina.getJugadores()) {
            if (!inscripcion.contiene(jugador)) {
                throw new ReglaNegocioException(jugador + " no está en el plantel inscripto de " + club);
            }
        }
        Nomina anterior = getNomina(club);
        if (anterior != null) {
            nominas.remove(anterior);
        }
        nominas.add(nomina);
    }

    public Nomina getNomina(Club club) {
        for (Nomina nomina : nominas) {
            if (nomina.getClub().equals(club)) {
                return nomina;
            }
        }
        return null;
    }

    // CU07: precondición, el partido está programado y con las dos nóminas cargadas.
    public void iniciar() throws ReglaNegocioException {
        verificarEstado(EstadoPartido.PROGRAMADO, "iniciar el partido");
        if (getNomina(local) == null || getNomina(visitante) == null) {
            throw new ReglaNegocioException("Para iniciar el partido deben estar cargadas las nóminas de ambos clubes");
        }
        this.estado = EstadoPartido.EN_JUEGO;
    }

    // CU07/CU08: valida el evento (RN12 a RN17) y recién después lo agrega. Si no es válido lanza la excepción
    // y el partido queda como estaba (rechazarEvento del diagrama de secuencia).
    public void registrarEvento(Evento evento) throws ReglaNegocioException {
        if (evento == null) {
            throw new ReglaNegocioException("El evento no puede ser nulo");
        }
        verificarEstado(EstadoPartido.EN_JUEGO, "registrar un evento"); // RN12
        if (evento instanceof Anulacion) {
            validarAnulacion((Anulacion) evento);
        } else {
            validarEventoDeJuego(evento);
        }
        eventos.add(evento);
        if (evento instanceof Tarjeta) {
            expulsarPorDobleAmarilla((Tarjeta) evento);
        }
    }

    // RN12: el club juega el partido y el jugador está en su nómina. RN15 y RN16: no actúa quien ya salió.
    private void validarEventoDeJuego(Evento evento) throws ReglaNegocioException {
        Club club = evento.getEquipo();
        if (!participa(club)) {
            throw new ReglaNegocioException(club + " no juega este partido");
        }
        Nomina nomina = getNomina(club);
        if (!nomina.contiene(evento.getJugador())) {
            throw new ReglaNegocioException(evento.getJugador() + " no está en la nómina de " + club);
        }
        if (evento instanceof Cambio) {
            validarCambio((Cambio) evento, nomina);
        } else {
            validarJugadorEnJuego(evento.getJugador());
        }
    }

    private void validarJugadorEnJuego(Jugador jugador) throws ReglaNegocioException {
        if (estaExpulsado(jugador)) {
            throw new ReglaNegocioException(jugador + " está expulsado y no puede participar de más eventos");
        }
        if (fueSustituido(jugador)) {
            throw new ReglaNegocioException(jugador + " fue sustituido y no puede volver a jugar este partido");
        }
    }

    // RN15 y RN16: sale alguien que está en cancha y no está expulsado, entra un suplente que no jugó,
    // y el club no pasa de 5 cambios.
    private void validarCambio(Cambio cambio, Nomina nomina) throws ReglaNegocioException {
        Jugador sale = cambio.getJugadorSaliente();
        Jugador entra = cambio.getJugadorEntrante();
        if (!nomina.contiene(entra)) {
            throw new ReglaNegocioException(entra + " no está en la nómina de " + cambio.getEquipo());
        }
        if (estaExpulsado(sale)) {
            throw new ReglaNegocioException(sale + " está expulsado y no puede ser sustituido");
        }
        if (fueSustituido(sale)) {
            throw new ReglaNegocioException(sale + " ya fue sustituido");
        }
        if (!nomina.esTitular(sale) && !haEntrado(sale)) {
            throw new ReglaNegocioException(sale + " no está en la cancha");
        }
        if (nomina.esTitular(entra)) {
            throw new ReglaNegocioException(entra + " es titular: solo puede entrar un suplente");
        }
        if (haEntrado(entra) || fueSustituido(entra) || estaExpulsado(entra)) {
            throw new ReglaNegocioException(entra + " no puede entrar: ya jugó o fue expulsado en este partido");
        }
        if (contarCambios(cambio.getEquipo()) >= MAX_CAMBIOS) {
            throw new ReglaNegocioException(cambio.getEquipo() + " ya hizo los " + MAX_CAMBIOS + " cambios permitidos");
        }
    }

    // RN17 y CU08: se anula un evento de este partido que no haya sido anulado antes.
    private void validarAnulacion(Anulacion anulacion) throws ReglaNegocioException {
        if (!eventos.contains(anulacion.getEventoAnulado())) {
            throw new ReglaNegocioException("El evento a anular no pertenece a este partido");
        }
        if (estaAnulado(anulacion.getEventoAnulado())) {
            throw new ReglaNegocioException("El evento ya fue anulado");
        }
    }

    // RN15 y S05: la segunda amarilla de un jugador equivale a una roja, que el partido registra en el mismo minuto.
    private void expulsarPorDobleAmarilla(Tarjeta tarjeta) throws ReglaNegocioException {
        if (tarjeta.esRoja() || contarAmarillas(tarjeta.getJugador()) < 2) {
            return;
        }
        eventos.add(new Tarjeta(tarjeta.getMinuto(), tarjeta.getJugador(), tarjeta.getEquipo(), ColorTarjeta.ROJA));
    }

    // Un evento está anulado si hay una Anulacion que lo referencia (RN17).
    public boolean estaAnulado(Evento evento) {
        for (Evento e : eventos) {
            if (e instanceof Anulacion && ((Anulacion) e).getEventoAnulado() == evento) {
                return true;
            }
        }
        return false;
    }

    // Experto: Partido tiene los eventos, así que sabe quién fue expulsado (roja vigente, directa o por doble amarilla).
    public boolean estaExpulsado(Jugador jugador) {
        for (Evento e : eventos) {
            if (e instanceof Tarjeta && ((Tarjeta) e).esRoja() && !estaAnulado(e) && mismoJugador(e.getJugador(), jugador)) {
                return true;
            }
        }
        return false;
    }

    private boolean fueSustituido(Jugador jugador) {
        for (Evento e : eventos) {
            if (e instanceof Cambio && !estaAnulado(e) && mismoJugador(((Cambio) e).getJugadorSaliente(), jugador)) {
                return true;
            }
        }
        return false;
    }

    private boolean haEntrado(Jugador jugador) {
        for (Evento e : eventos) {
            if (e instanceof Cambio && !estaAnulado(e) && mismoJugador(((Cambio) e).getJugadorEntrante(), jugador)) {
                return true;
            }
        }
        return false;
    }

    private int contarCambios(Club club) {
        int cambios = 0;
        for (Evento e : eventos) {
            if (e instanceof Cambio && !estaAnulado(e) && e.getEquipo().equals(club)) {
                cambios++;
            }
        }
        return cambios;
    }

    private int contarAmarillas(Jugador jugador) {
        int amarillas = 0;
        for (Evento e : eventos) {
            if (e instanceof Tarjeta && !((Tarjeta) e).esRoja() && !estaAnulado(e) && mismoJugador(e.getJugador(), jugador)) {
                amarillas++;
            }
        }
        return amarillas;
    }

    // Los jugadores se identifican por DNI, igual que en Nomina e Inscripcion.
    private boolean mismoJugador(Jugador a, Jugador b) {
        return a.getDni().equals(b.getDni());
    }

    // CU07: al cerrar, el partido suma un partido jugado a titulares y a suplentes que entraron.
    // El marcador no se guarda: se calcula desde los eventos (getGoles).
    public void finalizar() throws ReglaNegocioException {
        verificarEstado(EstadoPartido.EN_JUEGO, "finalizar el partido");
        this.estado = EstadoPartido.FINALIZADO;
        for (Nomina nomina : nominas) {
            for (Jugador jugador : nomina.getJugadores()) {
                if (nomina.esTitular(jugador) || haEntrado(jugador)) {
                    jugador.sumarPartidoJugado();
                }
            }
        }
    }

    public void suspender() throws ReglaNegocioException {
        if (estado != EstadoPartido.PROGRAMADO && estado != EstadoPartido.EN_JUEGO) {
            throw new ReglaNegocioException("Solo se puede suspender un partido programado o en juego (estado actual: " + estado + ")");
        }
        this.estado = EstadoPartido.SUSPENDIDO;
    }

    // Experto: el marcador se calcula desde los eventos, no se guarda aparte.
    public int getGoles(Club club) {
        int goles = 0;
        for (Evento evento : eventos) {
            goles += evento.golesPara(club);
        }
        return goles;
    }

    public boolean participa(Club club) {
        return local.equals(club) || visitante.equals(club);
    }

    private void verificarEstado(EstadoPartido esperado, String accion) throws ReglaNegocioException {
        if (estado != esperado) {
            throw new ReglaNegocioException("No se puede " + accion + ": está " + estado + " y debe estar " + esperado);
        }
    }

    public Club getLocal() {
        return local;
    }

    public Club getVisitante() {
        return visitante;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public Estadio getEstadio() {
        return estadio;
    }

    public TernaArbitral getTerna() {
        return terna;
    }

    public EstadoPartido getEstado() {
        return estado;
    }

    public List<Evento> getEventos() {
        return new ArrayList<>(eventos);
    }

    @Override
    public String toString() {
        return local + " " + getGoles(local) + " - " + getGoles(visitante) + " " + visitante + " (" + estado + ")";
    }
}
