// File: RegisterForm.java (Versi Lengkap - Mandiri)
import org.mindrot.jbcrypt.BCrypt;
import javax.swing.*;
import java.awt.*;

public class RegisterForm extends JFrame {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JPasswordField confirmPasswordField;
    private final UserDAO userDAO;

    public RegisterForm() {
        this.userDAO = new UserDAO();

        setTitle("Registrasi Akun Baru");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        Color warnaLatar = new Color(60, 63, 65);
        Color warnaTombol = new Color(40, 167, 69);
        Color warnaTeksPutih = Color.WHITE;
        Color warnaLink = new Color(0, 190, 255);

        getContentPane().setBackground(warnaLatar);
        setLayout(new BorderLayout(15, 15));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Buat Akun Baru");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(warnaTeksPutih);

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 15, 0));
        formPanel.setOpaque(false);
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        confirmPasswordField = new JPasswordField();

        formPanel.add(new JLabel("Username:")).setForeground(warnaTeksPutih);
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password:")).setForeground(warnaTeksPutih);
        formPanel.add(passwordField);
        formPanel.add(new JLabel("Konfirmasi Password:")).setForeground(warnaTeksPutih);
        formPanel.add(confirmPasswordField);

        JButton registerButton = new JButton("Daftar");
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.setBackground(warnaTombol);
        registerButton.setForeground(warnaTeksPutih);

        JButton backButton = new JButton("Tutup");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.putClientProperty("JButton.buttonType", "borderless");
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.setForeground(warnaLink);

        mainPanel.add(titleLabel);
        mainPanel.add(formPanel);
        mainPanel.add(registerButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(backButton);

        add(mainPanel, BorderLayout.CENTER);

        registerButton.addActionListener(e -> attemptRegister());
        backButton.addActionListener(e -> dispose());
    }

    private void attemptRegister() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (username.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Registrasi Gagal", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Password dan konfirmasi password tidak cocok!", "Registrasi Gagal", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (userDAO.checkUserExists(username)) {
                JOptionPane.showMessageDialog(this, "Username '" + username + "' sudah digunakan.", "Registrasi Gagal", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            userDAO.addUser(username, hashedPassword);
            JOptionPane.showMessageDialog(this, "Registrasi berhasil! Anda sekarang bisa login.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal terhubung ke database.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}