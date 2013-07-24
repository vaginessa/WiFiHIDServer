package org.catrobat.wifihidserver;

import java.util.ArrayList;

import org.catrobat.catroid.io.Command;
import org.catrobat.wifihidserver.Connection.Instruction;


public class InputHandler extends Thread implements Instruction{
	
	private Thread thisThread;
	private ArrayList<Command> instructionList;
	private KeyBoard keyboard;
	
	public InputHandler(KeyBoard keyboard_) {
		keyboard = keyboard_;
		this.setName("InputHandler");
	}
	
	public void run() {
		thisThread = this;
		instructionList = new ArrayList<Command>();
		while(thisThread == this){
			if (instructionList.size() == 0) {
				try {
					sleep(1L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				handleCommand(instructionList.get(0));
				instructionList.remove(0);
			}
		}
	}
	
	public void handleCommand(Command command) {
		switch (command.getCommandType()) {
		case SINGLE_KEY:
			keyboard.setKeyToHandle(command.getKey());
			break;
		case KEY_COMBINATION:
			keyboard.setKeyToHandle(command.getKeyComb());
			break;
		case MOUSE:			
			break;
		default:
			break;
		}			
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
		//instructionList.add(input);
	}
	
	public void stopThread(){
		thisThread = null;
	}
    
    public interface KeyToHandle{
    	public void setKeyToHandle(int key);
    	public void setKeyToHandle(int[] key);
    }
}
