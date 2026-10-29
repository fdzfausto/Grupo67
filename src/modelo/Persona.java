package modelo;

import java.time.LocalDate;
import java.util.Locale;

public class Persona {
    private String dni;
    private String nombre;
    private String apellido;
    private LocalDate fechaNacimiento;

    public Persona(String dni, String nombre, String apellido, LocalDate fechaNacimiento) {
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

    public String toString() {
        return nombre + " " + apellido;
    }
}
