package org.catrobat.catroid.io;

import java.io.Serializable;

public class Confirmation implements Serializable {
	private static final long serialVersionUID = 1L;
	private int versionId;

	public static enum ConfirmationState {
		COMMAND_SEND_SUCCESSFULL, ILLEGAL_CLASS, ILLEGAL_COMMAND, LEGAL_VERSION_ID, ILLEGAL_VERSION_ID
	};

	private ConfirmationState state;

	public Confirmation(ConfirmationState state) {
		this.state = state;
	}

	public ConfirmationState getConfirmationState() {
		return state;
	}

	public int getVersionId() {
		return versionId;
	}

	public void setVersionId(int versionId) {
		this.versionId = versionId;
	}
}
