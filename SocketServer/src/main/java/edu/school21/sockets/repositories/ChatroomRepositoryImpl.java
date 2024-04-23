package edu.school21.sockets.repositories;

import edu.school21.sockets.models.Chatroom;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@Component
public class ChatroomRepositoryImpl implements ChatroomRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public ChatroomRepositoryImpl(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public Optional findById(Long id) {
        String sqlQuery = "SELECT * FROM sockets.chatroom WHERE id = :id;";
        Chatroom room = namedParameterJdbcTemplate.query(sqlQuery,
                new MapSqlParameterSource().addValue("id", id),
                new BeanPropertyRowMapper<>(Chatroom.class)).stream().findAny().orElse(null);
        return (room == null) ? Optional.empty() : Optional.of(room);
    }

    @Override
    public List findAll() {
        String sqlQuery = "SELECT * FROM sockets.chatroom";
        List<Chatroom> chatrooms = namedParameterJdbcTemplate.query(sqlQuery, new BeanPropertyRowMapper<>(Chatroom.class));
        return (chatrooms.isEmpty()) ? null : chatrooms;
    }

    @Override
    public void save(Chatroom entity) {
        String sqlQuery = "INSERT INTO sockets.chatroom (id, name, ownerId) VALUES (:id, :name, :ownerId);";
        if ((namedParameterJdbcTemplate.update(sqlQuery, new MapSqlParameterSource().
                addValue("id", entity.getId()).
                addValue("name", entity.getName()).
                addValue("ownerId", entity.getOwner().getId()))) == 0) {
            System.out.println("An error occurred! User with id = " + entity.getId() + " not saved");
        }
    }

    @Override
    public void update(Chatroom entity) {
        String sqlQuery = "UPDATE sockets.chatroom SET name = :name, ownerId = :ownerId WHERE id = :id;";
        if ((namedParameterJdbcTemplate.update(sqlQuery, new MapSqlParameterSource().
                addValue("id", entity.getId()).
                addValue("name", entity.getName()).
                addValue("ownerId", entity.getOwner().getId()))) == 0) {
            System.out.println("An error occurred! User with id = " + entity.getId() + " not updated");
        }
    }

    @Override
    public void delete(Long id) {
        String sqlQuery = "DELETE FROM sockets.chatroom WHERE id = :id;";
        if ((namedParameterJdbcTemplate.update(sqlQuery, new MapSqlParameterSource().
                addValue("id", id))) == 0) {
            System.out.println("An error occurred! User with id = " + id + " wasn't deleted.");
        }
    }

    @Override
    public Optional<Chatroom> findByRoomName(String name) {
        String sqlQuery = "SELECT * FROM sockets.chatroom WHERE name = :name;";
        Chatroom chatroom = namedParameterJdbcTemplate.query(sqlQuery,
                new MapSqlParameterSource().addValue("name", name),
                new BeanPropertyRowMapper<>(Chatroom.class)).stream().findAny().orElse(null);
        return (chatroom == null) ? Optional.ofNullable(null) : Optional.of(chatroom);
    }

    @Override
    public Long generateId() {
        String sqlQuery = "SELECT MAX(id) FROM sockets.chatroom;";
        Integer maxId = namedParameterJdbcTemplate.queryForObject(sqlQuery, new MapSqlParameterSource(), Integer.class);
        Long id = (maxId == null) ? 1L : Integer.toUnsignedLong(maxId) + 1;
        return id;
    }
}
