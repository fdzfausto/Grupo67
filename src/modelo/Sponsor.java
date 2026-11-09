package modelo;

import excepcion.ReglaNegocioException;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Sponsor implements Serializable {
    private static final long serialVersionUID = 1L;
    private String nombre;
    private String marca;
    private String logo;
    private List<Patrocinio> patrocinios;

    public Sponsor(String nombre, String marca, String logo) {
        this.nombre = nombre;
        this.marca = marca;
        this.logo = logo;
        this.patrocinios = new ArrayList<>();
    }

    public String getNombre() {
        return nombre;
    }

    public String getMarca() {
        return marca;
    }

    public String getLogo() {
        return logo;
    }

    // Patrocinar un club
    public Patrocinio patrocinarClub(Club club, double monto, LocalDate fechaInicio, LocalDate fechaFin, boolean principal) throws ReglaNegocioException{
        if (club == null){
            throw new ReglaNegocioException("No hay club para patrocinar");
        }
        Patrocinio nuevo = new Patrocinio(this, club, null, monto, fechaInicio, fechaFin, principal);
        if (yaPatrocinaEnEsasFechas(nuevo)){
            throw new ReglaNegocioException(nombre + " ya patrocina a " + club + " en esas fechas");
        }
        if (principal && club.tienePrincipalEntre(fechaInicio, fechaFin)){
            throw new ReglaNegocioException(club + " ya tiene un sponsor principal en esas fechas");
        }
        patrocinios.add(nuevo);
        club.agregarPatrocinio(nuevo);
        return nuevo;
    }

    public Patrocinio patrocinarTorneo(Torneo torneo, double monto, LocalDate fechaInicio, LocalDate fechaFin) throws ReglaNegocioException{
        if (torneo == null){
            throw new ReglaNegocioException("No hay torneo");
        }
        Patrocinio nuevo = new Patrocinio (this, null, torneo, monto, fechaInicio, fechaFin, false);
        if (yaPatrocinaEnEsasFechas(nuevo)){
            throw new ReglaNegocioException(nombre + " ya patrocina el torneo " + torneo + " en esas fechas");
        }
        patrocinios.add(nuevo);
        return nuevo;
    }

    private boolean yaPatrocinaEnEsasFechas(Patrocinio nuevo){
        for (int i  = 0; i < patrocinios.size(); i++){
            Patrocinio p = patrocinios.get(i);
            boolean mismoDestinatario;
            if (nuevo.getClub() != null){
                mismoDestinatario = p.getClub() == nuevo.getClub();
            } else  {
                mismoDestinatario = p.getTorneo() == nuevo.getTorneo();
            }
            if (mismoDestinatario && p.seSuperponeCon(nuevo.getFechaInicio(), nuevo.getFechaFin())){
                return true;
            }
        }
        return false;
    }

    public List<Patrocinio> getPatrocinios() {
        return new ArrayList<>(patrocinios);
    }

    @Override
    public String toString() {
        return nombre + " (" + marca + ")";
    }
}
