package edu.school21.sockets.server;

import edu.school21.sockets.repositories.ChatroomRepository;
import edu.school21.sockets.repositories.MessagesRepository;
import edu.school21.sockets.services.UsersService;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

@Component
public class Server {
    public static LinkedList<ServerThread> serverList = new LinkedList<>();
    private final UsersService usersService;
    private final MessagesRepository messagesRepository;
    private final ChatroomRepository chatroomRepository;

    public Server(UsersService usersService, MessagesRepository messagesRepository,
                  ChatroomRepository chatroomRepository) {
        this.usersService = usersService;
        this.messagesRepository = messagesRepository;
        this.chatroomRepository = chatroomRepository;
    }

    public void start(Integer port) {
        try {
            try (ServerSocket server = new ServerSocket(port)) {
                while (!server.isClosed()) {
                    Socket socket = server.accept();
                    try {
                        serverList.add(new ServerThread(socket, usersService, messagesRepository, chatroomRepository));
                        System.out.println("New user connected");
                    } catch (IOException e) {
                        socket.close();
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
