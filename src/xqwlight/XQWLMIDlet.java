/*
XQWLMIDlet.java - Source Code for XiangQi Wizard Light, Part III

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

import java.io.InputStream;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.VolumeControl;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;

public class XQWLMIDlet extends MIDlet implements IExit {
    private static final String STORE_NAME = "XQWLight";

    static final String[] SOUND_NAME = {
            "click", "illegal", "move", "move2", "capture", "capture2",
            "check", "check2", "win", "draw", "loss",
    };

    static final int RS_DATA_LEN = 512;

    byte[] rsData = new byte[RS_DATA_LEN];
    public int moveMode, handicap, level, sound, music;
    Player midiPlayer = null;

    private final Display display;
    public XQWLCanvas canvas;
    public GameOverNew gameOver;
    public Menu gameMenu;

//    Form form = new Form("中国象棋");
//    Command cmdStart = new Command("开始", Command.OK, 1);
//    Command cmdExit = new Command("退出", Command.BACK, 1);
//
//    ChoiceGroup cgMoveMode = new ChoiceGroup("谁先走", Choice.EXCLUSIVE,
//            new String[]{"我先走", "电脑先走", "不用电脑"}, null);
//    ChoiceGroup cgHandicap = new ChoiceGroup("先走让子", Choice.POPUP,
//            new String[]{"不让子", "让左马", "让双马", "让九子"}, null);
//    ChoiceGroup cgLevel = new ChoiceGroup("电脑水平", Choice.POPUP,
//            new String[]{"入门", "业余", "专业"}, null);
//
//    {
//        form.append(cgMoveMode);
//        form.append(cgHandicap);
//        form.append(cgLevel);
//        form.addCommand(cmdStart);
//        form.addCommand(cmdExit);
//
//        form.setCommandListener(new CommandListener() {
//            public void commandAction(Command c, Displayable d) {
//                if (c == cmdStart) {
//                    moveMode = cgMoveMode.getSelectedIndex();
//                    handicap = cgHandicap.getSelectedIndex();
//                    level = cgLevel.getSelectedIndex();
//                    canvas.load();
//                    startMusic("canvas");
//                    Display.getDisplay(XQWLMIDlet.this).setCurrent(canvas);
//                } else if (c == cmdExit) {
//                    destroyApp(false);
//                    notifyDestroyed();
//                }
//            }
//        });
//    }

    private boolean started = false;

    public XQWLMIDlet() {
        this.display = Display.getDisplay(this);
        canvas = new XQWLCanvas(this);
        gameMenu = new Menu();
        gameMenu.midlet = this;
        gameOver = new GameOverNew();
        gameOver.midlet = this;
    }

    public void startApp() {
        if (started) {
            return;
        }
        started = true;
//        RestoreData();
        startMusic("form");
        OpenMenu();
    }

    public void pauseApp() {
        // Do Nothing
    }

    public void destroyApp(boolean unc) {
        stopMusic();
//        SavaData();
        started = false;
        notifyDestroyed();
    }

    Player createPlayer(String name, String type) {
        InputStream in = getClass().getResourceAsStream(name);
        try {
            return Manager.createPlayer(in, type);
            // If creating "Player" succeeded, no need to close "InputStream".
        } catch (Exception e) {
            try {
                in.close();
            } catch (Exception e2) {
                // Ignored
            }
            return null;
        }
    }

    void playSound(int response) {

    }

    void stopMusic() {

    }

    void startMusic(String strMusic) {

    }

    void RestoreData() {
        for (int i = 0; i < RS_DATA_LEN; i++) {
            rsData[i] = 0;
        }
        rsData[19] = 3;
        rsData[20] = 2;
        try {
            RecordStore rs = RecordStore.openRecordStore(STORE_NAME, true);
            RecordEnumeration re = rs.enumerateRecords(null, null, false);
            if (re.hasNextElement()) {
                int recordId = re.nextRecordId();
                if (rs.getRecordSize(recordId) == RS_DATA_LEN) {
                    rsData = rs.getRecord(recordId);
                } else {
                    rs.setRecord(recordId, rsData, 0, RS_DATA_LEN);
                }
            } else {
                rs.addRecord(rsData, 0, RS_DATA_LEN);
            }
            rs.closeRecordStore();
        } catch (Exception e) {
            // Ignored
        }
        moveMode = Util.MIN_MAX(0, rsData[16], 2);
        handicap = Util.MIN_MAX(0, rsData[17], 3);
        level = Util.MIN_MAX(1, rsData[18], 2);
        sound = Util.MIN_MAX(0, rsData[19], 5);
        music = Util.MIN_MAX(0, rsData[20], 5);
    }

    void SavaData() {
        rsData[16] = (byte) moveMode;
        rsData[17] = (byte) handicap;
        rsData[18] = (byte) level;
        rsData[19] = (byte) sound;
        rsData[20] = (byte) music;
        try {
            RecordStore rs = RecordStore.openRecordStore(STORE_NAME, true);
            RecordEnumeration re = rs.enumerateRecords(null, null, false);
            if (re.hasNextElement()) {
                int recordId = re.nextRecordId();
                rs.setRecord(recordId, rsData, 0, RS_DATA_LEN);
            } else {
                rs.addRecord(rsData, 0, RS_DATA_LEN);
            }
            rs.closeRecordStore();
        } catch (Exception e) {
            // Ignored
        }
    }

    public void OpenMenu() {
        gameMenu.start();
        display.setCurrent(gameMenu);
    }

    public void CloseMenu() {
        gameMenu.stop();
    }

    public void StartGame() {
        display.setCurrent(canvas);
        InitData();

        canvas.Start();
        startMusic("canvas");
    }

    public void CloseGame() {
        canvas.back();
    }

    public void exitMIDlet() {
        destroyApp(true);
    }

    public void OpenGameOver(boolean win) {
        gameOver.start(win);
        display.setCurrent(gameOver);
    }

    public void CloseGameOver() {
        gameOver.stop();
    }

    public void InitData() {
        moveMode = 0;
    }

    public void Exit() {
        exitMIDlet();
    }
}