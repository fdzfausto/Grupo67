package consola;



import controlador.ControladorPartido;
import controlador.ControladorReportes;
import controlador.ControladorTorneo;
import eventos.Cambio;
import eventos.ColorTarjeta;
import eventos.Evento;
import eventos.Falta;
import eventos.Gol;
import eventos.Remate;
import eventos.Tarjeta;
import eventos.TipoGol;
import eventos.ZonaRemate;
import excepcion.PersistenciaException;
import excepcion.ReglaNegocioException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import modelo.Arbitro;
import modelo.Club;
import modelo.Contrato;
import modelo.Entrenador;
import modelo.Estadio;
import modelo.Inscripcion;
import modelo.Jugador;
import modelo.Nomina;
import modelo.Partido;
import modelo.Patrocinio;
import modelo.Sancion;
import modelo.Sponsor;
import modelo.TernaArbitral;
import modelo.TipoTorneo;
import modelo.Torneo;
import notificaciones.AppMovil;
import notificaciones.CanalNotificacion;
import notificaciones.CorreoPrensa;
import notificaciones.Sms;
import notificaciones.Suscriptor;
import persistencia.RepositorioArchivo;
import reportes.Goleador;
import reportes.MetodoProyeccion;
import reportes.ProyeccionPorPromedio;
import reportes.ProyeccionPorXG;

// Menú principal por consola. Solo coordina: pide los datos al usuario y delega las reglas en el modelo.
public class Menu {

    private Consola consola;
    private Datos datos;

    private RepositorioArchivo repositorio;
    private ControladorTorneo controladorTorneo;
    private ControladorPartido controladorPartido;
    private ControladorReportes controladorReportes;

    private static final List<String> CATEGORIAS = Arrays.asList("Club", "Jugador", "Entrenador", "Árbitro", "Estadio", "Torneo");
    private static final String ARCHIVO_DATOS = "futbolstats.dat";

    public Menu() {
        this.consola = new Consola();
        this.datos = new Datos();

        this.repositorio = new RepositorioArchivo();
        this.controladorTorneo = new ControladorTorneo();
        this.controladorPartido = new ControladorPartido();
        this.controladorReportes = new ControladorReportes();
    }

    public void iniciar() {
        cargarDatos();

        try {
            menuPrincipal();
        } catch (IllegalStateException e) {
            consola.mostrar(e.getMessage());
        } finally {
            consola.cerrar();
        }
    }

    private void cargarDatos() {
        try {
            if (repositorio.existe(ARCHIVO_DATOS)) {
                datos = repositorio.cargar(ARCHIVO_DATOS, Datos.class);
                consola.mostrar("Datos recuperados correctamente.");
            } else {
                consola.mostrar("No existe un archivo previo. Se inicia con datos vacíos.");
            }
        } catch (PersistenciaException e) {
            consola.mostrar("No se pudieron cargar los datos: " + e.getMessage());
            consola.mostrar("Se iniciará con datos vacíos.");
            datos = new Datos();
        }
    }

    private void guardarDatos() {
        try {
            repositorio.guardar(datos, ARCHIVO_DATOS);
            consola.mostrar("Datos guardados correctamente.");
        } catch (PersistenciaException e) {
            consola.mostrar("No se pudieron guardar los datos: " + e.getMessage());
        }
    }

    // Si una regla de negocio no se cumple, cada menú muestra el motivo con este método y vuelve al menú.
    private void mostrarError(ReglaNegocioException e) {
        consola.mostrar("No se pudo completar la operación: " + e.getMessage());
    }

    // ---------- Menú principal ----------

    private void menuPrincipal() {
        boolean salir = false;
        while (!salir) {
            consola.titulo("FutbolStats");
            consola.mostrar("1) Datos del sistema (clubes, jugadores, árbitros, estadios, torneos)");
            consola.mostrar("2) Contratos de jugadores");
            consola.mostrar("3) Torneos: inscripción, fixture y estado");
            consola.mostrar("4) Partidos: programar, nómina y partido en vivo");
            consola.mostrar("5) Suscripciones y notificaciones");
            consola.mostrar("6) Sponsors y patrocinios");
            consola.mostrar("7) Reportes y proyecciones");
            consola.mostrar("8) Cargar datos de demostración");
            consola.mostrar("0) Salir");
            int opcion = consola.leerEntero("Opción");
            try {
                switch (opcion) {
                    case 1:
                        menuDatos();
                        break;
                    case 2:
                        menuContratos();
                        break;
                    case 3:
                        menuTorneos();
                        break;
                    case 4:
                        menuPartidos();
                        break;
                    case 5:
                        menuSuscripciones();
                        break;
                    case 6:
                        menuPatrocinios();
                        break;
                    case 7:
                        menuReportes();
                        break;
                    case 8:
                        datos.cargarDemostracion();
                        consola.mostrar("Datos de demostración cargados: torneo con 4 clubes y fixture generado.");
                        break;
                    case 0:
                        salir = true;
                        break;
                    default:
                        consola.mostrar("Opción inválida.");
                }
            } catch (ReglaNegocioException e) {
                mostrarError(e);
            }
        }
        consola.mostrar("Hasta luego.");
        guardarDatos();
    }

