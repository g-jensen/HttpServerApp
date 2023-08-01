package org;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileRepository {
    public FileRepository(String rootDirectory) {
        root = FileSystems.getDefault().getPath(rootDirectory);
    }
    public String[] fileNames(String directory) {
        Path path = root.resolve(directory);
        try {
            return Files.list(path).map(Path::toString).toArray(String[]::new);
        } catch (Exception e) {return new String[0];}
    }

    public byte[] fileData(String file) {
        Path path = root.resolve(file);
        try {
            return Files.readAllBytes(path);
        } catch (Exception e) {return new byte[0];}
    }
    public boolean isDirectory(String path) {
        Path newPath = root.resolve(path);
        return Files.isDirectory(newPath);
    }
    public boolean isFile(String path) {
        Path newPath = root.resolve(path);
        return Files.isRegularFile(newPath);
    }
    public Path getPath(String path) {
        return root.resolve(path);
    }
    private Path root;
}
