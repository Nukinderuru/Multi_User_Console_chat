package edu.school21.sockets.repositories;

import edu.school21.sockets.models.Chatroom;
import edu.school21.sockets.models.User;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class UsersRepositoryImpl implements UsersRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public UsersRepositoryImpl(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public Optional<User> findById(Long id) {
        String sqlQuery = "SELECT * FROM sockets.user WHERE id = :id;";
        User user = namedParameterJdbcTemplate.query(sqlQuery,
                new MapSqlParameterSource().addValue("id", id),
                new BeanPropertyRowMapper<>(User.class)).stream().findAny().orElse(null);
        return (user == null) ? Optional.empty() : Optional.of(user);
    }

    @Override
    public List<User> findAll() {
        String sqlQuery = "SELECT * FROM sockets.user";
        List<User> users = namedParameterJdbcTemplate.query(sqlQuery, new BeanPropertyRowMapper<>(User.class));
        return (users.isEmpty()) ? null : users;
    }

    @Override
    public void save(User entity) {
        String sqlQuery = "INSERT INTO sockets.user (id, username, password) VALUES (:id, :username, :password);";
        if ((namedParameterJdbcTemplate.update(sqlQuery, new MapSqlParameterSource().
                addValue("id", entity.getId()).
                addValue("username", entity.getUsername()).
                addValue("password", entity.getPassword()))) == 0) {
            System.out.println("An error occurred! User with id = " + entity.getId() + " not saved");
        }
    }

    @Override
    public void update(User entity) {
        String sqlQuery = "UPDATE sockets.user SET username = :username, password = :password WHERE id = :id;";
        if ((namedParameterJdbcTemplate.update(sqlQuery, new MapSqlParameterSource().
                addValue("id", entity.getId()).
                addValue("username", entity.getUsername()).
                addValue("password", entity.getPassword()))) == 0) {
            System.out.println("An error occurred! User with id = " + entity.getId() + " not updated");
        }
    }

    @Override
    public void delete(Long id) {
        String sqlQuery = "DELETE FROM sockets.user WHERE id = :id;";
        if ((namedParameterJdbcTemplate.update(sqlQuery, new MapSqlParameterSource().
                addValue("id", id))) == 0) {
            System.out.println("An error occurred! User with id = " + id + " wasn't deleted.");
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String query = "WITH users as (SELECT * FROM sockets.user where username = :username ORDER BY id),\n" +
                "    authorRoom as (SELECT DISTINCT userId, roomId FROM sockets.message)\n" +
                "    select u.*, ar.roomId as roomId, null as ownerRoomId, \n" +
                "    cr.name as chatName from users u\n" +
                "    join authorRoom ar ON u.id = ar.userId\n" +
                "    join sockets.chatroom cr ON ar.roomId = cr.id\n" +
                "    union all\n" +
                "    select u.*, null as roomId, cr.id, cr.name as chatName from users u\n" +
                "    join sockets.chatroom cr ON u.id = cr.ownerId;";
        SqlParameterSource namedParameters = new MapSqlParameterSource("username", username);
        User result = namedParameterJdbcTemplate.query(query, namedParameters, rs -> {
            ArrayList<Chatroom> rooms = new ArrayList<>();
            ArrayList<Chatroom> createdRooms = new ArrayList<>();
            while (rs.next()) {
                User user = new User(rs.getLong(1), rs.getString(2),
                        rs.getString(3), rooms, createdRooms);
                Integer roomId = (Integer) rs.getObject(4);
                Integer ownerRoomId = (Integer) rs.getObject(5);
                String roomName = rs.getString(6);

                if (roomId != null) {
                    user.getUsedRooms().add(new Chatroom(roomId.longValue(), roomName));
                } else if (ownerRoomId != null) {
                    user.getCreatedRooms().add(new Chatroom(ownerRoomId.longValue(), roomName, user, null));
                }
                return user;
            }
            return null;
        });
        if (result == null) {
            String sqlQuery = "SELECT * FROM sockets.user WHERE username = :username;";
            User user = namedParameterJdbcTemplate.query(sqlQuery,
                    new MapSqlParameterSource().addValue("username", username),
                    new BeanPropertyRowMapper<>(User.class)).stream().findAny().orElse(null);
            if (user != null) {
                user.setCreatedRooms(new ArrayList<>());
                user.setUsedRooms(new ArrayList<>());
            }
            result = user;
        }
        return (result == null) ? Optional.ofNullable(null) : Optional.of(result);
    }

    @Override
    public Long generateId() {
        String sqlQuery = "SELECT MAX(id) FROM sockets.user;";
        Integer maxId = namedParameterJdbcTemplate.queryForObject(sqlQuery, new MapSqlParameterSource(), Integer.class);
        Long id = (maxId == null) ? 1L : Integer.toUnsignedLong(maxId) + 1;
        return id;
    }
}
