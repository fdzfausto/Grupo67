package modelo;

import excepcion.ReglaNegocioException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Jugador extends Persona {
    private String posicion;
    private int numeroCamiseta;
    private double valorMercado;
    private int partidosJugados;
    private List<Contrato> contratos = new ArrayList<>();

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

    // Contratos de los jugadores
    public Contrato registrarContrato(Club club, LocalDate fechaInicio) throws ReglaNegocioException {
        if (getContratoVigente() != null){
            throw new ReglaNegocioException("Ya tiene contrato con " + getClubActual());
        }
        Contrato contrato = new Contrato(club, fechaInicio);
        contratos.add(contrato);
        return contrato;
    }

    public void rescindirContrato(LocalDate fecha) throws ReglaNegocioException {
        Contrato vigente = getContratoVigente();
        if (vigente == null){
            throw new ReglaNegocioException("No tiene contrato");
        }
        vigente.finalizar(fecha);
    }

    public Contrato transferir(Club nuevoClub,LocalDate fecha) throws ReglaNegocioException {
        rescindirContrato(fecha);
        return registrarContrato(nuevoClub, fecha);
    }

    public Contrato getContratoVigente() {
        for (int i = 0; i < contratos.size(); i++){
            Contrato c = contratos.get(i);
            if (c.estaVigente()){
                return c;
            }
        }
        return null;
    }

    public Club getClubActual() {
        Contrato vigente = getContratoVigente();
        if (vigente == null){
            return null;
        } else {
            return vigente.getClub();
        }
    }

    public List<Contrato> getContratos() {
        return new ArrayList<>(contratos);
    }

}
