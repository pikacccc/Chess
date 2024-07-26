/*
XQWLCanvas.java - Source Code for XiangQi Wizard Light, Part IV

XiangQi Wizard Light - a Chinese Chess Program for Java ME
Designed by Morning Yellow, Version: 1.70, Last Modified: Mar. 2013
Copyright (C) 2004-2013 www.xqbase.com

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package xqwlight;

import com.sun.j2me.global.FormatAbstractionLayer;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.GameCanvas;

class XQWLCanvas extends Canvas implements Runnable, IRestartGame {
    private static final int PHASE_LOADING = 0;
    private static final int PHASE_WAITING = 1;
    private static final int PHASE_THINKING = 2;
    private static final int PHASE_EXITTING = 3;

    private static final int COMPUTER_BLACK = 0;
    private static final int COMPUTER_RED = 1;
    private static final int COMPUTER_NONE = 2;

    private static final int RESP_HUMAN_SINGLE = -2;
    private static final int RESP_HUMAN_BOTH = -1;
    private static final int RESP_CLICK = 0;
    private static final int RESP_ILLEGAL = 1;
    private static final int RESP_MOVE = 2;
    private static final int RESP_MOVE2 = 3;
    private static final int RESP_CAPTURE = 4;
    private static final int RESP_CAPTURE2 = 5;
    private static final int RESP_CHECK = 6;
    private static final int RESP_CHECK2 = 7;
    private static final int RESP_WIN = 8;
    private static final int RESP_DRAW = 9;
    private static final int RESP_LOSS = 10;

    private static Image imgBackground, imgXQWLight, imgThinking;
    private static final String[] IMAGE_NAME = {
            null, null, null, null, null, null, null, null,
            "rk", "ra", "rb", "rn", "rr", "rc", "rp", null,
            "bk", "ba", "bb", "bn", "br", "bc", "bp", null,
    };
    private static int widthBackground, heightBackground;
    private static Font fontLarge = Font.getFont(Font.FACE_SYSTEM,
            Font.STYLE_BOLD + Font.STYLE_ITALIC, Font.SIZE_LARGE);
    private static Font fontSmall = Font.getFont(Font.FACE_SYSTEM,
            Font.STYLE_BOLD, Font.SIZE_SMALL);

    static {
        try {
            imgBackground = Image.createImage("/res/images/background.png");
            imgXQWLight = Image.createImage("/res/images/xqwlight.png");
            imgThinking = Image.createImage("/res/images/thinking.png");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        widthBackground = imgBackground.getWidth();
        heightBackground = imgBackground.getHeight();
    }

    XQWLMIDlet midlet;
    byte[] retractData = new byte[XQWLMIDlet.RS_DATA_LEN];

    private Position pos = new Position();
    private Search search = new Search(pos, 12);
    private int cursorX, cursorY;
    private int sqSelected, mvLast;
    private int normalWidth = getWidth();
    private int normalHeight = getHeight();

    volatile int phase = PHASE_LOADING;

    private boolean init = false;
    private Image imgBoard, imgSelected, imgSelected2, imgCursor, imgCursor2;
    private Image[] imgPieces = new Image[24];
    private int squareSize, width, height, left, right, top, bottom;

    private Graphics g;

    private boolean isRunning = false;

    private boolean pause = false;
    private PausePannel pp;

    private int Key;
    private int code;

    XQWLCanvas(XQWLMIDlet midlet_) {
        midlet = midlet_;
        setFullScreenMode(true);
        pp = new PausePannel(midlet, this, this.getWidth(), this.getHeight());
    }

    public void Start() {
        isRunning = true;
        load();
        Thread t = new Thread(this);
        t.start();
    }

    void load() {
        setFullScreenMode(true);
        cursorX = cursorY = 7;
        sqSelected = mvLast = 0;
        if (midlet.rsData[0] == 0) {
            pos.fromFen(Position.STARTUP_FEN[midlet.handicap]);
        } else {
            // Restore Record-Score Data
            pos.clearBoard();
            for (int sq = 0; sq < 256; sq++) {
                int pc = midlet.rsData[sq + 256];
                if (pc > 0) {
                    pos.addPiece(sq, pc);
                }
            }
            if (midlet.rsData[0] == 2) {
                pos.changeSide();
            }
            pos.setIrrev();
        }
        // Backup Retract Status
        System.arraycopy(midlet.rsData, 0, retractData, 0, XQWLMIDlet.RS_DATA_LEN);
        // Call "responseMove()" if Computer Moves First
        phase = PHASE_LOADING;
        if (pos.sdPlayer == 0 ? midlet.moveMode == COMPUTER_RED :
                midlet.moveMode == COMPUTER_BLACK) {
            new Thread() {
                public void run() {
                    while (phase == PHASE_LOADING) {
                        try {
                            sleep(100);
                        } catch (InterruptedException e) {
                            // Ignored
                        }
                    }
                    responseMove();
                    repaint();
                }
            }.start();
        }
    }

    public void Draw(Graphics g) {
        if (phase == PHASE_LOADING) {
            // Wait 1 second for resizing
            width = getWidth();
            height = getHeight();
            for (int i = 0; i < 10; i++) {
                if (width != normalWidth || height != normalHeight) {
                    break;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // Ignored
                }
                width = getWidth();
                height = getHeight();
            }
            if (!init) {
                init = true;

                String imagePath = "/res/images/";
                squareSize = Math.min(width / 9, height / 10);
                if (squareSize >= 36) {
                    squareSize = 50;
                    imagePath += "large/";
                } else if (squareSize >= 26) {
                    squareSize = 26;
                    imagePath += "medium/";
                } else if (squareSize >= 18) {
                    squareSize = 18;
                    imagePath += "small/";
                } else {
                    squareSize = 13;
                    imagePath += "tiny/";
                }
                int boardWidth = squareSize * 9;
                int boardHeight = squareSize * 10;
                try {
                    imgBoard = Image.createImage(imagePath + "board.png");
                    imgSelected = Image.createImage(imagePath + "selected.png");
                    imgSelected2 = Image.createImage(imagePath + "selected2.png");
                    imgCursor = Image.createImage(imagePath + "cursor.png");
                    imgCursor2 = Image.createImage(imagePath + "cursor2.png");
                    for (int pc = 0; pc < 24; pc++) {
                        if (IMAGE_NAME[pc] == null) {
                            imgPieces[pc] = null;
                        } else {
                            imgPieces[pc] = Image.createImage(imagePath + IMAGE_NAME[pc] + ".png");
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage());
                }
                left = (width - boardWidth) / 2;
                top = (height - boardHeight) / 2;
                right = left + boardWidth - 32;
                bottom = top + boardHeight - 32;
            }
            phase = PHASE_WAITING;
        }
        for (int x = 0; x < width; x += widthBackground) {
            for (int y = 0; y < height; y += heightBackground) {
                g.drawImage(imgBackground, x, y, Graphics.LEFT + Graphics.TOP);
            }
        }
        g.drawImage(imgBoard, width / 2, height / 2, Graphics.HCENTER + Graphics.VCENTER);
        for (int sq = 0; sq < 256; sq++) {
            if (Position.IN_BOARD(sq)) {
                int pc = pos.squares[sq];
                if (pc > 0) {
                    drawSquare(g, imgPieces[pc], sq);
                }
            }
        }
        int sqSrc = 0;
        int sqDst = 0;
        if (mvLast > 0) {
            sqSrc = Position.SRC(mvLast);
            sqDst = Position.DST(mvLast);
            drawSquare(g, (pos.squares[sqSrc] & 8) == 0 ? imgSelected : imgSelected2, sqSrc);
            drawSquare(g, (pos.squares[sqDst] & 8) == 0 ? imgSelected : imgSelected2, sqDst);
        } else if (sqSelected > 0) {
            drawSquare(g, (pos.squares[sqSelected] & 8) == 0 ? imgSelected : imgSelected2, sqSelected);
        }
        int sq = Position.COORD_XY(cursorX + Position.FILE_LEFT, cursorY + Position.RANK_TOP);
        if (midlet.moveMode == COMPUTER_RED) {
            sq = Position.SQUARE_FLIP(sq);
        }
        if (sq == sqSrc || sq == sqDst || sq == sqSelected) {
            drawSquare(g, (pos.squares[sq] & 8) == 0 ? imgCursor2 : imgCursor, sq);
        } else {
            drawSquare(g, (pos.squares[sq] & 8) == 0 ? imgCursor : imgCursor2, sq);
        }
        if (phase == PHASE_THINKING) {
            int x, y;
            x = width / 2 + 200;
            y = height / 2;
            g.drawImage(imgThinking, x, y, Graphics.LEFT + Graphics.TOP);
        } else if (phase == PHASE_EXITTING) {
            g.setFont(fontLarge);
            g.setColor(0x0000ff);
        }
        if (pause) pp.Draw(g);

        String numberString = String.valueOf(this.Key);
        int x = 0;
        int y = 0;
        g.setColor(0, 0, 0); // 黑色
        g.drawString(numberString, x, y, Graphics.TOP | Graphics.LEFT);

        String numberString_2 = String.valueOf(this.code);
        int x_2 = 0;
        int y_2 = 0;
        g.setColor(0, 0, 0); // 黑色
        g.drawString(numberString_2, x_2 + 50, y_2, Graphics.TOP | Graphics.LEFT);
    }

    protected void keyPressed(int keyCode) {
        this.Key = keyCode;
        if (phase == PHASE_EXITTING) {
            midlet.OpenMenu();
            midlet.CloseGame();
            return;
        }
        if (phase == PHASE_THINKING) {
            return;
        }

        int action = getGameAction(keyCode);
        if (keyCode == 8 || keyCode == 96 || keyCode == -6 || keyCode == 48 || keyCode == -31 || keyCode == -8 || keyCode == -9) {
            if (action != FIRE && action != UP && action != LEFT && action != RIGHT && action != DOWN) {
                pause = true;
            }
        }
        this.code = action;
        if (!pause) {
            int deltaX = 0, deltaY = 0;
            if (action == FIRE || keyCode == KEY_NUM5) {
                clickSquare();
            } else {
                switch (action) {
                    case UP:
                        deltaY = -1;
                        break;
                    case LEFT:
                        deltaX = -1;
                        break;
                    case RIGHT:
                        deltaX = 1;
                        break;
                    case DOWN:
                        deltaY = 1;
                        break;
                    default:
                        break;
                }
                cursorX = (cursorX + deltaX + 9) % 9;
                cursorY = (cursorY + deltaY + 10) % 10;
            }
        } else {
            pp.keyPressed(action);
        }
        repaint();
    }

    protected void hideNotify() {
        super.hideNotify();
        pause = true;
        repaint();
        System.out.println("Out");
    }

    protected void paint(Graphics g) {
        Draw(g);
    }

    private void clickSquare() {
        int sq = Position.COORD_XY(cursorX + Position.FILE_LEFT, cursorY + Position.RANK_TOP);
        if (midlet.moveMode == COMPUTER_RED) {
            sq = Position.SQUARE_FLIP(sq);
        }
        int pc = pos.squares[sq];
        if ((pc & Position.SIDE_TAG(pos.sdPlayer)) != 0) {
            midlet.playSound(RESP_CLICK);
            mvLast = 0;
            sqSelected = sq;
        } else {
            if (sqSelected > 0 && addMove(Position.MOVE(sqSelected, sq)) && !responseMove()) {
                midlet.rsData[0] = 0;
                phase = PHASE_EXITTING;
            }
        }
    }

    private void drawSquare(Graphics g, Image image, int sq) {
        int sqFlipped = (midlet.moveMode == COMPUTER_RED ? Position.SQUARE_FLIP(sq) : sq);
        int sqX = left + (Position.FILE_X(sqFlipped) - Position.FILE_LEFT) * squareSize + 2;
        int sqY = top + (Position.RANK_Y(sqFlipped) - Position.RANK_TOP) * squareSize + 2;
        g.drawImage(image, sqX, sqY, Graphics.LEFT + Graphics.TOP);
    }

    /**
     * Player Move Result
     */
    private boolean getResult() {
        return getResult(midlet.moveMode == COMPUTER_NONE ?
                RESP_HUMAN_BOTH : RESP_HUMAN_SINGLE);
    }

    /**
     * Computer Move Result
     */
    private boolean getResult(int response) {
        if (pos.isMate()) {
            midlet.playSound(response < 0 ? RESP_WIN : RESP_LOSS);
            midlet.OpenGameOver(response < 0);
            midlet.CloseGame();
            return true;
        }
//        int vlRep = pos.repStatus(3);
//        if (vlRep > 0) {
//            vlRep = (response < 0 ? pos.repValue(vlRep) : -pos.repValue(vlRep));
//            midlet.playSound(vlRep > Position.WIN_VALUE ? RESP_LOSS :
//                    vlRep < -Position.WIN_VALUE ? RESP_WIN : RESP_DRAW);
//            message = (vlRep > Position.WIN_VALUE ? "长打作负，请不要气馁！" :
//                    vlRep < -Position.WIN_VALUE ? "电脑长打作负，祝贺你取得胜利！" : "双方不变作和，辛苦了！");
//            return true;
//        }
//        if (pos.moveNum > 100) {
//            midlet.playSound(RESP_DRAW);
//            message = "超过自然限着作和，辛苦了！";
//            return true;
//        }
        if (response != RESP_HUMAN_SINGLE) {
            if (response >= 0) {
                midlet.playSound(response);
            }
            // Backup Retract Status
            System.arraycopy(midlet.rsData, 0, retractData, 0, XQWLMIDlet.RS_DATA_LEN);
            // Backup Record-Score Data
            midlet.rsData[0] = (byte) (pos.sdPlayer + 1);
            System.arraycopy(pos.squares, 0, midlet.rsData, 256, 256);
        }
        return false;
    }

    private boolean addMove(int mv) {
        if (pos.legalMove(mv)) {
            if (pos.makeMove(mv, true)) {
                midlet.playSound(pos.inCheck() ? RESP_CHECK :
                        pos.captured() ? RESP_CAPTURE : RESP_MOVE);
                if (pos.captured()) {
                    pos.setIrrev();
                }
                sqSelected = 0;
                mvLast = mv;
                return true;
            }
            midlet.playSound(RESP_ILLEGAL);
        }
        return false;
    }

    boolean responseMove() {
        if (getResult()) {
            return false;
        }
        if (midlet.moveMode == COMPUTER_NONE) {
            return true;
        }
        phase = PHASE_THINKING;
        repaint();
        mvLast = search.searchMain(1000 << (midlet.level << 1));
        pos.makeMove(mvLast, true);
        int response = pos.inCheck() ? RESP_CHECK2 :
                pos.captured() ? RESP_CAPTURE2 : RESP_MOVE2;
        if (pos.captured()) {
            pos.setIrrev();
        }
        phase = PHASE_WAITING;
        repaint();
        return !getResult(response);
    }

    void back() {
        midlet.rsData[0] = 0;
        midlet.startMusic("form");
    }

    void retract() {
        // Restore Retract Status
        System.arraycopy(retractData, 0, midlet.rsData, 0, XQWLMIDlet.RS_DATA_LEN);
        load();
        repaint();
    }

    public void Stop() {
        isRunning = false;
    }

    public void run() {
        while (isRunning) {
            for (int i = 0; i < 10; ++i) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // Ignored
                }

                repaint();
            }
            break;
        }
    }

    public void RestartGame() {
        pause = false;
        repaint();
    }
}