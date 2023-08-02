package main;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

class FileRepositoryTest {

    @Test
    void getsFileNamesInDirectory() {
        FileRepository fileRepository = new FileRepository(".");
        Path p = FileSystems.getDefault().getPath("./myFiles");
        String[] names = new String[]{"index.html","file1.txt","myPic.png"};
        Path[] paths = new Path[3];
        paths[0] = FileSystems.getDefault().getPath("index.html");
        paths[1] = FileSystems.getDefault().getPath("file1.txt");
        paths[2] = FileSystems.getDefault().getPath("myPic.png");
        Stream<Path> stream = Arrays.stream(paths);
        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(()-> Files.list(p)).thenReturn(stream);
            assertArrayEquals(names,fileRepository.fileNames("myFiles"));
        }
    }

    @Test
    void getsFileNamesInFakeDirectory() {
        FileRepository fileRepository = new FileRepository(".");
        Path p = FileSystems.getDefault().getPath("./myFiles");
        Path[] paths = new Path[3];
        paths[0] = FileSystems.getDefault().getPath("index.html");
        paths[1] = FileSystems.getDefault().getPath("file1.txt");
        paths[2] = FileSystems.getDefault().getPath("myPic.png");
        Stream<Path> stream = Arrays.stream(paths);
        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(()-> Files.list(p)).thenReturn(stream);
            assertArrayEquals(new String[0],fileRepository.fileNames("other"));
        }
    }

    @Test
    void getsFileData() {
        FileRepository fileRepository = new FileRepository(".");
        Path p = FileSystems.getDefault().getPath("./index.html");
        byte[] bytes = "<h1>hello</h1>".getBytes();
        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(()-> Files.readAllBytes(p)).thenReturn(bytes);
            assertArrayEquals(bytes,fileRepository.fileData("index.html"));
        }
    }

    @Test
    void getsFileDataFromFakeFile() {
        FileRepository fileRepository = new FileRepository(".");
        Path p = FileSystems.getDefault().getPath("./index.html");
        byte[] bytes = "<h1>hello</h1>".getBytes();
        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(()-> Files.readAllBytes(p)).thenReturn(bytes);
            assertNull(fileRepository.fileData("hello.html"));
        }
    }

    @Test
    void checksIfPathIsDirectory() {
        FileRepository fileRepository = new FileRepository(".");
        Path p = FileSystems.getDefault().getPath("./pages");
        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(()-> Files.isDirectory(p)).thenReturn(true);
            assertTrue(fileRepository.isDirectory("pages"));
            assertFalse(fileRepository.isDirectory("greg"));
            assertFalse(fileRepository.isDirectory("pages.txt"));
        }
    }

    @Test
    void checksIfPathIsFile() {
        FileRepository fileRepository = new FileRepository(".");
        Path p = FileSystems.getDefault().getPath("./index.html");
        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(()-> Files.isRegularFile(p)).thenReturn(true);
            assertTrue(fileRepository.isFile("index.html"));
            assertFalse(fileRepository.isFile("greg.txt"));
            assertFalse(fileRepository.isFile("greg"));
        }
    }

    @Test
    void getsPath() {
        FileRepository fileRepository = new FileRepository(".");
        Path p1 = FileSystems.getDefault().getPath("./index.html");
        Path p2 = FileSystems.getDefault().getPath("./pages");
        assertEquals(fileRepository.getPath("index.html"),p1);
        assertEquals(fileRepository.getPath("pages"),p2);
    }
}