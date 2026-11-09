package notificaciones;

// Canal "app móvil". El envío real queda simulado por consola.
public class AppMovil implements CanalNotificacion {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean enviar(Notificacion notificacion, Suscriptor suscriptor) {
        System.out.println("[App móvil] Para " + suscriptor.getNombre() + ": " + notificacion);
        return true;
    }

    // Dos instancias del mismo canal son equivalentes: así un Set no guarda el canal repetido.
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
        return "App móvil";
    }
}