    // ---------- CU01: administrar datos ----------

    private void menuDatos() {
        boolean volver = false;
        while (!volver) {
            consola.titulo("Datos del sistema");
            consola.mostrar("1) Ver listados");
            consola.mostrar("2) Alta de club");
            consola.mostrar("3) Alta de jugador");
            consola.mostrar("4) Alta de entrenador");
            consola.mostrar("5) Alta de árbitro");
            consola.mostrar("6) Alta de estadio");
            consola.mostrar("7) Alta de torneo");
            consola.mostrar("8) Modificar datos");
            consola.mostrar("9) Dar de baja");
            consola.mostrar("0) Volver");
            int opcion = consola.leerEntero("Opción");
            try {
                switch (opcion) {
                    case 1:
                        verListados();
                        break;
                    case 2:
                        altaClub();
                        break;
                    case 3:
                        altaJugador();
                        break;
                    case 4:
                        altaEntrenador();
                        break;
                    case 5:
                        altaArbitro();
                        break;
                    case 6:
                        altaEstadio();
                        break;
                    case 7:
                        altaTorneo();
                        break;
                    case 8:
                        modificarDatos();
                        break;
                    case 9:
                        darDeBaja();
                        break;
                    case 0:
                        volver = true;
                        break;
                    default:
                        consola.mostrar("Opción inválida.");
                }
            } catch (ReglaNegocioException e) {
                mostrarError(e);
            }
        }
    }

    private void verListados() {
        List<String> categorias = Arrays.asList("Clubes", "Jugadores", "Entrenadores", "Árbitros", "Estadios", "Torneos");
        String categoria = consola.elegir("¿Qué listado querés ver?", categorias);
        if (categoria == null) {
            return;
        }
        List<Object> elementos = new ArrayList<>();
        switch (categoria) {
            case "Clubes":
                elementos.addAll(datos.getClubes());
                break;
            case "Jugadores":
                elementos.addAll(datos.getJugadores());
                break;
            case "Entrenadores":
                elementos.addAll(datos.getEntrenadores());
                break;
            case "Árbitros":
                elementos.addAll(datos.getArbitros());
                break;
            case "Estadios":
                elementos.addAll(datos.getEstadios());
                break;
            default:
                elementos.addAll(datos.getTorneos());
        }
        consola.titulo(categoria);
        if (elementos.isEmpty()) {
            consola.mostrar("(no hay datos cargados)");
        }
        for (Object elemento : elementos) {
            consola.mostrar(" - " + elemento);
        }
    }

    // CU01: modificacion y baja
    // El modelo valida cada dato nuevo y Datos controla que lo que se borra no este en uso.

    private void modificarDatos() throws ReglaNegocioException {
        String categoria = consola.elegir("¿Qué querés modificar?", CATEGORIAS);
        if (categoria == null) {
            return;
        }
        if (categoria.equals("Club")) {
            Club club = consola.elegir("Club", datos.getClubes());
            if (club != null) {
                club.setSedeSocial(consola.leerTexto("Nueva sede social"));
            }
        } else if (categoria.equals("Jugador")) {
            Jugador jugador = consola.elegir("Jugador", datos.getJugadores());
            if (jugador != null) {
                jugador.setNumeroCamiseta(consola.leerEntero("Nuevo número de camiseta"));
                jugador.setValorMercado(consola.leerDecimal("Nuevo valor de mercado"));
            }
        } else if (categoria.equals("Entrenador")) {
            Entrenador entrenador = consola.elegir("Entrenador", datos.getEntrenadores());
            if (entrenador != null) {
                entrenador.setLicencia(consola.leerTexto("Nueva licencia"));
            }
        } else if (categoria.equals("Árbitro")) {
            Arbitro arbitro = consola.elegir("Árbitro", datos.getArbitros());
            if (arbitro != null) {
                arbitro.setCategoria(consola.leerTexto("Nueva categoría"));
            }
        } else if (categoria.equals("Estadio")) {
            Estadio estadio = consola.elegir("Estadio", datos.getEstadios());
            if (estadio != null) {
                estadio.setCapacidad(consola.leerEntero("Nueva capacidad"));
            }
        } else {
            Torneo torneo = consola.elegir("Torneo", datos.getTorneos());
            if (torneo != null) {
                torneo.modificarDatos(consola.leerTexto("Nuevo nombre"), consola.leerFecha("Nueva fecha de inicio"),
                        consola.leerFecha("Nueva fecha de fin"));
            }
        }
        consola.mostrar("Listo.");
    }

