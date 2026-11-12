package modelo;

import excepcion.ReglaNegocioException;

import java.time.LocalDate;

public class Entrenador extends Persona {
    private static final long serialVersionUID = 1L;
    private String especialidad;
    private String licencia;

    public Entrenador(String dni, String nombre, String apellido, LocalDate fechaNacimiento, String especialidad, String licencia) {
        super(dni, nombre, apellido, fechaNacimiento);
        this.especialidad = especialidad;
        this.licencia = licencia;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setLicencia(String licencia) throws ReglaNegocioException {
        if (licencia == null || licencia.trim().isEmpty()) {
            throw new ReglaNegocioException("La licencia no puede estar vacia");
        }
        this.licencia = licencia;
    }

    public String getLicencia() {
        return licencia;
    }
}