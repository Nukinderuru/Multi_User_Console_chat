package edu.school21.sockets.client.logic;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;

public class WriteMessage extends Thread {
    private final BufferedWriter out;
    private final BufferedReader reader;

    public WriteMessage(BufferedWriter out, BufferedReader inputConsole) {
        this.out = out;
        this.reader = inputConsole;
    }

    @Override
    public void run() {
        String message;
        while (true) {
            try {
                message = reader.readLine();
                JSONObject json = new JSONObject();
                json.put("message", message);
                if (message.equalsIgnoreCase("exit")) {
                    out.write(json + "\n");
                    out.flush();
                    break;
                } else {
                    out.write(json + "\n");
                    out.flush();
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
