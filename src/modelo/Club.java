package modelo;

import java.io.Serializable;
import java.time.LocalDate;

public class Club implements Serializable {
    private static final long serialVersionUID = 1L;
    private String nombre;
    private LocalDate fechaFundacion;
    private String sedeSocial;
    private Entrenador entrenador; // agregacion: el entrenador sigue existiendo aunque no tenga club

    public Club(String nombre, LocalDate fechaFundacion, String sedeSocial) {
        this.nombre = nombre;
        this.fechaFundacion = fechaFundacion;
        this.sedeSocial = sedeSocial;
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

    @Override
    public String toString() {
        return nombre;
    }
}

