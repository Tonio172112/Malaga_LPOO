package clases;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final int SQUARE_SIZE = 32;
    private static final int PROJECTILE_SIZE = 16;
    private static final int PROJECTILE_SPEED = 10;
    private static final int ENEMY_SIZE = 32;
    private static final int ENEMY_SPEED = 3;
    private static final int ENEMY_DROP_DISTANCE = 30;
    private static final int ENEMY_DROP_THRESHOLD = 2;
    private static final int GAME_WIDTH = 500;
    private static final int GAME_HEIGHT = 700;
    private static final int SQUARE_Y_POSITION = GAME_HEIGHT - SQUARE_SIZE - 20;
    private static final int ENEMY_SHOOT_INTERVAL = 700; // Tiempo entre disparos de los enemigos en milisegundos
    private static final int ENEMY_SHOOT_PROBABILITY = 3; // Probabilidad de disparo del enemigo en porcentajes

    private BufferedImage backgroundImage;
    private int squareX;
    private Timer gameTimer;
    private Timer shootTimer;
    private Timer enemyShootTimer;
    private int moveDirection;
    private boolean canShoot;
    private boolean isShooting;
    private int enemyDropCounter = 0;
    private List<Rectangle> projectiles;
    private List<Enemy> enemies;
    private List<Rectangle> enemyProjectiles;
    private int enemyDirection = ENEMY_SPEED;
    private int level = 1;
    private Random random = new Random();

    public GamePanel() {
        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        loadBackgroundImage();
        squareX = (GAME_WIDTH - SQUARE_SIZE) / 2;
        moveDirection = 0;
        canShoot = true;
        isShooting = false;
        projectiles = new ArrayList<>();
        enemies = new ArrayList<>();
        enemyProjectiles = new ArrayList<>();

        setFocusable(true);
        requestFocusInWindow();

        initializeEnemies();

        gameTimer = new Timer(10, this::gameLoop);
        gameTimer.start();

        shootTimer = new Timer(500, e -> canShoot = true);

        // Ajusta el intervalo de tiempo entre disparos de los enemigos
        enemyShootTimer = new Timer(ENEMY_SHOOT_INTERVAL, e -> {
            if (level > 1) { // Solo disparar si el nivel es mayor a 1
                shootEnemyProjectiles();
            }
        });
        enemyShootTimer.start();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_LEFT) moveDirection = -5;
                else if (key == KeyEvent.VK_RIGHT) moveDirection = 5;
                else if (key == KeyEvent.VK_SPACE) isShooting = true;
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_LEFT && moveDirection == -5 ||
                    key == KeyEvent.VK_RIGHT && moveDirection == 5) moveDirection = 0;
                else if (key == KeyEvent.VK_SPACE) isShooting = false;
            }
        });
    }

    private void loadBackgroundImage() {
        try {
            backgroundImage = ImageIO.read(getClass().getClassLoader().getResource("resources/bg.gif"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeEnemies() {
        enemies.clear();
        int rows = level == 1 ? 3 : 3;
        int cols = level == 1 ? 6 : 6;
        int xOffset = 10;
        int yOffset = 10;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int x = xOffset + col * (ENEMY_SIZE + 10);
                int y = yOffset + row * (ENEMY_SIZE + 10);
                enemies.add(new Enemy(x, y, ENEMY_SIZE, ENEMY_SIZE));
            }
        }
    }

    private void moveSquare(int dx) {
        squareX += dx;
        squareX = Math.max(0, Math.min(squareX, GAME_WIDTH - SQUARE_SIZE));
        repaint();
    }

    private void shootProjectile() {
        if (canShoot) {
            int projectileX = squareX + (SQUARE_SIZE - PROJECTILE_SIZE) / 2;
            int projectileY = SQUARE_Y_POSITION;
            projectiles.add(new Rectangle(projectileX, projectileY, PROJECTILE_SIZE, PROJECTILE_SIZE));
            canShoot = false;
            shootTimer.restart();
        }
    }

    private void shootEnemyProjectiles() {
        for (Enemy enemy : enemies) {
            if (random.nextInt(100) < ENEMY_SHOOT_PROBABILITY) { // Ajusta la probabilidad de que un enemigo dispare
                int projectileX = enemy.x + (ENEMY_SIZE - PROJECTILE_SIZE) / 2;
                int projectileY = enemy.y + ENEMY_SIZE;
                enemyProjectiles.add(new Rectangle(projectileX, projectileY, PROJECTILE_SIZE, PROJECTILE_SIZE));
            }
        }
    }

    private void updateProjectiles() {
        projectiles.removeIf(projectile -> {
            projectile.y -= PROJECTILE_SPEED;
            return projectile.y + PROJECTILE_SIZE < 0;
        });
    }

    private void updateEnemyProjectiles() {
        enemyProjectiles.removeIf(projectile -> {
            projectile.y += PROJECTILE_SPEED;
            return projectile.y > GAME_HEIGHT;
        });
    }

    private void updateEnemies() {
        boolean hitEdge = false;

        for (Enemy enemy : enemies) {
            enemy.x += enemyDirection;
            if (enemy.x <= 0 || enemy.x + ENEMY_SIZE >= GAME_WIDTH) hitEdge = true;
        }

        if (hitEdge) {
            enemyDropCounter++;
            enemyDirection = -enemyDirection;

            if (enemyDropCounter >= ENEMY_DROP_THRESHOLD) {
                for (Enemy enemy : enemies) {
                    enemy.y += ENEMY_DROP_DISTANCE;
                }
                enemyDropCounter = 0;
            }
        }
        repaint();
    }

    private void checkCollisions() {
        projectiles.removeIf(projectile -> {
            boolean hit = enemies.removeIf(enemy -> projectile.intersects(enemy));
            return hit;
        });

        enemyProjectiles.removeIf(projectile -> {
            boolean hit = projectile.intersects(new Rectangle(squareX, SQUARE_Y_POSITION, SQUARE_SIZE, SQUARE_SIZE));
            return hit;
        });
    }

    private void gameLoop(ActionEvent e) {
        if (moveDirection != 0) moveSquare(moveDirection);
        if (isShooting) shootProjectile();
        updateProjectiles();
        updateEnemyProjectiles();
        updateEnemies();
        checkCollisions();

        if (enemies.isEmpty()) {
            advanceToNextLevel();
        }
    }

    private void advanceToNextLevel() {
        level++;
        if (level <= 5) {
            initializeEnemies();
            // Otras configuraciones específicas para el siguiente nivel, si las hay
            System.out.println("Nivel " + level);
        } else {
            JOptionPane.showMessageDialog(this, "¡Has ganado el juego!");
            System.exit(0);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }

        g.setColor(Color.WHITE);
        g.fillRect(squareX, SQUARE_Y_POSITION, SQUARE_SIZE, SQUARE_SIZE);

        g.setColor(Color.RED);
        for (Rectangle projectile : projectiles) {
            g.fillRect(projectile.x, projectile.y, projectile.width, projectile.height);
        }

        g.setColor(Color.GREEN);
        for (Rectangle enemyProjectile : enemyProjectiles) {
            g.fillRect(enemyProjectile.x, enemyProjectile.y, enemyProjectile.width, enemyProjectile.height);
        }

        g.setColor(Color.BLUE);
        for (Enemy enemy : enemies) {
            g.fillRect(enemy.x, enemy.y, enemy.width, enemy.height);
        }
    }

    class Enemy extends Rectangle {
        private static final long serialVersionUID = 1L;

        public Enemy(int x, int y, int width, int height) {
            super(x, y, width, height);
        }
    }
}
