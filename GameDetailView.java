// File: GameDetailView.java (VERSI FINAL LENGKAP - Semua Perbaikan Digabungkan)
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

// Import untuk DJ-Native-Swing
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

public class GameDetailView extends JDialog {

    private final GameDAO dao;
    private final Game game;
    private final GameUI parentUI; 

    public GameDetailView(GameUI parent, Game game) {
        super(parent, "Detail Game: " + game.getName(), true);
        this.parentUI = parent;
        this.game = game;
        this.dao = new GameDAO();
        
        setSize(600, 750);
        setLayout(new BorderLayout(10, 10));

        // 1. Panel Atas: Gambar dan Judul Game
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // 2. Panel Tengah: Menggunakan JTabbedPane
        JTabbedPane tabbedPane = createTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        // 3. Panel Bawah: Untuk Rekomendasi Game
        JPanel recommendationPanel = createRecommendationPanel();
        add(recommendationPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(parent);
        setVisible(true);
    }
    
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        ImageIcon icon = game.getImageIcon(300, 300);
        if (icon != null) {
            imageLabel.setIcon(icon);
        } else {
            imageLabel.setText("Gambar tidak tersedia");
            imageLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
            imageLabel.setPreferredSize(new Dimension(300, 300));
        }
        
        JLabel titleLabel = new JLabel(game.getName(), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton trailerButton = new JButton("▶️ Tonton Trailer");
        trailerButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        trailerButton.addActionListener(e -> {
            String videoId = game.getYoutubeId();
            if (videoId != null && !videoId.isBlank()) {
                openTrailerPlayer(videoId);
            } else {
                JOptionPane.showMessageDialog(this, "Trailer tidak tersedia untuk game ini.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonContainer.setOpaque(false);
        buttonContainer.add(trailerButton);
        
        JPanel titleAndButtonPanel = new JPanel(new BorderLayout());
        titleAndButtonPanel.setOpaque(false);
        titleAndButtonPanel.add(titleLabel, BorderLayout.CENTER);
        titleAndButtonPanel.add(buttonContainer, BorderLayout.SOUTH);

        topPanel.add(imageLabel, BorderLayout.NORTH);
        topPanel.add(titleAndButtonPanel, BorderLayout.CENTER);
        return topPanel;
    }
    
    private void openTrailerPlayer(String videoId) {
        SwingUtilities.invokeLater(() -> {
            final JDialog trailerDialog = new JDialog(this, "Memutar Trailer: " + game.getName(), true);
            // Menyimpan ukuran normal terakhir sebagai array final agar bisa diakses di lambda
            final Dimension[] lastNormalSize = {new Dimension(800, 600)}; 
            trailerDialog.setSize(lastNormalSize[0]);
            trailerDialog.setResizable(true); // Memungkinkan pengguna untuk mengubah ukuran secara manual
            
            JPanel webBrowserPanel = new JPanel(new BorderLayout());
            final JWebBrowser webBrowser = new JWebBrowser();
            webBrowser.setBarsVisible(false); 
            // Menggunakan format URL YouTube embed yang lebih umum
            webBrowser.navigate("https://www.youtube.com/embed/VIDEO_ID" + videoId + "?autoplay=1&fs=1"); //
            
            webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
            
            // Panel Kontrol untuk tombol memperbesar/memperkecil
            JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JButton maximizeButton = new JButton("Perbesar Layar Penuh");
            JButton restoreButton = new JButton("Kembalikan Ukuran");

            maximizeButton.addActionListener(e -> {
                lastNormalSize[0] = trailerDialog.getSize(); // Simpan ukuran saat ini sebelum memaksimalkan
                trailerDialog.setSize(Toolkit.getDefaultToolkit().getScreenSize()); // Set ke ukuran layar penuh
                trailerDialog.setLocationRelativeTo(null); // Pusatkan di layar
                maximizeButton.setVisible(false);
                restoreButton.setVisible(true);
            });

            restoreButton.addActionListener(e -> {
                trailerDialog.setSize(lastNormalSize[0]); // Kembalikan ke ukuran normal terakhir
                trailerDialog.setLocationRelativeTo(this); // Pusatkan relatif terhadap jendela utama
                maximizeButton.setVisible(true);
                restoreButton.setVisible(false);
            });

            controlPanel.add(maximizeButton);
            controlPanel.add(restoreButton);
            restoreButton.setVisible(false); // Awalnya sembunyikan tombol restore

            trailerDialog.add(webBrowserPanel, BorderLayout.CENTER);
            trailerDialog.add(controlPanel, BorderLayout.SOUTH); // Tambahkan panel kontrol di bagian bawah

            trailerDialog.setLocationRelativeTo(this);
            
            trailerDialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    webBrowser.disposeNativePeer();
                }
            });

            trailerDialog.setVisible(true);
        });
    }

    private JTabbedPane createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        tabbedPane.addTab("Detail Utama", createMainDetailsPanel());
        tabbedPane.addTab("Deskripsi", createDescriptionPanel());
        tabbedPane.addTab("Ulasan Pengguna", createReviewsPanel());

        return tabbedPane;
    }

    private JPanel createMainDetailsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.anchor = GridBagConstraints.WEST;
        Font labelFont = new Font("Segoe UI", Font.BOLD, 16);
        Font valueFont = new Font("Segoe UI", Font.PLAIN, 16);
        
        // Rating
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Rating:"), gbc);
        gbc.gridx = 1;
        JPanel ratingStarsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        ratingStarsPanel.setOpaque(false);
        for (int i = 1; i <= 5; i++) {
            JLabel star = new JLabel(i <= game.getRating() ? "⭐" : "☆");
            star.setFont(new Font("SansSerif", Font.PLAIN, 20));
            ratingStarsPanel.add(star);
        }
        ratingStarsPanel.add(new JLabel(String.format(" (%.1f)", game.getRating())));
        panel.add(ratingStarsPanel, gbc);
        
        // Genre
        gbc.gridx = 0; gbc.gridy++; panel.add(new JLabel("Genre:"), gbc);
        gbc.gridx = 1; panel.add(new JLabel("<html>" + (game.getGenre() != null ? game.getGenre().replace(", ", "<br>") : "N/A") + "</html>"), gbc);
        
        // Tanggal Rilis
        gbc.gridx = 0; gbc.gridy++; panel.add(new JLabel("Tanggal Rilis:"), gbc);
        gbc.gridx = 1; panel.add(new JLabel(game.getReleaseDate()), gbc);
        
        // Popularitas
        gbc.gridx = 0; gbc.gridy++; panel.add(new JLabel("Popularitas:"), gbc);
        gbc.gridx = 1; panel.add(new JLabel(String.format("%,d", game.getPopularity())), gbc);
        
        // Atur Font
        for(Component c : panel.getComponents()) {
            if (c instanceof JLabel) {
                LayoutManager layout = ((JPanel) c.getParent()).getLayout();
                if (layout instanceof GridBagLayout) {
                    GridBagLayout gbl = (GridBagLayout) layout;
                    GridBagConstraints constraints = gbl.getConstraints(c);
                    c.setFont(constraints.gridx == 0 ? labelFont : valueFont);
                }
            }
        }
        return panel;
    }
    
