package notificaciones;

import eventos.Evento;
import excepcion.ReglaNegocioException;
import modelo.Partido;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Aviso de un hecho del partido: indica partido, minuto y hecho (RN20).
public class Notificacion implements Serializable {
    private static final long serialVersionUID = 1L;

    private Partido partido;
    private int minuto;
    private String hecho;
    private List<EnvioNotificacion> envios; // composición: cada envío pertenece a su notificación

    public Notificacion(Partido partido, int minuto, String hecho) throws ReglaNegocioException {
        if (partido == null) {
            throw new ReglaNegocioException("La notificación necesita un partido");
        }
        if (minuto < 0 || minuto > Evento.MINUTO_MAXIMO) {
            throw new ReglaNegocioException("El minuto debe estar entre 0 y " + Evento.MINUTO_MAXIMO);
        }
        if (hecho == null || hecho.isBlank()) {
            throw new ReglaNegocioException("La notificación debe indicar el hecho");
        }
        this.partido = partido;
        this.minuto = minuto;
        this.hecho = hecho;
        this.envios = new ArrayList<>();
    }

    // Creador: la notificación contiene a sus envíos, así que ella los crea (CU07/CU09, RN21).
    public EnvioNotificacion registrarEnvio(CanalNotificacion canal, Suscriptor suscriptor,
                                            boolean exitoso, String mensajeError) {
        EnvioNotificacion envio = new EnvioNotificacion(canal, suscriptor, exitoso, mensajeError);
        envios.add(envio);
        return envio;
    }

    public Partido getPartido() {
        return partido;
    }

    public int getMinuto() {
        return minuto;
    }

    public String getHecho() {
        return hecho;
    }

    public List<EnvioNotificacion> getEnvios() {
        return new ArrayList<>(envios);
    }

    @Override
    public String toString() {
        return partido + " - min " + minuto + ": " + hecho;
    }
}
