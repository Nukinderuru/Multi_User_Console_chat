package edu.school21.sockets.client.logic;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.net.Socket;

public class Client {
    private static Socket clientSocket;

    public Client(Integer port) throws IOException {
        clientSocket = new Socket("localhost", port);
    }

    public void run() {
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String message = null;
            boolean exit = false;
            Long roomId = null;
            while (true) {
                JSONTokener tokener = new JSONTokener(in);
                JSONObject json = new JSONObject(tokener);
                String input = (String) json.get("message");
                if (message != null && input.contains(message) && input.endsWith("---")) {
                    try {
                        roomId = ((Number) json.get("roomId")).longValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                String type = (String) json.get("type");
                if (type.equals("exit")) {
                    System.out.println(input);
                    exit = true;
                    break;
                }
                System.out.println(input);
                message = reader.readLine();
                out.write(message + "\n");
                out.flush();
            }
            if (!exit) {
                System.out.println("You entered the room " + message);
                new WriteMessage(out, reader).start();
                new ReadMessage(in, clientSocket, roomId).start();
            }
            if (clientSocket.isClosed() || exit) {
                System.out.println("You have left the chat.");
                out.close();
                System.exit(0);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
