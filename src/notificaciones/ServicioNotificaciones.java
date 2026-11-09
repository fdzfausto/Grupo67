package notificaciones;

import java.util.List;

// Envía cada notificación a los suscriptores por los canales que eligieron (CU09).
// Trabaja solo con la interfaz CanalNotificacion, nunca con un canal concreto (SOLID: Inversión
// de Dependencias), así que un canal nuevo se agrega sin modificar esta clase (RN21).
public class ServicioNotificaciones {

    public void notificar(Notificacion notificacion, List<Suscriptor> suscriptores) {
        if (notificacion == null || suscriptores == null) {
            throw new IllegalArgumentException("Se necesitan la notificación y la lista de suscriptores");
        }
        for (Suscriptor suscriptor : suscriptores) {
            for (CanalNotificacion canal : suscriptor.getCanalesHabilitados()) {
                enviarPorCanal(notificacion, suscriptor, canal);
            }
        }
    }

    // RN21: si un canal falla se registra el error y se sigue con los demás, sin frenar la carga del partido.
    private void enviarPorCanal(Notificacion notificacion, Suscriptor suscriptor, CanalNotificacion canal) {
        try {
            if (canal.enviar(notificacion, suscriptor)) {
                notificacion.registrarEnvio(canal, suscriptor, true, null);
            } else {
                notificacion.registrarEnvio(canal, suscriptor, false, "El canal no pudo entregar el aviso");
            }
        } catch (RuntimeException e) {
            String motivo = e.getMessage();
            if (motivo == null) {
                motivo = e.toString();
            }
            notificacion.registrarEnvio(canal, suscriptor, false, motivo);
        }
    }
}
