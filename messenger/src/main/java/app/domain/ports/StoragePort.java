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
    /**
     * Guarda un archivo en el sistema de almacenamiento.
     * 
     * @param file         Archivo a guardar.
     * @param subDirectory Subdirectorio donde almacenar (ej: "photos",
     *                     "signatures").
     * @return Ruta relativa del archivo guardado.
     * @throws IOException Si ocurre un error al guardar el archivo.
     */
    String save(File file, String subDirectory) throws IOException;

    /**
     * Guarda un archivo con un nombre personalizado.
     * 
     * @param file           Archivo a guardar.
     * @param subDirectory   Subdirectorio donde almacenar.
     * @param customFileName Nombre personalizado para el archivo.
     * @return Ruta relativa del archivo guardado.
     * @throws IOException Si ocurre un error al guardar el archivo.
     */
    String save(File file, String subDirectory, String customFileName) throws IOException;

    /**
     * Recupera un archivo del sistema de almacenamiento.
     * 
     * @param path Ruta relativa del archivo.
     * @return Archivo recuperado o null si no existe.
     */
    File get(String path);
}
