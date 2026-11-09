package modelo;

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
        if (evento.getEventoAnulado() != null) { // es una anulación
            validarAnulacion(evento.getEventoAnulado());
        } else {
            validarEventoDeJuego(evento);
        }
        eventos.add(evento);
        expulsarPorDobleAmarilla(evento);
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
        if (evento.getJugadorEntrante() != null) { // es un cambio
            validarCambio(evento, nomina);
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
    private void validarCambio(Evento cambio, Nomina nomina) throws ReglaNegocioException {
        Jugador sale = cambio.getJugador();
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
    private void validarAnulacion(Evento anulado) throws ReglaNegocioException {
        if (!eventos.contains(anulado)) {
            throw new ReglaNegocioException("El evento a anular no pertenece a este partido");
        }
        if (estaAnulado(anulado)) {
            throw new ReglaNegocioException("El evento ya fue anulado");
        }
    }

    // RN15 y S05: la segunda amarilla de un jugador equivale a una roja, que el partido registra en el mismo minuto.
    private void expulsarPorDobleAmarilla(Evento evento) throws ReglaNegocioException {
        Jugador jugador = evento.getJugador();
        if (evento.amonesta(jugador) && contarAmarillas(jugador) == 2) {
            eventos.add(new Tarjeta(evento.getMinuto(), jugador, evento.getEquipo(), ColorTarjeta.ROJA));
        }
    }

    // Un evento está anulado si hay una anulación que lo referencia (RN17).
    public boolean estaAnulado(Evento evento) {
        for (Evento e : eventos) {
            if (e.anula(evento)) {
                return true;
            }
        }
        return false;
    }

    // Experto: Partido tiene los eventos, así que sabe quién fue expulsado (roja vigente, directa o por doble amarilla).
    public boolean estaExpulsado(Jugador jugador) {
        for (Evento e : eventos) {
            if (e.expulsa(jugador) && !estaAnulado(e)) {
                return true;
            }
        }
        return false;
    }

    private boolean fueSustituido(Jugador jugador) {
        for (Evento e : eventos) {
            if (e.sustituye(jugador) && !estaAnulado(e)) {
                return true;
            }
        }
        return false;
    }

    private boolean haEntrado(Jugador jugador) {
        for (Evento e : eventos) {
            if (e.haceEntrar(jugador) && !estaAnulado(e)) {
                return true;
            }
        }
        return false;
    }

    private int contarCambios(Club club) {
        int cambios = 0;
        for (Evento e : eventos) {
            boolean esCambio = e.getJugadorEntrante() != null;
            if (esCambio && e.getEquipo().equals(club) && !estaAnulado(e)) {
                cambios++;
            }
        }
        return cambios;
    }

    private int contarAmarillas(Jugador jugador) {
        int amarillas = 0;
        for (Evento e : eventos) {
            if (e.amonesta(jugador) && !estaAnulado(e)) {
                amarillas++;
            }
        }
        return amarillas;
    }

    // CU07: al cerrar, el partido suma un partido jugado a titulares y a suplentes que entraron.
    // El marcador no se guarda: se calcula desde los eventos (getGoles).
    public void anularEvento(Evento evento, String motivo) throws ReglaNegocioException {
        if (!eventos.contains(evento)) {
            throw new ReglaNegocioException("El evento no pertenece a este partido");
        }
        for (int i = 0; i < eventos.size(); i++) {
            if (eventos.get(i) instanceof Anulacion) {
                Anulacion anulacion = (Anulacion) eventos.get(i);
                if (anulacion.getEventoAnulado() == evento) {
                    throw new ReglaNegocioException("Ese evento ya fue anulado");
                }
            }
        }
        registrarEvento(new Anulacion(evento, motivo));
    }

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
