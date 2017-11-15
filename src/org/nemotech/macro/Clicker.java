/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 Sean Niemann
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.nemotech.macro;

import java.awt.*;
import java.awt.event.*;

/**
 * This class is a macro tool that supports 'autoclicking' with a custom delay.
 * 
 * @author Sean Niemann
 * @since  09/13/2015
 */
public class Clicker extends Frame implements ActionListener, KeyEventDispatcher {
	
	private static final long serialVersionUID = 3156838326551517310L;

	public static void main(String[] args) {
        try {
        	new Clicker().create();
        } catch(AWTException e) {
        	System.err.println("Unable to create robot instance");
        }
    }
    
    private final int MIN_DELAY = 50;
    private final int MAX_DELAY = 60000;
    
    private Button buttonStart, buttonStop;
    private Label labelStatus, labelSeconds, labelMillis;
    private TextField textSeconds, textMillis;
    private Robot robot;
    private volatile boolean running;
    private int delay, clicks;
    private Thread clickThread;
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        switch(e.getKeyCode()) {
            case KeyEvent.VK_F1:
                if(checkInput() && buttonStart.isEnabled()) {
                    buttonStart.setEnabled(false);
                    buttonStop.setEnabled(true);
                    startClicker();
                }
                break;
            case KeyEvent.VK_F2:
                if(buttonStop.isEnabled()) {
                    buttonStop.setEnabled(false);
                    buttonStart.setEnabled(true);
                    stopClicker();
                }
                break;
        }
        return false;
    }
    
    public Clicker() throws AWTException {
        buttonStart = new Button("Start (F1)");
        buttonStop = new Button("Stop (F2)");
        buttonStop.setEnabled(false);
        labelStatus = new Label("Enter speed and press start", Label.CENTER);
        labelSeconds = new Label("Seconds");
        labelMillis = new Label("Millis");
        textSeconds = new TextField("0");
        textMillis = new TextField("0");
        robot = new Robot();
        running = false;
    }
    
    public void create() {
        setTitle("Auto Clicker");
        setLayout(null);
        setCloseOperation();
        setSize(240, 130);
        setResizable(false);
        setAlwaysOnTop(true);
        setLocationRelativeTo(null);
        addComponents();
        addActionListeners();
        addKeyEventDispatcher();
        setVisible(true);
    }
    
    private void addComponents() {
        add(buttonStart).setBounds(0, 20, 120, 40);
        add(buttonStop).setBounds(120, 20, 120, 40);
        add(labelStatus).setBounds(20, 60, 200, 20);
        add(textSeconds).setBounds(40, 100, 40, 20);
        add(labelSeconds).setBounds(80, 100, 60, 20);
        add(textMillis).setBounds(140, 100, 40, 20);
        add(labelMillis).setBounds(180, 100, 60, 20);
    }
    
    private void addActionListeners() {
        buttonStart.addActionListener(this);
        buttonStop.addActionListener(this);
    }
    
    private void addKeyEventDispatcher() {
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(this);
    }
    
    @Override
    public void actionPerformed(ActionEvent evt) {
        if(evt.getSource() == buttonStart && checkInput()) {
            buttonStart.setEnabled(false);
            buttonStop.setEnabled(true);
            startClicker();
        }
        if(evt.getSource() == buttonStop) {
            buttonStop.setEnabled(false);
            buttonStart.setEnabled(true);
            stopClicker();
        }
    }
    
    private void startClicker() {
        clicks = 0;
        running = true;
        clickThread = new Thread(new Runnable() {
            public void run() {
                try {
                    labelStatus.setText("Starting in: 3");
                    Thread.sleep(1000);
                    labelStatus.setText("Starting in: 2");
                    Thread.sleep(1000);
                    labelStatus.setText("Starting in: 1");
                    Thread.sleep(1000);
                    labelStatus.setText("Started with a " + delay + " ms delay");
                    while(running) {
                        robot.mousePress(InputEvent.BUTTON1_MASK);
                        robot.mouseRelease(InputEvent.BUTTON1_MASK);
                        clicks++;
                        Thread.sleep(delay);
                    }
                } catch(InterruptedException e) {
                    // ignore
                }
            }
        });
        clickThread.start();
    }
    
    private void stopClicker() {
        running = false;
        clickThread.interrupt();
        labelStatus.setText("Stopped after " + clicks + " clicks");
    }
    
    private boolean checkInput() {
        if(textSeconds.getText().equals("")) textSeconds.setText("0");
        if(textMillis.getText().equals("")) textMillis.setText("0");
        try {
            delay = Integer.parseInt(textSeconds.getText()) * 1000 + Integer.parseInt(textMillis.getText());
        } catch(NumberFormatException e) {
            labelStatus.setText("Delay requires numeric values");
            return false;
        }
        if(delay < MIN_DELAY) {
            labelStatus.setText("Delay mimimum is " + MIN_DELAY + " ms");
            return false;
        }
        if(delay > MAX_DELAY) {
            labelStatus.setText("Delay maximum is " + MAX_DELAY + " ms");
            return false;
        }
        return true;
    }
    
    private void setCloseOperation() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                evt.getWindow().dispose();
                System.exit(0);
            }
        });
    }
    
}
