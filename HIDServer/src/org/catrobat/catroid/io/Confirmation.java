package org.catrobat.catroid.io;

import java.io.Serializable;

public class Confirmation implements Serializable {
	private static final long serialVersionUID = 1L;

	public static enum ConfirmationState {
		COMMAND_SEND_SUCCESSFULL, ILLEGAL_CLASS, ILLEGAL_COMMAND
	};

	private ConfirmationState state;

	public Confirmation(ConfirmationState state) {
		this.state = state;
	}

	public ConfirmationState getConfirmationState() {
		return state;
	}
}
