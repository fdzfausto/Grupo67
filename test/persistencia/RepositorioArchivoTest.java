package persistencia;

import consola.Datos;
import excepcion.PersistenciaException;
import excepcion.ReglaNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

// Entrada/salida (sección 7 de la consigna): los datos se conservan entre ejecuciones.
class RepositorioArchivoTest {

    @TempDir
    Path carpeta;

    private RepositorioArchivo repositorio;
    private Path archivo;

    @BeforeEach
    void preparar() {
        repositorio = new RepositorioArchivo();
        archivo = carpeta.resolve("datos.dat");
    }

    private String ruta() {
        return archivo.toString();
    }

    @Test
    void guardaYRecuperaUnObjeto() throws PersistenciaException {
        List<String> original = new ArrayList<>(List.of("River", "Boca", "Racing"));

        repositorio.guardar(original, ruta());
        Object leido = repositorio.cargar(ruta(), ArrayList.class);

        assertEquals(original, leido);
    }

    @Test
    void existeSoloDespuesDeGuardar() throws PersistenciaException {
        assertFalse(repositorio.existe(ruta()));

        repositorio.guardar("hola", ruta());

        assertTrue(repositorio.existe(ruta()));
    }

    @Test
    void unaCarpetaNoCuentaComoArchivoExistente() throws PersistenciaException {
        assertFalse(repositorio.existe(carpeta.toString()));
    }

    @Test
    void guardarDosVecesConservaSoloLaUltimaVersion() throws PersistenciaException {
        repositorio.guardar("primera", ruta());
        repositorio.guardar("segunda", ruta());

        assertEquals("segunda", repositorio.cargar(ruta(), String.class));
    }

    @Test
    void despuesDeGuardarNoQuedanArchivosTemporales() throws PersistenciaException {
        repositorio.guardar("dato", ruta());

        String[] archivos = carpeta.toFile().list();

        assertEquals(1, archivos.length);
        assertEquals("datos.dat", archivos[0]);
    }

    @Test
    void creaLaCarpetaDestinoSiNoExiste() throws PersistenciaException {
        Path anidado = carpeta.resolve("nueva").resolve("datos.dat");

        repositorio.guardar("dato", anidado.toString());

        assertTrue(Files.exists(anidado));
    }

    @Test
    void recuperaTodoElSistemaConSusRelacionesCompartidas() throws PersistenciaException, ReglaNegocioException {
        Datos datos = new Datos();
        datos.cargarDemostracion();

        repositorio.guardar(datos, ruta());
        Datos leido = repositorio.cargar(ruta(), Datos.class);

        assertEquals(1, leido.getTorneos().size());
        assertEquals(4, leido.getClubes().size());
        assertEquals(80, leido.getJugadores().size());
        assertEquals(6, leido.getTorneos().get(0).getPartidos().size());
        // La serialización conserva una sola copia de cada objeto: el club del torneo es el mismo de la lista.
        assertSame(leido.getClubes().get(0), leido.getTorneos().get(0).getInscripciones().get(0).getClub());
        // El suscriptor sigue a los mismos objetos Club que existen en el sistema.
        assertTrue(leido.getSuscriptores().get(0).sigueA(leido.getClubes().get(0)));
    }

    // ---------- Errores ----------

    @Test
    void noSePuedeGuardarUnObjetoNulo() {
        try {
            repositorio.guardar(null, ruta());
            fail("Debía rechazar guardar un objeto nulo");
        } catch (PersistenciaException e) {
            // correcto
        }
        assertFalse(Files.exists(archivo));
    }

    @Test
    void guardarConNombreNuloSeRechaza() {
        try {
            repositorio.guardar("x", null);
            fail("Debía rechazar un nombre nulo");
        } catch (PersistenciaException e) {
            // correcto
        }
    }

    @Test
    void guardarConNombreEnBlancoSeRechaza() {
        try {
            repositorio.guardar("x", "  ");
            fail("Debía rechazar un nombre en blanco");
        } catch (PersistenciaException e) {
            // correcto
        }
    }

    @Test
    void cargarConNombreVacioSeRechaza() {
        try {
            repositorio.cargar("", String.class);
            fail("Debía rechazar un nombre vacío");
        } catch (PersistenciaException e) {
            // correcto
        }
    }

    @Test
    void existeConNombreNuloSeRechaza() {
        try {
            repositorio.existe(null);
            fail("Debía rechazar un nombre nulo");
        } catch (PersistenciaException e) {
            // correcto
        }
    }

    @Test
    void noSePuedeGuardarSobreUnaCarpeta() {
        try {
            repositorio.guardar("x", carpeta.toString());
            fail("Debía rechazar guardar sobre una carpeta");
        } catch (PersistenciaException e) {
            // correcto
        }
    }

    @Test
    void noSePuedeCargarUnaCarpeta() {
        try {
            repositorio.cargar(carpeta.toString(), String.class);
            fail("Debía rechazar cargar una carpeta");
        } catch (PersistenciaException e) {
            // correcto
        }
    }

    @Test
    void cargarUnArchivoInexistenteFalla() {
        try {
            repositorio.cargar(ruta(), String.class);
            fail("Debía fallar porque el archivo no existe");
        } catch (PersistenciaException e) {
            // correcto
        }
    }

    @Test
    void cargarUnArchivoVacioFalla() throws IOException {
        Files.createFile(archivo);

        try {
            repositorio.cargar(ruta(), String.class);
            fail("Debía fallar porque el archivo está vacío");
        } catch (PersistenciaException e) {
            // correcto
        }
    }

    @Test
    void cargarUnArchivoConContenidoQueNoEsUnObjetoFalla() throws IOException {
        Files.writeString(archivo, "esto no es un objeto serializado");

        try {
            repositorio.cargar(ruta(), String.class);
            fail("Debía fallar porque el contenido no es un objeto guardado");
        } catch (PersistenciaException e) {
            // correcto
        }
    }

    @Test
    void cargarUnArchivoTruncadoFalla() throws PersistenciaException, IOException {
        repositorio.guardar(new ArrayList<>(List.of("River", "Boca", "Racing", "Independiente")), ruta());
        byte[] completo = Files.readAllBytes(archivo);
        Files.write(archivo, Arrays.copyOf(completo, completo.length / 2));

        try {
            repositorio.cargar(ruta(), ArrayList.class);
            fail("Debía fallar porque el archivo está incompleto");
        } catch (PersistenciaException e) {
            // correcto
        }
    }

    @Test
    void cargarConUnTipoDistintoAlGuardadoFalla() throws PersistenciaException {
        repositorio.guardar("un texto", ruta());

        try {
            repositorio.cargar(ruta(), Integer.class);
            fail("Debía fallar porque el tipo no coincide");
        } catch (PersistenciaException e) {
            // correcto
        }
    }

    @Test
    void elTipoEsperadoNoPuedeSerNulo() throws PersistenciaException {
        repositorio.guardar("dato", ruta());

        try {
            repositorio.cargar(ruta(), null);
            fail("Debía rechazar un tipo nulo");
        } catch (PersistenciaException e) {
            // correcto
        }
    }
}
