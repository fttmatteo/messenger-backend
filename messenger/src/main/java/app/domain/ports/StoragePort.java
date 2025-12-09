package app.domain.ports;

import java.io.File;
import java.io.IOException;

public interface StoragePort {
    String save(File file, String subDirectory) throws IOException;

    String save(File file, String subDirectory, String customFileName) throws IOException;

    File get(String path);
}
