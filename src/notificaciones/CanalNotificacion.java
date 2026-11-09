package notificaciones;

import java.io.Serializable;

// Medio por el cual se avisa un hecho del partido (RN21). Un canal nuevo es una clase nueva
// que implementa esta interfaz, sin modificar ServicioNotificaciones (SOLID: Abierto/Cerrado
// e Inversión de Dependencias).
public interface CanalNotificacion extends Serializable {

    // Devuelve true si el aviso se envió; false si el canal no pudo enviarlo.
    boolean enviar(Notificacion notificacion, Suscriptor suscriptor);
}
