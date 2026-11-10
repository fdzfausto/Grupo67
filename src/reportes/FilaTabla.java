package reportes;

import modelo.Club;

public class FilaTabla  implements Comparable<FilaTabla> {
    private Club club;
    private int jugados;
    private int ganados;
    private int empatados;
    private int perdidos;
    private int golesAFavor;
    private int golesEnContra;

    public FilaTabla(Club club) {
        this.club = club;
    }

    public void registrarResultado(int golesPropios, int golesRival) {
        jugados++;
        golesAFavor = golesAFavor + golesPropios;
        golesEnContra = golesEnContra + golesRival;
        if (golesPropios > golesRival) {
            ganados++;
        } else if (golesPropios == golesRival) {
            empatados++;
        } else {
            perdidos++;
        }
    }

    public int getPuntos(){
        return ganados * 3 + empatados;
    }

    public int getDiferenciaGol(){
        return golesAFavor - golesEnContra;
    }

    public Club getClub() {
        return club;
    }

    public int getJugados() {
        return jugados;
    }

    public int getGanados() {
        return ganados;
    }

    public int getEmpatados() {
        return empatados;
    }

    public int getPerdidos() {
        return perdidos;
    }

    public int getGolesAFavor() {
        return golesAFavor;
    }

    public int getGolesEnContra() {
        return golesEnContra;
    }

    @Override
    public int compareTo(FilaTabla otra) {
        if (getPuntos() != otra.getPuntos()) {
            return otra.getPuntos() - getPuntos();
        }
        if (getDiferenciaGol() != otra.getDiferenciaGol()) {
            return otra.getDiferenciaGol() - getDiferenciaGol();
        }
        return otra.getGolesAFavor() - getGolesAFavor();
    }
    @Override
    public String toString() {
        return club + " | PJ " + jugados + " | G " + ganados + " | E " + empatados + " | P " + perdidos + " | GF " + golesAFavor + " | GC " + golesEnContra + " | DG " + getDiferenciaGol() + " | Pts " + getPuntos();
    }
}