    private void darDeBaja() throws ReglaNegocioException {
        String categoria = consola.elegir("¿Qué querés dar de baja?", CATEGORIAS);
        if (categoria == null) {
            return;
        }
        if (categoria.equals("Club")) {
            Club club = consola.elegir("Club", datos.getClubes());
            if (club != null) {
                datos.bajaClub(club);
            }
        } else if (categoria.equals("Jugador")) {
            Jugador jugador = consola.elegir("Jugador", datos.getJugadores());
            if (jugador != null) {
                datos.bajaJugador(jugador);
            }
        } else if (categoria.equals("Entrenador")) {
            Entrenador entrenador = consola.elegir("Entrenador", datos.getEntrenadores());
            if (entrenador != null) {
                datos.bajaEntrenador(entrenador);
            }
        } else if (categoria.equals("Árbitro")) {
            Arbitro arbitro = consola.elegir("Árbitro", datos.getArbitros());
            if (arbitro != null) {
                datos.bajaArbitro(arbitro);
            }
        } else if (categoria.equals("Estadio")) {
            Estadio estadio = consola.elegir("Estadio", datos.getEstadios());
            if (estadio != null) {
                datos.bajaEstadio(estadio);
            }
        } else {
            Torneo torneo = consola.elegir("Torneo", datos.getTorneos());
            if (torneo != null) {
                datos.bajaTorneo(torneo);
            }
        }
        consola.mostrar("Listo.");
    }

    private void altaClub() throws ReglaNegocioException {
        String nombre = consola.leerTexto("Nombre del club");
        if (nombre.isEmpty()) {
            throw new ReglaNegocioException("El club necesita un nombre");
        }
        LocalDate fundacion = consola.leerFecha("Fecha de fundación");
        String sede = consola.leerTexto("Sede social");
        datos.getClubes().add(new Club(nombre, fundacion, sede));
        consola.mostrar("Club creado: " + nombre);
    }

    private void altaJugador() throws ReglaNegocioException {
        String dni = pedirDniNuevo();
        String nombre = consola.leerTexto("Nombre");
        String apellido = consola.leerTexto("Apellido");
        LocalDate nacimiento = consola.leerFecha("Fecha de nacimiento");
        String posicion = consola.leerTexto("Posición");
        int camiseta = consola.leerEntero("Número de camiseta");
        double valor = consola.leerDecimal("Valor de mercado");
        Jugador jugador = new Jugador(dni, nombre, apellido, nacimiento, posicion, camiseta, valor, 0);
        datos.getJugadores().add(jugador);
        consola.mostrar("Jugador creado: " + jugador);
        if (consola.confirmar("¿Le registrás un contrato con un club ahora?")) {
            Club club = consola.elegir("Club", datos.getClubes());
            if (club != null) {
                jugador.registrarContrato(club, consola.leerFecha("Fecha de inicio del contrato"));
                consola.mostrar("Contrato registrado con " + club);
            }
        }
    }

    private void altaEntrenador() throws ReglaNegocioException {
        String dni = pedirDniNuevo();
        String nombre = consola.leerTexto("Nombre");
        String apellido = consola.leerTexto("Apellido");
        LocalDate nacimiento = consola.leerFecha("Fecha de nacimiento");
        List<String> especialidades = Arrays.asList("Director técnico", "Preparador físico", "Ayudante");
        String especialidad = consola.elegir("Especialidad", especialidades);
        if (especialidad == null) {
            return;
        }
        String licencia = consola.leerTexto("Licencia");
        Entrenador entrenador = new Entrenador(dni, nombre, apellido, nacimiento, especialidad, licencia);
        datos.getEntrenadores().add(entrenador);
        consola.mostrar("Entrenador creado: " + entrenador);
        if (consola.confirmar("¿Lo asignás a un club?")) {
            Club club = consola.elegir("Club", datos.getClubes());
            if (club != null) {
                club.asignarEntrenador(entrenador);
                consola.mostrar("Asignado a " + club);
            }
        }
    }

    private void altaArbitro() throws ReglaNegocioException {
        String dni = pedirDniNuevo();
        String nombre = consola.leerTexto("Nombre");
        String apellido = consola.leerTexto("Apellido");
        LocalDate nacimiento = consola.leerFecha("Fecha de nacimiento");
        String categoria = consola.elegir("Categoría", Arrays.asList("nacional", "internacional"));
        if (categoria == null) {
            return;
        }
        String colegio = consola.leerTexto("Colegio de árbitros");
        Arbitro arbitro = new Arbitro(dni, nombre, apellido, nacimiento, categoria, colegio);
        while (consola.confirmar("¿Es hincha declarado de algún club?")) {
            Club club = consola.elegir("Club", datos.getClubes());
            if (club == null) {
                break;
            }
            arbitro.agregarClubHincha(club);
        }
        datos.getArbitros().add(arbitro);
        consola.mostrar("Árbitro creado: " + arbitro + " (" + categoria + ")");
    }

    private void altaEstadio() {
        String nombre = consola.leerTexto("Nombre del estadio");
        int capacidad = consola.leerEntero("Capacidad");
        String calle = consola.leerTexto("Calle");
        String numero = consola.leerTexto("Número");
        String ciudad = consola.leerTexto("Ciudad");
        String provincia = consola.leerTexto("Provincia");
        String clima = consola.leerTexto("Condiciones habituales (clima o tipo de césped)");
        String direccion = calle + " " + numero + ", " + ciudad + ", " + provincia;
        datos.getEstadios().add(new Estadio(nombre, capacidad, direccion, clima));
        consola.mostrar("Estadio creado: " + nombre);
    }

