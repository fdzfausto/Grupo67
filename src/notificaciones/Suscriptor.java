package notificaciones;

import excepcion.ReglaNegocioException;
import modelo.Club;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

// Persona que sigue clubes y elige por qué canales recibe los avisos (CU10).
public class Suscriptor implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nombre;
    private String email;
    private String telefono;
    private Set<CanalNotificacion> canalesHabilitados;
    private Set<Club> clubesSiguiendo;

    // Equivale a registrarDatos(nombre, email, telefono) del diagrama de secuencia CU10.
    public Suscriptor(String nombre, String email, String telefono) throws ReglaNegocioException {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException("El suscriptor necesita un nombre");
        }
        if (email == null || !email.contains("@")) {
            throw new ReglaNegocioException("El suscriptor necesita un email válido");
        }
        if (telefono == null || telefono.isBlank()) {
            throw new ReglaNegocioException("El suscriptor necesita un teléfono");
        }
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.canalesHabilitados = new LinkedHashSet<>();
        this.clubesSiguiendo = new LinkedHashSet<>();
    }

    // CU10: por cada club elegido; si ya lo seguía se rechaza con el mensaje del diagrama de secuencia.
    public void seguir(Club club) throws ReglaNegocioException {
        if (club == null) {
            throw new ReglaNegocioException("Se debe indicar el club a seguir");
        }
        if (sigueA(club)) {
            throw new ReglaNegocioException("Ya seguías este club");
        }
        clubesSiguiendo.add(club);
    }

    // CU10: hay que elegir al menos un canal; si no, se pide de nuevo.
    public void configurarCanales(Set<CanalNotificacion> canales) throws ReglaNegocioException {
        if (!validarCanales(canales)) {
            throw new ReglaNegocioException("Se debe elegir al menos un canal");
        }
        this.canalesHabilitados = new LinkedHashSet<>(canales);
    }

    public boolean validarCanales(Set<CanalNotificacion> canales) {
        return canales != null && !canales.isEmpty() && !canales.contains(null);
    }

    // Experto: el suscriptor tiene su lista de clubes, así que él responde si sigue a uno.
    public boolean sigueA(Club club) {
        return clubesSiguiendo.contains(club);
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }

    public Set<CanalNotificacion> getCanalesHabilitados() {
        return new LinkedHashSet<>(canalesHabilitados);
    }

    public Set<Club> getClubesSiguiendo() {
        return new LinkedHashSet<>(clubesSiguiendo);
    }

    @Override
    public String toString() {
        return nombre + " (" + email + ")";
    }
}
