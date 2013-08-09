package org.catrobat.wifihidserver;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.catrobat.wifihidserver.InputHandler.KeyToHandle;


public class KeyBoard implements KeyToHandle {

    private Robot robot;
    private final static ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
    
    public KeyBoard() throws AWTException {
    	this.robot = new Robot();
    }

    public boolean typeDown(char character) {
    	switch (character) {
    	case 'a': robot.keyPress(KeyEvent.VK_A); break;
    	case 'b': robot.keyPress(KeyEvent.VK_B); break;
    	case 'c': robot.keyPress(KeyEvent.VK_C); break;
    	case 'd': robot.keyPress(KeyEvent.VK_D); break;
    	case 'e': robot.keyPress(KeyEvent.VK_E); break;
    	case 'f': robot.keyPress(KeyEvent.VK_F); break;
    	case 'g': robot.keyPress(KeyEvent.VK_G); break;
    	case 'h': robot.keyPress(KeyEvent.VK_H); break;
    	case 'i': robot.keyPress(KeyEvent.VK_I); break;
    	case 'j': robot.keyPress(KeyEvent.VK_J); break;
    	case 'k': robot.keyPress(KeyEvent.VK_K); break;
    	case 'l': robot.keyPress(KeyEvent.VK_L); break;
    	case 'm': robot.keyPress(KeyEvent.VK_M); break;
    	case 'n': robot.keyPress(KeyEvent.VK_N); break;
    	case 'o': robot.keyPress(KeyEvent.VK_O); break;
    	case 'p': robot.keyPress(KeyEvent.VK_P); break;
    	case 'q': robot.keyPress(KeyEvent.VK_Q); break;
    	case 'r': robot.keyPress(KeyEvent.VK_R); break;
    	case 's': robot.keyPress(KeyEvent.VK_S); break;
    	case 't': robot.keyPress(KeyEvent.VK_T); break;
    	case 'u': robot.keyPress(KeyEvent.VK_U); break;
    	case 'v': robot.keyPress(KeyEvent.VK_V); break;
    	case 'w': robot.keyPress(KeyEvent.VK_W); break;
    	case 'x': robot.keyPress(KeyEvent.VK_X); break;
    	case 'y': robot.keyPress(KeyEvent.VK_Y); break;
    	case 'z': robot.keyPress(KeyEvent.VK_Z); break;
    	case '0': robot.keyPress(KeyEvent.VK_0); break;
//    	case '1': robot.keyPress(KeyEvent.VK_1); break;
//    	case '2': robot.keyPress(KeyEvent.VK_2); break;
//    	case '3': robot.keyPress(KeyEvent.VK_3); break;
//    	case '4': robot.keyPress(KeyEvent.VK_4); break;
    	case '1': robot.keyPress(KeyEvent.VK_LEFT); break;
    	case '2': robot.keyPress(KeyEvent.VK_RIGHT); break;
    	case '3': robot.keyPress(KeyEvent.VK_UP); break;
    	case '4': robot.keyPress(KeyEvent.VK_DOWN); break;
    	case '5': robot.keyPress(KeyEvent.VK_5); break;
    	case '6': robot.keyPress(KeyEvent.VK_6); break;
    	case '7': robot.keyPress(KeyEvent.VK_7); break;
    	case '8': robot.keyPress(KeyEvent.VK_8); break;
    	case '9': robot.keyPress(KeyEvent.VK_9); break;
    	case ' ': robot.keyPress(KeyEvent.VK_SPACE); break;
    	case 14: robot.keyPress(KeyEvent.VK_SHIFT); break;
    	case 17: robot.keyPress(KeyEvent.VK_CONTROL); break;
    	//TODO: arrow keys
    	default:
    		System.out.println("Illegeal character." + character);
    		return false;
    		//throw new IllegalArgumentException("Cannot type character " + character);
    	}
    	return true;
    }
    
