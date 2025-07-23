// File: EditGameForm.java (Versi Lengkap - DIUBAH)
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

public class EditGameForm extends JFrame {
    private final JTextField nameField, genreField, ratingField, popularityField;
    private JFormattedTextField releaseDateField;
    private final JTextArea descriptionArea;
    private final JLabel imageLabel;
    private byte[] imageBytes;
    private final Game gameToEdit;
    private final GameDAO dao;
    private final GameUI parentUI;

    public EditGameForm(Game game, GameUI parent) {
        this.gameToEdit = game;
        this.imageBytes = game.getImage();
        this.dao = new GameDAO();
        this.parentUI = parent;
        setTitle("Edit Game: " + gameToEdit.getName());
        setSize(400, 600);
        
        // --- Warna ---
        Color warnaLatar = new Color(60, 63, 65);
        Color warnaTeksPutih = Color.WHITE;

        getContentPane().setBackground(warnaLatar);
        setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(0, 1, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        formPanel.setBackground(warnaLatar);

        nameField = new JTextField(gameToEdit.getName());
        genreField = new JTextField(gameToEdit.getGenre());
        ratingField = new JTextField(String.valueOf(gameToEdit.getRating()));
        popularityField = new JTextField(String.valueOf(gameToEdit.getPopularity()));
        descriptionArea = new JTextArea(gameToEdit.getDescription(), 3, 20);
        imageLabel = new JLabel(imageBytes != null ? "Gambar sudah ada." : "Belum ada gambar.");
        imageLabel.setForeground(warnaTeksPutih);
        
        // Jika ada gambar, tampilkan pratinjau kecil
        if (imageBytes != null && imageBytes.length > 0) {
            try {
                ImageIcon currentImage = new ImageIcon(javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(imageBytes)));
                imageLabel.setIcon(new ImageIcon(currentImage.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH)));
                imageLabel.setText("Gambar saat ini.");
            } catch (IOException e) {
                imageLabel.setText("Gagal memuat gambar saat ini.");
                e.printStackTrace();
            }
        }


        try {
            MaskFormatter Mf = new MaskFormatter("####-##-##");
            Mf.setPlaceholderCharacter('_');
            releaseDateField = new JFormattedTextField(Mf);
            releaseDateField.setValue(gameToEdit.getReleaseDate());
        } catch (ParseException e) { // Menggunakan ParseException dari java.text
            e.printStackTrace();
            releaseDateField = new JFormattedTextField(gameToEdit.getReleaseDate());
        }

        JButton uploadButton = new JButton("Pilih Gambar Baru");
        JButton simpanButton = new JButton("Simpan Perubahan");
        simpanButton.setBackground(new Color(0, 120, 215));
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
        setLocationRelativeTo(parent);
        setVisible(true);
        
        uploadButton.addActionListener(e -> pilihGambar());
        simpanButton.addActionListener(e -> simpanPerubahan());
    }

    private void simpanPerubahan() {
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
        
        gameToEdit.setName(name);
        gameToEdit.setGenre(genre);
        gameToEdit.setReleaseDate(releaseDateStr);
        gameToEdit.setDescription(description);
        gameToEdit.setImage(imageBytes);
        gameToEdit.setRating(rating);
        gameToEdit.setPopularity(popularity);

        try {
            dao.updateGame(gameToEdit);
            JOptionPane.showMessageDialog(this, "Game berhasil diperbarui!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            parentUI.refreshGames();
            dispose();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memperbarui game di database: " + e.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void pilihGambar() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Pilih Gambar Baru Game");
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