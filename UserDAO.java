// File: UserDAO.java (Versi Final Lengkap)
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;

public class UserDAO {

    /**
     * Memverifikasi kredensial pengguna dengan membandingkan hash password.
     */
    public boolean verifyUser(String username, String plainPassword) {
        String sql = "SELECT password FROM users WHERE username = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    return BCrypt.checkpw(plainPassword, storedHash);
                }
                return false; // Username tidak ditemukan
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Mengecek apakah username sudah ada di database.
     * @param username Username yang akan dicek.
     * @return true jika sudah ada, false jika belum.
     */
    public boolean checkUserExists(String username) throws SQLException {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Jika ada baris, berarti user ada
            }
        }
    }

    /**
     * Menambahkan pengguna baru ke database.
     * @param username Username baru.
     * @param hashedPassword Password yang sudah di-hash.
     */
    public void addUser(String username, String hashedPassword) throws SQLException {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.executeUpdate();
        }
    }

    // --- BARU (Ditambahkan untuk Fitur Favorit) ---
    /**
     * Mendapatkan ID pengguna berdasarkan username.
     * @param username Username yang akan dicari.
     * @return ID pengguna jika ditemukan, -1 jika tidak.
     */
    public int getUserIdByUsername(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Mengindikasikan user tidak ditemukan
    }

    // --- BARU (Ditambahkan untuk Fitur Admin/User) ---
    /**
     * Mendapatkan peran (role) pengguna berdasarkan ID.
     * @param userId ID pengguna.
     * @return String peran ('admin' atau 'user') jika ditemukan, null jika tidak.
     */
    public String getUserRole(int userId) {
        String sql = "SELECT role FROM users WHERE id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Mengindikasikan peran tidak ditemukan
    }
}