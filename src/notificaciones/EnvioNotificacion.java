package notificaciones;

import java.io.Serializable;

// Registro de un intento de envío: por qué canal, a quién y si salió bien (RN21).
public class EnvioNotificacion implements Serializable {
    private static final long serialVersionUID = 1L;

    private CanalNotificacion canal;
    private Suscriptor suscriptor;
    private boolean exitoso;
    private String mensajeError; // null si el envío fue exitoso

    // Constructor de paquete: los envíos los crea la Notificacion que los contiene (GRASP Creador).
    EnvioNotificacion(CanalNotificacion canal, Suscriptor suscriptor, boolean exitoso, String mensajeError) {
        this.canal = canal;
        this.suscriptor = suscriptor;
        this.exitoso = exitoso;
        this.mensajeError = mensajeError;
    }

    public CanalNotificacion getCanal() {
        return canal;
    }

    public Suscriptor getSuscriptor() {
        return suscriptor;
    }

    public boolean isExitoso() {
        return exitoso;
    }

    public String getMensajeError() {
        return mensajeError;
    }

    @Override
    public String toString() {
        String resultado;
        if (exitoso) {
            resultado = "enviado";
        } else {
            resultado = "falló (" + mensajeError + ")";
        }
        return canal + " -> " + suscriptor.getNombre() + ": " + resultado;
    }
}
