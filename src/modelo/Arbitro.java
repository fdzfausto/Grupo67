package modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Arbitro extends Persona { // herencia de Persona
    private String categoria;
    private String colegio;
    private List<Club> clubesHincha; // asociacion con Club

    public Arbitro(String dni, String nombre, String apellido, LocalDate fechaNacimiento, String categoria, String colegio) {
        super(dni, nombre, apellido, fechaNacimiento);
        this.categoria = categoria;
        this.colegio = colegio;
        this.clubesHincha = new ArrayList<>();
    }

    public String getCategoria() {
        return categoria;
    }

    public String getColegio() {
        return colegio;
    }

    public void agregarClubHincha(Club club) {
        if (!clubesHincha.contains(club)) {
            clubesHincha.add(club);
        }
    }

    public boolean esHinchaDe (Club club) {
        return clubesHincha.contains(club);
    }

    public List<Club> getClubesHincha() {
        return new ArrayList<>(clubesHincha);
    }
}
