package consola;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

// Entrada y salida por consola: lee datos del usuario y repite la pregunta hasta que el dato es válido.
public class Consola {
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Scanner scanner;

    public Consola() {
        this.scanner = new Scanner(System.in);
    }

    public void mostrar(String texto) {
        System.out.println(texto);
    }

    public void titulo(String texto) {
        System.out.println();
        System.out.println("=== " + texto + " ===");
    }

    public String leerTexto(String pregunta) {
        System.out.print(pregunta + ": ");
        if (!scanner.hasNextLine()) {
            throw new IllegalStateException("Se cerró la entrada de datos");
        }
        return scanner.nextLine().trim();
    }

    public int leerEntero(String pregunta) {
        while (true) {
            String texto = leerTexto(pregunta);
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                mostrar("Ingresá un número entero.");
            }
        }
    }

    public double leerDecimal(String pregunta) {
        while (true) {
            String texto = leerTexto(pregunta).replace(',', '.');
            try {
                return Double.parseDouble(texto);
            } catch (NumberFormatException e) {
                mostrar("Ingresá un número (por ejemplo 150000.50).");
            }
        }
    }

    public LocalDate leerFecha(String pregunta) {
        while (true) {
            String texto = leerTexto(pregunta + " (aaaa-mm-dd)");
            try {
                return LocalDate.parse(texto);
            } catch (DateTimeParseException e) {
                mostrar("Fecha inválida. Formato: aaaa-mm-dd, por ejemplo 2026-05-10.");
            }
        }
    }

    public LocalDateTime leerFechaHora(String pregunta) {
        while (true) {
            String texto = leerTexto(pregunta + " (aaaa-mm-dd hh:mm)");
            try {
                return LocalDateTime.parse(texto, FORMATO_FECHA_HORA);
            } catch (DateTimeParseException e) {
                mostrar("Fecha y hora inválidas. Formato: aaaa-mm-dd hh:mm, por ejemplo 2026-05-10 18:00.");
            }
        }
    }

    public boolean confirmar(String pregunta) {
        String respuesta = leerTexto(pregunta + " (s/n)");
        return respuesta.equalsIgnoreCase("s");
    }

    // Muestra la lista numerada y devuelve el elemento elegido, o null si no hay nada o el usuario cancela (0).
    public <T> T elegir(String titulo, List<T> opciones) {
        if (opciones.isEmpty()) {
            mostrar("No hay opciones disponibles para: " + titulo);
            return null;
        }
        mostrar(titulo);
        for (int i = 0; i < opciones.size(); i++) {
            mostrar("  " + (i + 1) + ") " + opciones.get(i));
        }
        mostrar("  0) Cancelar");
        while (true) {
            int numero = leerEntero("Opción");
            if (numero == 0) {
                return null;
            }
            if (numero >= 1 && numero <= opciones.size()) {
                return opciones.get(numero - 1);
            }
            mostrar("Elegí un número entre 0 y " + opciones.size() + ".");
        }
    }

    // Pide varios números separados por coma (por ejemplo 1,3,5) y devuelve los elementos elegidos.
    public <T> java.util.ArrayList<T> elegirVarios(String titulo, List<T> opciones) {
        java.util.ArrayList<T> elegidos = new java.util.ArrayList<>();
        mostrar(titulo);
        for (int i = 0; i < opciones.size(); i++) {
            mostrar("  " + (i + 1) + ") " + opciones.get(i));
        }
        while (true) {
            String texto = leerTexto("Números separados por coma (vacío para ninguno)");
            elegidos.clear();
            boolean valido = true;
            if (!texto.isEmpty()) {
                for (String parte : texto.split(",")) {
                    try {
                        int numero = Integer.parseInt(parte.trim());
                        if (numero < 1 || numero > opciones.size() || elegidos.contains(opciones.get(numero - 1))) {
                            valido = false;
                        } else {
                            elegidos.add(opciones.get(numero - 1));
                        }
                    } catch (NumberFormatException e) {
                        valido = false;
                    }
                }
            }
            if (valido) {
                return elegidos;
            }
            mostrar("Entrada inválida: usá números de la lista, sin repetir, separados por coma.");
        }
    }

    public void cerrar() {
        scanner.close();
    }
}
