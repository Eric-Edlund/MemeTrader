package com.memetrader.webserver;

import com.memetrader.common.StockUser;
import com.zaxxer.hikari.HikariDataSource;
import com.memetrader.common.DatabaseConfig;
import com.memetrader.common.MemeStockRepository;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Repository
public class UserRepository {

    public UserRepository(DatabaseConfig databaseConfig) {
        this.dataSource = databaseConfig.dataSource();
    }

    private final HikariDataSource dataSource;
    private static Logger logger = Logger.getLogger(MemeStockRepository.class.getName());

    public Optional<StockUser> findByEmail(String email) {
        try (final var conn = dataSource.getConnection()) {
            try (final var stmt = conn.prepareStatement(
                    "SELECT id, password FROM Account WHERE email = ?"
            )) {
                stmt.setString(1, email);

                try (final var resultSet = stmt.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(new StockUser(
                                resultSet.getInt("id"),
                                email,
                                resultSet.getString("password"),
                                List.of()
                        ));
                    } else {
                        return Optional.empty();
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean saveVerified(String userName, String email, String password) {
        try (final var conn = dataSource.getConnection()) {
            try (final var stmt = conn.prepareStatement(
                    "INSERT INTO Account (name, email, password) VALUES (?, ?, ?)"
            )) {
                stmt.setString(1, userName);
                stmt.setString(2, email);
                stmt.setString(3, password);

                return stmt.execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
