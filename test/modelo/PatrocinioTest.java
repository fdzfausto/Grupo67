package modelo;

import excepcion.ReglaNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class PatrocinioTest {
    private Sponsor adidas;
    private Sponsor nike;
    private Club river;
    private Torneo torneo;
    private LocalDate inicio2026;
    private LocalDate fin2026;

    @BeforeEach
    void preparar() throws ReglaNegocioException{
        adidas = new Sponsor("Adidas", "Adidas", "adidas.png");
        nike = new Sponsor("Nike", "Nike", "nike.png");
        river = DatosPrueba.club("River");
        torneo = new Torneo("Liga Profesional 2026", LocalDate.of(2026,3,1), LocalDate.of(2026,12,15), TipoTorneo.NACIONAL, 2,30 );
        inicio2026 = LocalDate.of(2026,1,1);
        fin2026 = LocalDate.of(2026,12,31);
    }

    @Test
    void patrocinarUnClubCreaElPatrocinioEnElSponsorYEnElClub() throws ReglaNegocioException {
        Patrocinio patrocinio = adidas.patrocinarClub(river, 1000000, inicio2026, fin2026, true);

        assertEquals(1, adidas.getPatrocinios().size());
        assertEquals(1, river.getPatrocinios().size());
        assertEquals(adidas, patrocinio.getSponsor());
        assertEquals(river, patrocinio.getClub());
        assertNull(patrocinio.getTorneo());
        assertTrue(patrocinio.isPrincipal());
    }

    @Test
    void patrocinarUnTorneoNoLoAgregaAUnClub() throws ReglaNegocioException{
        Patrocinio patrocinio = adidas.patrocinarTorneo(torneo, 2000000, inicio2026, fin2026);
        assertEquals(torneo, patrocinio.getTorneo());
        assertNull(patrocinio.getClub());
        assertFalse(patrocinio.isPrincipal());
    }

    @Test
    void elMismoSponsorPuedeVolverAPatrocinarAlClubEnFechasQueNoSePisan() throws ReglaNegocioException{
        adidas.patrocinarClub(river, 1000000, inicio2026, fin2026, false);
        adidas.patrocinarClub(river, 1000000, LocalDate.of(2027,1,1), LocalDate.of(2027,12,31), false);
        assertEquals(2, adidas.getPatrocinios().size());
    }

    @Test
    void unClubPuedeTenerUnPrincipalYOtrosSponsorsNoPrincipales() throws ReglaNegocioException {
        adidas.patrocinarClub(river, 1000000, inicio2026, fin2026, true);
        nike.patrocinarClub(river, 300000, inicio2026, fin2026, false);

        assertEquals(2, river.getPatrocinios().size());
    }

    @Test
    void elSponsorPrincipalVigenteEsElQueCubreLaFechaDeHoy() throws ReglaNegocioException {
        LocalDate hoy = LocalDate.now();
        adidas.patrocinarClub(river, 1000000, hoy.minusMonths(1), hoy.plusMonths(1), true);

        assertEquals(adidas, river.getSponsorPrincipalVigente());
    }

    @Test
    void sinPatrociniosElClubNoTieneSponsorPrincipal() {
        assertNull(river.getSponsorPrincipalVigente());
    }

    // ---------- Reglas de negocio (tienen que dar error) ----------

    @Test
    void rn22NoPermiteDosPatrociniosDelMismoSponsorAlMismoClubEnFechasQueSePisan() throws ReglaNegocioException {
        adidas.patrocinarClub(river, 1000000, inicio2026, fin2026, false);
        try {
            adidas.patrocinarClub(river, 500000, LocalDate.of(2026, 6, 1), LocalDate.of(2027, 6, 1), false);
            fail("Tenia que rechazar el segundo patrocinio (RN22)");
        } catch (ReglaNegocioException e) {
            assertEquals(1, adidas.getPatrocinios().size()); // no se agrego nada
        }
    }

    @Test
    void rn22NoPermiteDosPatrociniosDelMismoSponsorAlMismoTorneoEnFechasQueSePisan() throws ReglaNegocioException {
        adidas.patrocinarTorneo(torneo, 2000000, inicio2026, fin2026);
        try {
            adidas.patrocinarTorneo(torneo, 1000000, inicio2026, fin2026);
            fail("Tenia que rechazar el segundo patrocinio del torneo (RN22)");
        } catch (ReglaNegocioException e) {
            assertEquals(1, adidas.getPatrocinios().size());
        }
    }

    @Test
    void rn23NoPermiteDosSponsorsPrincipalesALaVezEnElMismoClub() throws ReglaNegocioException {
        adidas.patrocinarClub(river, 1000000, inicio2026, fin2026, true);
        try {
            nike.patrocinarClub(river, 300000, LocalDate.of(2026, 6, 1), LocalDate.of(2027, 6, 1), true);
            fail("Tenia que rechazar el segundo sponsor principal (RN23)");
        } catch (ReglaNegocioException e) {
            assertEquals(1, river.getPatrocinios().size());
            assertEquals(0, nike.getPatrocinios().size());
        }
    }

    @Test
    void noPermiteMontoCero() {
        try {
            adidas.patrocinarClub(river, 0, inicio2026, fin2026, false);
            fail("Tenia que rechazar el monto 0");
        } catch (ReglaNegocioException e) {
            assertEquals(0, adidas.getPatrocinios().size());
        }
    }

    @Test
    void noPermiteFechaDeFinAnteriorALaDeInicio() {
        try {
            adidas.patrocinarClub(river, 1000000, fin2026, inicio2026, false);
            fail("Tenia que rechazar las fechas al reves");
        } catch (ReglaNegocioException e) {
            assertEquals(0, adidas.getPatrocinios().size());
        }
    }

    @Test
    void noPermitePatrocinarSinClub() {
        try {
            adidas.patrocinarClub(null, 1000000, inicio2026, fin2026, false);
            fail("Tenia que rechazar el club vacio");
        } catch (ReglaNegocioException e) {
            assertEquals(0, adidas.getPatrocinios().size());
        }
    }
}
