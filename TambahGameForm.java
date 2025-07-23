import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException; // Import ditambahkan
import java.time.LocalDate;     // Import ditambahkan
import java.time.format.DateTimeParseException; // Import ditambahkan

public class TambahGameForm extends JFrame {
    private final JTextField nameField, genreField, ratingField, popularityField;
    private JFormattedTextField releaseDateField;
    private final JTextArea descriptionArea;
    private final JLabel imageLabel;
    private byte[] imageBytes;
    private final GameDAO dao;
    private final GameUI parentUI;

    public TambahGameForm(GameUI parent) {
        this.parentUI = parent;
        this.dao = new GameDAO();
        setTitle("Tambah Game Baru");
        setSize(400, 600);
        
        Color warnaLatar = new Color(60, 63, 65);
        Color warnaTeksPutih = Color.WHITE;

        getContentPane().setBackground(warnaLatar);
        setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(0, 1, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        formPanel.setBackground(warnaLatar);

        nameField = new JTextField();
        genreField = new JTextField();
        ratingField = new JTextField();
        popularityField = new JTextField();
        descriptionArea = new JTextArea(3, 20);
        imageLabel = new JLabel("Belum ada gambar", SwingConstants.CENTER);
        imageLabel.setForeground(warnaTeksPutih);

        try {
            MaskFormatter Mf = new MaskFormatter("####-##-##");
            Mf.setPlaceholderCharacter('_');
            releaseDateField = new JFormattedTextField(Mf);
        } catch (ParseException e) { // Menggunakan ParseException dari java.text
            e.printStackTrace();
            releaseDateField = new JFormattedTextField("YYYY-MM-DD");
        }

        JButton uploadButton = new JButton("Pilih Gambar");
        JButton simpanButton = new JButton("Simpan");
        simpanButton.setBackground(new Color(40, 167, 69));
        simpanButton.setForeground(Color.WHITE);

        formPanel.add(new JLabel("Nama:")).setForeground(warnaTeksPutih);
        formPanel.add(nameField);
        formPanel.add(new JLabel("Genre:")).setForeground(warnaTeksPutih);
        formPanel.add(genreField);
        formPanel.add(new JLabel("Rating:")).setForeground(warnaTeksPutih);
        formPanel.add(ratingField);
        formPanel.add(new JLabel("Tanggal Rilis (YYYY-MM-DD):")).setForeground(warnaTeksPutih);
        formPanel.add(releaseDateField);
        formPanel.add(new JLabel("Popularitas:")).setForeground(warnaTeksPutih);
        formPanel.add(popularityField);
        formPanel.add(new JLabel("Deskripsi:")).setForeground(warnaTeksPutih);
        formPanel.add(new JScrollPane(descriptionArea));
        formPanel.add(new JLabel("Gambar:")).setForeground(warnaTeksPutih);
        formPanel.add(imageLabel);
        formPanel.add(uploadButton);
        formPanel.add(simpanButton);

        add(formPanel, BorderLayout.CENTER);
        setLocationRelativeTo(parentUI);
        setVisible(true);

        uploadButton.addActionListener(e -> pilihGambar());
        simpanButton.addActionListener(e -> simpanGame());
    }
    
    private void simpanGame() {
        // Validasi input
        String name = nameField.getText().trim();
        String genre = genreField.getText().trim();
        String releaseDateStr = releaseDateField.getText().replace("_", "").trim();
        String description = descriptionArea.getText().trim();
        String ratingStr = ratingField.getText().trim();
        String popularityStr = popularityField.getText().trim();

        if (name.isEmpty() || genre.isEmpty() || releaseDateStr.isEmpty() || description.isEmpty() || ratingStr.isEmpty() || popularityStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Input Tidak Lengkap", JOptionPane.WARNING_MESSAGE);
            return;
        }

        float rating;
        int popularity;

        try {
            rating = Float.parseFloat(ratingStr);
            if (rating < 0 || rating > 5) { // Validasi rentang rating
                JOptionPane.showMessageDialog(this, "Rating harus antara 0.0 sampai 5.0.", "Input Tidak Valid", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Rating tidak valid. Masukkan angka (contoh: 4.5).", "Input Tidak Valid", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            popularity = Integer.parseInt(popularityStr);
            if (popularity < 0) { // Popularitas tidak boleh negatif
                JOptionPane.showMessageDialog(this, "Popularitas tidak boleh negatif.", "Input Tidak Valid", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Popularitas tidak valid. Masukkan angka bulat.", "Input Tidak Valid", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Memvalidasi format tanggal YYYY-MM-DD
            LocalDate.parse(releaseDateStr);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Format tanggal rilis tidak valid. Gunakan YYYY-MM-DD.", "Input Tidak Valid", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Game newGame = new Game();
        newGame.setName(name);
        newGame.setGenre(genre);
        newGame.setReleaseDate(releaseDateStr);
        newGame.setDescription(description);
        newGame.setImage(imageBytes);
        newGame.setRating(rating);
        newGame.setPopularity(popularity);

        try {
            dao.addGame(newGame);
            JOptionPane.showMessageDialog(this, "Game berhasil ditambahkan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            parentUI.refreshGames();
            dispose();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal menambahkan game ke database: " + e.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void pilihGambar() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Pilih Gambar Game");
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (FileInputStream fis = new FileInputStream(file)) {
                imageBytes = fis.readAllBytes();
                imageLabel.setText("Gambar dipilih: " + file.getName());
                imageLabel.setIcon(new ImageIcon(new ImageIcon(file.getAbsolutePath()).getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH))); // Menampilkan pratinjau kecil
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Gagal membaca gambar: " + e.getMessage(), "Error Membaca File", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
}   