package org.catrobat.wifihidserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.catrobat.catroid.io.Command;
import org.catrobat.catroid.io.Confirmation.ConfirmationState;
import org.catrobat.wifihidserver.Connection.Instruction;


public class InputHandler extends Thread implements Instruction{
	
	private Thread thisThread;
	private HashMap<Command, Connection> instructionList;
	private KeyBoard keyboard;
	
	public InputHandler(KeyBoard keyboard) {
		this.keyboard = keyboard;
		this.setName("InputHandler");
	}
	
	public void run() {
		thisThread = this;
		instructionList = new HashMap<Command, Connection>();
		while(thisThread == this){
			if (instructionList.size() == 0) {
				try {
					sleep(1L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				Iterator<Entry<Command, Connection>> it = instructionList.entrySet().iterator();
				Command command = it.next().getKey();
				
				if (!handleCommand(command)) {
					instructionList.get(command).confirm(ConfirmationState.ILLEGAL_COMMAND);
				} else {
					instructionList.get(command).confirm(ConfirmationState.COMMAND_SEND_SUCCESSFULL);
				}
				instructionList.remove(command);
			}
		}
	}
	
	public boolean handleCommand(Command command) {
		switch (command.getCommandType()) {
		case SINGLE_KEY:
			if (!keyboard.setKeyToHandle(command.getKey())) {
				return false;
			}
			break;
		case KEY_COMBINATION:
			if (!keyboard.setKeyToHandle(command.getKeyComb())) {
				return false;
			}
			break;
		case MOUSE:			
			break;
		default:
			break;
		}	
		return true;
	}
	
	public void onIncoming(Command input, Connection connection){
		if(input.getCommandType() == Command.commandType.SINGLE_KEY){
			System.out.println("Ip: " + connection.getIp() + " sends a " + input.getCommandType() + ": '" 
					+ input.getKey() + "' (ASCII)");
		}
		if(input.getCommandType() ==  Command.commandType.KEY_COMBINATION){
			System.out.print("Ip: " + connection.getIp() + " sends ");
			int [] commands = input.getKeyComb();
			System.out.print("'" + commands[0]);
			for(int i = 1; i < commands.length; i++){
				System.out.print("' + " + "'" + commands[i]);
			}
			System.out.print("' (ASCII)\n");
		}		
		instructionList.put(input, connection);
	}
	
	public void stopThread(){
		thisThread = null;
	}
    
    public interface KeyToHandle{
    	public boolean setKeyToHandle(int key);
    	public boolean setKeyToHandle(int[] key);
    }
}
