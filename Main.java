// File: Main.java (VERSI FINAL - Dengan Inisialisasi Native Interface)
import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.SwingUtilities;

// 1. Tambahkan import ini
import chrriis.dj.nativeswing.swtimpl.NativeInterface;

public class Main {
    public static void main(String[] args) {
        // 2. Tambahkan baris ini di awal
        NativeInterface.open();

        try {
            FlatDarkLaf.setup();
        } catch (Exception e) {
            System.err.println("Gagal menginisialisasi tema FlatLaf.");
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new GameUI().setVisible(true));
        
        // Baris ini menjalankan event pump untuk library native
        NativeInterface.runEventPump();
    }
}