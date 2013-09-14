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
	private final static ScheduledExecutorService worker = Executors
			.newSingleThreadScheduledExecutor();

	public KeyBoard() throws AWTException {
		this.robot = new Robot();
	}

	public boolean typeDown(char character) {
		switch (character) {
		case 1:
			robot.keyPress(KeyEvent.VK_ALT);
			break;
		case 2:
			robot.keyPress(KeyEvent.VK_ALT_GRAPH);
			break;
		case 8:
			robot.keyPress(KeyEvent.VK_BACK_SPACE);
			break;
		case 9:
			robot.keyPress(KeyEvent.VK_TAB);
			break;
		case 13:
			robot.keyPress(KeyEvent.VK_ENTER);
			break;
		case 16:
			robot.keyPress(KeyEvent.VK_SHIFT);
			break;
		case 17:
			robot.keyPress(KeyEvent.VK_CONTROL);
			break;
		case 20:
			robot.keyPress(KeyEvent.VK_CAPS_LOCK);
			break;
		case 27:
			robot.keyPress(KeyEvent.VK_ESCAPE);
			break;
		case 28:
			robot.keyPress(KeyEvent.VK_UP);
			break;
		case 32:
			robot.keyPress(KeyEvent.VK_SPACE);
			break;
		case 37:
			robot.keyPress(KeyEvent.VK_LEFT);
			break;
		case 39:
			robot.keyPress(KeyEvent.VK_RIGHT);
			break;
		case 40:
			robot.keyPress(KeyEvent.VK_DOWN);
			break;
		default:
			System.out.println("Illegeal character." + character);
			return false;
			// throw new IllegalArgumentException("Cannot type character " +
			// character);
		}
		return true;
	}

	public void typeUp(char character) {
		switch (character) {
		case 1:
			robot.keyRelease(KeyEvent.VK_ALT);
			break;
		case 2:
			robot.keyRelease(KeyEvent.VK_ALT_GRAPH);
			break;
		case 8:
			robot.keyRelease(KeyEvent.VK_BACK_SPACE);
			break;
		case 9:
			robot.keyRelease(KeyEvent.VK_TAB);
			break;
		case 13:
			robot.keyRelease(KeyEvent.VK_ENTER);
			break;
		case 16:
			robot.keyRelease(KeyEvent.VK_SHIFT);
			break;
		case 17:
			robot.keyRelease(KeyEvent.VK_CONTROL);
			break;
		case 20:
			robot.keyRelease(KeyEvent.VK_CAPS_LOCK);
			break;
		case 27:
			robot.keyRelease(KeyEvent.VK_ESCAPE);
			break;
		case 28:
			robot.keyRelease(KeyEvent.VK_UP);
			break;
		case 32:
			robot.keyRelease(KeyEvent.VK_SPACE);
			break;
		case 37:
			robot.keyRelease(KeyEvent.VK_LEFT);
			break;
		case 39:
			robot.keyRelease(KeyEvent.VK_RIGHT);
			break;
		case 40:
			robot.keyRelease(KeyEvent.VK_DOWN);
			break;
		default:
			throw new IllegalArgumentException("Cannot type character "
					+ character);
		}
	}

	public boolean setKeyToHandle(int key_) {
		Byte key = (byte) (key_);
		final char command = (char) key.byteValue();
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

	public boolean setKeyToHandle(int[] list) {
		Byte key;
		char command;
		for (int i = 0; i < list.length; i++) {
			key = (byte) (list[i]);
			command = (char) key.byteValue();
			if (!typeDown(command)) {
				return false;
			}
		}
		for (int i = list.length - 1; i >= 0; i--) {
			key = (byte) (list[i]);
			command = (char) key.byteValue();
			typeUp(command);
		}
		return true;
	}
}