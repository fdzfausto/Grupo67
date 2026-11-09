package modelo;

import excepcion.ReglaNegocioException;

import java.io.Serializable;
import java.time.LocalDate;

public class Patrocinio implements Serializable {
    private static final long serialVersionUID = 1L;
    private Sponsor sponsor;
    private Club club;
    private Torneo torneo;
    private double monto;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private boolean principal;

    Patrocinio(Sponsor sponsor, Club club, Torneo torneo, double monto, LocalDate fechaInicio, LocalDate fechaFin, boolean principal) throws ReglaNegocioException {
        if (club == null && torneo == null) {
            throw new ReglaNegocioException("El patrocinio necesita un club o un torneo");
        }
        if (club != null && torneo != null) {
            throw new ReglaNegocioException("El patrocinio es para un club o para un torneo, no para los dos");
        }
        if (monto <= 0) {
            throw new ReglaNegocioException("El monto debe ser mayor que 0");
        }
        if (fechaInicio == null || fechaFin == null) {
            throw new ReglaNegocioException("Faltan las fechas del patrocinio");
        }
        if (principal && club == null) {
            throw new ReglaNegocioException("Solo un club puede tener sponsor principal");
        }
        this.sponsor = sponsor;
        this.club = club;
        this.torneo = torneo;
        this.monto = monto;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.principal = principal;
    }

    public Sponsor getSponsor() {
        return sponsor;
    }

    public Club getClub() {
        return club;
    }

    public Torneo getTorneo() {
        return torneo;
    }

    public double getMonto() {
        return monto;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public boolean isPrincipal() {
        return principal;
    }

    // Vigente = hoy esta entre la fecha de inicio y la de fin (incluidas)
    public boolean estaVigente() {
        LocalDate hoy = LocalDate.now();
        return !hoy.isBefore(fechaInicio) && !hoy.isAfter(fechaFin);
    }

    public boolean seSuperponeCon(LocalDate inicio, LocalDate fin) {
        return !inicio.isAfter(fechaFin) && !fin.isBefore(inicio);
    }

    @Override
    public String toString() {
        String destinatario;
        if (club != null) {
            destinatario = club.toString();
        } else {
            destinatario = torneo.toString();
        }
        String tipo = "";
        if (principal){
            tipo = " [principal]";
        }
        return sponsor.getNombre() + " -> " + destinatario + tipo + " $" + monto + " (" + fechaInicio + " - " + fechaFin + ")";
    }
}
