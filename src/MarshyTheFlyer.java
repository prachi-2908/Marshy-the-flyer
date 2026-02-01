import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;
import javax.sound.sampled.*; 
import java.net.URL;


public class MarshyTheFlyer extends JPanel implements ActionListener, KeyListener {
    int boardWidth=360;
    int boardHeight=640;

    Clip backgroundMusic;

    //Game Images
    Image bgImg;
    Image birdImg;
    Image topImg;
    Image bottomImg;

    //marshy bird
    int birdX = boardHeight/8;
    int birdY = boardHeight/2;
    int birdWidth = 43;
    int birdHeight = 33;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img){
            this.img = img;
        }
    }

    //the pipes
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

        Pipe(Image img)
        {
            this.img = img;
        }
    }

    //the logic
    Bird bird;
    int velocityX=-6;
    int velocityY=0;
    int gravity= 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipesTimer;
    boolean gameStarted = false;
    boolean gameOver = false;
    double score = 0;
    int  highscore = 0;
    boolean paused = false;

    public MarshyTheFlyer() {
        setPreferredSize(new Dimension(boardWidth,boardHeight));
        setFocusable(true);
        addKeyListener(this);

        // loading images
        bgImg = new ImageIcon(getClass().getResource("./bg.jpg")).getImage();
        birdImg =  new ImageIcon(getClass().getResource("./marshy.png")).getImage();
        topImg = new  ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomImg =  new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        //playing music 
        playMusic("./bgm.wav");

        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        //pipestimer
        placePipesTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                placePipes();
            }
        });
       // placePipesTimer.start();

        //game timer
        gameLoop = new Timer(1000/60,this);
        //gameLoop.start();

    }

    //music 
        public void playMusic(String soundFile) {
        try {
            URL url = getClass().getResource(soundFile);
            if (url == null) {
                System.out.println("Music file not found!");
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioIn);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY); // Keep playing
            backgroundMusic.start();
        } catch (Exception e) {
            System.out.println("Error loading music: " + e.getMessage());
        }
    }

    public void placePipes() {
        int randomPipeY = (int) (pipeY - pipeHeight/4 - Math.random()*(pipeHeight/2));
        int openingspace = boardHeight/4;

        Pipe topPipe = new Pipe(topImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingspace; 
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
    // 1. Drawing the background
    g.drawImage(bgImg, 0, 0, boardWidth, boardHeight, null);

    // 2. Drawing the bird
    g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

    // 3. Drawing pipes
    for (Pipe pipe : pipes) {
        g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
    }

    // 4. Score 
    g.setColor(Color.BLACK); 
    g.setFont(new Font("Comic Sans MS", Font.BOLD, 32));
    g.drawString(String.valueOf((int) score), 20, 45);

    // 5. Game Over / Pause / Start 
    if (gameOver) {
        drawCenteredString(g, "GAME OVER", 40, boardHeight / 2 - 50, true);
        drawCenteredString(g, "Score: " + (int)score, 24, boardHeight / 2, false);
        drawCenteredString(g, "High Score: " + highscore, 24, boardHeight / 2 + 40, false);
        drawCenteredString(g, "Press SPACE to Restart", 18, boardHeight / 2 + 90, false);
    } 
    else if (paused) {
        drawCenteredString(g, "PAUSED", 40, boardHeight / 2 - 20, true);
        drawCenteredString(g, "Press P to Resume", 18, boardHeight / 2 + 30, false);
    } 
    else if (!gameStarted) {
        drawCenteredString(g, "MARSHY THE FLYER", 30, boardHeight / 2 - 40, true);
        drawCenteredString(g, "Press SPACE to Start", 18, boardHeight / 2 + 20, false);
    }
}

// Centering the text

private void drawCenteredString(Graphics g, String text, int size, int y, boolean isTitle) {
    Font font = new Font("Comic Sans MS", isTitle ? Font.BOLD : Font.PLAIN, size);
    g.setFont(font);
    FontMetrics metrics = g.getFontMetrics(font);
    int x = (boardWidth - metrics.stringWidth(text)) / 2;

    // Draw Shadow (Offset by 2 pixels)
    g.setColor(new Color(0, 0, 0, 150)); // 
    g.drawString(text, x + 2, y + 2);

    // Drawing Main Text
    g.setColor(Color.YELLOW); // 
    g.drawString(text, x, y);
}


    public void move() {
        velocityY += gravity;
        bird.y += velocityY;
        bird.y= Math.max(bird.y, 0);

        for(int i=0;i<pipes.size();i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            if(!pipe.passed && bird.x > pipe.x + pipe.width)
            {
                pipe.passed = true;
                score += 0.5;
                //playVFX("./point.wav");
            }

            if(collision(bird,pipe))
            {
                gameOver = true;
            }
        }

        if(bird.y > boardHeight)
        {
            gameOver = true;
        }
        
    }

    public boolean collision(Bird a,Pipe b)
    {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if( gameStarted && !gameOver && !paused)
        {
            move();
        }
        repaint();
        if (gameOver) 
        {
            placePipesTimer.stop();
            gameLoop.stop();

            if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
        }
            if(score>highscore)
            {
                highscore = (int) score;
            }
        }
    }


    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {

        if(e.getKeyCode() == KeyEvent.VK_P && gameStarted && !gameOver)
        {
            paused = !paused;
            if(paused)
            {
                placePipesTimer.stop();
                backgroundMusic.stop();
            }
            else 
            {
                placePipesTimer.start();
                if (backgroundMusic != null) 
                {
                    backgroundMusic.setFramePosition(0);
                    backgroundMusic.start();
                }
            }
            repaint();
            return;

        }
        if(e.getKeyCode() == KeyEvent.VK_SPACE) {
            if(!gameStarted)
            {
                gameStarted=true;
                gameLoop.start();
                placePipesTimer.start();
                return;
            }
            velocityY=-9;
            //playMusic("./jump.wav");

            if(gameOver)
            {
                bird.y= birdY;
                velocityY = 0;
                pipes.clear();
                score = 0;
                gameOver = false;
                paused = false;
                gameStarted = true;

                //restart the music 
                if (backgroundMusic != null) 
                {
                    backgroundMusic.setFramePosition(0);
                    backgroundMusic.start();
                }
                gameLoop.start();
                placePipesTimer.start();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }


}