    private JPanel createDescriptionPanel() {
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setOpaque(false); 

        JTextArea descriptionArea = new JTextArea(game.getDescription());
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionArea.setEditable(false);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descriptionArea.setOpaque(false);
        descriptionArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        wrapperPanel.add(scrollPane, BorderLayout.CENTER);
        return wrapperPanel;
    }

    private JPanel createReviewsPanel() {
        JPanel reviewsOuterPanel = new JPanel(new BorderLayout(10, 10));
        reviewsOuterPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel reviewsListPanel = new JPanel();
        reviewsListPanel.setLayout(new BoxLayout(reviewsListPanel, BoxLayout.Y_AXIS));
        
        refreshReviews(reviewsListPanel);
        
        JScrollPane scrollPane = new JScrollPane(reviewsListPanel);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Ulasan dari Komunitas"));
        
        reviewsOuterPanel.add(scrollPane, BorderLayout.CENTER);

        if (parentUI.getCurrentUserId() != -1) {
            JPanel addReviewPanel = createAddReviewPanel(reviewsListPanel);
            reviewsOuterPanel.add(addReviewPanel, BorderLayout.SOUTH);
        } else {
            JLabel loginPrompt = new JLabel("Login untuk memberikan ulasan.", SwingConstants.CENTER);
            loginPrompt.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            reviewsOuterPanel.add(loginPrompt, BorderLayout.SOUTH);
        }

        return reviewsOuterPanel;
    }