    private void altaTorneo() throws ReglaNegocioException {
        String nombre = consola.leerTexto("Nombre del torneo");
        LocalDate inicio = consola.leerFecha("Fecha de inicio");
        LocalDate fin = consola.leerFecha("Fecha de fin");
        TipoTorneo tipo = consola.elegir("Tipo de torneo", Arrays.asList(TipoTorneo.values()));
        if (tipo == null) {
            return;
        }
        int cupos = consola.leerEntero("Cupos de clasificación");
        int corte = consola.leerEntero("Puntaje de corte de años anteriores");
        Torneo torneo = new Torneo(nombre, inicio, fin, tipo, cupos, corte);
        datos.getTorneos().add(torneo);
        consola.mostrar("Torneo creado: " + torneo);
    }

    private String pedirDniNuevo() throws ReglaNegocioException {
        String dni = consola.leerTexto("DNI");
        if (dni.isEmpty()) {
            throw new ReglaNegocioException("El DNI es obligatorio");
        }
        if (datos.existeDni(dni)) {
            throw new ReglaNegocioException("Ya existe una persona con el DNI " + dni);
        }
        return dni;
    }

    // ---------- CU02: contratos de jugadores ----------

    private void menuContratos() {
        boolean volver = false;
        while (!volver) {
            consola.titulo("Contratos de jugadores");
            consola.mostrar("1) Registrar contrato (jugador libre)");
            consola.mostrar("2) Transferir jugador a otro club");
            consola.mostrar("3) Rescindir contrato");
            consola.mostrar("4) Ver historial de contratos de un jugador");
            consola.mostrar("0) Volver");
            int opcion = consola.leerEntero("Opción");
            try {
                switch (opcion) {
                    case 1:
                        registrarContrato();
                        break;
                    case 2:
                        transferirJugador();
                        break;
                    case 3:
                        rescindirContrato();
                        break;
                    case 4:
                        verHistorial();
                        break;
                    case 0:
                        volver = true;
                        break;
                    default:
                        consola.mostrar("Opción inválida.");
                }
            } catch (ReglaNegocioException e) {
                mostrarError(e);
            }
        }
    }

    // Elige un jugador: primero se elige el club (o "libres") para no mostrar una lista enorme.
    private Jugador elegirJugador() {
        List<Jugador> candidatos;
        if (consola.confirmar("¿El jugador pertenece a un club? (n = jugador libre)")) {
            Club club = consola.elegir("Club", datos.getClubes());
            if (club == null) {
                return null;
            }
            candidatos = datos.jugadoresDe(club);
        } else {
            candidatos = new ArrayList<>();
            for (Jugador jugador : datos.getJugadores()) {
                if (jugador.getClubActual() == null) {
                    candidatos.add(jugador);
                }
            }
        }
        return consola.elegir("Jugador", candidatos);
    }

    private void registrarContrato() throws ReglaNegocioException {
        Jugador jugador = elegirJugador();
        Club club = null;
        if (jugador != null) {
            club = consola.elegir("Club que lo contrata", datos.getClubes());
        }
        if (jugador == null || club == null) {
            return;
        }
        jugador.registrarContrato(club, consola.leerFecha("Fecha de inicio"));
        consola.mostrar(jugador + " ahora juega en " + club);
    }

    private void transferirJugador() throws ReglaNegocioException {
        Jugador jugador = elegirJugador();
        Club destino = null;
        if (jugador != null) {
            destino = consola.elegir("Club de destino", datos.getClubes());
        }
        if (jugador == null || destino == null) {
            return;
        }
        jugador.transferir(destino, consola.leerFecha("Fecha de la transferencia"));
        consola.mostrar(jugador + " fue transferido a " + destino);
    }

    private void rescindirContrato() throws ReglaNegocioException {
        Jugador jugador = elegirJugador();
        if (jugador == null) {
            return;
        }
        jugador.rescindirContrato(consola.leerFecha("Fecha de rescisión"));
        consola.mostrar("Contrato rescindido: " + jugador + " queda libre (sus datos y estadísticas se conservan)");
    }

    private void verHistorial() {
        Jugador jugador = elegirJugador();
        if (jugador == null) {
            return;
        }
        consola.titulo("Historial de " + jugador);
        if (jugador.getContratos().isEmpty()) {
            consola.mostrar("(sin contratos)");
        }
        for (Contrato contrato : jugador.getContratos()) {
            consola.mostrar(" - " + contrato);
        }
    }

    // ---------- CU03 y CU04: inscripción y fixture ----------

    private void menuTorneos() {
        boolean volver = false;
        while (!volver) {
            consola.titulo("Torneos");
            consola.mostrar("1) Inscribir un club en un torneo");
            consola.mostrar("2) Generar el fixture");
            consola.mostrar("3) Ver el fixture");
            consola.mostrar("4) Avanzar el estado del torneo");
            consola.mostrar("0) Volver");
            int opcion = consola.leerEntero("Opción");
            try {
                switch (opcion) {
                    case 1:
                        inscribirClub();
                        break;
                    case 2:
                        generarFixture();
                        break;
                    case 3:
                        verFixture();
                        break;
                    case 4:
                        avanzarEstado();
                        break;
                    case 0:
                        volver = true;
                        break;
                    default:
                        consola.mostrar("Opción inválida.");
                }
            } catch (ReglaNegocioException e) {
                mostrarError(e);
            }
        }
    }

