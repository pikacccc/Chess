package xqwlight;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class PausePannel {
    private int selectedOption = 0;
    private IExit exit;
    private IRestartGame restart;

    private Image Popup;
    private Image Play;
    private Image Exit;
    private int bg_x;
    private int bg_y;
    private int play_x;
    private int play_y;
    private int exit_x;
    private int exit_y;

    public PausePannel(IExit exit, IRestartGame restart, int width, int height) {
        this.exit = exit;
        this.restart = restart;
        InitImage();
        InitCoordinates(width, height);
    }

    private void InitImage() {
        Popup = Util.LoadImg("/res/images/popup.png");
        Play = Util.LoadImg("/res/images/btn_continue.png");
        Exit = Util.LoadImg("/res/images/btn_quit.png");
    }

    public void InitCoordinates(int width, int height) {
        int center_x = width / 2;
        int center_y = height / 2;

        bg_x = center_x - Popup.getWidth() / 2;
        bg_y = center_y - Popup.getHeight() / 2;
        play_x = center_x - Play.getWidth() / 2;
        play_y = center_y - Play.getHeight() / 2 - 25;
        exit_x = center_x - Exit.getWidth() / 2;
        exit_y = center_y - Exit.getHeight() / 2 + 25;
    }

    public void Draw(Graphics g) {
        g.drawImage(Popup, bg_x, bg_y, 0);
        if (selectedOption == 0) {
            g.setColor(0xFADF5F);
        } else {
            g.setColor(0xFFFFCF);
        }
        g.fillRect(play_x - 32, play_y - 10, 140, 32);
        g.drawImage(Play, play_x, play_y, 0);
        if (selectedOption == 1) {
            g.setColor(0xFADF5F);
        } else {
            g.setColor(0xFFFFCF);
        }
        g.fillRect(exit_x - 32, exit_y - 10, 140, 32);
        g.drawImage(Exit, exit_x, exit_y, 0);
    }

    protected void keyPressed(int action) {
        switch (action) {
            case Canvas.UP:
                selectedOption = (selectedOption - 1 + 2) % 2;
                break;
            case Canvas.DOWN:
                selectedOption = (selectedOption + 1) % 2;
                break;
            case Canvas.FIRE:
                executeSelectedOption();
                break;
            default:
                break;
        }
    }

    private void executeSelectedOption() {
        if (selectedOption == 0) {
            restart.RestartGame();
        } else if (selectedOption == 1) {
            exit.Exit();
        }
    }
}
