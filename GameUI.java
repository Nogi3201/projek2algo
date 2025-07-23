// File: GameUI.java (VERSI FINAL PAMUNGKAS - Dengan Ikon & Empty State + Favorit Visual Feedback + Debugging)
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import javax.swing.Timer;

public class GameUI extends JFrame {

    private final GameDAO dao;
    private final int currentUserId;
    private final String currentUserRole;
    private final JPanel mainGridPanel;
    private final JPanel featuredPanelContainer;
    private final CardLayout featuredCardLayout;
    private final List<Game> featuredGames;
    private int currentFeaturedIndex = 0;
    private Timer slideTimer;
    private ImageIcon starIcon, editIcon, deleteIcon, favoriteFilledIcon, favoriteBorderIcon;

    public GameUI() {
        this(-1, "guest");
    }

    public GameUI(int userId, String userRole) {
        this.currentUserId = userId;
        this.currentUserRole = userRole;
        this.dao = new GameDAO();
        this.featuredGames = createHardcodedFeaturedGames();
        this.featuredCardLayout = new CardLayout();
        this.featuredPanelContainer = new JPanel(featuredCardLayout);

        loadIcons();
        String windowTitle = "Aplikasi Info Game";
        if (!userRole.equals("guest")) {
            windowTitle += " (" + userRole.substring(0, 1).toUpperCase() + userRole.substring(1) + ")";
        } else {
            windowTitle += " (Tamu)";
        }

        setTitle(windowTitle);
        try {
            ImageIcon appIcon = new ImageIcon(getClass().getResource("/icons/app_icon.png"));
            setIconImage(appIcon.getImage());
        } catch (Exception e) {
            System.err.println("Gagal memuat ikon aplikasi 'app_icon.png'");
        }

        setSize(1200, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainContentPanel = new JPanel(new BorderLayout(0, 10));
        
        JPanel heroPanel = createFeaturedSlider();
        mainContentPanel.add(heroPanel, BorderLayout.NORTH);

        mainGridPanel = new JPanel(new GridLayout(0, 4, 15, 15));
        mainGridPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JScrollPane scrollPane = new JScrollPane(mainGridPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Jelajahi Semua Game"));
        
        JPanel bottomSectionPanel = new JPanel(new BorderLayout());
        setupControlPanel(bottomSectionPanel);
        bottomSectionPanel.add(scrollPane, BorderLayout.CENTER);
        
        mainContentPanel.add(bottomSectionPanel, BorderLayout.CENTER);

        refreshGames();
        setupMenuBar();
        getContentPane().add(mainContentPanel);

        if (featuredGames.size() > 1) {
            startSlideTimer();
        }
    }

    private List<Game> createHardcodedFeaturedGames() {
        List<Game> games = new ArrayList<>();
        games.add(createFeaturedGame(9991, "Dead Cells", "Roguelike, Metroidvania", "Jelajahi kastil yang luas dan selalu berubah... dengan asumsi Anda mampu bertarung melewati para penjaganya.", "/icons/deadcells_bg.png"));
        games.add(createFeaturedGame(9992, "Hollow Knight", "Metroidvania, Action-Adventure", "Jelajahi gua-gua yang berkelok, kota-kota kuno, dan limbah mematikan. Lawan serangga-serangga yang tercemar.", "/icons/hollowknight_bg.png"));
        games.add(createFeaturedGame(9993, "Cuphead", "Run and Gun, Platformer", "Game aksi lari dan tembak klasik yang sangat berfokus pada pertarungan bos. Terinspirasi oleh kartun tahun 1930-an.", "/icons/cuphead_bg.png"));
        games.add(createFeaturedGame(9994, "Celeste", "Platformer, Adventure", "Bantu Madeline bertahan dari iblis-iblis batinnya dalam perjalanannya ke puncak Gunung Celeste.", "/icons/celeste_bg.png"));
        games.add(createFeaturedGame(9995, "ULTRAKILL", "Action, FPS, Shooter", "Gim tembak-menembak orang pertama retro ultra-kejam yang menggabungkan gaya klasik dengan penilaian karakter.", "/icons/ultrakill_bg.png"));
        return games;
    }
    
    private Game createFeaturedGame(int id, String name, String genre, String desc, String imagePath) {
        Game game = new Game();
        game.setId(id);
        game.setName(name);
        game.setGenre(genre);
        game.setDescription(desc);
        try {
            game.setImageIcon(new ImageIcon(getClass().getResource(imagePath)));
        } catch (Exception e) {
            System.err.println("Gagal memuat gambar latar: " + imagePath);
        }
        return game;
    }

    private JPanel createFeaturedSlider() {
        JPanel sliderPanel = new JPanel(new BorderLayout());
        sliderPanel.setPreferredSize(new Dimension(getWidth(), 350));

        if (featuredGames.isEmpty()) return sliderPanel;

        for (Game game : featuredGames) {
            featuredPanelContainer.add(new FadeableBackgroundPanel(game), String.valueOf(game.getId()));
        }
        sliderPanel.add(featuredPanelContainer, BorderLayout.CENTER);

        JPanel navigationPanel = createFeaturedNavigation();
        sliderPanel.add(navigationPanel, BorderLayout.SOUTH);
        
        Component firstCard = featuredPanelContainer.getComponent(0);
        if (firstCard instanceof FadeableBackgroundPanel) {
            ((FadeableBackgroundPanel)firstCard).setAlpha(1f);
        }
        featuredCardLayout.first(featuredPanelContainer);
        
        return sliderPanel;
    }

    private JPanel createFeaturedNavigation() {
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        navPanel.setOpaque(false);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 0, 10, 0));
        wrapper.add(navPanel, BorderLayout.CENTER);
        
        ButtonGroup buttonGroup = new ButtonGroup();
        for (int i = 0; i < featuredGames.size(); i++) {
            JRadioButton navButton = new JRadioButton();
            navButton.putClientProperty("index", i);
            navButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            navButton.setOpaque(false);
            
            final int index = i;
            navButton.addActionListener(e -> {
                if (slideTimer.isRunning()) slideTimer.restart();
                showFeaturedGame(index);
            });
            buttonGroup.add(navButton);
            navPanel.add(navButton);
        }
        ((JRadioButton)navPanel.getComponent(0)).setSelected(true);
        return wrapper;
    }
    
