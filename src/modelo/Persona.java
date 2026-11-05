package modelo;

import java.io.Serializable;
import java.time.LocalDate;

public abstract class Persona implements Serializable {
    private static final long serialVersionUID = 1L;
    private String dni;
    private String nombre;
    private String apellido;
    private LocalDate fechaNacimiento;

    protected Persona(String dni, String nombre, String apellido, LocalDate fechaNacimiento) {
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getDni() {
        return dni;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    @Override
    public String toString() {
        return nombre + " " + apellido;
    }
}