    private Torneo elegirTorneo() {
        return consola.elegir("Torneo", datos.getTorneos());
    }

    private void inscribirClub() throws ReglaNegocioException {
        Torneo torneo = elegirTorneo();
        if (torneo == null) {
            return;
        }
        Club club = consola.elegir("Club a inscribir", datos.getClubes());
        if (club == null) {
            return;
        }
        List<Jugador> plantel = datos.jugadoresDe(club);
        consola.mostrar("Se inscribe el plantel con contrato vigente: " + plantel.size() + " jugadores.");
        controladorTorneo.inscribirClub(torneo, club, plantel);
        consola.mostrar(club + " inscripto en " + torneo.getNombre());
    }

    private void generarFixture() throws ReglaNegocioException {
        Torneo torneo = elegirTorneo();
        if (torneo == null) {
            return;
        }
        controladorTorneo.generarFixture(torneo);
        consola.mostrar("Fixture generado: " + torneo.getPartidos().size() + " partidos.");
    }

    private void verFixture() {
        Torneo torneo = elegirTorneo();
        if (torneo == null) {
            return;
        }
        if (torneo.getFixture() == null) {
            consola.mostrar("El torneo todavía no tiene fixture.");
            return;
        }
        consola.titulo("Fixture de " + torneo.getNombre());
        List<List<Partido>> fechas = torneo.getFixture().getFechas();
        for (int i = 0; i < fechas.size(); i++) {
            consola.mostrar("Fecha " + (i + 1) + ":");
            for (Partido partido : fechas.get(i)) {
                consola.mostrar("   " + partido);
            }
        }
    }

    private void avanzarEstado() throws ReglaNegocioException {
        Torneo torneo = elegirTorneo();
        if (torneo == null) {
            return;
        }
        controladorTorneo.avanzarEstado(torneo);
        consola.mostrar("El torneo ahora está " + torneo.getEstado());
    }

    // ---------- CU05 a CU08: partidos ----------

    private void menuPartidos() {
        Torneo torneo = elegirTorneo();
        if (torneo == null) {
            return;
        }
        Partido partido = consola.elegir("Partido", torneo.getPartidos());
        if (partido == null) {
            return;
        }
        boolean volver = false;
        while (!volver) {
            consola.titulo("Partido: " + partido);
            consola.mostrar("1) Programar (fecha, estadio y terna arbitral)");
            consola.mostrar("2) Cargar nómina de un club");
            consola.mostrar("3) Iniciar el partido");
            consola.mostrar("4) Registrar un evento");
            consola.mostrar("5) Anular un evento");
            consola.mostrar("6) Ver eventos");
            consola.mostrar("7) Finalizar el partido");
            consola.mostrar("8) Suspender el partido");
            consola.mostrar("0) Volver");
            int opcion = consola.leerEntero("Opción");
            try {
                switch (opcion) {
                    case 1:
                        programarPartido(torneo, partido);
                        break;
                    case 2:
                        cargarNomina(torneo, partido);
                        break;
                    case 3:
                        controladorPartido.iniciarPartido(partido);
                        consola.mostrar("El partido está EN_JUEGO.");
                        break;
                    case 4:
                        registrarEvento(partido);
                        break;
                    case 5:
                        anularEvento(partido);
                        break;
                    case 6:
                        verEventos(partido);
                        break;
                    case 7:
                        finalizarPartido(torneo, partido);
                        break;
                    case 8:
                        controladorPartido.suspenderPartido(partido);
                        consola.mostrar("El partido quedó SUSPENDIDO.");
                        break;
                    case 0:
                        volver = true;
                        break;
                    default:
                        consola.mostrar("Opción inválida.");
                }
            } catch (ReglaNegocioException e) {
                mostrarError(e);
            }
        }
    }

    private void programarPartido(Torneo torneo, Partido partido) throws ReglaNegocioException {
        consola.mostrar("El torneo va del " + torneo.getFechaInicio() + " al " + torneo.getFechaFin() + ".");
        var fechaHora = consola.leerFechaHora("Fecha y hora");
        Estadio estadio = consola.elegir("Estadio", datos.getEstadios());
        if (estadio == null) {
            return;
        }
        List<Arbitro> disponibles = new ArrayList<>(datos.getArbitros());
        Arbitro principal = consola.elegir("Árbitro principal", disponibles);
        if (principal == null) {
            return;
        }
        disponibles.remove(principal);
        Arbitro asistente1 = consola.elegir("Primer asistente", disponibles);
        if (asistente1 == null) {
            return;
        }
        disponibles.remove(asistente1);
        Arbitro asistente2 = consola.elegir("Segundo asistente", disponibles);
        if (asistente2 == null) {
            return;
        }
        TernaArbitral terna = new TernaArbitral(principal, asistente1, asistente2);
        controladorTorneo.programarPartido(torneo, partido, fechaHora, estadio, terna); // valida la disponibilidad (CU14)
        consola.mostrar("Partido programado el " + fechaHora + " en " + estadio + ".");
    }