    private void showFeaturedGame(int index) {
        if (index == currentFeaturedIndex) return;
        
        Component currentComp = getVisibleComponent(featuredPanelContainer);
        if (currentComp == null) return;
        
        final FadeableBackgroundPanel outgoingPanel = (FadeableBackgroundPanel) currentComp;
        
        currentFeaturedIndex = index;
        featuredCardLayout.show(featuredPanelContainer, String.valueOf(featuredGames.get(index).getId()));
        Component nextComp = getVisibleComponent(featuredPanelContainer);
        if (nextComp == null) return;

        final FadeableBackgroundPanel incomingPanel = (FadeableBackgroundPanel) nextComp;
        
        outgoingPanel.startFadeOut();
        incomingPanel.startFadeIn();

        updateNavigationButtons();
    }
    
    private Component getVisibleComponent(JPanel container) {
        for (Component component : container.getComponents()) {
            if (component.isVisible()) {
                return component;
            }
        }
        return container.getComponentCount() > 0 ? container.getComponent(0) : null;
    }

    private void updateNavigationButtons() {
        JPanel navContainer = (JPanel)((JPanel)featuredPanelContainer.getParent().getComponent(1)).getComponent(0);
        for (Component c : navContainer.getComponents()) {
            if (c instanceof JRadioButton) {
                JRadioButton button = (JRadioButton) c;
                int buttonIndex = (int)button.getClientProperty("index");
                button.setSelected(buttonIndex == currentFeaturedIndex);
            }
        }
    }

    private void startSlideTimer() {
        slideTimer = new Timer(5000, e -> {
            int nextIndex = (currentFeaturedIndex + 1) % featuredGames.size();
            showFeaturedGame(nextIndex);
        });
        slideTimer.start();
    }
    
