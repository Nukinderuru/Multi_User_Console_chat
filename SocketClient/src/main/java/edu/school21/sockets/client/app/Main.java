package edu.school21.sockets.client.app;

import edu.school21.sockets.client.logic.Client;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Integer portNum = getPort(args);
        Client client = new Client(portNum);
        client.run();
    }

    private static Integer getPort(String[] args) {
        if (args.length != 1) {
            System.out.println("Wrong number of arguments! There should be one: port number");
            System.exit(1);
        }
        String[] parts = args[0].split("=");
        return Integer.parseInt(parts[1]);
    }
}