    public void typeUp(char character) {
    	switch (character) {
    	case 'a': robot.keyRelease(KeyEvent.VK_A); break;
    	case 'b': robot.keyRelease(KeyEvent.VK_B); break;
    	case 'c': robot.keyRelease(KeyEvent.VK_C); break;
    	case 'd': robot.keyRelease(KeyEvent.VK_D); break;
    	case 'e': robot.keyRelease(KeyEvent.VK_E); break;
    	case 'f': robot.keyRelease(KeyEvent.VK_F); break;
    	case 'g': robot.keyRelease(KeyEvent.VK_G); break;
    	case 'h': robot.keyRelease(KeyEvent.VK_H); break;
    	case 'i': robot.keyRelease(KeyEvent.VK_I); break;
    	case 'j': robot.keyRelease(KeyEvent.VK_J); break;
    	case 'k': robot.keyRelease(KeyEvent.VK_K); break;
    	case 'l': robot.keyRelease(KeyEvent.VK_L); break;
    	case 'm': robot.keyRelease(KeyEvent.VK_M); break;
    	case 'n': robot.keyRelease(KeyEvent.VK_N); break;
    	case 'o': robot.keyRelease(KeyEvent.VK_O); break;
    	case 'p': robot.keyRelease(KeyEvent.VK_P); break;
    	case 'q': robot.keyRelease(KeyEvent.VK_Q); break;
    	case 'r': robot.keyRelease(KeyEvent.VK_R); break;
    	case 's': robot.keyRelease(KeyEvent.VK_S); break;
    	case 't': robot.keyRelease(KeyEvent.VK_T); break;
    	case 'u': robot.keyRelease(KeyEvent.VK_U); break;
    	case 'v': robot.keyRelease(KeyEvent.VK_V); break;
    	case 'w': robot.keyRelease(KeyEvent.VK_W); break;
    	case 'x': robot.keyRelease(KeyEvent.VK_X); break;
    	case 'y': robot.keyRelease(KeyEvent.VK_Y); break;
    	case 'z': robot.keyRelease(KeyEvent.VK_Z); break;
//    	case '0': robot.keyRelease(KeyEvent.VK_0); break;
//    	case '1': robot.keyRelease(KeyEvent.VK_1); break;
//    	case '2': robot.keyRelease(KeyEvent.VK_2); break;
//    	case '3': robot.keyRelease(KeyEvent.VK_3); break;
//    	case '4': robot.keyRelease(KeyEvent.VK_4); break;
    	case '1': robot.keyRelease(KeyEvent.VK_LEFT); break;
    	case '2': robot.keyRelease(KeyEvent.VK_RIGHT); break;
    	case '3': robot.keyRelease(KeyEvent.VK_UP); break;
    	case '4': robot.keyRelease(KeyEvent.VK_DOWN); break;
    	case '5': robot.keyRelease(KeyEvent.VK_5); break;
    	case '6': robot.keyRelease(KeyEvent.VK_6); break;
    	case '7': robot.keyRelease(KeyEvent.VK_7); break;
    	case '8': robot.keyRelease(KeyEvent.VK_8); break;
    	case '9': robot.keyRelease(KeyEvent.VK_9); break;
    	case ' ': robot.keyRelease(KeyEvent.VK_SPACE); break;
    	case 14: robot.keyRelease(KeyEvent.VK_SHIFT); break;
    	case 17: robot.keyRelease(KeyEvent.VK_CONTROL); break;
    	//TODO: arrow keys
    	default:
    		throw new IllegalArgumentException("Cannot type character " + character);
    	}
    }
    
    public boolean setKeyToHandle(int key_){
    	Byte key = (byte)(key_);
    	final char command = (char)key.byteValue();
    	if (!typeDown(command)) {
    		return false;
    	}
    	Runnable task = new Runnable() {
			
			@Override
			public void run() {
				typeUp(command);				
			}
		};
		worker.schedule(task, 225, TimeUnit.MILLISECONDS);
    	return true;
    }
    
    public boolean setKeyToHandle(int[] list){
    	Byte key;
    	char command;
		for(int i = 0; i < list.length; i++){
			key = (byte)(list[i]);
			command = (char)key.byteValue();
			if (!typeDown(command)) {
				return false;
			}
		}
		for(int i = list.length - 1; i >= 0; i--){
			key = (byte)(list[i]);
			command = (char)key.byteValue();
			typeUp(command);
		}
		return true;
    }
}