    private void cargarNomina(Torneo torneo, Partido partido) throws ReglaNegocioException {
        Club club = consola.elegir("Club", Arrays.asList(partido.getLocal(), partido.getVisitante()));
        if (club == null) {
            return;
        }
        List<Jugador> plantel = datos.jugadoresDe(club);
        Nomina nomina = new Nomina(club);
        if (consola.confirmar("¿Armar la nómina automáticamente (11 titulares y 5 suplentes)?")) {
            for (int i = 0; i < Nomina.CANTIDAD_TITULARES && i < plantel.size(); i++) {
                nomina.agregarTitular(plantel.get(i));
            }
            for (int i = Nomina.CANTIDAD_TITULARES; i < Nomina.CANTIDAD_TITULARES + 5 && i < plantel.size(); i++) {
                nomina.agregarSuplente(plantel.get(i));
            }
        } else {
            List<Jugador> titulares = consola.elegirVarios("Elegí los " + Nomina.CANTIDAD_TITULARES + " titulares", plantel);
            for (Jugador jugador : titulares) {
                nomina.agregarTitular(jugador);
            }
            List<Jugador> restantes = new ArrayList<>(plantel);
            restantes.removeAll(titulares);
            List<Jugador> suplentes = consola.elegirVarios("Elegí hasta " + Nomina.MAX_SUPLENTES + " suplentes", restantes);
            for (Jugador jugador : suplentes) {
                nomina.agregarSuplente(jugador);
            }
        }
        controladorTorneo.cargarNomina(torneo, partido, nomina); // valida suspensiones y plantel inscripto (CU06)
        consola.mostrar("Nómina cargada: " + nomina);
    }

    private void registrarEvento(Partido partido) throws ReglaNegocioException {
        List<String> tipos = Arrays.asList("Gol", "Remate", "Tarjeta amarilla", "Tarjeta roja", "Cambio", "Falta");
        String tipo = consola.elegir("Tipo de evento", tipos);
        if (tipo == null) {
            return;
        }
        Club club = consola.elegir("Club del jugador", Arrays.asList(partido.getLocal(), partido.getVisitante()));
        if (club == null) {
            return;
        }
        Nomina nomina = partido.getNomina(club);
        if (nomina == null) {
            throw new ReglaNegocioException("Todavía no se cargó la nómina de " + club);
        }
        List<Jugador> jugadores = nomina.getJugadores();
        Jugador jugador = consola.elegir("Jugador", jugadores);
        if (jugador == null) {
            return;
        }
        int minuto = consola.leerEntero("Minuto (0 a " + Evento.MINUTO_MAXIMO + ")");
        Evento evento = crearEvento(tipo, minuto, jugador, club, jugadores);
        if (evento == null) {
            return;
        }
        List<Evento> eventosNuevos = controladorPartido.registrarEvento(
                partido, evento, datos.getSuscriptores());

        consola.mostrar("Evento registrado: min " + minuto + " - " + evento);

        for (Evento eventoNuevo : eventosNuevos) {
            if (eventoNuevo != evento) {
                consola.mostrar("El partido registró además: " + eventoNuevo);
            }
        }
    }

    private Evento crearEvento(String tipo, int minuto, Jugador jugador, Club club, List<Jugador> jugadores)
            throws ReglaNegocioException {
        switch (tipo) {
            case "Gol": {
                TipoGol tipoGol = consola.elegir("Tipo de gol", Arrays.asList(TipoGol.values()));
                if (tipoGol == null) {
                    return null;
                }
                return new Gol(minuto, jugador, club, tipoGol);
            }
            case "Remate": {
                ZonaRemate zona = consola.elegir("Zona del remate", Arrays.asList(ZonaRemate.values()));
                if (zona == null) {
                    return null;
                }
                return new Remate(minuto, jugador, club, zona);
            }
            case "Tarjeta amarilla":
                return new Tarjeta(minuto, jugador, club, ColorTarjeta.AMARILLA);
            case "Tarjeta roja":
                return new Tarjeta(minuto, jugador, club, ColorTarjeta.ROJA);
            case "Cambio": {
                consola.mostrar("El jugador elegido es el que sale.");
                Jugador entrante = consola.elegir("Jugador que entra", jugadores);
                if (entrante == null) {
                    return null;
                }
                return new Cambio(minuto, jugador, entrante, club);
            }
            default:
                return new Falta(minuto, jugador, club, consola.leerTexto("Tipo de falta"));
        }
    }

    private void anularEvento(Partido partido) throws ReglaNegocioException {
        List<Evento> anulables = new ArrayList<>();
        for (Evento evento : partido.getEventos()) {
            if (evento.getEventoAnulado() == null && !partido.estaAnulado(evento)) {
                anulables.add(evento);
            }
        }
        Evento evento = consola.elegir("Evento a anular", anulables);
        if (evento == null) {
            return;
        }
        String motivo = consola.leerTexto("Motivo de la anulación");
        controladorPartido.anularEvento(partido, evento, motivo);
        consola.mostrar("Evento anulado (queda registrado junto con su anulación). " + partido);
    }

