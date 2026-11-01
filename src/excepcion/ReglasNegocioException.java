package excepcion;

public class ReglasNegocioException extends Exception {
    // cada vez que se viole un RN, aparece un mensaje
    public ReglasNegocioException(String mensaje) {
        super(mensaje);
    }
}
