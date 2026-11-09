package notificaciones;

// Canal "SMS". El envío real queda simulado por consola.
public class Sms implements CanalNotificacion {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean enviar(Notificacion notificacion, Suscriptor suscriptor) {
        System.out.println("[SMS] Al " + suscriptor.getTelefono() + ": " + notificacion);
        return true;
    }

    @Override
    public boolean equals(Object otro) {
        return otro != null && getClass() == otro.getClass();
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "SMS";
    }
}
