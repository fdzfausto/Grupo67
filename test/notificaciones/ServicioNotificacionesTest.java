package notificaciones;

import excepcion.ReglaNegocioException;
import modelo.Club;
import modelo.Partido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

// CU09, RN21 (un canal que falla no frena a los demás) y RN22 (un canal nuevo no cambia el servicio).
class ServicioNotificacionesTest {

    // Canal de prueba: anota a quién le llegó el aviso y siempre responde que lo entregó.
    private static class CanalRegistrador implements CanalNotificacion {
        private static final long serialVersionUID = 1L;
        final List<String> destinatarios = new ArrayList<>();

        @Override
        public boolean enviar(Notificacion notificacion, Suscriptor suscriptor) {
            destinatarios.add(suscriptor.getNombre());
            return true;
        }
    }

    // Canal de prueba que siempre falla con una excepción.
    private static class CanalQueExplota implements CanalNotificacion {
        private static final long serialVersionUID = 1L;
        private final RuntimeException error;

        CanalQueExplota(RuntimeException error) {
            this.error = error;
        }

        @Override
        public boolean enviar(Notificacion notificacion, Suscriptor suscriptor) {
            throw error;
        }
    }

    // Canal de prueba que no lanza nada pero no logra entregar.
    private static class CanalQueNoEntrega implements CanalNotificacion {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean enviar(Notificacion notificacion, Suscriptor suscriptor) {
            return false;
        }
    }

    private ServicioNotificaciones servicio;
    private Notificacion notificacion;

    @BeforeEach
    void preparar() throws ReglaNegocioException {
        servicio = new ServicioNotificaciones();
        Partido partido = new Partido(new Club("River", LocalDate.of(1901, 5, 25), "Nunez"),
                new Club("Boca", LocalDate.of(1905, 4, 3), "La Boca"));
        notificacion = new Notificacion(partido, 33, "Gol de River");
    }

    private Suscriptor suscriptor(String nombre, CanalNotificacion... canales) throws ReglaNegocioException {
        Suscriptor s = new Suscriptor(nombre, nombre.toLowerCase() + "@mail.com", "1155");
        Set<CanalNotificacion> set = new LinkedHashSet<>();
        for (CanalNotificacion canal : canales) {
            set.add(canal);
        }
        if (!set.isEmpty()) {
            s.configurarCanales(set);
        }
        return s;
    }

    // Cuenta los envíos que fallaron.
    private int cantidadDeFallidos() {
        int fallidos = 0;
        for (EnvioNotificacion envio : notificacion.getEnvios()) {
            if (!envio.isExitoso()) {
                fallidos++;
            }
        }
        return fallidos;
    }

    @Test
    void enviaElAvisoPorCadaCanalHabilitadoDeCadaSuscriptor() throws ReglaNegocioException {
        CanalRegistrador a = new CanalRegistrador();
        CanalRegistrador b = new CanalRegistrador();
        List<Suscriptor> suscriptores = new ArrayList<>();
        suscriptores.add(suscriptor("Ana", a, b));
        suscriptores.add(suscriptor("Beto", a));

        servicio.notificar(notificacion, suscriptores);

        assertEquals(List.of("Ana", "Beto"), a.destinatarios);
        assertEquals(List.of("Ana"), b.destinatarios);
        assertEquals(3, notificacion.getEnvios().size());
        assertEquals(0, cantidadDeFallidos());
    }

    @Test
    void unCanalQueLanzaUnaExcepcionSeRegistraYNoFrenaALosDemas() throws ReglaNegocioException {
        CanalRegistrador bueno = new CanalRegistrador();
        CanalNotificacion malo = new CanalQueExplota(new IllegalStateException("Servidor SMS caído"));
        List<Suscriptor> suscriptores = new ArrayList<>();
        suscriptores.add(suscriptor("Ana", malo, bueno));
        suscriptores.add(suscriptor("Beto", bueno));

        servicio.notificar(notificacion, suscriptores); // no debe lanzar nada

        assertEquals(List.of("Ana", "Beto"), bueno.destinatarios); // los demás siguen funcionando
        assertEquals(3, notificacion.getEnvios().size());
        assertEquals(1, cantidadDeFallidos());
        for (EnvioNotificacion envio : notificacion.getEnvios()) {
            if (!envio.isExitoso()) {
                assertEquals("Servidor SMS caído", envio.getMensajeError());
                assertEquals("Ana", envio.getSuscriptor().getNombre());
            }
        }
    }

    @Test
    void siLaExcepcionNoTieneMensajeSeGuardaSuDescripcion() throws ReglaNegocioException {
        CanalNotificacion malo = new CanalQueExplota(new NullPointerException());

        servicio.notificar(notificacion, List.of(suscriptor("Ana", malo)));

        EnvioNotificacion envio = notificacion.getEnvios().get(0);
        assertFalse(envio.isExitoso());
        assertEquals("java.lang.NullPointerException", envio.getMensajeError());
    }

    @Test
    void unCanalQueNoLogroEntregarQuedaRegistradoComoFallido() throws ReglaNegocioException {
        servicio.notificar(notificacion, List.of(suscriptor("Ana", new CanalQueNoEntrega())));

        EnvioNotificacion envio = notificacion.getEnvios().get(0);
        assertFalse(envio.isExitoso());
        assertEquals("El canal no pudo entregar el aviso", envio.getMensajeError());
    }

    @Test
    void unCanalNuevoFuncionaSinModificarElServicio() throws ReglaNegocioException {
        // RN22: se agrega "mensajería instantánea" solo implementando la interfaz.
        CanalRegistrador mensajeria = new CanalRegistrador();

        servicio.notificar(notificacion, List.of(suscriptor("Ana", mensajeria)));

        assertEquals(List.of("Ana"), mensajeria.destinatarios);
    }

    @Test
    void unSuscriptorSinCanalesNoRecibeNada() throws ReglaNegocioException {
        servicio.notificar(notificacion, List.of(suscriptor("Ana")));

        assertTrue(notificacion.getEnvios().isEmpty());
    }

    @Test
    void conUnaListaVaciaDeSuscriptoresNoSeHaceNada() {
        servicio.notificar(notificacion, new ArrayList<>());

        assertTrue(notificacion.getEnvios().isEmpty());
    }

    @Test
    void sinNotificacionSeRechaza() {
        try {
            servicio.notificar(null, new ArrayList<>());
            fail("Debía rechazar una notificación nula");
        } catch (IllegalArgumentException e) {
            // correcto
        }
    }

    @Test
    void sinListaDeSuscriptoresSeRechaza() {
        try {
            servicio.notificar(notificacion, null);
            fail("Debía rechazar una lista de suscriptores nula");
        } catch (IllegalArgumentException e) {
            // correcto
        }
    }

    @Test
    void losTresCanalesInicialesEntreganElAviso() throws ReglaNegocioException {
        Suscriptor s = suscriptor("Ana", new AppMovil(), new CorreoPrensa(), new Sms());

        servicio.notificar(notificacion, List.of(s));

        assertEquals(3, notificacion.getEnvios().size());
        assertEquals(0, cantidadDeFallidos());
    }
}
