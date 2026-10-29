package modelo;

import java.time.LocalDate;

public class Jugador extends Persona {
    private String posicion;
    private int numeroCamiseta;
    private double valorMercado;
    private int partidosJugados;

    public Jugador (String dni, String nombre, String apellido, LocalDate fechaNacimiento, String posicion, int numeroCamiseta, double valorMercado, int partidosJugados) {
        super(dni, nombre, apellido, fechaNacimiento);
        this.posicion = posicion;
        this.numeroCamiseta = numeroCamiseta;
        this.valorMercado = valorMercado;
        this.partidosJugados = partidosJugados;
    }

    public String getPosicion() {
        return posicion;
    }

    public int getNumeroCamiseta() {
        return numeroCamiseta;
    }

    public void setNumeroCamiseta(int numeroCamiseta) {
        this.numeroCamiseta = numeroCamiseta;
    }

    public double getValorMercado() {
        return valorMercado;
    }

    public void setValorMercado(double valorMercado) {
        this.valorMercado = valorMercado;
    }

    public int getPartidosJugados() {
        return partidosJugados;
    }

    public void sumarPartidoJugado(){
        this.partidosJugados++;
    }

    public String toString() {
        return super.toString() + " (Numero de camiseta: " + numeroCamiseta + " - " + posicion + " - " + ")";
    }
}
