package modelo;

import java.time.LocalDate;

public class Entrenador extends Persona {
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

    public String getLicencia() {
        return licencia;
    }
}
