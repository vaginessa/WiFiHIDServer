package org.catrobat.wifihidserver;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

	  import org.catrobat.catroid.io.Command;
import org.catrobat.catroid.io.Confirmation.ConfirmationState;
import org.catrobat.wifihidserver.Connection.Instruction;


public class InputHandler extends Thread implements Instruction{
	
	private Thread thisThread;
	private HashMap<Command, Connection> instructionList = new HashMap<Command, Connection>();
	private KeyBoard keyboard;
	
	public InputHandler(KeyBoard keyboard) {
		this.keyboard = keyboard;
		this.setName("InputHandler");
	}
	
	public void run() {
		thisThread = this;
		while(thisThread == this){
			if (instructionList.size() == 0) {
				try {
					sleep(1L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				synchronized (instructionList) {
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
			System.out.println("Ip: " + connection.getIp() + " sends a " + input.getCommandType() + ": -" 
					+ getKeyName(input.getKey()) + "-");
		}
		if(input.getCommandType() ==  Command.commandType.KEY_COMBINATION){
			System.out.print("Ip: " + connection.getIp() + " sends ");
			int [] commands = input.getKeyComb();
			System.out.print("'" + commands[0]);
			for(int i = 1; i < commands.length; i++){
				System.out.print("' + " + "-" + getKeyName(commands[i]));
			}
			System.out.print("-\n");
		}		
		synchronized (instructionList) {
			instructionList.put(input, connection);
		}
		
	}
	
	public void stopThread(){
		thisThread = null;
	}
    
    public interface KeyToHandle{
    	public boolean setKeyToHandle(int key);
    	public boolean setKeyToHandle(int[] key);
    }
    
    public String getKeyName(int key) {
    	String keyName = "";
    	switch (key) {
			case 1:
				keyName = "ALT";
				break;
			case 2:
				keyName = "ALT GR";
				break;
			case 8:
				keyName = "BACK SPACE";
				break;
			case 9:
				keyName = "TAB";
				break;
			case 13:
				keyName = "ENTER";
				break;
			case 16:
				keyName = "SHIFT";
				break;
			case 17:
				keyName = "CTRL";
				break;
			case 20:
				keyName = "CAPS LOCK";
				break;
			case 27:
				keyName = "ESC";
				break;
			case 28:
				keyName = "UP";
				break;
			case 32:
				keyName = "SPACE";
				break;
			case 37:
				keyName = "LEFT";
				break;
			case 39:
				keyName = "RIGHT";
				break;
			case 40:
				keyName = "DOWN";
				break;
			default:
				break;
		}
    	return keyName;
    }
}
