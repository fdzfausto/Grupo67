package modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Club implements Serializable {
    private static final long serialVersionUID = 1L;
    private String nombre;
    private LocalDate fechaFundacion;
    private String sedeSocial;
    private Entrenador entrenador;// agregacion: el entrenador sigue existiendo aunque no tenga club
    private List<Patrocinio> patrocinios; // asociacion con patrocinio

    public Club(String nombre, LocalDate fechaFundacion, String sedeSocial) {
        this.nombre = nombre;
        this.fechaFundacion = fechaFundacion;
        this.sedeSocial = sedeSocial;
        this.patrocinios = new ArrayList<>();
    }

    public String getNombre() {
        return nombre;
    }

    public LocalDate getFechaFundacion() {
        return fechaFundacion;
    }

    public String getSedeSocial() {
        return sedeSocial;
    }

    public Entrenador getEntrenador() {
        return entrenador;
    }

    public void asignarEntrenador(Entrenador entrenador) {
        this.entrenador = entrenador;
    }

    void agregarPatrocinio(Patrocinio patrocinio) {
        patrocinios.add(patrocinio);
    }

    public boolean tienePrincipalEntre(LocalDate inicio, LocalDate fin) {
        for (int i = 0; i < patrocinios.size(); i++) {
            Patrocinio p = patrocinios.get(i);
            if (p.isPrincipal() && p.seSuperponeCon(inicio, fin)){
                return true;
            }
        }
        return false;
    }

    public Sponsor getSponsorPrincipalVigente(){
        for (int i = 0; i<patrocinios.size(); i++){
            Patrocinio p = patrocinios.get(i);
            if (p.isPrincipal() && p.estaVigente()){
                return p.getSponsor();
            }
        }
        return null;
    }

    public List<Patrocinio> getPatrocinios() {
        return new ArrayList<>(patrocinios);
    }

    @Override
    public String toString() {
        return nombre;
    }
}

