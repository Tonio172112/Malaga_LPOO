package clases;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
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
    private static final int ENEMY_SHOOT_PROBABILITY = 5;
    private static final int CAMICASE_SPEED = 7;
    private static final int CAMICASE_SPAWN_INTERVAL = 1100;

    private static final int MAX_ENEMY_Y = 50 + SQUARE_Y_POSITION - ENEMY_SIZE;

    private static final int ENEMY_HEALTH = 50;
    private static final int BOSS_MOVE_INTERVAL = 100;
    private static final int BOSS_SHOOT_INTERVAL = 400;
    private static final int BOSS_FAST_SHOOT_INTERVAL = 300; 

    private static final int ENEMY_WIDTH = 150;
    private static final int ENEMY_HEIGHT = 100;

    private ImageIcon backgroundImage;
    private ImageIcon playerImage;
    private ImageIcon bulletImage;
    private ImageIcon enemyImage;
    private ImageIcon enemyBulletImage;
    private ImageIcon camicaseImage;
    private ImageIcon corazonImage;
    private ImageIcon explosionImage;
    private ImageIcon bossImage;

    private int squareX;
    private Timer gameTimer;
    private Timer shootTimer;
    private Timer enemyShootTimer;
    private Timer camicaseSpawnTimer;
    private Timer bossShootTimer;
    private int moveDirection;
    private boolean canShoot;
    private boolean isShooting;
    private int enemyDropCounter = 0;
    private List<Rectangle> projectiles;
    private List<Enemy> enemies;
    private List<Rectangle> enemyProjectiles;
    private List<Camicase> camicases;
    private int enemyDirection = ENEMY_SPEED;
    private int level = 1;
    private int lives = 3;
    private Random random = new Random();
    private boolean gameOver = false;

    private boolean isPlayerImmune = false;
    private boolean isPlayerVisible = true;
    private Timer immunityTimer;
    private Timer blinkTimer;

    private boolean isExploding = false;
    private int explosionX, explosionY;
    private boolean showPlayer = true;

    private BossEnemy bossEnemy;
    private Timer bossMoveTimer;
    private boolean hasWon = false;

    public GamePanel() {
        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        squareX = (GAME_WIDTH - SQUARE_SIZE) / 2;
        moveDirection = 0;
        canShoot = true;
        isShooting = false;
        projectiles = new ArrayList<>();
        enemies = new ArrayList<>();
        enemyProjectiles = new ArrayList<>();
        camicases = new ArrayList<>();

        setFocusable(true);
        requestFocusInWindow();

        initializeEnemies();

        gameTimer = new Timer(10, this::gameLoop);
        gameTimer.start();

        shootTimer = new Timer(500, e -> canShoot = true);

        enemyShootTimer = new Timer(ENEMY_SHOOT_INTERVAL, e -> {
            if (level > 1 && level != 5) {
                shootEnemyProjectiles();
            }
        });
        enemyShootTimer.start();

        camicaseSpawnTimer = new Timer(CAMICASE_SPAWN_INTERVAL, e -> {
            if (level >= 3 && level != 5) {
                spawnCamicase();
            }
        });
        camicaseSpawnTimer.start();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (gameOver) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        resetGame();
                    } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        System.exit(0); // Sale del juego si se presiona ESC
                    }
                } else {
                    int key = e.getKeyCode();
                    if (key == KeyEvent.VK_LEFT) moveDirection = -5;
                    else if (key == KeyEvent.VK_RIGHT) moveDirection = 5;
                    else if (key == KeyEvent.VK_SPACE) isShooting = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int key = e.getKeyCode();
                if (!gameOver) {
                    if (key == KeyEvent.VK_LEFT && moveDirection == -5 ||
                        key == KeyEvent.VK_RIGHT && moveDirection == 5) moveDirection = 0;
                    else if (key == KeyEvent.VK_SPACE) isShooting = false;
                }
            }
        });

        immunityTimer = new Timer(1500, e -> {
            isPlayerImmune = false;
            blinkTimer.stop();
            isPlayerVisible = true;
        });

        immunityTimer.setRepeats(false);

        blinkTimer = new Timer(100, e -> isPlayerVisible = !isPlayerVisible);

        backgroundImage = new ImageIcon(getClass().getClassLoader().getResource("resources/bg.gif"));
        bulletImage = new ImageIcon(getClass().getClassLoader().getResource("resources/bullet.png"));
        playerImage = new ImageIcon(getClass().getClassLoader().getResource("resources/player.png"));
        enemyImage = new ImageIcon(getClass().getClassLoader().getResource("resources/enemigoVioleta.png"));
        enemyBulletImage = new ImageIcon(getClass().getClassLoader().getResource("resources/enemy_bullet.png"));
        camicaseImage = new ImageIcon(getClass().getClassLoader().getResource("resources/camicase.PNG"));
        corazonImage = new ImageIcon(getClass().getClassLoader().getResource("resources/corazon.png"));
        explosionImage = new ImageIcon(getClass().getClassLoader().getResource("resources/explosion.gif"));
        bossImage = new ImageIcon(getClass().getClassLoader().getResource("resources/boss.png"));
    }

    private void initializeEnemies() {
        enemies.clear();
        camicases.clear();

        if (level == 5) {
            initializeBossEnemy();
        } else {
            int rows = level == 1 ? 3 : (level == 2 ? 4 : 4);
            int cols = level == 1 ? 6 : (level == 2 ? 5 : 6);

            int xOffset = 10;
            int yOffset = 40;

            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    int x = xOffset + col * (ENEMY_SIZE + 10);
                    int y = yOffset + row * (ENEMY_SIZE + 10);
                    enemies.add(new Enemy(x, y, ENEMY_SIZE, ENEMY_SIZE));
                }
            }
        }
    }

    private void initializeBossEnemy() {
        int bossX = (GAME_WIDTH - ENEMY_WIDTH) / 2;
        int bossY = 50;
        bossEnemy = new BossEnemy(bossX, bossY, ENEMY_WIDTH, ENEMY_HEIGHT, ENEMY_SPEED);

        bossMoveTimer = new Timer(BOSS_MOVE_INTERVAL, e -> moveBossEnemy());
        bossMoveTimer.start();

        bossShootTimer = new Timer(BOSS_SHOOT_INTERVAL, e -> shootEnemyProjectiles());
        bossShootTimer.start();
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
        if (level == 5 && bossEnemy != null) {
            int projectileX = bossEnemy.x + (ENEMY_WIDTH - PROJECTILE_SIZE) / 2;
            int projectileY = bossEnemy.y + ENEMY_HEIGHT;
            enemyProjectiles.add(new Rectangle(projectileX, projectileY, PROJECTILE_SIZE, PROJECTILE_SIZE));
        } else {
            for (Enemy enemy : enemies) {
                if (random.nextInt(100) < ENEMY_SHOOT_PROBABILITY) {
                    int projectileX = enemy.x + (ENEMY_SIZE - PROJECTILE_SIZE) / 2;
                    int projectileY = enemy.y + ENEMY_SIZE;
                    enemyProjectiles.add(new Rectangle(projectileX, projectileY, PROJECTILE_SIZE, PROJECTILE_SIZE));
                }
            }
        }
    }

    private void spawnCamicase() {
        if (camicases.size() < 5) {
            int camicaseX = random.nextInt(GAME_WIDTH - ENEMY_SIZE);
            int camicaseY = -ENEMY_SIZE;
            camicases.add(new Camicase(camicaseX, camicaseY, ENEMY_SIZE, ENEMY_SIZE));
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
                    if (enemy.y + ENEMY_DROP_DISTANCE <= MAX_ENEMY_Y) {
                        enemy.y += ENEMY_DROP_DISTANCE;
                    } else {
                        enemy.y = MAX_ENEMY_Y;
                    }
                }
                enemyDropCounter = 0;
            }
        }

        repaint();
    }

    private void updateCamicases() {
        List<Camicase> camicasesToRemove = new ArrayList<>();
        for (Camicase camicase : camicases) {
            camicase.y += CAMICASE_SPEED;
            if (camicase.y > GAME_HEIGHT) {
                camicasesToRemove.add(camicase);
            }
        }
        camicases.removeAll(camicasesToRemove);
    }

    private void checkCollisions() {
        
        Iterator<Rectangle> projectileIterator = projectiles.iterator();
        while (projectileIterator.hasNext()) {
            Rectangle projectile = projectileIterator.next();

            
            Iterator<Camicase> camicaseIterator = camicases.iterator();
            while (camicaseIterator.hasNext()) {
                Camicase camicase = camicaseIterator.next();
                Rectangle camicaseBounds = new Rectangle(camicase.x, camicase.y, camicase.width, camicase.height);

                if (projectile.intersects(camicaseBounds)) {
                    camicase.hit();
                    projectileIterator.remove(); 
                    if (camicase.isDestroyed()) {
                        camicaseIterator.remove(); 
                    }
                    break; 
                }
            }

            // Colisiones con enemigos
            Iterator<Enemy> enemyIterator = enemies.iterator();
            while (enemyIterator.hasNext()) {
                Enemy enemy = enemyIterator.next();
                Rectangle enemyBounds = new Rectangle(enemy.x, enemy.y, enemy.width, enemy.height);

                if (projectile.intersects(enemyBounds)) {
                    enemy.hit();
                    projectileIterator.remove(); 
                    if (enemy.isDestroyed()) {
                        enemyIterator.remove(); 
                    }
                    break; 
                }
            }
        }

        // Verificamos las colisiones entre proyectiles enemigos y el jugador
        Iterator<Rectangle> enemyProjectileIterator = enemyProjectiles.iterator();
        while (enemyProjectileIterator.hasNext()) {
            Rectangle projectile = enemyProjectileIterator.next();
            Rectangle playerBounds = new Rectangle(squareX, SQUARE_Y_POSITION, SQUARE_SIZE, SQUARE_SIZE);

            if (projectile.intersects(playerBounds)) {
                enemyProjectileIterator.remove(); 
                loseLife();

                if (lives <= 0) {
                    gameOver(); 
                    return;
                } else {
                    startPlayerImmunity(); 
                }
            }
        }

       
        Iterator<Camicase> camicaseIterator = camicases.iterator();
        while (camicaseIterator.hasNext()) {
            Camicase camicase = camicaseIterator.next();
            Rectangle camicaseBounds = new Rectangle(camicase.x, camicase.y, camicase.width, camicase.height);
            Rectangle playerBounds = new Rectangle(squareX, SQUARE_Y_POSITION, SQUARE_SIZE, SQUARE_SIZE);

            if (camicaseBounds.intersects(playerBounds)) {
                camicase.hit();
                if (camicase.isDestroyed()) {
                    camicaseIterator.remove(); 
                }
                loseLife();

                if (lives <= 0) {
                    gameOver(); 
                    return;
                }
            }
        }

        
        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            Rectangle enemyBounds = new Rectangle(enemy.x, enemy.y, enemy.width, enemy.height);
            Rectangle playerBounds = new Rectangle(squareX, SQUARE_Y_POSITION, SQUARE_SIZE, SQUARE_SIZE);

            if (playerBounds.intersects(enemyBounds)) {
                enemyIterator.remove(); 
                loseLife();

                if (lives <= 0) {
                    gameOver(); 
                    return;
                }
            }
        }

        
        if (enemies.isEmpty() && level != 5) {
            nextLevel();
        }

        
        if (level >= 4) {
            for (Camicase camicase : camicases) {
                camicase.shootLaser(enemyProjectiles);
            }
        }

        
        if (level == 5 && bossEnemy != null) {
            checkCollisionsWithBoss();
        }
    }


    private void checkCollisionsWithBoss() {
        if (bossEnemy != null) {
            for (int i = 0; i < projectiles.size(); i++) {
                Rectangle projectile = projectiles.get(i);
                if (projectile.intersects(bossEnemy)) {
                    projectiles.remove(i);
                    i--;
                    bossEnemy.health--;
                    if (bossEnemy.health <= 0) {
                        bossEnemy = null;
                        hasWon = true;
                        gameOver = true;
                        break;
                    }
                }
            }

            for (int i = 0; i < enemyProjectiles.size(); i++) {
                Rectangle projectile = enemyProjectiles.get(i);
                if (projectile.intersects(new Rectangle(squareX, SQUARE_Y_POSITION, SQUARE_SIZE, SQUARE_SIZE))) {
                    enemyProjectiles.remove(i);
                    i--;
                    loseLife();
                }
            }
        }
    }

    private void triggerExplosion(int x, int y) {
        isExploding = true;
        explosionX = x;
        explosionY = y;

        Timer explosionTimer = new Timer(300, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isExploding = false;
            }
        });
        explosionTimer.setRepeats(false);
        explosionTimer.start();
    }

    private void loseLife() {
        lives--;
        if (lives <= 0) {
            gameOver();
            triggerExplosion(squareX, SQUARE_Y_POSITION);
        } else {
            startPlayerImmunity();
        }
    }

    private void startPlayerImmunity() {
        isPlayerImmune = true;
        immunityTimer.restart();
        blinkTimer.start();
    }

    private void nextLevel() {
        level++;

  
        initializeEnemies();

        repaint();
    }


    private void gameOver() {
        level = 1;
        gameOver = true;
        gameTimer.stop();
        shootTimer.stop();
        enemyShootTimer.stop();
        camicaseSpawnTimer.stop();
        if (bossMoveTimer != null) {
            bossMoveTimer.stop();
        }
        if (bossShootTimer != null) {
            bossShootTimer.stop();
        }
        projectiles.clear();
        enemyProjectiles.clear();
        camicases.clear();
        enemies.clear();
        showPlayer = false;
        repaint();
    }

    private void resetGame() {
        gameOver = false;
        hasWon = false;
        level = 1;  
        lives = 3;  
        squareX = (GAME_WIDTH - SQUARE_SIZE) / 2;  
        moveDirection = 0;
        canShoot = true;
        showPlayer = true;

        projectiles.clear();
        enemies.clear();
        enemyProjectiles.clear();
        camicases.clear();

        isExploding = false;
        isPlayerVisible = true;
        isPlayerImmune = false;

        gameTimer.stop();
        shootTimer.stop();
        enemyShootTimer.stop();
        camicaseSpawnTimer.stop();

        if (bossMoveTimer != null) {
            bossMoveTimer.stop();
        }
        if (bossShootTimer != null) {
            bossShootTimer.stop();
        }

        gameTimer.start();
        shootTimer.start();
        enemyShootTimer.start();
        camicaseSpawnTimer.start();

        initializeEnemies();

        repaint();  
    }
    
    private void gameLoop(ActionEvent e) {
        if (!gameOver) {
            moveSquare(moveDirection);
            if (isShooting) shootProjectile();
            updateProjectiles();
            updateEnemyProjectiles();
            updateEnemies();
            updateCamicases();
            checkCollisions();

            if (level == 5 && bossEnemy != null) {
                moveBossEnemy();
            }

            repaint();
        }
    }


    private void moveBossEnemy() {
        if (bossEnemy != null) {
            if (bossEnemy.health <= ENEMY_HEALTH / 2 && !bossEnemy.hasMovedToMid) {
                bossEnemy.y += bossEnemy.speed;
                if (bossEnemy.y > GAME_HEIGHT / 2 - bossEnemy.height / 2) {
                    bossEnemy.y = GAME_HEIGHT / 2 - bossEnemy.height / 2;
                    bossEnemy.hasMovedToMid = true;

                    
                    bossShootTimer.setDelay(BOSS_FAST_SHOOT_INTERVAL);
                }
            }

            bossEnemy.x += bossEnemy.direction * ENEMY_SPEED;
            if (bossEnemy.x <= 0 || bossEnemy.x + ENEMY_WIDTH >= GAME_WIDTH) {
                bossEnemy.direction *= -1;
            }
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(backgroundImage.getImage(), 0, 0, GAME_WIDTH, GAME_HEIGHT, this);

        if (isPlayerVisible && showPlayer) {
            g.drawImage(playerImage.getImage(), squareX, SQUARE_Y_POSITION, SQUARE_SIZE, SQUARE_SIZE, this);
        }

        if (isExploding) {
            g.drawImage(explosionImage.getImage(), explosionX, explosionY, SQUARE_SIZE, SQUARE_SIZE, this);
        }

        for (Rectangle projectile : projectiles) {
            g.drawImage(bulletImage.getImage(), projectile.x, projectile.y, projectile.width, projectile.height, this);
        }

        for (Enemy enemy : enemies) {
            g.drawImage(enemyImage.getImage(), enemy.x, enemy.y, enemy.width, enemy.height, this);
        }

        for (Rectangle projectile : enemyProjectiles) {
            g.drawImage(enemyBulletImage.getImage(), projectile.x, projectile.y, projectile.width, projectile.height, this);
        }

        for (Camicase camicase : camicases) {
            g.drawImage(camicaseImage.getImage(), camicase.x, camicase.y, camicase.width, camicase.height, this);
        }

        for (int i = 0; i < lives; i++) {
            int heartX = 10 + i * 40;
            int heartY = 10;
            g.drawImage(corazonImage.getImage(), heartX, heartY, 30, 30, null);
        }

        if (level == 5 && bossEnemy != null) {
            g.drawImage(bossImage.getImage(), bossEnemy.x, bossEnemy.y, bossEnemy.width, bossEnemy.height, this);
        }

        if (gameOver) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 30));
            FontMetrics fm = g2d.getFontMetrics();
            String message = hasWon ? "¡HAS GANADO!" : "¡Has Perdido!";
            int x = (getWidth() - fm.stringWidth(message)) / 2;
            int y = getHeight() / 2;
            g2d.drawString(message, x, y);

            String restartMessage = "Presiona Enter para reiniciar";
            String exitMessage = "o ESC para salir";
            x = (20 + getWidth() - fm.stringWidth(restartMessage)) / 4;
            y += fm.getHeight() + 20;
            g2d.drawString(restartMessage, x, y);

            y += fm.getHeight() + 10;
            g2d.drawString(exitMessage, x, y);

            g2d.dispose();
        }
    }

    private static class Enemy {
        int x, y, width, height;
        int hitsToDestroy;

        public Enemy(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public void hit() {
            hitsToDestroy--;
        }

        public boolean isDestroyed() {
            return hitsToDestroy <= 0;
        }
    }

    private static class Camicase extends Enemy {

        public Camicase(int x, int y, int width, int height) {
            super(x, y, width, height);
        }

        public void shootLaser(List<Rectangle> enemyProjectiles) {
            int laserWidth = PROJECTILE_SIZE;
            int laserHeight = 2 * PROJECTILE_SIZE;

            int projectileX1 = x + (width - laserWidth) / 2 - 10;
            int projectileX2 = x + (width - laserWidth) / 2 + 10;
            int projectileY = y + height;

            Rectangle laser1 = new Rectangle(projectileX1, projectileY, laserWidth, laserHeight);
            Rectangle laser2 = new Rectangle(projectileX2, projectileY, laserWidth, laserHeight);

            enemyProjectiles.add(laser1);
            enemyProjectiles.add(laser2);
        }
    }

    private static class BossEnemy extends Rectangle {
        private static final long serialVersionUID = 1L;
        private int health;
        private int speed;
        private int direction = 1;
        private boolean hasMovedToMid = false;

        public BossEnemy(int x, int y, int width, int height, int speed) {
            super(x, y, width, height);
            this.health = ENEMY_HEALTH;
            this.speed = speed;
        }
    }
}
