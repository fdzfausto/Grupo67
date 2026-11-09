package modelo;

import java.io.Serializable;

public class Estadio implements Serializable {
    private static final long serialVersionUID = 1L;
    private String nombre;
    private int capacidad;
    private String direccion;
    private String clima;
    private String terreno;

    public Estadio(String nombre, int capacidad, String direccion, String clima) {
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.direccion = direccion;
        this.clima = clima;
        this.terreno = terreno;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getClima() {
        return clima;
    }

    public String getTerreno() {
        return terreno;
    }

    @Override
    public String toString() {
        return nombre + "( " + capacidad + " personas)" ;
    }
}
