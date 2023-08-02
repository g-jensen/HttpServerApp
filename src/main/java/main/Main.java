package main;

import commandparser.BadUsageException;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, BadUsageException {
        new ServerApplication(args,System.out).run();
    }
}