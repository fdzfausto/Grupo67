package notificaciones;

import excepcion.ReglaNegocioException;
import modelo.Club;
import modelo.Partido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

// RN20: cada notificación indica el partido, el minuto y el hecho. RN21: guarda el resultado de cada envío.
class NotificacionTest {

    private Partido partido;
    private Suscriptor suscriptor;

    @BeforeEach
    void preparar() throws ReglaNegocioException {
        Club river = new Club("River", LocalDate.of(1901, 5, 25), "Nunez");
        Club boca = new Club("Boca", LocalDate.of(1905, 4, 3), "La Boca");
        partido = new Partido(river, boca);
        suscriptor = new Suscriptor("Marcos", "marcos@prensa.com", "1155551234");
    }

    @Test
    void laNotificacionGuardaPartidoMinutoYHecho() throws ReglaNegocioException {
        Notificacion notificacion = new Notificacion(partido, 33, "Gol de River");

        assertSame(partido, notificacion.getPartido());
        assertEquals(33, notificacion.getMinuto());
        assertEquals("Gol de River", notificacion.getHecho());
        assertTrue(notificacion.getEnvios().isEmpty());
    }

    @Test
    void exigeUnPartido() {
        try {
            new Notificacion(null, 10, "Gol");
            fail("Debía rechazar un partido nulo");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó el dato
        }
    }

    @Test
    void unMinutoNegativoSeRechaza() {
        try {
            new Notificacion(partido, -1, "Gol");
            fail("Debía rechazar el minuto -1");
        } catch (ReglaNegocioException e) {
            // correcto
        }
    }

    @Test
    void unMinutoMayorACientoVeinteSeRechaza() {
        try {
            new Notificacion(partido, 121, "Gol");
            fail("Debía rechazar el minuto 121");
        } catch (ReglaNegocioException e) {
            // correcto
        }
    }

    @Test
    void losMinutosLimiteSonValidos() throws ReglaNegocioException {
        new Notificacion(partido, 0, "Inicio");
        new Notificacion(partido, 120, "Final");
    }

    @Test
    void exigeIndicarElHecho() {
        try {
            new Notificacion(partido, 10, null);
            fail("Debía rechazar un hecho nulo");
        } catch (ReglaNegocioException e) {
            // correcto
        }
        try {
            new Notificacion(partido, 10, "   ");
            fail("Debía rechazar un hecho en blanco");
        } catch (ReglaNegocioException e) {
            // correcto
        }
    }

    @Test
    void registrarUnEnvioExitosoLoGuardaSinMensajeDeError() throws ReglaNegocioException {
        Notificacion notificacion = new Notificacion(partido, 33, "Gol");

        EnvioNotificacion envio = notificacion.registrarEnvio(new Sms(), suscriptor, true, null);

        assertEquals(1, notificacion.getEnvios().size());
        assertTrue(envio.isExitoso());
        assertNull(envio.getMensajeError());
        assertSame(suscriptor, envio.getSuscriptor());
        assertEquals(new Sms(), envio.getCanal());
    }

    @Test
    void registrarUnEnvioFallidoConservaElMotivo() throws ReglaNegocioException {
        Notificacion notificacion = new Notificacion(partido, 33, "Gol");

        EnvioNotificacion envio = notificacion.registrarEnvio(new AppMovil(), suscriptor, false, "Sin conexión");

        assertFalse(envio.isExitoso());
        assertEquals("Sin conexión", envio.getMensajeError());
    }

    @Test
    void laListaDeEnviosDevueltaEsUnaCopia() throws ReglaNegocioException {
        Notificacion notificacion = new Notificacion(partido, 33, "Gol");
        notificacion.registrarEnvio(new Sms(), suscriptor, true, null);

        List<EnvioNotificacion> copia = notificacion.getEnvios();
        copia.clear();

        assertEquals(1, notificacion.getEnvios().size()); // encapsulamiento
    }
}
