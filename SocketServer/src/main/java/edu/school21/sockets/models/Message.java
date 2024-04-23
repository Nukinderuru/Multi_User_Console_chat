package edu.school21.sockets.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private Long id;
    private User author;
    private Chatroom room;
    private String text;
    private Timestamp timestamp;
}
