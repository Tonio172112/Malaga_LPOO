package clases;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final int SQUARE_SIZE = 48;
    private static final int PROJECTILE_SIZE = 16;
    private static final int PROJECTILE_SPEED = 10;
    private static final int ENEMY_SIZE = 38;
    private static final int ENEMY_SPEED = 3;
    private static final int ENEMY_DROP_DISTANCE = 30;
    private static final int ENEMY_DROP_THRESHOLD = 2;
    private static final int GAME_WIDTH = 500;
    private static final int GAME_HEIGHT = 700;
    private static final int SQUARE_Y_POSITION = GAME_HEIGHT - SQUARE_SIZE - 20;
    private static final int ENEMY_SHOOT_INTERVAL = 400;
    private static final int ENEMY_SHOOT_PROBABILITY = 4;
    
    private JLabel levelLabel;
    private JLabel livesLabel;

    private ImageIcon backgroundImage;
    private ImageIcon playerImage;
    private ImageIcon bulletImage;
    private ImageIcon enemyImage;
    private ImageIcon enemyBulletImage;
    
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
    private int lives = 3; 
    private Random random = new Random();

    public GamePanel() {
        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
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

        enemyShootTimer = new Timer(ENEMY_SHOOT_INTERVAL, e -> {
            if (level > 1) {
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
    
    
    backgroundImage = new ImageIcon(getClass().getClassLoader().getResource("resources/bg.gif"));
    
    bulletImage = new ImageIcon(getClass().getClassLoader().getResource("resources/bullet.png"));
    
    playerImage = new ImageIcon(getClass().getClassLoader().getResource("resources/player.png"));
    
    enemyImage = new ImageIcon(getClass().getClassLoader().getResource("resources/enemigoVioleta.png"));
    
    enemyBulletImage = new ImageIcon(getClass().getClassLoader().getResource("resources/enemy_bullet.png"));
    
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
            if (random.nextInt(100) < ENEMY_SHOOT_PROBABILITY) {
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
    
    public void setLevelLabel(JLabel levelLabel) {
        this.levelLabel = levelLabel;
    }

    public void setLivesLabel(JLabel livesLabel) {
        this.livesLabel = livesLabel;
    }

    private void updateLabels() {
        if (levelLabel != null) {
            levelLabel.setText("Nivel: " + level);
        }
        if (livesLabel != null) {
            livesLabel.setText("Vidas: " + lives);
        }
    }
    
    

    private void checkCollisions() {
    	projectiles.removeIf(projectile -> {
            boolean hit = enemies.removeIf(enemy -> projectile.intersects(enemy));
            return hit;
        });

        enemyProjectiles.removeIf(projectile -> {
            boolean hit = projectile.intersects(new Rectangle(squareX, SQUARE_Y_POSITION, SQUARE_SIZE, SQUARE_SIZE));
            if (hit) {
                lives--; 
                if (lives <= 0) {
                    JOptionPane.showMessageDialog(this, "¡Has perdido! Fin del juego.");
                    System.exit(0);
                }
                updateLabels(); 
            }
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
            System.out.println("Nivel " + level);
            updateLabels(); 
        } else {
            JOptionPane.showMessageDialog(this, "¡Has ganado el juego!");
            System.exit(0);
        }
    }
    

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
        }

        g.drawImage(playerImage.getImage(), squareX, SQUARE_Y_POSITION, SQUARE_SIZE, SQUARE_SIZE, this);

        for (Rectangle projectile : projectiles) {
            g.drawImage(bulletImage.getImage(), projectile.x, projectile.y, projectile.width, projectile.height, this);
        }

        for (Rectangle enemyProjectile : enemyProjectiles) {
            g.drawImage(enemyBulletImage.getImage(), enemyProjectile.x, enemyProjectile.y, enemyProjectile.width, enemyProjectile.height, this);  
        }

        for (Enemy enemy : enemies) {
            g.drawImage(enemyImage.getImage(), enemy.x, enemy.y, enemy.width, enemy.height, this);
        }

    }

    class Enemy extends Rectangle {
        private static final long serialVersionUID = 1L;

        public Enemy(int x, int y, int width, int height) {
            super(x, y, width, height);
        }
    }
}