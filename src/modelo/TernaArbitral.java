package modelo;

import excepcion.ReglaNegocioException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

// Un árbitro principal y dos asistentes (RN10).
public class TernaArbitral implements Serializable {
    private static final long serialVersionUID = 1L;

    private Arbitro principal;
    private List<Arbitro> asistentes;

    public TernaArbitral(Arbitro principal, Arbitro asistente1, Arbitro asistente2) throws ReglaNegocioException {
        this.principal = principal;
        this.asistentes = new ArrayList<>();
        asistentes.add(asistente1);
        asistentes.add(asistente2);
        if (!validarTerna()) {
            throw new ReglaNegocioException("La terna debe tener un principal y dos asistentes distintos");
        }
    }

    // final: se usa en el constructor, así que una subclase no debe poder cambiarlo.
    public final boolean validarTerna() {
        List<Arbitro> arbitros = getArbitros();
        if (arbitros.contains(null)) {
            return false;
        }
        // Si hay un árbitro repetido, el conjunto queda con menos de 3 elementos.
        return asistentes.size() == 2 && new HashSet<>(arbitros).size() == 3;
    }

    public Arbitro getPrincipal() {
        return principal;
    }

    public List<Arbitro> getAsistentes() {
        return new ArrayList<>(asistentes);
    }

    // Los tres árbitros juntos, útil para validar disponibilidad (CU14).
    public final List<Arbitro> getArbitros() {
        List<Arbitro> arbitros = new ArrayList<>();
        arbitros.add(principal);
        arbitros.addAll(asistentes);
        return arbitros;
    }

    @Override
    public String toString() {
        return "Principal: " + principal + " - Asistentes: " + asistentes;
    }
}
