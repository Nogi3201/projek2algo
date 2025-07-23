// File: GameDAO.java (VERSI FINAL LENGKAP)
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameDAO {
    
    private Game mapResultSetToGame(ResultSet rs) throws SQLException {
        Game g = new Game();
        g.setId(rs.getInt("id"));
        g.setName(rs.getString("name"));
        g.setGenre(rs.getString("genre"));
        g.setRating(rs.getFloat("rating"));
        g.setReleaseDate(rs.getString("release_date"));
        g.setPopularity(rs.getInt("popularity"));
        g.setDescription(rs.getString("description"));
        g.setYoutubeId(rs.getString("youtube_id")); 

        Blob imageBlob = rs.getBlob("image");
        if (imageBlob != null) {
            g.setImage(imageBlob.getBytes(1, (int) imageBlob.length()));
        }
        return g;
    }

    public List<Game> getRecommendedGames(int currentGameId, String genre) {
        List<Game> list = new ArrayList<>();
        if (genre != null && !genre.isEmpty()) {
            String primaryGenre = genre.split(",")[0].trim();
            String sql = "SELECT * FROM games WHERE genre LIKE ? AND id != ? ORDER BY popularity DESC LIMIT 3";
            try (Connection conn = DBConnection.connect();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "%" + primaryGenre + "%");
                stmt.setInt(2, currentGameId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToGame(rs));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public void addOrUpdateReview(UserReview review) throws SQLException {
        String checkSql = "SELECT review_id FROM user_reviews WHERE user_id = ? AND game_id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, review.getUserId());
            checkStmt.setInt(2, review.getGameId());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String updateSql = "UPDATE user_reviews SET rating = ?, comment = ? WHERE user_id = ? AND game_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, review.getRating());
                    updateStmt.setString(2, review.getComment());
                    updateStmt.setInt(3, review.getUserId());
                    updateStmt.setInt(4, review.getGameId());
                    updateStmt.executeUpdate();
                }
            } else {
                String insertSql = "INSERT INTO user_reviews (game_id, user_id, rating, comment) VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, review.getGameId());
                    insertStmt.setInt(2, review.getUserId());
                    insertStmt.setInt(3, review.getRating());
                    insertStmt.setString(4, review.getComment());
                    insertStmt.executeUpdate();
                }
            }
        }
    }

    public List<UserReview> getReviewsForGame(int gameId) {
        List<UserReview> reviews = new ArrayList<>();
        String sql = "SELECT r.*, u.username FROM user_reviews r JOIN users u ON r.user_id = u.id WHERE r.game_id = ? ORDER BY r.created_at DESC";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gameId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UserReview review = new UserReview();
                    review.setReviewId(rs.getInt("review_id"));
                    review.setGameId(rs.getInt("game_id"));
                    review.setUserId(rs.getInt("user_id"));
                    review.setUsername(rs.getString("username")); 
                    review.setRating(rs.getInt("rating"));
                    review.setComment(rs.getString("comment"));
                    review.setCreatedAt(rs.getTimestamp("created_at"));
                    reviews.add(review);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviews;
    }

    public List<Game> getAllGames() {
        List<Game> list = new ArrayList<>();
        String sql = "SELECT * FROM games ORDER BY name ASC";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToGame(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // ... Sisa method (search, sorting, add/update/delete game, favorites) tetap sama ...
    // Tidak perlu mengubah apa pun dari sini ke bawah.
    public List<Game> getGamesSortedByRating() {
        List<Game> list = new ArrayList<>();
        String sql = "SELECT * FROM games ORDER BY rating DESC";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToGame(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public List<Game> getGamesSortedByPopularity() {
        List<Game> list = new ArrayList<>();
        String sql = "SELECT * FROM games ORDER BY popularity DESC";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToGame(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Game> getGamesSortedByReleaseDate(boolean newestFirst) {
        List<Game> list = new ArrayList<>();
        String order = newestFirst ? "DESC" : "ASC";
        String sql = "SELECT * FROM games ORDER BY release_date " + order;
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToGame(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Game> searchGames(String keyword) {
        List<Game> list = new ArrayList<>();
        String sql = "SELECT * FROM games WHERE name LIKE ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToGame(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public List<String> getAllGenres() {
        List<String> genres = new ArrayList<>();
        String sql = "SELECT DISTINCT genre FROM games"; 
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            genres.add("Semua Genre");
            while (rs.next()) {
                String genreString = rs.getString("genre");
                if (genreString != null && !genreString.trim().isEmpty()) {
                    String[] genreArray = genreString.split("\\s*,\\s*");
                    for (String g : genreArray) {
                        if (!genres.contains(g)) {
                            genres.add(g);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return genres;
    }

    public List<Game> getGamesByGenre(String genre) {
        List<Game> list = new ArrayList<>();
        String sql = "SELECT * FROM games WHERE genre LIKE ? ORDER BY name ASC";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + genre + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToGame(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public void addGame(Game game) throws SQLException {
        String sql = "INSERT INTO games (name, genre, rating, release_date, popularity, description, image, youtube_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, game.getName());
            stmt.setString(2, game.getGenre());
            stmt.setFloat(3, game.getRating());
            stmt.setString(4, game.getReleaseDate());
            stmt.setInt(5, game.getPopularity());
            stmt.setString(6, game.getDescription());
            if (game.getImage() != null) stmt.setBytes(7, game.getImage());
            else stmt.setNull(7, Types.BLOB);
            stmt.setString(8, game.getYoutubeId());
            stmt.executeUpdate();
        }
    }

    public void updateGame(Game game) throws SQLException {
        String sql = "UPDATE games SET name = ?, genre = ?, rating = ?, release_date = ?, " +
                     "popularity = ?, description = ?, image = ?, youtube_id = ? WHERE id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, game.getName());
            stmt.setString(2, game.getGenre());
            stmt.setFloat(3, game.getRating());
            stmt.setString(4, game.getReleaseDate());
            stmt.setInt(5, game.getPopularity());
            stmt.setString(6, game.getDescription());
            if (game.getImage() != null) stmt.setBytes(7, game.getImage());
            else stmt.setNull(7, Types.BLOB);
            stmt.setString(8, game.getYoutubeId());
            stmt.setInt(9, game.getId());
            stmt.executeUpdate();
        }
    }

    public void deleteGame(int gameId) throws SQLException {
        String sql = "DELETE FROM games WHERE id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gameId);
            stmt.executeUpdate();
        }
    }
    
    public void addFavorite(int userId, int gameId) throws SQLException {
        String sql = "INSERT INTO user_favorites (user_id, game_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, gameId);
            stmt.executeUpdate();
        }
    }

    public void removeFavorite(int userId, int gameId) throws SQLException {
        String sql = "DELETE FROM user_favorites WHERE user_id = ? AND game_id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, gameId);
            stmt.executeUpdate();
        }
    }
    
    public boolean isFavorite(int userId, int gameId) {
        String sql = "SELECT COUNT(*) FROM user_favorites WHERE user_id = ? AND game_id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, gameId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Game> getFavoriteGames(int userId) {
        List<Game> list = new ArrayList<>();
        String sql = "SELECT * FROM games WHERE id IN (SELECT game_id FROM user_favorites WHERE user_id = ?)";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToGame(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}