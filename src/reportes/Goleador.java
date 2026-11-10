package reportes;

import modelo.Club;
import modelo.Jugador;

public class Goleador implements Comparable<Goleador>{
    private Jugador jugador;
    private Club club;
    private int goles;

    public Goleador(Jugador jugador, Club club){
        this.jugador = jugador;
        this.club = club;
        this.goles = 0;
    }

    public void sumarGol(){
        goles++;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public Club getClub() {
        return club;
    }

    public int getGoles() {
        return goles;
    }

    @Override
    public int compareTo(Goleador otro) {
        return otro.getGoles() - goles;
    }

    @Override
    public String toString() {
        return jugador + " (" +  club + ") - " + goles + " goles" ;
    }
}