    private void setupControlPanel(JPanel container) {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        controlPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        JTextField searchField = new JTextField(25);
        searchField.putClientProperty("JTextField.placeholderText", "Ketik nama game untuk dicari...");
        JButton searchButton = new JButton("Cari");
        
        JComboBox<String> genreComboBox = new JComboBox<>();
        List<String> genres = dao.getAllGenres();
        genres.forEach(genreComboBox::addItem);
        
        JButton clearButton = new JButton("Reset");

        searchButton.addActionListener(e -> performLoad(() -> dao.searchGames(searchField.getText())));
        searchField.addActionListener(e -> searchButton.doClick());
        
        genreComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selectedGenre = (String) e.getItem();
                performLoad(() -> "Semua Genre".equals(selectedGenre) ? dao.getAllGames() : dao.getGamesByGenre(selectedGenre));
            }
        });
        
        clearButton.addActionListener(e -> {
            searchField.setText("");
            genreComboBox.setSelectedIndex(0);
            refreshGames();
        });
        
        controlPanel.add(new JLabel("Pencarian:"));
        controlPanel.add(searchField);
        controlPanel.add(searchButton);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(new JLabel("Filter Genre:"));
        controlPanel.add(genreComboBox);
        controlPanel.add(clearButton);

        container.add(controlPanel, BorderLayout.NORTH);
    }
    
    private JButton createIconButton(ImageIcon icon, String toolTip, Color hoverColor) {
        JButton button = new JButton(icon);
        button.setToolTipText(toolTip);
        button.setBorder(new EmptyBorder(4, 4, 4, 4));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                button.setContentAreaFilled(true);
                button.setBackground(hoverColor);
            }
            @Override public void mouseExited(MouseEvent e) {
                button.setContentAreaFilled(false);
            }
        });
        return button;
    }
    
    private void displayGames(List<Game> games) {
        mainGridPanel.removeAll();
        List<String> featuredNames = new ArrayList<>();
        for (Game g : featuredGames) {
            featuredNames.add(g.getName());
        }

        if (games != null && !games.isEmpty() && games.stream().anyMatch(g -> !featuredNames.contains(g.getName()))) {
            mainGridPanel.setLayout(new GridLayout(0, 4, 15, 15)); // Reset layout jika ada game
            for (Game g : games) {
                if (!featuredNames.contains(g.getName())) {
                    addGameCardToGrid(g);
                }
            }
        } else {
            mainGridPanel.setLayout(new BorderLayout()); // Set layout agar emptyPanel di tengah
            JPanel emptyPanel = new JPanel();
            emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
            emptyPanel.setOpaque(false);
            
            JLabel kosong = new JLabel("Tidak Ada Game yang Ditemukan");
            kosong.setFont(new Font("Segoe UI", Font.BOLD, 18));
            kosong.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel subText = new JLabel("Coba gunakan kata kunci lain atau reset filter.");
            subText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            subText.setForeground(Color.GRAY);
            subText.setAlignmentX(Component.CENTER_ALIGNMENT);

            emptyPanel.add(Box.createVerticalGlue());
            emptyPanel.add(kosong);
            emptyPanel.add(Box.createVerticalStrut(5));
            emptyPanel.add(subText);
            emptyPanel.add(Box.createVerticalGlue());
            
            mainGridPanel.add(emptyPanel, BorderLayout.CENTER);
        }
        mainGridPanel.revalidate();
        mainGridPanel.repaint();
    }
    
    private void addGameCardToGrid(Game g) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.putClientProperty("FlatLaf.style", "arc: 8");
        card.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, UIManager.getColor("Component.borderColor")));

        JLabel imageLabel = new JLabel(g.getImageIcon(250, 250));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        imageLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { new GameDetailView(GameUI.this, g); }
        });
        card.add(imageLabel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(new EmptyBorder(5, 8, 8, 8));
        
        JLabel titleLabel = new JLabel(g.getName());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel ratingLabel = new JLabel(String.format(" %.1f", g.getRating()));
        ratingLabel.setIcon(starIcon);
        ratingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ratingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setOpaque(false);
        
        if (currentUserRole.equals("admin")) {
            JButton editButton = createIconButton(editIcon, "Edit Game", new Color(0, 100, 200, 70));
            editButton.addActionListener(e -> new EditGameForm(g, this));
            
            JButton deleteButton = createIconButton(deleteIcon, "Hapus Game", new Color(200, 0, 0, 70));
            deleteButton.addActionListener(e -> {
                int response = JOptionPane.showConfirmDialog(this, "Hapus '" + g.getName() + "'?", "Konfirmasi", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    try {
                        dao.deleteGame(g.getId());
                        refreshGames();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Gagal menghapus game: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            
            buttonPanel.add(editButton);
            buttonPanel.add(deleteButton);
        }
        if (!currentUserRole.equals("guest")) {
            JButton favButton = createIconButton(favoriteBorderIcon, "Tambahkan ke Favorit", new Color(255, 215, 0, 70));
            
            boolean isCurrentlyFavorite = dao.isFavorite(currentUserId, g.getId());
            favButton.setIcon(isCurrentlyFavorite ? favoriteFilledIcon : favoriteBorderIcon);
            favButton.setToolTipText(isCurrentlyFavorite ? "Hapus dari Favorit" : "Tambahkan ke Favorit");

            favButton.addActionListener(e -> {
                try {
                    boolean nowFavorite = dao.isFavorite(currentUserId, g.getId());
                    if (nowFavorite) {
                        dao.removeFavorite(currentUserId, g.getId());
                        favButton.setIcon(favoriteBorderIcon);
                        favButton.setToolTipText("Tambahkan ke Favorit");
                        JOptionPane.showMessageDialog(this, "'" + g.getName() + "' dihapus dari favorit.", "Favorit Dihapus", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        dao.addFavorite(currentUserId, g.getId());
                        favButton.setIcon(favoriteFilledIcon);
                        favButton.setToolTipText("Hapus dari Favorit");
                        JOptionPane.showMessageDialog(this, "'" + g.getName() + "' ditambahkan ke favorit.", "Favorit Ditambahkan", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Gagal memproses favorit: " + ex.getMessage(), "Error Database", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            });
            buttonPanel.add(favButton);
        }

        JPanel ratingAndButtonPanel = new JPanel(new BorderLayout());
        ratingAndButtonPanel.setOpaque(false);
        ratingAndButtonPanel.add(ratingLabel, BorderLayout.WEST);
        ratingAndButtonPanel.add(buttonPanel, BorderLayout.EAST);
        
        bottomPanel.add(titleLabel);
        bottomPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        bottomPanel.add(ratingAndButtonPanel);
        
        card.add(bottomPanel, BorderLayout.SOUTH);
        mainGridPanel.add(card);
    }
    
    public void refreshGames() { performLoad(dao::getAllGames); }
    private void performLoad(Supplier<List<Game>> gameSupplier) {
        SwingWorker<List<Game>, Void> worker = new SwingWorker<>() {
            @Override protected List<Game> doInBackground() { return gameSupplier.get(); }
            @Override protected void done() {
                try { 
                    displayGames(get()); 
                } catch (InterruptedException | ExecutionException e) { 
                    JOptionPane.showMessageDialog(GameUI.this, "Gagal memuat game: " + e.getMessage(), "Error Loading", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
    
    private void loadIcons() {
        try {
            starIcon = new ImageIcon(getClass().getResource("/icons/star.png"));
            editIcon = new ImageIcon(getClass().getResource("/icons/edit.png"));
            deleteIcon = new ImageIcon(getClass().getResource("/icons/delete.png"));
            favoriteFilledIcon = new ImageIcon(getClass().getResource("/icons/favorite_filled.png"));
            favoriteBorderIcon = new ImageIcon(getClass().getResource("/icons/favorite_border.png"));
        } catch (Exception e) {
            System.err.println("Gagal memuat ikon dari folder /icons. Pastikan file ikon ada!");
            starIcon = new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
            editIcon = new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
            deleteIcon = new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
            favoriteFilledIcon = new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
            favoriteBorderIcon = new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
        }
    }
    
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        
        JMenu menu = new JMenu("Menu");
        menuBar.add(menu);

        if (currentUserRole.equals("admin")) {
            JMenuItem tambahItem = new JMenuItem("Tambah Game Baru");
            tambahItem.addActionListener(e -> new TambahGameForm(this));
            menu.add(tambahItem);
        }

        JMenuItem refreshItem = new JMenuItem("Refresh Semua Game");
        refreshItem.addActionListener(e -> refreshGames());
        menu.add(refreshItem);
        
        if (!currentUserRole.equals("guest")) {
            JMenuItem favoritItem = new JMenuItem("Lihat Game Favorit");
            favoritItem.addActionListener(e -> {
                performLoad(() -> dao.getFavoriteGames(currentUserId));
                JOptionPane.showMessageDialog(this, "Menampilkan game favorit Anda.", "Filter Favorit", JOptionPane.INFORMATION_MESSAGE);
            });
            menu.add(favoritItem);
        }

        menu.add(new JSeparator());
        JMenuItem keluarItem = new JMenuItem("Keluar");
        keluarItem.addActionListener(e -> System.exit(0));
        menu.add(keluarItem);

        JMenu akunMenu = new JMenu("Akun");
        menuBar.add(akunMenu);
        
        if (currentUserRole.equals("guest")) {
            JMenuItem loginItem = new JMenuItem("Login");
            loginItem.addActionListener(e -> promptLogin());
            akunMenu.add(loginItem);
            JMenuItem registerItem = new JMenuItem("Daftar Akun");
            registerItem.addActionListener(e -> new RegisterForm().setVisible(true));
            akunMenu.add(registerItem);
        } else {
            JMenuItem logoutItem = new JMenuItem("Logout");
            logoutItem.addActionListener(e -> {
                this.dispose();
                new GameUI().setVisible(true);
            });
            akunMenu.add(logoutItem);
        }
    }
    
    private void promptLogin() {
        new LoginForm((userId, userRole) -> {
            this.dispose();
            new GameUI(userId, userRole).setVisible(true);
        }).setVisible(true);
    }
    public int getCurrentUserId() { return currentUserId; }

    private class FadeableBackgroundPanel extends JPanel {
        private float alpha = 0f;
        private final Timer animationTimer;
        private final Game game;
        private boolean fadingIn = false;

        public FadeableBackgroundPanel(Game game) {
            this.game = game;
            setLayout(new BorderLayout());
            setOpaque(false);

            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setOpaque(false);
            content.setBorder(new EmptyBorder(40, 60, 40, 60));
            
            JLabel title = new JLabel(game.getName());
            title.setFont(new Font("Segoe UI", Font.BOLD, 48));
            title.setForeground(Color.WHITE);
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            JLabel genre = new JLabel(game.getGenre());
            genre.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            genre.setForeground(Color.LIGHT_GRAY);
            genre.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            JTextArea description = new JTextArea(game.getDescription());
            description.setWrapStyleWord(true);
            description.setLineWrap(true);
            description.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            description.setForeground(Color.WHITE);
            description.setOpaque(false);
            description.setEditable(false);
            description.setMaximumSize(new Dimension(600, 80));
            description.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            JButton detailButton = new JButton("Lihat Detail & Ulasan");
            detailButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            detailButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            detailButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            detailButton.addActionListener(e -> {
                // --- DEBUGGING OUTPUT BARU DI SINI ---
                Game gameFromDb = dao.searchGames(game.getName()).stream().findFirst().orElse(null);
                if(gameFromDb != null) {
                    System.out.println("--- Debugging Detail Game dari Featured Slider ---");
                    System.out.println("Nama Game dari DB: " + gameFromDb.getName());
                    System.out.println("ID: " + gameFromDb.getId());
                    System.out.println("Rating: " + gameFromDb.getRating());
                    System.out.println("Genre: " + gameFromDb.getGenre());
                    System.out.println("Tanggal Rilis: " + gameFromDb.getReleaseDate());
                    System.out.println("Popularitas: " + gameFromDb.getPopularity());
                    System.out.println("YouTube ID: " + gameFromDb.getYoutubeId());
                    System.out.println("--------------------------------------------------");
                    new GameDetailView(GameUI.this, gameFromDb);
                } else {
                    System.out.println("Game '" + game.getName() + "' (featured) TIDAK ditemukan di database utama.");
                    JOptionPane.showMessageDialog(GameUI.this, "Detail untuk game ini tidak ditemukan di database utama. Silakan tambahkan ke database.", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
            });
            
            content.add(title);
            content.add(Box.createRigidArea(new Dimension(0, 10)));
            content.add(genre);
            content.add(Box.createRigidArea(new Dimension(0, 15)));
            content.add(description);
            content.add(Box.createRigidArea(new Dimension(0, 20)));
            content.add(detailButton);
            
            add(content, BorderLayout.WEST);

            animationTimer = new Timer(20, e -> {
                if (fadingIn) {
                    alpha = Math.min(1f, alpha + 0.05f);
                } else {
                    alpha = Math.max(0f, alpha - 0.05f);
                }
                repaint();
                if (alpha >= 1f || alpha <= 0f) ((Timer) e.getSource()).stop();
            });
        }
        
        public void setAlpha(float alpha) { this.alpha = alpha; }
        public void startFadeIn() { fadingIn = true; if (!animationTimer.isRunning()) animationTimer.start(); }
        public void startFadeOut() { fadingIn = false; if (!animationTimer.isRunning()) animationTimer.start(); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            
            ImageIcon bgIcon = game.getRawImageIcon();
            if (bgIcon != null) {
                Image image = bgIcon.getImage();
                double imgWidth = image.getWidth(this);
                double imgHeight = image.getHeight(this);
                double panelWidth = getWidth();
                double panelHeight = getHeight();
                
                double scale = Math.max(panelWidth / imgWidth, panelHeight / imgHeight);
                int scaledWidth = (int) (imgWidth * scale);
                int scaledHeight = (int) (imgHeight * scale);
                int x = (int) ((panelWidth - scaledWidth) / 2);
                int y = (int) ((panelHeight - scaledHeight) / 2);

                g2d.drawImage(image, x, y, scaledWidth, scaledHeight, this);
            }
            
            g2d.setColor(new Color(0, 0, 0, 120));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.dispose();
        }
    }
}