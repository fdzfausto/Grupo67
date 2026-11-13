package notificaciones;

import excepcion.ReglaNegocioException;
import modelo.Club;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

// CU10: registrar suscriptor, elegir canales y seguir clubes.
class SuscriptorTest {

    private Suscriptor suscriptor;
    private Club river;
    private Club boca;

    @BeforeEach
    void preparar() throws ReglaNegocioException {
        suscriptor = new Suscriptor("Marcos Prensa", "marcos@prensa.com", "1155551234");
        river = new Club("River", LocalDate.of(1901, 5, 25), "Nunez");
        boca = new Club("Boca", LocalDate.of(1905, 4, 3), "La Boca");
    }

    private Set<CanalNotificacion> canales(CanalNotificacion... canales) {
        Set<CanalNotificacion> set = new LinkedHashSet<>();
        for (CanalNotificacion canal : canales) {
            set.add(canal);
        }
        return set;
    }

    @Test
    void unSuscriptorNuevoNoSigueClubesNiTieneCanales() {
        assertTrue(suscriptor.getClubesSiguiendo().isEmpty());
        assertTrue(suscriptor.getCanalesHabilitados().isEmpty());
        assertEquals("Marcos Prensa", suscriptor.getNombre());
    }

    @Test
    void elNombreVacioSeRechaza() {
        try {
            new Suscriptor(" ", "a@b.com", "1155");
            fail("Debía rechazar el nombre vacío");
        } catch (ReglaNegocioException e) {
            // correcto: la regla de negocio rechazó el dato
        }
    }

    @Test
    void elNombreNuloSeRechaza() {
        try {
            new Suscriptor(null, "a@b.com", "1155");
            fail("Debía rechazar el nombre nulo");
        } catch (ReglaNegocioException e) {
            // correcto
        }
    }

    @Test
    void elEmailSinArrobaSeRechaza() {
        try {
            new Suscriptor("Ana", "sin-arroba", "1155");
            fail("Debía rechazar el email sin @");
        } catch (ReglaNegocioException e) {
            // correcto
        }
    }

    @Test
    void elEmailNuloSeRechaza() {
        try {
            new Suscriptor("Ana", null, "1155");
            fail("Debía rechazar el email nulo");
        } catch (ReglaNegocioException e) {
            // correcto
        }
    }

    @Test
    void elTelefonoVacioSeRechaza() {
        try {
            new Suscriptor("Ana", "a@b.com", "");
            fail("Debía rechazar el teléfono vacío");
        } catch (ReglaNegocioException e) {
            // correcto
        }
    }

    @Test
    void alSeguirUnClubElSuscriptorLoReconoce() throws ReglaNegocioException {
        suscriptor.seguir(river);

        assertTrue(suscriptor.sigueA(river));
        assertFalse(suscriptor.sigueA(boca));
    }

    @Test
    void noSePuedeSeguirDosVecesAlMismoClub() throws ReglaNegocioException {
        suscriptor.seguir(river);

        try {
            suscriptor.seguir(river);
            fail("Debía rechazar seguir dos veces al mismo club");
        } catch (ReglaNegocioException e) {
            assertEquals("Ya seguías este club", e.getMessage()); // mensaje del diagrama de secuencia CU10
        }
        assertEquals(1, suscriptor.getClubesSiguiendo().size());
    }

    @Test
    void noSePuedeSeguirUnClubNulo() {
        try {
            suscriptor.seguir(null);
            fail("Debía rechazar un club nulo");
        } catch (ReglaNegocioException e) {
            // correcto
        }
    }

    @Test
    void hayQueElegirAlMenosUnCanal() {
        try {
            suscriptor.configurarCanales(new HashSet<>());
            fail("Debía rechazar un conjunto vacío de canales");
        } catch (ReglaNegocioException e) {
            // correcto
        }
        try {
            suscriptor.configurarCanales(null);
            fail("Debía rechazar un conjunto nulo de canales");
        } catch (ReglaNegocioException e) {
            // correcto
        }
        assertTrue(suscriptor.getCanalesHabilitados().isEmpty());
    }

    @Test
    void validarCanalesRechazaConjuntosVaciosNulosOConElementosNulos() {
        Set<CanalNotificacion> conNulo = new HashSet<>();
        conNulo.add(null);

        assertFalse(suscriptor.validarCanales(null));
        assertFalse(suscriptor.validarCanales(new HashSet<>()));
        assertFalse(suscriptor.validarCanales(conNulo));
        assertTrue(suscriptor.validarCanales(canales(new Sms())));
    }

    @Test
    void losCanalesElegidosQuedanHabilitados() throws ReglaNegocioException {
        suscriptor.configurarCanales(canales(new AppMovil(), new Sms()));

        assertEquals(2, suscriptor.getCanalesHabilitados().size());
        assertTrue(suscriptor.getCanalesHabilitados().contains(new Sms()));
        assertFalse(suscriptor.getCanalesHabilitados().contains(new CorreoPrensa()));
    }

    @Test
    void elMismoCanalNoSeRepiteEnElConjunto() throws ReglaNegocioException {
        suscriptor.configurarCanales(canales(new Sms(), new Sms()));

        assertEquals(1, suscriptor.getCanalesHabilitados().size()); // equals/hashCode por clase
    }

    @Test
    void configurarCanalesReemplazaLosAnteriores() throws ReglaNegocioException {
        suscriptor.configurarCanales(canales(new AppMovil(), new Sms()));
        suscriptor.configurarCanales(canales(new CorreoPrensa()));

        assertEquals(1, suscriptor.getCanalesHabilitados().size());
        assertTrue(suscriptor.getCanalesHabilitados().contains(new CorreoPrensa()));
    }
}
