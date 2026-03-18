
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;

public class MarshyTheFlyer extends JPanel implements ActionListener, KeyListener {

    int boardWidth = 360;
    int boardHeight = 640;
    Clip backgroundMusic;
    JButton themeButton; // Button for theme
// Game Images
    Image bgImg, birdImg, topImg, bottomImg;
// Theme state
    int currentTheme = 0; // 0 for Default, 1 for Night/Alt
//High Score
    String highScoreFile = "highscore.txt";
// marshy bird
    int birdX = boardHeight / 8;
    int birdY = boardHeight / 2;
    int birdWidth = 43;
    int birdHeight = 33;

// marshy accessories
    int GAME = 0;
    int accMenu = 1;
    int currentState = GAME;

    BufferedImage[] hats;
    int hatIndex = 0;
    boolean hatSelected = false;

    int totalHats = 5;
    int[] hatUnlockScore = {0, 5, 10, 15, 20};

// size for menu display
    int birdW = 43;
    int birdH = 33;

    class Bird {

        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;

    class Pipe {

        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }
    Bird bird;
    int velocityX = -4;
    int velocityY = 0;
    int gravity = 1;
    ArrayList<Pipe> pipes;
    Random random = new Random();
    Timer gameLoop;
    Timer placePipesTimer;
    boolean gameStarted = false;
    boolean gameOver = false;
    double score = 0;
    int highscore = 0;
    boolean paused = false;

    public MarshyTheFlyer() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);
        setLayout(null);

// 1. Setting up Theme Button
        themeButton = new JButton("Switch Theme");
        themeButton.setBounds(120, 10, 120, 30);
        themeButton.setOpaque(false);
        themeButton.setFocusable(true);
        themeButton.addActionListener(e -> switchTheme());
        add(themeButton);
        styleThemeButton();

// Accesories Menu
        setLayout(null); // allows button positioning
        hatSelected = false;

        ImageIcon originalIcon = new ImageIcon(getClass().getResource("/magic.png"));
        Image scaledImg = originalIcon.getImage().getScaledInstance(40, 30, Image.SCALE_SMOOTH);
        ImageIcon hatIcon = new ImageIcon(scaledImg);

        JButton accButton = new JButton(hatIcon);
        accButton.setBounds(10, 10, 40, 40);

        accButton.setBorderPainted(false);
        accButton.setContentAreaFilled(false);
        accButton.setFocusPainted(false);

        add(accButton);

        accButton.addActionListener(e -> {
            currentState = accMenu;
            hatSelected = true;
            requestFocusInWindow();
            repaint();
        });

        add(accButton);

        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {

                if (currentState == accMenu) {

                    int x = e.getX();
                    int y = e.getY();

                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;

                    // LEFT arrow
                    if (x > cx - 100 && x < cx - 40
                            && y > cy - 40 && y < cy + 40) {

                        hatIndex = (hatIndex - 1 + hats.length) % hats.length;
                        if (highscore >= hatUnlockScore[hatIndex]) {
                            hatSelected = true;
                        } else {
                            hatSelected = false;
                        }
                        repaint();
                    }

                    // RIGHT arrow
                    if (x > cx + 40 && x < cx + 100
                            && y > cy - 40 && y < cy + 40) {
                        hatSelected = true;
                        hatIndex = (hatIndex + 1) % hats.length;

                        if (highscore >= hatUnlockScore[hatIndex]) {
                            hatSelected = true;
                        } else {
                            hatSelected = false;
                        }
                        repaint();
                    }

                    //  BACK button
                    if (x > getWidth() - 50 && x < getWidth() - 10 && y > 10 && y < 50) {

                        currentState = GAME;
                        repaint();
                    }
                }
            }
        });

