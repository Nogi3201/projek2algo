// File: Game.java (VERSI FINAL LENGKAP - Dengan getRawImageIcon)
import javax.swing.ImageIcon;
import java.awt.Image;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;

public class Game {
    private int id;
    private String name;
    private String genre;
    private float rating;
    private String releaseDate;
    private int popularity;
    private String description;
    private byte[] image;
    private String youtubeId;
    
    private ImageIcon imageIcon; 

    // --- Getters dan Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
    public int getPopularity() { return popularity; }
    public void setPopularity(int popularity) { this.popularity = popularity; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public byte[] getImage() { return image; }
    public void setImage(byte[] image) { this.image = image; }
    public String getYoutubeId() { return youtubeId; }
    public void setYoutubeId(String youtubeId) { this.youtubeId = youtubeId; }
    public void setImageIcon(ImageIcon icon) { this.imageIcon = icon; }

    // --- METHOD BARU UNTUK MENGAMBIL IKON ASLI ---
    public ImageIcon getRawImageIcon() {
        return this.imageIcon;
    }
    
    public ImageIcon getImageIcon(int width, int height) {
        ImageIcon sourceIcon = this.imageIcon;
        
        // Jika tidak ada imageIcon (dari hardcode), coba dari database
        if (sourceIcon == null && image != null && image.length > 0) {
            try {
                sourceIcon = new ImageIcon(ImageIO.read(new ByteArrayInputStream(image)));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        
        // Jika ada ikon (baik dari hardcode maupun db), ubah ukurannya
        if (sourceIcon != null) {
            Image scaledImage = sourceIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);
        }

        return null;
    }
}