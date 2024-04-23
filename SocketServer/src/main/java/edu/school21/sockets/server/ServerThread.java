package edu.school21.sockets.server;

import edu.school21.sockets.models.Chatroom;
import edu.school21.sockets.models.Message;
import edu.school21.sockets.models.User;
import edu.school21.sockets.repositories.ChatroomRepository;
import edu.school21.sockets.repositories.MessagesRepository;
import edu.school21.sockets.services.UsersService;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ServerThread extends Thread {
    private final BufferedReader in;
    private final BufferedWriter out;
    private final UsersService usersService;
    private final MessagesRepository messagesRepository;
    private final ChatroomRepository chatroomRepository;
    private boolean exit;

    public ServerThread(Socket socket, UsersService usersService, MessagesRepository messagesRepository,
                        ChatroomRepository chatroomRepository)
            throws IOException {
        this.chatroomRepository = chatroomRepository;
        exit = false;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.usersService = usersService;
        this.messagesRepository = messagesRepository;
        start();
    }

    @Override
    public void run() {
        Optional<User> optUser = authenticate();
        JSONObject jsonOut = new JSONObject();
        if (!optUser.isPresent()) {
            if (!exit) {
                jsonOut.put("message", "No such user found, closing the connection...");
                jsonOut.put("type", "server command");
                send(jsonOut.toString());
                exit();
            }
        } else {
            User user = optUser.get();
            Chatroom chatroom = getIntoRoom(user);
            if (chatroom != null) {
                jsonOut.put("message", chatroom.getName() + "--------------");
                jsonOut.put("roomId", chatroom.getId());
                jsonOut.put("type", "server command");
                send(jsonOut.toString());
                showHistory(chatroom);
                startMessaging(user, chatroom);
            }
        }
    }

    private String[] getUserNameAndPassword() {
        try {
            JSONObject jsonOut = new JSONObject();
            jsonOut.put("message", "Enter username:");
            jsonOut.put("type", "server command");
            send(jsonOut.toString());
            String username = in.readLine();
            jsonOut.put("message", "Enter password:");
            jsonOut.put("type", "server command");
            send(jsonOut.toString());
            String password = in.readLine();
            return new String[]{username, password};
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<User> signIn(String[] data) {
        return usersService.signIn(data[0], data[1]);
    }

    private User signUp(String[] data) {
        return usersService.signUp(data[0], data[1]);
    }

    private Optional<User> authenticate() {
        try {
            JSONObject jsonOut = new JSONObject();
            String menuMessage = "1. signIn\n2. SignUp\n3. Exit";
            jsonOut.put("message", "Hello from Server!\n" + menuMessage);
            jsonOut.put("type", "server command");
            send(jsonOut.toString());
            String command = in.readLine();
            while (true) {
                if (command.equalsIgnoreCase("signIn") || command.equals("1")) {
                    String[] data = getUserNameAndPassword();
                    return signIn(data);
                } else if (command.equalsIgnoreCase("signUp") || command.equals("2")) {
                    String[] data = getUserNameAndPassword();
                    User user = signUp(data);
                    if (user != null) {
                        jsonOut.put("message", "Signed up successfully! Now you need to sign in\n");
                    } else {
                        jsonOut.put("message", "Oops! Something went wrong!\n");
                    }
                    jsonOut.put("type", "server command");
                    send(jsonOut.toString());
                    command = in.readLine();
                } else if (command.equalsIgnoreCase("exit") || command.equals("3")) {
                    System.out.println("User disconnected");
                    exit();
                    break;
                } else {
                    jsonOut.put("message", "Wrong command. Try again\n" + menuMessage);
                    jsonOut.put("type", "server command");
                    send(jsonOut.toString());
                    command = in.readLine();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    private Chatroom getIntoRoom(User user) {
        try {
            String menuMessage = "1. Create room\n2. Choose room\n3. Exit";
            JSONObject jsonOut = new JSONObject();
            jsonOut.put("message", menuMessage);
            jsonOut.put("type", "server command");
            send(jsonOut.toString());
            String command = in.readLine();
            while (true) {
                if (command.equals("createRoom") || command.equals("1")) {
                    jsonOut.put("message", "Enter room name:");
                    jsonOut.put("type", "server command");
                    send(jsonOut.toString());
                    command = in.readLine();
                    Optional<Chatroom> room = chatroomRepository.findByRoomName(command);
                    if (room.isPresent()) {
                        jsonOut.put("message", "Such room already exists.\n" + menuMessage);
                        jsonOut.put("type", "server command");
                        send(jsonOut.toString());
                        command = in.readLine();
                    } else {
                        Chatroom newRoom = new Chatroom(chatroomRepository.generateId(), command, user,
                                new ArrayList<>());
                        chatroomRepository.save(newRoom);
                        user.getCreatedRooms().add(newRoom);
                        return newRoom;
                    }
                } else if (command.equals("chooseRoom") || command.equals("2")) {
                    List<Chatroom> rooms = chatroomRepository.findAll();
                    StringBuilder sb = new StringBuilder();
                    int i = 1;
                    for (Chatroom room : rooms) {
                        sb.append(i).append(". ").append(room.getName()).append("\n");
                        ++i;
                    }
                    sb.append(i).append(" Exit.");
                    jsonOut.put("message", sb);
                    jsonOut.put("type", "server command");
                    send(jsonOut.toString());
                    command = in.readLine();
                    if (command.equalsIgnoreCase("exit") || command.equals(String.valueOf(i))) {
                        exit();
                        break;
                    }
                    Optional<Chatroom> room = chatroomRepository.findByRoomName(command);
                    if (room.isPresent()) {
                        user.getUsedRooms().add(room.get());
                        return room.get();
                    } else {
                        jsonOut.put("message", "No such room found: " + command);
                        jsonOut.put("type", "server command");
                        send(jsonOut.toString());
                        break;
                    }
                } else if (command.equalsIgnoreCase("exit") || command.equals("3")) {
                    exit();
                    break;
                } else {
                    jsonOut.put("message", "Wrong command. Try again\n" + menuMessage);
                    jsonOut.put("type", "server command");
                    send(jsonOut.toString());
                    command = in.readLine();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private void startMessaging(User user, Chatroom room) {
        while (true) {
            JSONTokener tokener = new JSONTokener(in);
            JSONObject jsonIn = new JSONObject(tokener);
            String input = (String) jsonIn.get("message");
            if (input != null) {
                if (input.equalsIgnoreCase("exit")) {
                    exit();
                    break;
                }
                Long id = messagesRepository.generateId();
                Message message = new Message(id, user, room, input,
                        new Timestamp(System.currentTimeMillis()));
                messagesRepository.save(message);
                for (ServerThread serverThread : Server.serverList) {
                    if (serverThread == this) {
                        continue;
                    }
                    String time = String.format("%1$td.%1$tm.%1$tY %1$tH:%1$tM ", message.getTimestamp());
                    JSONObject jsonOut = new JSONObject();
                    jsonOut.put("message", input);
                    jsonOut.put("id", id);
                    jsonOut.put("roomId", room.getId());
                    jsonOut.put("userName", user.getUsername());
                    jsonOut.put("date", time);
                    jsonOut.put("type", "user message");
                    serverThread.send(jsonOut.toString());
                }
            }
        }
    }

    private void showHistory(Chatroom room) {
        List<Message> messages = messagesRepository.findAllByRoomId(room.getId());
        if (messages != null) {
            Collections.reverse(messages);
            for (Message message : messages) {
                String time = String.format("%1$td.%1$tm.%1$tY %1$tH:%1$tM ", message.getTimestamp());
                JSONObject json = new JSONObject();
                json.put("message", message.getText());
                json.put("id", message.getId());
                json.put("roomId", room.getId());
                json.put("userName", message.getAuthor().getUsername());
                json.put("date", time);
                json.put("type", "user message");
                send(json.toString());
            }
            if (room.getMessages() == null) {
                room.setMessages(messages);
            }
        }
    }

    private void send(String message) {
        try {
            out.write(message + "\n");
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void exit() {
        try {
            System.out.println("User disconnected");
            JSONObject json = new JSONObject();
            json.put("message", "Exiting...");
            json.put("type", "exit");
            send(json.toString());
            sleep(3000);
            for (int i = 0; i < Server.serverList.size(); ++i) {
                if (Server.serverList.get(i).equals(this)) {
                    Server.serverList.remove(Server.serverList.get(i));
                }
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        exit = true;
    }
}
