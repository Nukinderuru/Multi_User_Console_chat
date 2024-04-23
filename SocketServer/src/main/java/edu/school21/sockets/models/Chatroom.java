package edu.school21.sockets.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Chatroom {
    private Long id;
    private String name;
    private User owner;
    List<Message> messages;

    public Chatroom(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
