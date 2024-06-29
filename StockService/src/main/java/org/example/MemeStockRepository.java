package org.example;
import org.springframework.web.bind.annotation.ResponseStatus;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.sql.*;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


@Repository
public class MemeStockRepository {

    public MemeStockRepository(DatabaseConfig databaseConfig) {
        this.dataSource = databaseConfig.dataSource();
    }

    private final HikariDataSource dataSource;
    private static Logger logger = Logger.getLogger(MemeStockRepository.class.getName());

    public long getTotalOwnedShares(int stockId) {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT COUNT(*) AS is_real FROM Stock WHERE id = ?"
            )) {
                stmt.setInt(1, stockId);
                try (ResultSet result = stmt.executeQuery()) {
                    if (result.next() && result.getInt("is_real") == 0) {
                        throw new StockNotFoundException("The stock " + stockId + " is not in the database.");
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT time, " +
                            "(COALESCE(SUM(CASE WHEN action = 'BUY' THEN numShares ELSE -numShares END), 0)) AS total_owned " +
                            "FROM Ledger " +
                            "WHERE stockId = ?"
            )) {
                stmt.setInt(1, stockId);

                try (ResultSet result = stmt.executeQuery()) {
                    if (result.next()) {
                        return result.getLong("total_owned");
                    } else {
                        return 0;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a list of points representing the total number of shares owned at all change points in the given range.
     * If no point exists at the exact startDate, we will search backwards
     * to find the latest point before it, meaning that the result may contain up to one point before the startDate.
     * If no point is found before the range, a point representing the creation of the stock with value 0 will be
     * added to the beginning.
     * @param stockId   Assumes the stock does exist.
     * @param startDate Inclusive start date.
     * @param endDate   Inclusive end date.
     * @return A list of total shares owned at each point from the given range for the given stock.
     */
    public List<PricePoint> getStockHistory(int stockId, OffsetDateTime startDate, OffsetDateTime endDate) {
        List<PricePoint> result = new ArrayList<>();
        final var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT time, " +
                            "(SELECT COALESCE(SUM(CASE WHEN action = 'BUY' THEN numShares ELSE -numShares END), 0) " +
                            "FROM MemeStockExchange.Ledger " +
                            "WHERE stockId = ? AND time <= l.time) AS total_owned " +
                            "FROM MemeStockExchange.Ledger l " +
                            "WHERE stockId = ? AND time BETWEEN ? AND ? " +
                            "ORDER BY time"
            )) {
                stmt.setInt(1, stockId);
                stmt.setInt(2, stockId);
                stmt.setObject(3, formatter.format(startDate));
                stmt.setObject(4, formatter.format(endDate));

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        result.add(new PricePoint(
                                rs.getTimestamp("time").toInstant().atOffset(ZoneOffset.UTC),
                                (rs.getLong("total_owned"))
                        ));
                    }
                }
            }

            if (result.isEmpty()) {
                try (final var stmt = conn.prepareStatement(
                        "SELECT time, " +
                                "(SELECT COALESCE(SUM(CASE WHEN action = 'BUY' THEN numShares ELSE -numShares END), 0) " +
                                "FROM MemeStockExchange.Ledger " +
                                "WHERE stockId = ? AND time <= ?) AS total_owned " +
                                "FROM MemeStockExchange.Ledger " +
                                "WHERE stockId = ? AND time <= ? " +
                                "ORDER BY time DESC LIMIT 1"
                )) {
                    stmt.setInt(1, stockId);
                    stmt.setString(2, formatter.format(startDate));
                    stmt.setInt(3, stockId);
                    stmt.setString(4, formatter.format(startDate));

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            result.add(new PricePoint(
                                    rs.getTimestamp("time").toInstant().atOffset(ZoneOffset.UTC),
                                    (rs.getLong("total_owned"))
                            ));
                        } else {
                            result.add(new PricePoint(startDate, 0));
                        }
                    }
                }
            }

            // Add a point for the creation date of the stock
            try (final var stmt = conn.prepareStatement("SELECT dateCreated FROM Stock WHERE id = ?")) {
                stmt.setInt(1, stockId);
                try (final var rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        final var dateCreated = rs.getTimestamp("dateCreated").toInstant().atOffset(ZoneOffset.UTC);
                        result.add(0, new PricePoint(
                                dateCreated,
                                0L
                        ));
                    }
                }
            }
            return result;
        } catch (SQLException e) {
            //TODO: LOG
            return new ArrayList<>();
        }
    }

    @Nullable
    public StockMetadataV1 getMetadata(int stockId) {
        try (final Connection conn = dataSource.getConnection()) {
            try (final var stmt = conn.prepareStatement(
                    "SELECT title, description, createdBy, symbol, imageLink FROM Stock WHERE id = ?"
            )) {
                stmt.setInt(1, stockId);

                try (final var result = stmt.executeQuery()) {
                    if (result.next()) {
                        return new StockMetadataV1(
                                result.getString("title"),
                                result.getString("description"),
                                result.getInt("createdBy"),
                                result.getString("symbol"),
                                result.getString("imageLink")
                        );
                    } else {
                        logger.log(Level.INFO, "No stock with the given id found");
                        return null;
                    }
                }
            } catch (SQLException e) {
                logger.log(Level.FINE, "Failed to query db for metadata of stock " + stockId, e);
                return null;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to connect to the database", e);
            return null;
        }
    }

    public int writeToLedger(int userId, int stockId, StockOrder stockOrder, long numShares, long totalPrice, boolean dryRun) throws StockOrderException {
        try (final var conn = dataSource.getConnection()) {
            if (dryRun) {
                conn.setAutoCommit(false);
            }
            try (final var stmt = conn.prepareStatement(
                    "INSERT INTO Ledger (action, userId, stockId, numShares, totalPrice) VALUES (?, ?, ?, ?, ?)"
            )) {
                stmt.setString(1, switch (stockOrder) {
                    case Buy -> "BUY";
                    case Sell -> "SELL";
                });
                stmt.setInt(2, userId);
                stmt.setInt(3, stockId);
                stmt.setLong(4, numShares);
                stmt.setLong(5, totalPrice);

                stmt.execute();

            } catch (SQLException ex) {
                logger.log(Level.FINE, "Failed to write to ledger");
                switch (ex.getSQLState()) {
                    case "45001": throw new StockOrderException(StockOrderException.Problem.InsufficientFunds);
                    case "45002": throw new StockOrderException(StockOrderException.Problem.InsufficientHoldings);
                    default: {
                        ex.printStackTrace();
                        throw new RuntimeException("Error writing to ledger");
                    }
                }
            }
            if (dryRun) {
                conn.rollback();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error connecting to database");
        }

        return -1;
    }

    /**
     * Attempts to create a stock from the given meme.
     *
     * @param source A unique id for the meme to prevent duplicates
     * @param url    The image url
     * @param name   The title of the meme
     */
    public void tryInsertMeme(String source, String url, String name) {
        try (final var conn = dataSource.getConnection()) {
            try (final var stmt = conn.prepareStatement(
                    "INSERT INTO Stock (title, description, createdBy, symbol, imageLink, source) SELECT ?, ?, 1, ?, ?, ? WHERE NOT EXISTS (SELECT 1 FROM Stock WHERE source = ?);"
            )) {
                stmt.setString(1, name);
                stmt.setString(2, null);
                stmt.setString(3, "SYMB");
                stmt.setString(4, url);
                stmt.setString(5, source);
                stmt.setString(6, source);

                stmt.execute();
            }


        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to connect to database");
            throw new RuntimeException(e);
        }
    }

    /**
     * @return The id of every meme missing a description.
     */
    public List<Integer> getMissingDescriptions() {
        try (final var conn = dataSource.getConnection()) {
            try (final var stmt = conn.prepareStatement(
                    "SELECT id FROM Stock WHERE description IS NULL"
            )) {
                try (final var resultSet = stmt.executeQuery()) {
                    List<Integer> ids = new ArrayList<>();
                    while (resultSet.next()) {
                        ids.add(resultSet.getInt("id"));
                    }
                    return ids;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to connect to database");
            throw new RuntimeException(e);
        }
    }

    /**
     * Updates the description to the given stock.
     *
     * @param stockId     Id of existing stock.
     * @param description Text content to update with.
     */
    public void addDescription(int stockId, String description) {
        try (final var conn = dataSource.getConnection()) {
            try (final var stmt = conn.prepareStatement(
                    "UPDATE Stock SET description = ? WHERE id = ?"
            )) {
                stmt.setString(1, description);
                stmt.setInt(2, stockId);
                stmt.execute();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to connect to database");
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets a list of all stock metadata.
     *
     * @return List of metadata for all stocks.
     */
    public List<StockSearchResultV1> getAllMetadata() {
        try (final Connection conn = dataSource.getConnection()) {
            try (var stmt = conn.prepareStatement(
                    "SELECT id, title, symbol, imageLink FROM Stock"
            )) {
                try (final var resultSet = stmt.executeQuery()) {
                    final List<StockSearchResultV1> result = new ArrayList<>();
                    while (resultSet.next()) {
                        result.add(new StockSearchResultV1(
                                resultSet.getInt("id"),
                                resultSet.getString("title"),
                                resultSet.getString("symbol"),
                                resultSet.getString("imageLink")
                        ));
                    }
                    return result;
                }
            } catch (SQLException e) {
                logger.log(Level.FINE, "Failed to query db for all stock metadatas", e);
                return List.of();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to connect to the database", e);
            return List.of();
        }
    }

    /**
     * Returns the total number of shares owned by a user for a specific stock.
     *
     * @param userId  The id of the user.
     * @param stockId The id of the stock.
     * @return The total number of shares owned by the user.
     * @throws StockNotFoundException If the stock does not exist.
     */
    public long getTotalOwnedSharesByUser(int userId, int stockId) {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT COUNT(*) AS is_real FROM Stock WHERE id = ?"
            )) {
                stmt.setInt(1, stockId);
                try (ResultSet result = stmt.executeQuery()) {
                    if (result.next() && result.getInt("is_real") == 0) {
                        throw new StockNotFoundException("The stock " + stockId + " is not in the database.");
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT COALESCE(SUM(CASE WHEN action = 'BUY' THEN numShares ELSE -numShares END), 0) AS total_owned " +
                            "FROM Ledger " +
                            "WHERE userId = ? AND stockId = ?"
            )) {
                stmt.setInt(1, userId);
                stmt.setInt(2, stockId);

                try (ResultSet result = stmt.executeQuery()) {
                    if (result.next()) {
                        return result.getInt("total_owned");
                    } else {
                        return 0;
                    }
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Failed to query database for total owned shares", e);
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to connect to database", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * @return The date of the last published article, or null if no article has ever been published.
     */
    public @Nullable OffsetDateTime lastPublishedStory() {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT published FROM Article ORDER BY published DESC LIMIT 1"
            )) {
                try (final var resultSet = stmt.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getTimestamp("published").toInstant().atOffset(ZoneOffset.UTC);
                    } else {
                        return null;
                    }
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to connect to database", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * @return List of all available stock ids.
     */
    public List<Integer> getAllStocks() {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id FROM Stock"
            )) {
                List<Integer> result = new ArrayList<>();
                try (final var resultSet = stmt.executeQuery()) {
                    while (resultSet.next()) {
                        result.add(resultSet.getInt("id"));
                    }
                }
                return result;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to connect to database", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds the given article to the database under the current date.
     * @param articleTitle
     * @param articleBody
     * @param imageUrl The url for the associated article image.
     */
    public void addArticle(String articleTitle, String articleBody, String imageUrl) {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO Article (title, body, imageUrl) VALUES (?, ?, ?)"
            )) {
                stmt.setString(1, articleTitle);
                stmt.setString(2, articleBody);
                stmt.setString(3, imageUrl);
                stmt.execute();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to connect to database", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * @param num Number of most recent articles to get.
     * @return The most recent articles, most recent first.
     */
    public List<StockArticle> getArticles(int num) {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id, title, published, body, imageUrl FROM Article ORDER BY published DESC LIMIT ?"
            )) {
                stmt.setInt(1, num);

                List<StockArticle> result = new ArrayList<>();
                try (final var resultSet = stmt.executeQuery()) {
                    while (resultSet.next()) {
                        result.add(new StockArticle(
                                resultSet.getInt("id"),
                                resultSet.getString("title"),
                                resultSet.getTimestamp("published").toInstant().atOffset(ZoneOffset.UTC),
                                resultSet.getString("body"),
                                resultSet.getString("imageUrl")
                        ));
                    }
                }
                return result;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to connect to database", e);
            throw new RuntimeException(e);
        }
    }

    public byte[] getArticleImage(int articleId) {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT image FROM Article WHERE id = ?"
            )) {
                stmt.setInt(1, articleId);

                try (final var resultSet = stmt.executeQuery()) {
                    resultSet.next();
                    final var image = resultSet.getBytes("image");
                    System.out.println(image.length);
                    return image;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to connect to database", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the account balance of the given user.
     *
     * @param userId The ID of the user.
     * @return The account balance of the user.
     */
    public long getAcctBalance(int userId) {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT balance FROM AccountBalance WHERE userId = ?"
            )) {
                stmt.setInt(1, userId);

                try (ResultSet result = stmt.executeQuery()) {
                    if (result.next()) {
                        return result.getLong("balance");
                    } else {
                        // throw new AccountNotFoundException("The account " + userId + " is not in the database.");
                        throw new RuntimeException("No account found");
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to connect to database", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a list of holdings for the given user.
     *
     * @param userId The ID of the user.
     * @return A list of holdings for the user.
     */
    public List<Holding> getHoldings(int userId) {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT stockId, numShares FROM Holding WHERE userId = ? AND numShares != 0"
            )) {
                stmt.setInt(1, userId);

                try (ResultSet result = stmt.executeQuery()) {
                    List<Holding> holdings = new ArrayList<>();
                    while (result.next()) {
                        holdings.add(new Holding(result.getInt("stockId"), result.getLong("numShares"), null));
                    }
                    return holdings;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to connect to database", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the given user's bio to the given bio.
     * @param userId Id of existing user.
     * @param newBio New bio content.
     */
    public void setUserBio(int userId, String newBio) {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE Account SET bio = ? WHERE id = ?"
            )) {
                stmt.setString(1, newBio);
                stmt.setInt(2, userId);
                stmt.execute();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to connect to database", e);
            throw new RuntimeException(e);
        }
    }

    public @NonNull UserMetadata getUserMetadata(int userId) {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT name, email, bio FROM Account WHERE id = ?"
            )) {
                stmt.setInt(1, userId);

                try (var resultSet = stmt.executeQuery()) {
                    if (resultSet.next()) {
                        return new UserMetadata(
                                userId,
                                resultSet.getString("name"),
                                resultSet.getString("email"),
                                resultSet.getString("bio")
                        );
                    } else {
                        throw new RuntimeException("Missing user");
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed while querying database");
            throw new RuntimeException(e);
        }
    }

    public Map<OffsetDateTime, BalanceHoldingsPair> getAccountHistory(OffsetDateTime startDate, OffsetDateTime endDate, int userId) {
        final var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));

        try (final var conn = dataSource.getConnection()) {
            try (final var stmt = conn.prepareStatement(
                    // Every update in either the user's holding history, balance, or the value of any stock.
                    "(\n" +
                            "SELECT L.time, L.userId, L.stockId, HH.sharesOwned, SH.totalSharesOwned, BH.balance, BH.userId AS bal_userId\n" +
                            "FROM Ledger L\n" +
                            "LEFT JOIN HoldingHistory HH ON L.id = HH.ledgerId\n" +
                            "JOIN StockHistory SH ON L.id = SH.ledgerId\n" +
                            "LEFT JOIN AccountBalanceHistory BH ON L.id = BH.ledgerId\n" +
                            // "WHERE (BH.userId = ? OR BH.userId IS NULL) AND (HH.userId = ? OR HH.userId IS NULL)\n" +
                            "AND L.time < ?\n" +
                            "ORDER BY L.time DESC LIMIT 1\n" +
                            ")\n" +
                            "UNION ALL" +
                    "(SELECT L.time, L.userId, L.stockId, HH.sharesOwned, SH.totalSharesOwned, BH.balance, BH.userId AS bal_userId\n" +
                            "FROM Ledger L\n" +
                            "LEFT JOIN HoldingHistory HH ON L.id = HH.ledgerId\n" +
                            "JOIN StockHistory SH ON L.id = SH.ledgerId\n" +
                            "LEFT JOIN AccountBalanceHistory BH ON L.id = BH.ledgerId\n" +
                            // "WHERE (BH.userId = ? OR BH.userId IS NULL) AND (HH.userId = ? OR HH.userId IS NULL)\n" +
                            "AND L.time BETWEEN ? AND ?\n" +
                            "ORDER BY L.time" +
                            ")\n" +
                            "UNION ALL\n" +
                            "(\n" +
                            "SELECT L.time, L.userId, L.stockId, HH.sharesOwned, SH.totalSharesOwned, BH.balance, BH.userId AS bal_userId\n" +
                            "FROM Ledger L\n" +
                            "LEFT JOIN HoldingHistory HH ON L.id = HH.ledgerId\n" +
                            "JOIN StockHistory SH ON L.id = SH.ledgerId\n" +
                            "LEFT JOIN AccountBalanceHistory BH ON L.id = BH.ledgerId\n" +
                            // "WHERE (BH.userId = ? OR BH.userId IS NULL) AND (HH.userId = ? OR HH.userId IS NULL)\n" +
                            "AND L.time > ?\n" +
                            "ORDER BY L.time ASC LIMIT 1\n" +
                            ");"

//                    "SELECT L.time, L.userId, L.stockId, HH.sharesOwned, SH.totalSharesOwned, BH.balance, BH.userId AS bal_userId " +
//                            "FROM Ledger L " +
//                            "LEFT JOIN HoldingHistory HH ON L.id = HH.ledgerId " +
//                            "JOIN StockHistory SH ON L.id = SH.ledgerId " +
//                            "LEFT JOIN AccountBalanceHistory BH ON L.id = BH.ledgerId " +
//                            // "WHERE (BH.userId = ? OR BH.userId IS NULL) OR (HH.userId = ? OR HH.userId IS NULL) " +
//                            "AND L.time BETWEEN ? AND ? " +
//                            "ORDER BY L.time;"
            )) {
                // stmt.setInt(1, userId);
                // stmt.setInt(2, userId);
                stmt.setObject(1, formatter.format(startDate));
                stmt.setObject(2, formatter.format(endDate));
                stmt.setObject(3, formatter.format(startDate));
                stmt.setObject(4, formatter.format(endDate));

                Map<OffsetDateTime, BalanceHoldingsPair> result = new HashMap<>();
                try (final var resultSet = stmt.executeQuery()) {
                    Long lastBalance = null;
                    Map<Integer, RawHolding> lastHoldings = new HashMap<>();

                    // Aggregate the results? Normalize the results? We're filling in the wholes, because the result set
                    // is just a list of deltas.
                    while (resultSet.next()) {
                        final long bal = resultSet.getLong("balance");
                        if (!resultSet.wasNull()) {
                            if (resultSet.getInt("bal_userId") == userId) {
                                lastBalance = bal;
                            }
                        }

                        final int stockId = resultSet.getInt("stockId");
                        lastHoldings.putIfAbsent(stockId, new RawHolding(stockId, 0, 0));
                        var currentRawHolding = lastHoldings.get(stockId);
                        final long totalSharesOwned = resultSet.getLong("totalSharesOwned");
                        if (!resultSet.wasNull()) {
                            lastHoldings.put(stockId, new RawHolding(stockId, currentRawHolding.amtOwned(), totalSharesOwned));
                        }
                        currentRawHolding = lastHoldings.putIfAbsent(stockId, new RawHolding(stockId, 0, 0));
                        final long amtOwned = resultSet.getLong("sharesOwned");
                        if (!resultSet.wasNull()) {
                            lastHoldings.put(stockId, new RawHolding(stockId, amtOwned, currentRawHolding.totalOwned()));
                        }

                        final var time = resultSet.getTimestamp("time").toInstant().atOffset(ZoneOffset.UTC);
                        result.put(time, new BalanceHoldingsPair(
                                lastBalance == null ? 0 : lastBalance, //TODO: I *think* that nullability is impossible for new data
                                lastHoldings.values().stream().toList()
                        ));
                    }
                }

                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public record BalanceHoldingsPair(long balance, List<RawHolding> holdings) {};
    public record RawHolding(int stockId, long amtOwned, long totalOwned) {
    }
}