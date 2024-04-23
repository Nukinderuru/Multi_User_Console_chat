package edu.school21.sockets.app;

import com.zaxxer.hikari.HikariDataSource;
import edu.school21.sockets.config.SocketsApplicationConfig;
import edu.school21.sockets.server.Server;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {
        Integer portNum = getPort(args);
        ApplicationContext context = new AnnotationConfigApplicationContext(SocketsApplicationConfig.class);
        createTable(context);
        Server server = context.getBean("server", Server.class);
        server.start(portNum);
    }

    private static Integer getPort(String[] args) {
        if (args.length != 1) {
            System.out.println("Wrong number of arguments! There should be one: port number");
            System.exit(1);
        }
        String[] parts = args[0].split("=");
        return Integer.parseInt(parts[1]);
    }

    private static void createTable(ApplicationContext context) {
        DataSource dataSource = context.getBean("hikariDataSource", HikariDataSource.class);
        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();
//            stmt.execute("DROP SCHEMA IF EXISTS sockets CASCADE;");
            stmt.execute("CREATE SCHEMA IF NOT EXISTS sockets;");
            stmt.execute("CREATE TABLE IF NOT EXISTS sockets.user (id INTEGER PRIMARY KEY NOT NULL, " +
                    "username VARCHAR(50) NOT NULL, password VARCHAR(100) NOT NULL);");
            stmt.execute("CREATE TABLE IF NOT EXISTS sockets.chatroom (id INTEGER PRIMARY KEY NOT NULL, " +
                    "name VARCHAR(25) NOT NULL, ownerId INTEGER REFERENCES sockets.user(id));");
            stmt.execute("CREATE TABLE IF NOT EXISTS sockets.message " +
                    "(id INTEGER PRIMARY KEY NOT NULL, userId INTEGER REFERENCES sockets.user(id), " +
                    "roomId INTEGER REFERENCES sockets.chatroom(id), text VARCHAR(255) NOT NULL, " +
                    "time TIMESTAMP NOT NULL);");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
