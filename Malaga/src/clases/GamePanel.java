package clases;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

class GamePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final int SQUARE_SIZE = 32;
    private static final int GAME_WIDTH = 500;
    private static final int GAME_HEIGHT = 700;
    private static final int SQUARE_Y_POSITION = GAME_HEIGHT - SQUARE_SIZE - 20;
    private static final int PROJECTILE_SIZE = 16;
    private static final int PROJECTILE_SPEED = 10;
    private static final int ENEMY_SIZE = 32;
    private static final int ENEMY_SPEED = 3;
    private static final int ENEMY_DROP_DISTANCE = 30;
    private static final int ENEMY_DROP_THRESHOLD = 2;
    

    private ImageIcon backgroundGif;
    
    private int squareX;
    private Timer gameTimer;
    private Timer shootTimer;
    private int moveDirection;
    private boolean canShoot;
    private boolean isShooting;
    private int enemyDropCounter = 0;

    private List<Rectangle> projectiles;
    private List<Enemy> enemies;
    private int enemyDirection = ENEMY_SPEED;

    public GamePanel() {
    	
    	backgroundGif = new ImageIcon(getClass().getClassLoader().getResource("resources/bg.gif"));

        
        
        squareX = (GAME_WIDTH - SQUARE_SIZE) / 2;
        moveDirection = 0;
        canShoot = true;
        isShooting = false;
        projectiles = new ArrayList<>();
        enemies = new ArrayList<>();

        setFocusable(true);
        requestFocusInWindow();

        // Inicializa los enemigos
        initializeEnemies();

        gameTimer = new Timer(10, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (moveDirection != 0) {
                    moveSquare(moveDirection);
                }
                if (isShooting) {
                    shootProjectile();
                }
                updateProjectiles();
                updateEnemies();
                checkCollisions();
            }
        });
        gameTimer.start();

        shootTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canShoot = true;
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_LEFT) {
                    moveDirection = -5;
                } else if (key == KeyEvent.VK_RIGHT) {
                    moveDirection = 5;
                } else if (key == KeyEvent.VK_SPACE) {
                    isShooting = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int key = e.getKeyCode();
                if ((key == KeyEvent.VK_LEFT && moveDirection == -5) ||
                    (key == KeyEvent.VK_RIGHT && moveDirection == 5)) {
                    moveDirection = 0;
                } else if (key == KeyEvent.VK_SPACE) {
                    isShooting = false;
                }
            }
        });
    }

    private void initializeEnemies() {
        int rows = 3; 
        int cols = 6; 
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

    private void updateProjectiles() {
        for (int i = 0; i < projectiles.size(); i++) {
            Rectangle projectile = projectiles.get(i);
            projectile.y -= PROJECTILE_SPEED;
            if (projectile.y + PROJECTILE_SIZE < 0) {
                projectiles.remove(i);
                i--;
            }
        }
    }

    private void updateEnemies() {
        boolean hitEdge = false;

        for (Enemy enemy : enemies) {
            enemy.x += enemyDirection;
            if (enemy.x <= 0 || enemy.x + ENEMY_SIZE >= GAME_WIDTH) {
                hitEdge = true;
            }
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
        for (int i = 0; i < projectiles.size(); i++) {
            Rectangle projectile = projectiles.get(i);
            for (int j = 0; j < enemies.size(); j++) {
                Enemy enemy = enemies.get(j);
                if (projectile.intersects(enemy)) {
                    projectiles.remove(i);
                    enemies.remove(j);
                    i--;
                    break;
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (backgroundGif != null) {
            g.drawImage(backgroundGif.getImage(), 0, 0, getWidth(), getHeight(), this);
        }
        
        
        g.setColor(Color.WHITE);
        g.fillRect(squareX, SQUARE_Y_POSITION, SQUARE_SIZE, SQUARE_SIZE);

        for (Rectangle projectile : projectiles) {
            g.fillRect(projectile.x, projectile.y, projectile.width, projectile.height);
        }

        g.setColor(Color.RED);
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