    private void verEventos(Partido partido) {
        consola.titulo(partido.toString());
        if (partido.getEventos().isEmpty()) {
            consola.mostrar("(sin eventos)");
        }
        for (Evento evento : partido.getEventos()) {
            String marca = "";
            if (partido.estaAnulado(evento)) {
                marca = "  [ANULADO]";
            }
            consola.mostrar(" min " + evento.getMinuto() + " - " + evento + marca);
        }
    }

    private void finalizarPartido(Torneo torneo, Partido partido)
            throws ReglaNegocioException {

        controladorPartido.finalizarPartido(
                torneo, partido, datos.getSuscriptores());

        consola.mostrar("Partido finalizado: " + partido);

        if (!torneo.getSanciones().isEmpty()) {
            consola.mostrar("Sanciones del torneo:");
            for (Sancion sancion : torneo.getSanciones()) {
                consola.mostrar(" - " + sancion);
            }
        }
    }

    // ---------- CU10: suscripciones ----------

    private void menuSuscripciones() {
        boolean volver = false;
        while (!volver) {
            consola.titulo("Suscripciones y notificaciones");
            consola.mostrar("1) Alta de suscriptor");
            consola.mostrar("2) Elegir canales de un suscriptor");
            consola.mostrar("3) Seguir un club");
            consola.mostrar("4) Ver suscriptores");
            consola.mostrar("0) Volver");
            int opcion = consola.leerEntero("Opción");
            try {
                switch (opcion) {
                    case 1:
                        altaSuscriptor();
                        break;
                    case 2:
                        configurarCanales();
                        break;
                    case 3:
                        seguirClub();
                        break;
                    case 4:
                        verSuscriptores();
                        break;
                    case 0:
                        volver = true;
                        break;
                    default:
                        consola.mostrar("Opción inválida.");
                }
            } catch (ReglaNegocioException e) {
                mostrarError(e);
            }
        }
    }

    private void altaSuscriptor() throws ReglaNegocioException {
        String nombre = consola.leerTexto("Nombre");
        String email = consola.leerTexto("Email");
        String telefono = consola.leerTexto("Teléfono");
        datos.getSuscriptores().add(new Suscriptor(nombre, email, telefono));
        consola.mostrar("Suscriptor creado. Falta elegir sus canales y los clubes que sigue.");
    }

    private void configurarCanales() throws ReglaNegocioException {
        Suscriptor suscriptor = consola.elegir("Suscriptor", datos.getSuscriptores());
        if (suscriptor == null) {
            return;
        }
        List<CanalNotificacion> disponibles = Arrays.asList(new AppMovil(), new CorreoPrensa(), new Sms());
        Set<CanalNotificacion> elegidos = new LinkedHashSet<>(consola.elegirVarios("Canales disponibles", disponibles));
        suscriptor.configurarCanales(elegidos);
        consola.mostrar("Canales habilitados: " + suscriptor.getCanalesHabilitados());
    }

    private void seguirClub() throws ReglaNegocioException {
        Suscriptor suscriptor = consola.elegir("Suscriptor", datos.getSuscriptores());
        if (suscriptor == null) {
            return;
        }
        Club club = consola.elegir("Club a seguir", datos.getClubes());
        if (club == null) {
            return;
        }
        suscriptor.seguir(club);
        consola.mostrar(suscriptor.getNombre() + " ahora sigue a " + club);
    }

    private void verSuscriptores() {
        consola.titulo("Suscriptores");
        if (datos.getSuscriptores().isEmpty()) {
            consola.mostrar("(no hay suscriptores)");
        }
        for (Suscriptor suscriptor : datos.getSuscriptores()) {
            consola.mostrar(" - " + suscriptor + " | canales: " + suscriptor.getCanalesHabilitados()
                    + " | clubes: " + suscriptor.getClubesSiguiendo());
        }
    }

    // ---------- CU11: patrocinios ----------

    private void menuPatrocinios() {
        boolean volver = false;
        while (!volver) {
            consola.titulo("Sponsors y patrocinios");
            consola.mostrar("1) Alta de sponsor");
            consola.mostrar("2) Patrocinar un club");
            consola.mostrar("3) Patrocinar un torneo");
            consola.mostrar("4) Ver patrocinios");
            consola.mostrar("0) Volver");
            int opcion = consola.leerEntero("Opción");
            try {
                switch (opcion) {
                    case 1:
                        altaSponsor();
                        break;
                    case 2:
                        patrocinarClub();
                        break;
                    case 3:
                        patrocinarTorneo();
                        break;
                    case 4:
                        verPatrocinios();
                        break;
                    case 0:
                        volver = true;
                        break;
                    default:
                        consola.mostrar("Opción inválida.");
                }
            } catch (ReglaNegocioException e) {
                mostrarError(e);
            }
        }
    }

    private void altaSponsor() {
        String nombre = consola.leerTexto("Nombre del sponsor");
        String marca = consola.leerTexto("Marca");
        String logo = consola.leerTexto("Logo (nombre del archivo)");
        datos.getSponsors().add(new Sponsor(nombre, marca, logo));
        consola.mostrar("Sponsor creado: " + nombre);
    }

