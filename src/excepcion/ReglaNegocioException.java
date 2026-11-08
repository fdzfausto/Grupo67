package excepcion;

public class ReglaNegocioException extends Exception {
    private static final long serialVersionUID = 1L;

    // cada vez que se viole un RN, aparece un mensaje
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
