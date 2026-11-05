package modelo;

import java.io.Serializable;
import java.time.LocalDate;

public class Contrato implements Serializable {
    private static final long serialVersionUID = 1L;
    private Club club; // asociacion con Club
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    Contrato(Club club, LocalDate fechaInicio) {
        this.club = club;
        this.fechaInicio = fechaInicio;
        this.fechaFin = null;
    }

    public Club getClub() {
        return club;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public boolean estaVigente() {
        return fechaFin == null;
    }

    public void finalizar(LocalDate fecha){
        this.fechaFin = fecha;
    }

    @Override
    public String toString() {
        String hasta;
        if (estaVigente()) {
            hasta = "Vigente";
        } else  {
            hasta = fechaFin.toString();
        }
        return club + " (" + fechaInicio + " - " + hasta + ")";
    }
}
