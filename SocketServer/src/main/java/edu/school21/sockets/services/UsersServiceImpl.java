package edu.school21.sockets.services;

import edu.school21.sockets.models.User;
import edu.school21.sockets.repositories.UsersRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Optional;

@Component
public class UsersServiceImpl implements UsersService {
    private final UsersRepository usersRepository;
    private final PasswordEncoder encoder;

    public UsersServiceImpl(UsersRepository usersRepository, PasswordEncoder encoder) {
        this.usersRepository = usersRepository;
        this.encoder = encoder;
    }

    @Override
    public User signUp(String username, String password) {
        Long id = usersRepository.generateId();
        User user = new User(id, username, encoder.encode(password), new ArrayList<>(), new ArrayList<>());
        usersRepository.save(user);
        return user;
    }

    @Override
    public Optional<User> signIn(String username, String password) {
        Optional<User> optUser = usersRepository.findByUsername(username);
        if (optUser.isPresent()) {
            User user = optUser.get();
            if (encoder.matches(password, user.getPassword())) {
                return Optional.of(user);
            } else {
                System.out.println("Incorrect password");
            }
        } else {
            System.out.println("No user with such username found: " + username);
        }
        return Optional.empty();
    }
}
