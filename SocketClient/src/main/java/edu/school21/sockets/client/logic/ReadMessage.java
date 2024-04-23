package edu.school21.sockets.client.logic;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;

public class ReadMessage extends Thread {
    private final BufferedReader in;
    private final Socket clientSocket;
    private final Long roomId;

    public ReadMessage(BufferedReader in, Socket clientSocket, Long roomId) {
        this.in = in;
        this.clientSocket = clientSocket;
        this.roomId = roomId;
    }

    @Override
    public void run() {
        while (true) {
            JSONTokener tokener = new JSONTokener(in);
            JSONObject json = new JSONObject(tokener);
            try {
                String message = (String) json.get("message");
                if (message.equalsIgnoreCase("Exiting...")) {
                    exit();
                    break;
                }
                Long msgRoomId = ((Number) json.get("roomId")).longValue();
                if (msgRoomId.equals(roomId)) {
                    String userName = (String) json.get("userName");
                    String dateTime = (String) json.get("date");
                    System.out.println(dateTime + " " + userName + ": " + message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void exit() {
        try {
            in.close();
            if (!clientSocket.isClosed()) {
                clientSocket.close();
            }
            interrupt();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
