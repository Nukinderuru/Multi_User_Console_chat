package edu.school21.sockets.repositories;

import edu.school21.sockets.models.Chatroom;
import edu.school21.sockets.models.Message;
import edu.school21.sockets.models.User;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MessagesRepositoryImpl implements MessagesRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final UsersRepository usersRepository;
    private final ChatroomRepository chatroomRepository;

    public MessagesRepositoryImpl(DataSource dataSource, UsersRepository usersRepository,
                                  ChatroomRepository chatroomRepository) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.usersRepository = usersRepository;
        this.chatroomRepository = chatroomRepository;
    }

    @Override
    public Optional<Message> findById(Long id) {
        String sqlQuery = "SELECT * FROM sockets.message WHERE id = :id;";
        SqlParameterSource namedParameters = new MapSqlParameterSource("id", id);
        Message result = namedParameterJdbcTemplate.query(sqlQuery, namedParameters,
                rs -> {
                    User user = null;
                    Chatroom chatroom = null;
                    while (rs.next()) {
                        Optional<User> optUser = usersRepository.findById(rs.getLong(2));
                        if (optUser.isPresent()) {
                            user = optUser.get();
                        }
                        Optional<Chatroom> optRoom = chatroomRepository.findById(rs.getLong(3));
                        if (optRoom.isPresent()) {
                            chatroom = optRoom.get();
                        }
                        Message message = new Message(rs.getLong(1), user,
                                chatroom, rs.getString(4), rs.getTimestamp(5));
                        return message;
                    }
                    return null;
                });
        return (result == null) ? Optional.ofNullable(null) : Optional.of(result);
    }

    @Override
    public List<Message> findAll() {
        String sqlQuery = "SELECT * FROM sockets.message;";
        List<Message> messages = namedParameterJdbcTemplate.query(sqlQuery, new BeanPropertyRowMapper<>(Message.class));
        return (messages.isEmpty()) ? null : messages;
    }

    private List<Message> findLimited(int limit, Long roomId) {
        String sqlQuery = "SELECT * FROM sockets.message WHERE roomId = :roomId ORDER BY time DESC LIMIT :limit;";
        List<Message> messages = namedParameterJdbcTemplate.query(sqlQuery, new MapSqlParameterSource().
                        addValue("roomId", roomId).addValue("limit", limit),
                new BeanPropertyRowMapper<>(Message.class));
        return (messages.isEmpty()) ? null : messages;
    }

    @Override
    public void save(Message entity) {
        String sqlQuery = "INSERT INTO sockets.message (id, userId, roomId, text, time) " +
                "VALUES (:id, :userId, :roomId, :text, :time);";
        if ((namedParameterJdbcTemplate.update(sqlQuery, new MapSqlParameterSource().
                addValue("id", entity.getId()).
                addValue("userId", entity.getAuthor().getId()).
                addValue("roomId", entity.getRoom().getId()).
                addValue("text", entity.getText()).
                addValue("time", entity.getTimestamp()))) == 0) {
            System.out.println("An error occurred! Message with id = " + entity.getId() + " not saved");
        }
    }

    @Override
    public void update(Message entity) {
        String sqlQuery = "UPDATE sockets.message SET userId = :userId, roomId = :roomId, " +
                "text = :text, time = :time WHERE id = :id;";
        if ((namedParameterJdbcTemplate.update(sqlQuery, new MapSqlParameterSource().
                addValue("id", entity.getId()).
                addValue("userId", entity.getAuthor().getId()).
                addValue("roomId", entity.getRoom().getId()).
                addValue("text", entity.getText()).
                addValue("time", entity.getTimestamp()))) == 0) {
            System.out.println("An error occurred! Message with id = " + entity.getId() + " not updated");
        }
    }

    @Override
    public void delete(Long id) {
        String sqlQuery = "DELETE FROM sockets.message WHERE id = :id;";
        if ((namedParameterJdbcTemplate.update(sqlQuery, new MapSqlParameterSource().
                addValue("id", id))) == 0) {
            System.out.println("An error occurred! Message with id = " + id + " wasn't deleted.");
        }
    }

    @Override
    public Long generateId() {
        String sqlQuery = "SELECT MAX(id) FROM sockets.message;";
        Integer maxId = namedParameterJdbcTemplate.queryForObject(sqlQuery, new MapSqlParameterSource(), Integer.class);
        Long id = (maxId == null) ? 1L : Integer.toUnsignedLong(maxId) + 1;
        return id;
    }

    public List<Message> findAllByRoomId(Long roomId) {
        List<Message> allMessages = findLimited(30, roomId);
        if (allMessages != null) {
            ArrayList<Message> messages = new ArrayList<>();
            for (Message msg : allMessages) {
                Optional<Message> optMsg = findById(msg.getId());
                optMsg.ifPresent(messages::add);
            }
            return messages;
        }
        return null;
    }
}
