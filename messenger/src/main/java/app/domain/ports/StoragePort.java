package app.domain.ports;

import java.io.File;
import java.io.IOException;

/**
 * Puerto de salida para almacenamiento de archivos (fotos, firmas).
 */
public interface StoragePort {
    String save(File file, String subDirectory, String customFileName) throws IOException;

    File get(String path);

    String getUrl(String path);
}
