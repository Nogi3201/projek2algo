// File: LoginForm.java (DIUBAH)
import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer; // --- DIUBAH --- dari Consumer menjadi BiConsumer

public class LoginForm extends JFrame {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final UserDAO userDAO;
    private final BiConsumer<Integer, String> onLoginSuccess; // --- DIUBAH ---

    // --- DIUBAH --- Konstruktor sekarang menerima userId dan userRole
    public LoginForm(BiConsumer<Integer, String> onLoginSuccess) {
        this.userDAO = new UserDAO();
        this.onLoginSuccess = onLoginSuccess;

        // ... (Kode setup UI tetap sama)
        setTitle("Login Akun");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        Color warnaLatar = new Color(60, 63, 65);
        Color warnaTombol = new Color(0, 120, 215);
        Color warnaTeksPutih = Color.WHITE;
        Color warnaTeksAbu = new Color(180, 180, 180);

        getContentPane().setBackground(warnaLatar);
        setLayout(new BorderLayout(15, 15));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("LOGIN AKUN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(warnaTeksPutih);

        JLabel subtitleLabel = new JLabel("Silakan login untuk menggunakan fitur ini");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setForeground(warnaTeksAbu);

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 15, 0));
        formPanel.setOpaque(false);
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        
        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(warnaTeksPutih);
        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(warnaTeksPutih);

        formPanel.add(userLabel);
        formPanel.add(usernameField);
        formPanel.add(passLabel);
        formPanel.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setBackground(warnaTombol);
        loginButton.setForeground(warnaTeksPutih);

        mainPanel.add(titleLabel);
        mainPanel.add(subtitleLabel);
        mainPanel.add(formPanel);
        mainPanel.add(loginButton);
        
        add(mainPanel, BorderLayout.CENTER);

        loginButton.addActionListener(e -> attemptLogin());
        passwordField.addActionListener(e -> attemptLogin());
    }

    private void attemptLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan password tidak boleh kosong.", "Login Gagal", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean isLoggedIn = userDAO.verifyUser(username, password);
        if (isLoggedIn) {
            int userId = userDAO.getUserIdByUsername(username);
            String userRole = userDAO.getUserRole(userId); // --- BARU --- Ambil role pengguna
            
            if (userId != -1 && userRole != null) {
                // --- DIUBAH --- Panggil callback dengan ID dan Role
                onLoginSuccess.accept(userId, userRole);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal mendapatkan data atau peran pengguna.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Username atau password salah!", "Login Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }
}