package reportes;

import modelo.EstadoPartido;
import modelo.Partido;

import java.util.Comparator;
import java.util.List;

public class ComparadorTabla implements Comparator<FilaTabla> {
    private List<Partido> partidos;

    public ComparadorTabla(List<Partido> partidos) {
        this.partidos = partidos;
    }

    @Override
    public int compare(FilaTabla a, FilaTabla b) {
        int resultado = a.compareTo(b);
        if (resultado != 0) {
            return resultado;
        }
        int puntosA = 0;
        int puntosB = 0;
        for (int i = 0; i < partidos.size(); i++) {
            Partido p = partidos.get(i);
            if (p.getEstado() == EstadoPartido.FINALIZADO && p.participa(a.getClub()) && p.participa(b.getClub())) {
                int golesA = p.getGoles(a.getClub());
                int golesB = p.getGoles(b.getClub());
                if (golesA > golesB) {
                    puntosA = puntosA + 3;
                } else if (golesA == golesB) {
                    puntosA = puntosA + 1;
                    puntosB = puntosB + 1;
                } else {
                    puntosB = puntosB + 3;
                }
            }
        }
        if (puntosA != puntosB) {
            return puntosB - puntosA;
        }
        // S06: si siguen empatados (o no jugaron entre ellos), por orden alfabetico
        return a.getClub().getNombre().compareTo(b.getClub().getNombre());

    }

}
