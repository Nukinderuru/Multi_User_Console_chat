package edu.school21.sockets.repositories;

import edu.school21.sockets.models.Chatroom;

import java.util.Optional;

public interface ChatroomRepository extends CrudRepository<Chatroom> {
    Optional<Chatroom> findByRoomName(String username);

    Long generateId();
}
