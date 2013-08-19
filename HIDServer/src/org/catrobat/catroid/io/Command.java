package org.catrobat.catroid.io;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Command implements Serializable {
	public static enum commandType {
		SINGLE_KEY, KEY_COMBINATION, MOUSE
	};

	private int key;
	private commandType type;
	private int[] keyComb;

	public Command(int key, commandType type) {
		this.key = key;
		this.type = type;
	}

	public Command(int[] keyComb, commandType type) {
		this.keyComb = keyComb;
		this.type = type;
	}

	public int getKey() {
		return key;
	}

	public int[] getKeyComb() {
		return keyComb;
	}

	public commandType getCommandType() {
		return type;
	}
}