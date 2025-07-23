// File: DBConnection.java (DIUBAH - Pop-up diganti)
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class DBConnection {
    // Pastikan nama database, user, dan password sesuai dengan setup Anda
    private static final String URL = "jdbc:mysql://localhost:3306/game_info_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Ganti jika MySQL Anda memiliki password

    public static Connection connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            // Error ini kritis, aplikasi tidak bisa berjalan tanpa driver
            String errorMessage = "FATAL ERROR: Driver MySQL (mysql-connector-j...jar) tidak ditemukan.\n" +
                                  "Pastikan file .jar sudah ditambahkan ke 'Referenced Libraries' proyek Anda.\n\n" +
                                  "Detail Eror: " + e.getMessage();
            JOptionPane.showMessageDialog(null, errorMessage, "Koneksi Gagal", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1); // Keluar dari aplikasi jika driver tidak ada
            return null;
        } catch (SQLException e) {
            // Error ini biasanya karena XAMPP/MySQL belum jalan
            String errorMessage = "FATAL ERROR: Gagal terhubung ke database MySQL.\n" +
                                  "Pastikan server (XAMPP) Anda sedang berjalan di port 3306.\n\n" +
                                  "URL: " + URL + "\n" +
                                  "Detail Eror: " + e.getMessage();
            JOptionPane.showMessageDialog(null, errorMessage, "Koneksi Gagal", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1); // Keluar juga dari aplikasi
            return null;
        }
    }
}