    private void patrocinarClub() throws ReglaNegocioException {
        Sponsor sponsor = consola.elegir("Sponsor", datos.getSponsors());
        Club club = null;
        if (sponsor != null) {
            club = consola.elegir("Club", datos.getClubes());
        }
        if (sponsor == null || club == null) {
            return;
        }
        double monto = consola.leerDecimal("Monto");
        LocalDate inicio = consola.leerFecha("Fecha de inicio");
        LocalDate fin = consola.leerFecha("Fecha de fin");
        boolean principal = consola.confirmar("¿Es sponsor principal del club?");
        Patrocinio patrocinio = sponsor.patrocinarClub(club, monto, inicio, fin, principal);
        consola.mostrar("Patrocinio registrado: " + patrocinio);
    }

    private void patrocinarTorneo() throws ReglaNegocioException {
        Sponsor sponsor = consola.elegir("Sponsor", datos.getSponsors());
        Torneo torneo = null;
        if (sponsor != null) {
            torneo = elegirTorneo();
        }
        if (sponsor == null || torneo == null) {
            return;
        }
        double monto = consola.leerDecimal("Monto");
        LocalDate inicio = consola.leerFecha("Fecha de inicio");
        LocalDate fin = consola.leerFecha("Fecha de fin");
        Patrocinio patrocinio = sponsor.patrocinarTorneo(torneo, monto, inicio, fin);
        consola.mostrar("Patrocinio registrado: " + patrocinio);
    }

    private void verPatrocinios() {
        consola.titulo("Patrocinios");
        boolean hay = false;
        for (Sponsor sponsor : datos.getSponsors()) {
            for (Patrocinio patrocinio : sponsor.getPatrocinios()) {
                consola.mostrar(" - " + patrocinio);
                hay = true;
            }
        }
        if (!hay) {
            consola.mostrar("(no hay patrocinios)");
        }
    }

    // ---------- CU12 y CU13: reportes y proyecciones ----------

    private void menuReportes() {
        Torneo torneo = elegirTorneo();
        if (torneo == null) {
            return;
        }
        boolean volver = false;
        while (!volver) {
            consola.titulo("Reportes de " + torneo.getNombre());
            consola.mostrar("1) Tabla de posiciones");
            consola.mostrar("2) Goleadores");
            consola.mostrar("3) Valla menos vencida");
            consola.mostrar("4) xG de un partido");
            consola.mostrar("5) Probabilidad de clasificación de un club");
            consola.mostrar("0) Volver");
            switch (consola.leerEntero("Opción")) {
                case 1:
                    consola.mostrar(controladorReportes.generarTablaPosiciones(torneo));
                    break;
                case 2:
                    verGoleadores(torneo);
                    break;
                case 3:
                    verValla(torneo);
                    break;
                case 4:
                    verXG(torneo);
                    break;
                case 5:
                    verProbabilidad(torneo);
                    break;
                case 0:
                    volver = true;
                    break;
                default:
                    consola.mostrar("Opción inválida.");
            }
        }
    }

    private void verGoleadores(Torneo torneo) {
        List<Goleador> goleadores = controladorReportes.obtenerGoleadores(torneo);
        consola.titulo("Goleadores");
        if (goleadores.isEmpty()) {
            consola.mostrar("(todavía no hay goles en partidos finalizados)");
        }
        for (int i = 0; i < goleadores.size(); i++) {
            consola.mostrar((i + 1) + ". " + goleadores.get(i));
        }
    }

    private void verValla(Torneo torneo) {
        Club club = controladorReportes.obtenervallaMenosVencida(torneo);
        if (club == null) {
            consola.mostrar("Todavía no hay partidos finalizados.");
        } else {
            consola.mostrar("Valla menos vencida: " + club);
        }
    }

    private void verXG(Torneo torneo) {
        Partido partido = consola.elegir("Partido", torneo.getPartidos());
        if (partido == null) {
            return;
        }
        consola.mostrar(String.format("xG de %s: %.2f", partido.getLocal(), controladorReportes.calcularXGClub(partido, partido.getLocal())));
        consola.mostrar(String.format("xG de %s: %.2f", partido.getVisitante(), controladorReportes.calcularXGClub(partido, partido.getVisitante())));
    }

    private void verProbabilidad(Torneo torneo) {
        List<Club> clubes = new ArrayList<>();
        for (Inscripcion inscripcion : torneo.getInscripciones()) {
            clubes.add(inscripcion.getClub());
        }
        Club club = consola.elegir("Club", clubes);
        if (club == null) {
            return;
        }
        String metodo = consola.elegir("Método de cálculo",
                Arrays.asList("Promedio de puntos por partido", "Simulación con xG promedio"));
        if (metodo == null) {
            return;
        }
        MetodoProyeccion proyeccion;
        if (metodo.startsWith("Promedio")) {
            proyeccion = new ProyeccionPorPromedio();
        } else {
            proyeccion = new ProyeccionPorXG();
        }
        double probabilidad = controladorReportes.calcularProbabilidadClasificacion(torneo, club, proyeccion);
        consola.mostrar(String.format("Probabilidad de que %s clasifique: %.1f%% (%s)", club, probabilidad, metodo));
    }
}