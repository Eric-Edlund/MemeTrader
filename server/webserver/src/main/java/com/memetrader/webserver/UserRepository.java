package com.memetrader.webserver;

import com.memetrader.common.StockUser;
import com.zaxxer.hikari.HikariDataSource;
import com.memetrader.common.DatabaseConfig;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Repository
public class UserRepository {

    public UserRepository(DatabaseConfig databaseConfig) {
        this.dataSource = databaseConfig.dataSource();
    }

    private final HikariDataSource dataSource;
    private static Logger logger = Logger.getGlobal();

    public Optional<StockUser> findByEmail(String email) {
        try (final var conn = dataSource.getConnection()) {
            try (final var stmt = conn.prepareStatement(
                    "SELECT id, password FROM Account WHERE email = ?")) {
                stmt.setString(1, email);

                try (final var resultSet = stmt.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(new StockUser(
                                resultSet.getInt("id"),
                                email,
                                resultSet.getString("password"),
                                List.of()));
                    } else {
                        return Optional.empty();
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates an unverified account in the verification queue.
     * 
     * @param password Hashed
     * @return The account creation attempt id, or the error if something
     *         went wrong.
     */
    public @NonNull Result<String, AccountCreationError> saveUnverifiedAccount(@NonNull String email,
            @NonNull String password, @NonNull String code) {
        final String uuid = UUID.randomUUID().toString();
        try (final var conn = dataSource.getConnection()) {
            try (final var stmt = conn.prepareStatement(
                    "INSERT INTO AccountVerificationQueue (email, password, code, attemptId) VALUES (?, ?, ?, ?)")) {
                stmt.setString(1, email);
                stmt.setString(2, password);
                stmt.setString(3, code);
                stmt.setString(4, uuid);

                stmt.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "SQL ERROR while saving unverified user to database: " + e.getMessage());
            return Result.err(AccountCreationError.Unknown);
        }

        return Result.ok(uuid);
    }

    /**
     * If a user creation attempt is found with the matching attemptId and code,
     * the creation attempt is promoted to a full account.
     * 
     * @returns null if the attempt is not found or expired, email of verified
     *          account otherwise.
     */
    public String verifyAccount(@NonNull String attemptId, @NonNull String code) {
        String email = null, password = null;

        try (final var conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try (final var stmt = conn
                    .prepareStatement("SELECT email, password FROM AccountVerificationQueue WHERE attemptId = ?")) {
                stmt.setString(1, attemptId);

                try (final var resultSet = stmt.executeQuery()) {
                    if (resultSet.next()) {
                        email = resultSet.getString("email");
                        password = resultSet.getString("password");
                    } else {
                        return null;
                    }
                }
            }

            try (final var stmt = conn.prepareStatement(
                    "DELETE FROM AccountVerificationQueue WHERE attemptId = ? AND code = ?")) {
                stmt.setString(1, attemptId);
                stmt.setString(2, code);

                int count = stmt.executeUpdate();
                if (count == 0) {
                    return null;
                }
            }

            try (final var stmt = conn.prepareStatement(
                    "INSERT INTO Account (email, password) VALUES (?, ?)")) {
                stmt.setString(1, email);
                stmt.setString(2, password);

                stmt.execute();
            }

            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            return null;
        }

        return email;
    }

    public boolean addFundsToAccount(long userId, long amt) {
        try (var conn = dataSource.getConnection()) {
            try (var stmt = conn.prepareStatement("UPDATE AccountBalance WHERE userId = ? SET balance = balance + ?")) {
                stmt.setLong(1, userId);
                stmt.setLong(2, amt);

                return stmt.execute();
            }
        } catch (SQLException e) {
            return false;
        }
    }
}
