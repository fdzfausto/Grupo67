package modelo;

import excepcion.ReglaNegocioException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Arbitro extends Persona { // herencia de Persona
    private static final long serialVersionUID = 1L;
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

    public void setCategoria(String categoria) throws ReglaNegocioException {
        if (categoria == null || categoria.trim().isEmpty()) {
            throw new ReglaNegocioException("La categoria no puede estar vacia");
        }
        this.categoria = categoria;
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