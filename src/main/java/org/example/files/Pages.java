package org.example.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.StringJoiner;

public class Pages {
    public static String getPageByPath(String path) throws FileNotFoundException {
        if (path.equals("/")) {
            return readPage("index.html");
        } else {
            return readPage(path.substring(1));
        }
    }
    public static String readPage(String fileName) throws FileNotFoundException {
        File page = new File("web/" + fileName);

        StringJoiner result = new StringJoiner("\n");
        try(Scanner scanner = new Scanner(page)) {
            while(scanner.hasNextLine()) {
                result.add(scanner.nextLine());
            }
        }

        return result.toString();
    }
}