//  Loading Initial Assets
        loadAssets();
        loadHighScore();
        playMusic(currentTheme == 0 ? "/bgm.wav" : "/bgm2.wav");
        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();
        placePipesTimer = new Timer(1500, e -> placePipes());
        gameLoop = new Timer(1000 / 60, this);

        try {
            hats = new BufferedImage[5];

            hats[0] = ImageIO.read(getClass().getResource("/cap.png"));
            hats[1] = ImageIO.read(getClass().getResource("/glasses.png"));
            hats[2] = ImageIO.read(getClass().getResource("/crown.png"));
            hats[3] = ImageIO.read(getClass().getResource("/wicked.png"));
            hats[4] = ImageIO.read(getClass().getResource("/magic.png"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAssets() {
        if (currentTheme == 0) {
            bgImg = new ImageIcon(getClass().getResource("/bg.jpg")).getImage();
            birdImg = new ImageIcon(getClass().getResource("/marshy.png")).getImage();
            topImg = new ImageIcon(getClass().getResource("/toppipe.png")).getImage();
            bottomImg = new ImageIcon(getClass().getResource("/bottompipe.png")).getImage();
        } else {
// Dark theme images
            bgImg = new ImageIcon(getClass().getResource("/bg2.jpg")).getImage();
            birdImg = new ImageIcon(getClass().getResource("/marshy.png")).getImage();
            topImg = new ImageIcon(getClass().getResource("/topalt.png")).getImage();
            bottomImg = new ImageIcon(getClass().getResource("/bottomalt.png")).getImage();
        }
        if (bird != null) {
            bird.img = birdImg;
        }
    }

    private void styleThemeButton() {
        themeButton.setFont(new Font("Comic Sans MS", Font.BOLD, 12));
        themeButton.setFocusPainted(false);
        themeButton.setBorderPainted(false);
        themeButton.setOpaque(true);

        if (currentTheme == 0) {
            themeButton.setBackground(new Color(255, 255, 255));
            themeButton.setForeground(new Color(0, 0, 0));
        } else {
            themeButton.setBackground(new Color(30, 30, 60));
            themeButton.setForeground(new Color(0, 255, 255));
        }

    }

    private void switchTheme() {
        currentTheme = (currentTheme == 0) ? 1 : 0;
        loadAssets();
        styleThemeButton();

        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
        playMusic(currentTheme == 0 ? "/bgm.wav" : "/bgm2.wav");
        repaint();
    }

    public void playMusic(String soundFile) {
        try {
            URL url = getClass().getResource(soundFile);
            if (url == null) {
                return;
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioIn);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundMusic.start();
        } catch (Exception e) {
            System.out.println("Music Error: " + e.getMessage());
        }
    }

    public void placePipes() {
        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingspace = boardHeight / 4;
        Pipe topPipe = new Pipe(topImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);
        Pipe bottomPipe = new Pipe(bottomImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingspace;
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (currentState == GAME) {
            draw(g);
        } else if (currentState == accMenu) {
            drawAccessoryMenu(g);
        }
    }

    int[] hatOffset = {13, 22, 6, 12, 7};

    int[] hatOffsetX = {7, 4, 3, 5, 2};

    int[] hatHeight = {22, 22, 22, 26, 20};

    void drawAccessoryMenu(Graphics g) {

        g.drawImage(bgImg, 0, 0, boardWidth, boardHeight, null);

        int cx = (getWidth() - birdW) / 2;
        int cy = (getHeight() - birdH) / 2;

// bird
        g.drawImage(birdImg, cx, cy, birdW, birdH, null);

// hat
        int hatW = birdW - 4;
        int hatH = hatHeight[hatIndex];
        int hatX = cx + hatOffsetX[hatIndex];

        g.drawImage(hats[hatIndex],
                hatX,
                cy - hatH + hatOffset[hatIndex],
                hatW,
                hatH,
                null);

        // arrows
        g.setFont(new Font("Arial", Font.BOLD, 40));
        

        int dx = getWidth() / 2;
        int dy = getHeight() / 2;

        g.drawString("<", dx - 80, dy + 10); // left
        g.drawString(">", dx + 60, dy + 10); // right

        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.setColor(Color.YELLOW);
        g.drawString("X", getWidth() - 40, 40);

        g.setFont(new Font("Comic Sans MS", Font.BOLD, 20));

        String text = "Hat " + (hatIndex + 1) + "/" + totalHats;
        g.setColor(Color.YELLOW);
        g.drawString(text, getWidth() / 2 - 50, 100);

        if (highscore < hatUnlockScore[hatIndex]) {
            g.setColor(Color.RED);
            g.setFont(new Font("Comic Sans MS", Font.BOLD, 18));
            g.drawString("LOCKED", getWidth() / 2 - 40, cy + 100);
        }
    }

// Drawing everything on screen
    public void draw(Graphics g) {
        g.drawImage(bgImg, 0, 0, boardWidth, boardHeight, null);
        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

//  draw hat on bird
        if (hatSelected && highscore >= hatUnlockScore[hatIndex]) {
            int hatH = hatHeight[hatIndex];
            int hatW = bird.width - 4;

            int hatX = bird.x + hatOffsetX[hatIndex];
            int hatY = bird.y - hatH + hatOffset[hatIndex];

            g.drawImage(hats[hatIndex],
                    hatX,
                    hatY, // adjust if needed
                    hatW,
                    hatH,
                    null);

        }

        for (Pipe pipe : pipes) {
            Image pImg = (pipe.y < 0) ? topImg : bottomImg;
            g.drawImage(pImg, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }
// DYNAMIC TEXT COLORING 
        if (currentTheme == 1) { // Cosmic/Dark Theme
            g.setColor(new Color(0, 255, 255)); // Cyan for score
        } else {

            g.setColor(Color.BLACK);
        }
        g.setFont(new Font("Comic Sans MS", Font.BOLD, 32));
        g.drawString(String.valueOf((int) score), 20, 80);
        if (gameOver) {
            drawCenteredString(g, "GAME OVER", 40, boardHeight / 2 - 50, true);
            drawCenteredString(g, "Score: " + (int) score, 24, boardHeight / 2, false);
            drawCenteredString(g, "High Score: " + highscore, 24, boardHeight / 2 + 40, false);
            drawCenteredString(g, "Press SPACE to Restart", 18, boardHeight / 2 + 90, false);
        } else if (paused) {
            drawCenteredString(g, "PAUSED", 40, boardHeight / 2 - 20, true);
            drawCenteredString(g, "Press P to Resume", 18, boardHeight / 2 + 30, false);

        } else if (!gameStarted) {
            drawCenteredString(g, "MARSHY THE FLYER", 30, boardHeight / 2 - 40, true);
            drawCenteredString(g, "Press SPACE to Start", 18, boardHeight / 2 - 9, false);
        }
    }

    private void drawCenteredString(Graphics g, String text, int size, int y, boolean isTitle) {
        Font font = new Font("Comic Sans MS", isTitle ? Font.BOLD : Font.PLAIN, size);
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics(font);
        int x = (boardWidth - metrics.stringWidth(text)) / 2;
// Shadow/Outline for readability
        g.setColor(new Color(0, 0, 0, 180));
        g.drawString(text, x + 2, y + 2);
// Main Text Color based on theme
        if (currentTheme == 1) {
            g.setColor(isTitle ? new Color(255, 215, 0) : Color.WHITE);
        } else {
            g.setColor(Color.YELLOW); // Default theme color
        }
        g.drawString(text, x, y);
    }

    public void move() {
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);

            pipe.x += velocityX;
            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                pipe.passed = true;
                score += 0.5;
            }
            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }
        if (bird.y > boardHeight) {
            gameOver = true;
        }
        if (score > 10) {
            velocityX = -6;
        }
        if (score > 15) {
            velocityX = -8;
            placePipesTimer.setDelay(1300);
        }
        if (score > 21) {
            velocityX = -9;
            placePipesTimer.setDelay(1100);

        }
    }

    //Load High Score
    public void loadHighScore() {
        try {
            File file = new File(highScoreFile);
            if (!file.exists()) {
                file.createNewFile();
                highscore = 0;
                return;
            }
            Scanner scan = new Scanner(file);
            if (scan.hasNextInt()) {
                highscore = scan.nextInt();
                scan.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void saveHighScore() {
        try {
            FileWriter writer = new FileWriter(highScoreFile);
            writer.write(String.valueOf(highscore));
            writer.close();

        } catch (Exception e) {
            System.out.println("Can't save high score");
        }

    }

    public boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width && a.x + a.width > b.x && a.y < b.y + b.height && a.y + a.height
                > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameStarted && !gameOver && !paused) {
            move();
        }
        repaint();
        if (gameOver) {
            placePipesTimer.stop();
            gameLoop.stop();
            if (backgroundMusic != null) {
                backgroundMusic.stop();
            }
            if (score > highscore) {
                highscore = (int) score;
                saveHighScore();
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_P && gameStarted && !gameOver) {
            paused = !paused;
            if (paused) {
                placePipesTimer.stop();
                backgroundMusic.stop();
            } else {
                placePipesTimer.start();
                backgroundMusic.start();
            }
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (!gameStarted) {
                gameStarted = true;
                gameLoop.start();

                placePipesTimer.start();
                return;
            }
            velocityY = -9;
            if (gameOver) {
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                score = 0;
                gameOver = false;
                gameStarted = true;
                if (backgroundMusic != null) {
                    backgroundMusic.setFramePosition(0);
                    backgroundMusic.start();
                }
                gameLoop.start();
                placePipesTimer.start();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
