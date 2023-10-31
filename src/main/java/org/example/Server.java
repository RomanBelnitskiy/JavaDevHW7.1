package org.example;

import org.example.files.Files;
import org.example.files.Pages;
import org.example.request.HttpRequest;
import org.example.response.HttpResponse;
import org.example.utils.PropertyLoader;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.example.request.RequestMapper.mapToRequest;

public class Server {
    private static final String BASE_CAT_URL = "https://http.cat/%s.jpg";

    private int port;
    private long timeout;
    private int threadPoolSize;

    public Server() {
        port = PropertyLoader.getServerPort();
        timeout = PropertyLoader.getTimeout();
        threadPoolSize = PropertyLoader.getThreadPoolSize();
    }

    public void run() {
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println(String.format("Server started at port %s", port));

            while (true) {
                System.out.println("Waiting for connection");

                Socket socket = serverSocket.accept();
                System.out.println("Client connection successfully created");

                executorService.execute(() -> connectionHandler(socket));
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void connectionHandler(Socket socket) {
        try {
            Optional<HttpRequest> optionalHttpRequest = requestWorker(socket);
            if(optionalHttpRequest.isPresent()) {
                HttpRequest request = optionalHttpRequest.get();
                responseWorker(socket, request);
            }

            closeStreamsAndSocket(socket);
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private String readRequest(InputStream inputStream) throws Exception {
        Thread.sleep(timeout);

        byte[] buffer = new byte[1024*16];
        int length = 0;

        while (inputStream.available() > 0) {
            int read = inputStream.read(buffer, length, inputStream.available());
            length += read;
            Thread.sleep(timeout);
        }

        return new String(buffer, 0, length);
    }

    private Optional<HttpRequest> requestWorker(Socket socket) throws Exception {
        InputStream is = socket.getInputStream();
        String requestText = readRequest(is);

        if (requestText.isEmpty()) {
            return Optional.empty();
        }

        HttpRequest request = mapToRequest(requestText);
        printRequest(request);

        return Optional.of(request);
    }

    private void responseWorker(Socket socket, HttpRequest request) throws Exception {
        HttpResponse response = new HttpResponse();
        response.setProtocol(request.getProtocol());
        try {
            if ("/".equals(request.getPath()) || request.getPath().endsWith(".html")) {
                String body = Pages.getPageByPath(request.getPath());
                response.setStatusCode(200);
                response.setStatusMessage("OK");
                response.setBody(body);
            } else {
                int code = extractCodeFromPath(request);
                String url = String.format(BASE_CAT_URL, code);
                java.net.http.HttpRequest httpRequest = java.net.http.HttpRequest.newBuilder()
                        .uri(new URI(url))
                        .GET()
                        .build();
                HttpClient httpClient = HttpClient.newHttpClient();
                java.net.http.HttpResponse<byte[]> httpResponse = httpClient.send(httpRequest,
                        java.net.http.HttpResponse.BodyHandlers.ofByteArray());

                if (httpResponse.statusCode() == 200) {
                    Files.writeImage(code, httpResponse.body());
                    showPageWithStatusCodeImage(response, code, httpResponse.body());
                } else {
                    showPageNotFound(response, "/statusCodeNotFound.html");
                }
            }
        } catch(FileNotFoundException | NumberFormatException e) {
            showPageNotFound(response, "/notFound.html");
        }
        String responseText = response.convertToText();
        printResponse(responseText);
        byte[] responseBytes = responseText.getBytes(StandardCharsets.UTF_8);

        socket.getOutputStream().write(responseBytes);
        socket.getOutputStream().flush();
    }

    private static Integer extractCodeFromPath(HttpRequest request) {
        return Integer.valueOf(request.getPath().substring(1));
    }

    private static void showPageNotFound(HttpResponse response, String pagePath) throws FileNotFoundException {
        String body = Pages.getPageByPath(pagePath);
        response.setStatusCode(404);
        response.setStatusMessage("Not Found");
        response.setBody(body);
    }

    private static void showPageWithStatusCodeImage(HttpResponse response, int code, byte[] image) throws FileNotFoundException {
        String encodedImage = Base64.getEncoder().encodeToString(image);
        String body = Pages.getPageByPath("/statusCode.html").replace("%s", String.valueOf(code));
        body = body.replace("%img", encodedImage);
        response.setStatusCode(200);
        response.setStatusMessage("OK");
        response.setBody(body);
    }

    private void closeStreamsAndSocket(Socket socket) throws IOException {
        socket.getInputStream().close();
        socket.getOutputStream().close();
        socket.close();
    }

    private void printRequest(HttpRequest request) {
        System.out.println("================================");
        System.out.println("REQUEST => " + request);
        System.out.println("================================");
    }

    private void printResponse(String response) {
        System.out.println("================================");
        System.out.println("RESPONSE => " + response);
        System.out.println("================================");
    }
}
