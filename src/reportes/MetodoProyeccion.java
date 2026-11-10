package reportes;

import modelo.Club;
import modelo.Torneo;

public interface MetodoProyeccion {
    double calcular(Torneo torneo, Club club); // devuelve la probabilidad de que el club clasifique
}
