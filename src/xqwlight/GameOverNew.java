package xqwlight;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.GameCanvas;

public class GameOverNew extends GameCanvas implements Runnable {
    private boolean isRunning = false;
    private Graphics g;
    private int selectedOption = 0;

    public XQWLMIDlet midlet;

    private Image Bg;
    private Image GameOverWin;
    private Image GameOverLose;
    private Image Restart;
    private Image Exit;

    private int width;
    private int height;
    private int bg_x;
    private int bg_y;
    private int gameOver_winx;
    private int gameOver_winy;
    private int gameOver_losex;
    private int gameOver_losey;
    private int restart_x;
    private int restart_y;
    private int exit_x;
    private int exit_y;

    private boolean win;

    public GameOverNew() {
        super(false);
        setFullScreenMode(true);
        g = getGraphics();
        LoadImages();
        InitCoordinates();
    }

    private void LoadImages() {
        Bg = Util.LoadImg("/res/images/background.png");
        GameOverWin = Util.LoadImg("/res/images/title_win.png");
        GameOverLose = Util.LoadImg("/res/images/title_lose.png");
        Restart = Util.LoadImg("/res/images/btn_restart.png");
        Exit = Util.LoadImg("/res/images/btn_quit.png");
    }

    public void InitCoordinates() {
        width = getWidth();
        height = getHeight();

        int center_x = width / 2;
        int center_y = height / 2;

        bg_x = center_x - Bg.getWidth() / 2;
        bg_y = center_y - Bg.getHeight() / 2;
        gameOver_winx = center_x - GameOverWin.getWidth() / 2;
        gameOver_winy = center_y - GameOverWin.getHeight() / 2 - 70;
        gameOver_losex = center_x - GameOverLose.getWidth() / 2;
        gameOver_losey = center_y - GameOverLose.getHeight() / 2 - 70;
        restart_x = center_x - Restart.getWidth() / 2;
        restart_y = center_y - Restart.getHeight() / 2 + 120;
        exit_x = center_x - Exit.getWidth() / 2;
        exit_y = center_y - Exit.getHeight() / 2 + 170;
    }

    public void start(boolean win) {
        this.win = win;
        isRunning = true;
        Thread t = new Thread(this);
        t.start();
    }

    public void run() {
        delay(150);
        draw();
        while (isRunning) {
//            tick();
//            if (!isRunning) break;
//            draw();
        }
    }

    public void delay(int value) {
        try {
            Thread.sleep((long)value);
        } catch (Exception var3) {
        }

    }

    private int keyTrigger = 0;

    private void tick() {
//        int keys = getKeyStates();
//
//        int inv = 0xffffffff - keyTrigger;
//        int key = inv & keys;
//        keyTrigger &= keys;
//
//        if ((key & DOWN_PRESSED) != 0) {
//            selectedOption = (selectedOption + 1) % 2;
//            keyTrigger |= DOWN_PRESSED;
//        }
//        if ((key & UP_PRESSED) != 0) {
//            selectedOption = (selectedOption - 1 + 2) % 2;
//            keyTrigger |= UP_PRESSED;
//        }
//        if ((key & FIRE_PRESSED) != 0) {
//            executeSelectedOption();
//            keyTrigger |= FIRE_PRESSED;
//        }
    }

    public void stop() {
        isRunning = false;
    }

    private void draw() {
        g.setColor(0);
        g.fillRect(0, 0, width, height);
        g.drawImage(Bg, bg_x, bg_y, 0);
        if (win)
            g.drawImage(GameOverWin, gameOver_winx, gameOver_winy, Graphics.TOP | Graphics.LEFT);
        else {
            g.drawImage(GameOverLose, gameOver_losex, gameOver_losey, Graphics.TOP | Graphics.LEFT);
        }
        if (selectedOption == 0) {
            g.setColor(0xFADF5F);
        } else {
            g.setColor(0xFFFFCF);
        }
        g.fillRect(restart_x - 32, restart_y - 10, 140, 32);
        g.drawImage(Restart, restart_x, restart_y, 0);
        if (selectedOption == 1) {
            g.setColor(0xFADF5F);
        } else {
            g.setColor(0xFFFFCF);
        }
        g.fillRect(exit_x - 32, exit_y - 10, 140, 32);
        g.drawImage(Exit, exit_x, exit_y, 0);
        flushGraphics();
    }

    protected void keyPressed(int keyCode) {
        int gameAction = getGameAction(keyCode);
        if (gameAction == UP) {
            selectedOption = (selectedOption - 1 + 2) % 2;
        } else if (gameAction == DOWN) {
            selectedOption = (selectedOption + 1) % 2;
        } else if (gameAction == FIRE) {
            executeSelectedOption();
        }
        draw();
    }

    private void executeSelectedOption() {
        if (selectedOption == 0) {
            midlet.StartGame();
            midlet.CloseGameOver();
        } else if (selectedOption == 1) {
            midlet.exitMIDlet();
        }
    }
}
