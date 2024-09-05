package clases;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.InputStream;

public class GameWindow {

    private static final int GAME_WIDTH = 500;
    private static final int GAME_HEIGHT = 700;

    public static void main(String[] args) {
        JFrame frmMalaga = new JFrame("Juego");
        frmMalaga.setIconImage(Toolkit.getDefaultToolkit().getImage(GameWindow.class.getResource("/resources/logo_malaga.jpg")));
        frmMalaga.setTitle("Malaga");

        frmMalaga.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmMalaga.setSize(900, 850);
        frmMalaga.setLocationRelativeTo(null);
        frmMalaga.setResizable(false);

       
        GamePanel gamePanel = new GamePanel();
        gamePanel.setSize(GAME_WIDTH, GAME_HEIGHT);
        gamePanel.setBackground(Color.GRAY);
        gamePanel.setLocation(200, 100);

        
        JPanel logoPanel = new JPanel();
        logoPanel.setSize(884, 89);
        logoPanel.setLocation(0, 0);
        logoPanel.setBackground(new Color(35, 31, 30));

        ImageIcon logoName = null;
        try {
            InputStream inputStream = GameWindow.class.getClassLoader().getResourceAsStream("resources/logo_name.jpeg");
            if (inputStream != null) {
                logoName = new ImageIcon(ImageIO.read(inputStream));
            } else {
                System.out.println("Error: La imagen no se pudo cargar correctamente.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        logoPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        JLabel logoNameLabel = new JLabel(logoName != null ? logoName : new ImageIcon());
        logoPanel.add(logoNameLabel);

        
        frmMalaga.getContentPane().setBackground(new Color(35, 31, 30));
        frmMalaga.getContentPane().setLayout(null);
        frmMalaga.getContentPane().add(gamePanel);
        frmMalaga.getContentPane().add(logoPanel);
        frmMalaga.setVisible(true);

        
        
    }
}

