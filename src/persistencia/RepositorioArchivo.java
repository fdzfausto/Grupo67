
package persistencia;

import excepcion.PersistenciaException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class RepositorioArchivo {

    public void guardar(Object objeto, String nombreArchivo) throws PersistenciaException {
        if (objeto == null) {
            throw new PersistenciaException("No se pueden guardar datos nulos");
        }

        Path archivo = obtenerRuta(nombreArchivo);
        Path temporal = null;

        try {
            Path directorio = archivo.getParent();

            if (Files.exists(archivo) && Files.isDirectory(archivo)) {
                throw new PersistenciaException("La ruta indicada es un directorio");
            }

            Files.createDirectories(directorio);

            temporal = Files.createTempFile(directorio, "futbolstats-", ".tmp");

            try (ObjectOutputStream salida = new ObjectOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(temporal)))) {

                salida.writeObject(objeto);
            }

            try {
                Files.move(temporal, archivo,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporal, archivo,
                        StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (IOException e) {
            throw new PersistenciaException(
                    "No se pudieron guardar los datos en: " + nombreArchivo, e);
        } finally {
            if (temporal != null) {
                try {
                    Files.deleteIfExists(temporal);
                } catch (IOException e) {
                    // Se intenta eliminar el archivo temporal al finalizar.
                }
            }
        }
    }

    public <T> T cargar(String nombreArchivo, Class<T> tipo) throws PersistenciaException {
        if (tipo == null) {
            throw new PersistenciaException("El tipo de dato no puede ser nulo");
        }

        Path archivo = obtenerRuta(nombreArchivo);

        if (!Files.exists(archivo)) {
            throw new PersistenciaException("No existe el archivo: " + nombreArchivo);
        }

        if (Files.isDirectory(archivo)) {
            throw new PersistenciaException("La ruta indicada es un directorio");
        }

        try {
            if (Files.size(archivo) == 0) {
                throw new PersistenciaException("El archivo está vacío");
            }

            try (ObjectInputStream entrada = new ObjectInputStream(
                    new BufferedInputStream(Files.newInputStream(archivo)))) {

                Object objeto = entrada.readObject();

                if (!tipo.isInstance(objeto)) {
                    throw new PersistenciaException(
                            "El contenido del archivo no corresponde al tipo esperado: "
                                    + tipo.getSimpleName());
                }

                return tipo.cast(objeto);
            }

        } catch (EOFException e) {
            throw new PersistenciaException("El archivo está incompleto o dañado", e);
        } catch (IOException | ClassNotFoundException e) {
            throw new PersistenciaException(
                    "No se pudieron recuperar los datos de: " + nombreArchivo, e);
        }
    }

    public boolean existe(String nombreArchivo) throws PersistenciaException {
        Path archivo = obtenerRuta(nombreArchivo);
        return Files.isRegularFile(archivo);
    }

    private Path obtenerRuta(String nombreArchivo) throws PersistenciaException {
        if (nombreArchivo == null || nombreArchivo.trim().isEmpty()) {
            throw new PersistenciaException("El nombre del archivo no puede estar vacío");
        }

        try {
            return Path.of(nombreArchivo).toAbsolutePath();
        } catch (InvalidPathException e) {
            throw new PersistenciaException("La ruta del archivo no es válida", e);
        }
    }
}
