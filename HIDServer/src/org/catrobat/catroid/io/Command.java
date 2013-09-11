package org.catrobat.catroid.io;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Command implements Serializable {
	public static enum commandType {
		SINGLE_KEY, KEY_COMBINATION, MOUSE
	};

	private int key;
	private commandType type;
	private int[] keyCombination;

	public Command(int key, commandType type) {
		this.key = key;
		this.type = type;
	}

	public Command(int[] keyCombination, commandType type) {
		this.keyCombination = keyCombination;
		this.type = type;
	}

	public int getKey() {
		return key;
	}

	public int[] getKeyComb() {
		return keyCombination;
	}

	public commandType getCommandType() {
		return type;
	}
}