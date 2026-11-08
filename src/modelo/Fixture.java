package modelo;

import excepcion.ReglaNegocioException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Organiza los partidos de un torneo: todos contra todos, una sola rueda (S03).
// Los partidos se crean SIN_PROGRAMAR: estadio, hora y terna se asignan después.
public class Fixture implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<List<Partido>> fechas; // cada elemento es una fecha del torneo

    // Constructor de paquete: solo Torneo crea el fixture (GRASP Creador).
    Fixture(List<Club> clubes) throws ReglaNegocioException {
        this.fechas = new ArrayList<>();
        generar(clubes);
    }

    // Método del círculo: un club queda fijo y los demás rotan en cada fecha.
    // Si la cantidad es impar se agrega un "libre" (null) y ese club descansa esa fecha.
    private void generar(List<Club> clubes) throws ReglaNegocioException {
        List<Club> rotacion = new ArrayList<>(clubes);
        if (rotacion.size() % 2 != 0) {
            rotacion.add(null);
        }
        int n = rotacion.size();
        for (int ronda = 0; ronda < n - 1; ronda++) {
            List<Partido> fecha = new ArrayList<>();
            for (int i = 0; i < n / 2; i++) {
                Club a = rotacion.get(i);
                Club b = rotacion.get(n - 1 - i);
                if (a == null || b == null) {
                    continue; // el club emparejado con el libre no juega esta fecha
                }
                // Se alterna la localía para que no siempre sea local el mismo club.
                if ((ronda + i) % 2 == 0) {
                    fecha.add(new Partido(a, b));
                } else {
                    fecha.add(new Partido(b, a));
                }
            }
            fechas.add(fecha);
            rotacion.add(1, rotacion.remove(n - 1)); // rota todos menos el primero
        }
    }

    public List<List<Partido>> getFechas() {
        List<List<Partido>> copia = new ArrayList<>();
        for (List<Partido> fecha : fechas) {
            copia.add(new ArrayList<>(fecha));
        }
        return copia;
    }

    public List<Partido> getPartidos() {
        List<Partido> partidos = new ArrayList<>();
        for (List<Partido> fecha : fechas) {
            partidos.addAll(fecha);
        }
        return partidos;
    }

    @Override
    public String toString() {
        return "Fixture de " + fechas.size() + " fechas y " + getPartidos().size() + " partidos";
    }
}