    private void refreshReviews(JPanel panelToRefresh) {
        panelToRefresh.removeAll();
        List<UserReview> reviews = dao.getReviewsForGame(game.getId());
        if (reviews.isEmpty()) {
            JLabel noReviewsLabel = new JLabel("  Belum ada ulasan untuk game ini.");
            noReviewsLabel.setBorder(new EmptyBorder(10, 5, 10, 5));
            panelToRefresh.add(noReviewsLabel);
        } else {
            for (UserReview review : reviews) {
                panelToRefresh.add(createSingleReviewPanel(review));
                panelToRefresh.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }
        panelToRefresh.revalidate();
        panelToRefresh.repaint();
    }

    private JPanel createSingleReviewPanel(UserReview review) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEtchedBorder());

        // ** INI BARIS YANG DIPERBAIKI **
        String date = new SimpleDateFormat("dd MMM,yyyy").format(review.getCreatedAt());
        JLabel headerLabel = new JLabel(String.format("<html><b>%s</b> <font color='gray'>- %s</font></html>", review.getUsername(), date));
        headerLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        JPanel bodyPanel = new JPanel(new BorderLayout());
        bodyPanel.setOpaque(false);
        bodyPanel.setBorder(new EmptyBorder(0, 5, 5, 5));

        JPanel starsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        starsPanel.setOpaque(false);
        for(int i = 0; i < 5; i++) {
            starsPanel.add(new JLabel(i < review.getRating() ? "⭐" : "☆"));
        }
        
        JTextArea commentArea = new JTextArea(review.getComment());
        commentArea.setWrapStyleWord(true);
        commentArea.setLineWrap(true);
        commentArea.setEditable(false);
        commentArea.setOpaque(false);
        
        bodyPanel.add(starsPanel, BorderLayout.NORTH);
        bodyPanel.add(commentArea, BorderLayout.CENTER);

        panel.add(headerLabel, BorderLayout.NORTH);
        panel.add(bodyPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAddReviewPanel(JPanel reviewsListPanel) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Berikan Ulasan Anda"));
        
        JPanel ratingPanel = new JPanel();
        JComboBox<Integer> ratingComboBox = new JComboBox<>(new Integer[]{5, 4, 3, 2, 1});
        ratingPanel.add(new JLabel("Rating Anda:"));
        ratingPanel.add(ratingComboBox);

        JTextArea commentArea = new JTextArea(3, 30);
        JScrollPane commentScrollPane = new JScrollPane(commentArea);
        
        JButton submitButton = new JButton("Kirim Ulasan");
        submitButton.addActionListener(e -> {
            try {
                UserReview newReview = new UserReview();
                newReview.setGameId(game.getId());
                newReview.setUserId(parentUI.getCurrentUserId());
                newReview.setRating((Integer) ratingComboBox.getSelectedItem());
                newReview.setComment(commentArea.getText());

                dao.addOrUpdateReview(newReview);
                JOptionPane.showMessageDialog(this, "Ulasan berhasil dikirim!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                
                refreshReviews(reviewsListPanel);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Gagal mengirim ulasan.", "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        
        panel.add(ratingPanel, BorderLayout.NORTH);
        panel.add(commentScrollPane, BorderLayout.CENTER);
        panel.add(submitButton, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createRecommendationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Anda Mungkin Juga Suka"));

        JPanel cardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        List<Game> recommendedGames = dao.getRecommendedGames(game.getId(), game.getGenre());

        if(recommendedGames.isEmpty()){
            panel.add(new JLabel("  Tidak ada rekomendasi saat ini."), BorderLayout.CENTER);
        } else {
            for (Game recGame : recommendedGames) {
                cardsPanel.add(createSmallGameCard(recGame));
            }
        }
        
        panel.add(cardsPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSmallGameCard(Game recGame) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setPreferredSize(new Dimension(150, 200));
        card.setToolTipText(recGame.getName());
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel imageLabel = new JLabel(recGame.getImageIcon(150, 150));
        JLabel titleLabel = new JLabel(recGame.getName(), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        card.add(imageLabel, BorderLayout.CENTER);
        card.add(titleLabel, BorderLayout.SOUTH);
        
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Tutup dialog saat ini dan buka yang baru untuk game yang direkomendasikan
                GameDetailView.this.dispose();
                new GameDetailView(parentUI, recGame);
            }
        });

        return card;
    }
}