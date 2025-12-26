package app.domain.ports;

import java.io.File;
import java.io.IOException;

/**
 * Puerto de salida para almacenamiento de archivos (fotos, firmas).
 */
public interface StoragePort {

    /**
     * Guarda un archivo en el sistema de almacenamiento.
     */
    String save(File file, String subDirectory, String customFileName) throws IOException;

    /**
     * Obtiene un archivo del sistema de almacenamiento.
     */
    File get(String path);
}
