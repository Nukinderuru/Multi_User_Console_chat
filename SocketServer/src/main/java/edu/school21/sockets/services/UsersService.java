package edu.school21.sockets.services;

import edu.school21.sockets.models.User;

import java.util.Optional;

public interface UsersService {
    User signUp(String username, String password);

    Optional<User> signIn(String username, String password);
}
