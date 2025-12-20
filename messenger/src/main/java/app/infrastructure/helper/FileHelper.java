package app.infrastructure.helper;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilidades para conversión y manejo de archivos temporales.
 */
@Component
public class FileHelper {

    public File convertToFile(MultipartFile multipartFile) throws IOException {
        String originalName = multipartFile.getOriginalFilename();
        String extension = "";

        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        if (extension.isEmpty()) {
            String contentType = multipartFile.getContentType();
            if (contentType != null) {
                extension = getExtensionFromContentType(contentType);
            }
        }

        if (extension.isEmpty() || ".bin".equals(extension)) {
            extension = detectExtensionFromBytes(multipartFile);
        }
        if (extension.isEmpty()) {
            extension = ".tmp";
        }

        File tempFile = File.createTempFile("upload-", extension);
        multipartFile.transferTo(tempFile);
        return tempFile;
    }

    private String getExtensionFromContentType(String contentType) {
        return switch (contentType) {
            case "image/jpeg", "image/jpg" -> ".jpeg";
            case "image/png" -> ".png";
            case "application/pdf" -> ".pdf";
            default -> "";
        };
    }

    private String detectExtensionFromBytes(MultipartFile multipartFile) {
        try (InputStream is = multipartFile.getInputStream()) {
            byte[] header = new byte[8];
            int read = is.read(header);

            if (read >= 4) {
                if (header[0] == (byte) 0x89 && header[1] == (byte) 0x50 &&
                        header[2] == (byte) 0x4E && header[3] == (byte) 0x47) {
                    return ".png";
                }

                if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF) {
                    return ".jpeg";
                }
                if (header[0] == (byte) 0x25 && header[1] == (byte) 0x50 &&
                        header[2] == (byte) 0x44 && header[3] == (byte) 0x46) {
                    return ".pdf";
                }
            }
        } catch (IOException e) {
            return "";
        }

        return "";
    }

    public <T> T withTempFile(MultipartFile multipartFile, FileOperation<T> operation) throws IOException {
        File tempFile = convertToFile(multipartFile);
        try {
            return operation.execute(tempFile);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Error processing temporary file", e);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    @FunctionalInterface
    public interface FileOperation<T> {
        T execute(File tempFile) throws Exception;
    }

    public void cleanupTempFiles(List<File> files) {
        if (files == null)
            return;
        for (File f : files) {
            if (f != null && f.exists()) {
                f.delete();
            }
        }
    }

    public List<File> convertToFiles(
            List<MultipartFile> multipartFiles) throws IOException {
        List<File> files = new ArrayList<>();
        if (multipartFiles == null)
            return files;

        for (MultipartFile mf : multipartFiles) {
            if (mf != null && !mf.isEmpty()) {
                files.add(convertToFile(mf));
            }
        }
        return files;
    }
}
