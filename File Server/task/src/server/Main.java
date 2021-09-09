package server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    static List<String> files = new ArrayList<>();

    public static void main(String[] args) {
//        readFiles();
        System.err.println(files);
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String[] answer = scanner.nextLine().split(" ");
            System.err.println(Arrays.toString(answer));
            if (answer[0].equals("exit")) {
                break;
            }
            switch (answer[0]) {
                case "add":
                    add(answer[1]);
                    break;
                case "get":
                    get(answer[1]);
                    break;
                case "delete":
                    delete(answer[1]);
                    break;
                default:
                    System.out.println("Invalid input");
            }
        }
//        saveFiles();
    }

    private static void saveFiles() {
        try {
            Files.writeString(Paths.get("fileserver.db"), String.join("\n", files));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readFiles() {
        try {
            files = Files.lines(Paths.get("fileserver.db")).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void delete(String filename) {
        if (files.remove(filename)) {
            System.out.printf("The file %s was deleted\n", filename);
        } else {
            System.out.printf("The file %s not found\n", filename);
        }
    }

    private static void get(String filename) {
        if (files.contains(filename)) {
            System.out.printf("The file %s was sent\n", filename);
        } else {
            System.out.printf("The file %s not found\n", filename);
        }
    }

    private static void add(String filename) {
        if (files.contains(filename) || !List.of(new String[]{
                "file1", "file2", "file3", "file4", "file5",
                "file6", "file7", "file8", "file9", "file10"
        }).contains(filename)) {
            System.out.printf("Cannot add the file %s\n", filename);
        } else {
            files.add(filename);
            System.out.printf("The file %s added successfully\n", filename);
        }
    }
}