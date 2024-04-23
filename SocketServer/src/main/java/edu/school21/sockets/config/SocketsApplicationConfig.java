package edu.school21.sockets.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import edu.school21.sockets.repositories.ChatroomRepositoryImpl;
import edu.school21.sockets.repositories.MessagesRepositoryImpl;
import edu.school21.sockets.repositories.UsersRepository;
import edu.school21.sockets.repositories.UsersRepositoryImpl;
import edu.school21.sockets.server.Server;
import edu.school21.sockets.services.UsersServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@PropertySources({@PropertySource("classpath:db.properties")})
public class SocketsApplicationConfig {
    @Autowired
    private Environment environment;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    @Autowired
    public HikariConfig hikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(environment.getRequiredProperty("db.url"));
        config.setUsername(environment.getRequiredProperty("db.user"));
        config.setPassword(environment.getRequiredProperty("db.password"));
        return config;
    }

    @Bean
    @Autowired
    public HikariDataSource hikariDataSource(HikariConfig hikariConfig) {
        return new HikariDataSource(hikariConfig);
    }

    @Bean
    @Autowired
    public UsersRepositoryImpl usersRepository(HikariDataSource hikariDataSource) {
        return new UsersRepositoryImpl(hikariDataSource);
    }

    @Bean
    @Autowired
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Autowired
    public UsersServiceImpl usersService(UsersRepository usersRepository, PasswordEncoder encoder) {
        return new UsersServiceImpl(usersRepository, encoder);
    }

    @Bean
    @Autowired
    public MessagesRepositoryImpl messagesRepository(HikariDataSource hikariDataSource,
                                                     UsersRepositoryImpl usersRepository,
                                                     ChatroomRepositoryImpl chatroomRepository) {
        return new MessagesRepositoryImpl(hikariDataSource, usersRepository, chatroomRepository);
    }

    @Bean
    @Autowired
    public ChatroomRepositoryImpl chatroomRepository(HikariDataSource hikariDataSource) {
        return new ChatroomRepositoryImpl(hikariDataSource);
    }

    @Bean
    @Autowired
    public Server server(UsersServiceImpl usersService, MessagesRepositoryImpl messagesRepository,
                         ChatroomRepositoryImpl chatroomRepository) {
        return new Server(usersService, messagesRepository, chatroomRepository);
    }
}
