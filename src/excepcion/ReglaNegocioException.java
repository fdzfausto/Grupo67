package excepcion;

public class ReglaNegocioException extends Exception {
    // cada vez que se viole un RN, aparece un mensaje
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
