package modelo;

import excepcion.ReglaNegocioException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Datos de apoyo para las pruebas: clubes, planteles, nóminas, árbitros y ternas.
class DatosPrueba {

    static Club club(String nombre) {
        return new Club(nombre, LocalDate.of(1905, 4, 3), "Sede de " + nombre);
    }

    // Plantel de 20 jugadores; "base" asegura DNIs distintos entre clubes.
    static List<Jugador> plantel(int base) {
        List<Jugador> plantel = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            String dni = String.valueOf(base * 100 + i);
            plantel.add(new Jugador(dni, "Jugador" + dni, "Apellido" + dni,
                    LocalDate.of(1998, 1, 1), "Delantero", i + 1, 1000000, 0));
        }
        return plantel;
    }

    // 11 titulares (índices 0 a 10) y 5 suplentes (índices 11 a 15).
    static Nomina nomina(Club club, List<Jugador> plantel) throws ReglaNegocioException {
        Nomina nomina = new Nomina(club);
        for (int i = 0; i < 11; i++) {
            nomina.agregarTitular(plantel.get(i));
        }
        for (int i = 11; i < 16; i++) {
            nomina.agregarSuplente(plantel.get(i));
        }
        return nomina;
    }

    static Arbitro arbitro(int numero, String categoria) {
        return new Arbitro("9" + numero, "Arbitro" + numero, "Apellido" + numero,
                LocalDate.of(1980, 6, 15), categoria, "Colegio Nacional");
    }

    static TernaArbitral terna(int base, String categoria) throws ReglaNegocioException {
        return new TernaArbitral(arbitro(base, categoria), arbitro(base + 1, categoria), arbitro(base + 2, categoria));
    }

    static Estadio estadio(String nombre) {
        return new Estadio(nombre, 40000, "Av. Principal 100, Buenos Aires", "Césped natural");
    }
}
