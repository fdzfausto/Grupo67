package notificaciones;

// Canal "correo para prensa". El envío real queda simulado por consola.
public class CorreoPrensa implements CanalNotificacion {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean enviar(Notificacion notificacion, Suscriptor suscriptor) {
        System.out.println("[Correo] A " + suscriptor.getEmail() + ": " + notificacion);
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
        return "Correo prensa";
    }
}
