package app.domain.ports;

import java.io.File;
import java.io.IOException;

/**
 * Puerto (interfaz) para operaciones de almacenamiento de archivos.
 * 
 * Abstrae el sistema de almacenamiento (local, S3, etc.) permitiendo guardar,
 * recuperar y gestionar archivos de manera independiente de la implementación.
 */
public interface StoragePort {
    String save(File file, String subDirectory) throws IOException;

    String save(File file, String subDirectory, String customFileName) throws IOException;

    File get(String path);
}
