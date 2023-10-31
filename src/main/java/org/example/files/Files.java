package org.example.files;

import java.io.*;
import java.util.Base64;

public class Files {
    public String encodeFile(String fileName) throws Exception {
        File image = new File(fileName);
        InputStream is = new FileInputStream(image);
        byte[] byteArray = is.readAllBytes();
        String base64String = Base64.getEncoder().encodeToString(byteArray);

        System.out.println("image in base64 format ===>> " + base64String);
        return base64String;
    }

    public void decodeFile(String newFileName, String base64String) throws Exception {
        byte[] byteArrayDecode = Base64.getDecoder().decode(base64String);
        try (FileOutputStream fos = new FileOutputStream(newFileName)) {
            fos.write(byteArrayDecode);
        }
    }

    public static void writeImage(int code, byte[] bytes) throws IOException {
        OutputStream os = new FileOutputStream(String.format("cats/%s.jpg", code));
        os.write(bytes);
        os.close();
    }
}
