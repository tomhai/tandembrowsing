package org.tandembrowsing.io.soap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

import org.tandembrowsing.state.StateMachine;
import org.tandembrowsing.io.Event;
import org.tandembrowsing.io.Operation;
import org.tandembrowsing.model.Cell;

public class TestClient implements IEventThread {

	private static Logger logger = Logger.getLogger("org.tandembrowsing.io.soap");
	
	public static final void main(String args[]) {
		TestClient test = new TestClient();
		try {
			LayoutManagerClient client = new LayoutManagerClient(test);
			
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			boolean exit = false;
			while(!exit){
				logger.info("Command: ");
				String input = in.readLine();
				String args2[] = input.split("\\s");
				if (args2[0].equalsIgnoreCase(Event.EVENT_CHANGE_STATE)) {
					Event event = new Event(args2[0], args2[1], args2[2]);
					logger.info(args2[0]+" to "+args2[1]+" for "+args2[2]);
					boolean moreOperations = true;
					while(moreOperations) {
						logger.info("Add parameter: ");
						String inputO = in.readLine();
						String argsO[] = inputO.split("\\s");
						if(argsO[0].equalsIgnoreCase("setContent")) {
							Operation setContent = event.addOperation(argsO[0]);
							setContent.addParameter("id", argsO[1]);
							setContent.addParameter("resource", argsO[2]);				
							logger.info(argsO[0]+"("+"id:"+argsO[1]+",url:"+argsO[2]+")");
						} else if(argsO[0].equalsIgnoreCase("resizeAndMove")) {
							Operation resizeAndMove = event.addOperation(argsO[0]);
							resizeAndMove.addParameter(Cell.ID, argsO[1]);
							if(!argsO[2].equalsIgnoreCase("n"))
								resizeAndMove.addParameter(Cell.WIDTH, argsO[2]);	
							if(!argsO[3].equalsIgnoreCase("n"))
								resizeAndMove.addParameter(Cell.HEIGHT, argsO[3]);
							if(!argsO[4].equalsIgnoreCase("n"))
								resizeAndMove.addParameter(Cell.X_POSITION, argsO[4]);
							if(!argsO[5].equalsIgnoreCase("n"))
								resizeAndMove.addParameter(Cell.Y_POSITION, argsO[5]);
							if(!argsO[6].equalsIgnoreCase("n"))
								resizeAndMove.addParameter(Cell.Z_INDEX, argsO[6]);	
							logger.info(argsO[0]+"("+"id:"+argsO[1]+",width:"+argsO[2]+",height:"+argsO[3]+",xPosition:"+argsO[4]+",yPosition:"+argsO[5]+",zIndex:"+argsO[6]+")");
						} else if(argsO[0].equalsIgnoreCase("reload") || argsO[0].equalsIgnoreCase("mute") || argsO[0].equalsIgnoreCase("unMute")) {
							Operation reload = event.addOperation(argsO[0]);
							reload.addParameter(Cell.ID, argsO[1]);		
							logger.info(argsO[0]+"("+"id:"+argsO[1]+")");
						} else if(argsO[0].equalsIgnoreCase("run")) {
							moreOperations = false;
						} else if (argsO[0].equalsIgnoreCase("help")) {
							System.out.println("\nSupported operations:");
							System.out.println("  setContent <id> <url>");
							System.out.println("  resizeAndMove <id> <width> <height> <xPosition> <yPosition> <zIndex> (value 'n' means no change)");
							System.out.println("  reload <id>");
							System.out.println("  run\n");
						} else {
							logger.info("Wrong syntax!");
						}
					}		
					logger.info(client.processEvent(event));	
			
				} else if (args2[0].equalsIgnoreCase(Event.EVENT_MODIFY_STATE)) {
					Event event = new Event(args2[0], "", args2[1]);
					logger.info(args2[0]+" for "+args2[1]);
					boolean moreOperations = true;
					while(moreOperations) {
						logger.info("Add operation: ");
						String inputO = in.readLine();
						String argsO[] = inputO.split("\\s");
						if(argsO[0].equalsIgnoreCase("setContent")) {
							Operation setContent = event.addOperation(argsO[0]);
							setContent.addParameter(Cell.ID, argsO[1]);
							setContent.addParameter(Cell.RESOURCE, argsO[2]);				
							logger.info(argsO[0]+"("+"id:"+argsO[1]+",url:"+argsO[2]+")");
						} else if(argsO[0].equalsIgnoreCase("resizeAndMove")) {
							Operation resizeAndMove = event.addOperation(argsO[0]);
							resizeAndMove.addParameter(Cell.ID, argsO[1]);
							if(!argsO[2].equalsIgnoreCase("n"))
								resizeAndMove.addParameter(Cell.WIDTH, argsO[2]);	
							if(!argsO[3].equalsIgnoreCase("n"))
								resizeAndMove.addParameter(Cell.HEIGHT, argsO[3]);
							if(!argsO[4].equalsIgnoreCase("n"))
								resizeAndMove.addParameter(Cell.X_POSITION, argsO[4]);
							if(!argsO[5].equalsIgnoreCase("n"))
								resizeAndMove.addParameter(Cell.Y_POSITION, argsO[5]);
							if(!argsO[6].equalsIgnoreCase("n"))
								resizeAndMove.addParameter(Cell.Z_INDEX, argsO[6]);	
							logger.info(argsO[0]+"("+"id:"+argsO[1]+",width:"+argsO[2]+",height:"+argsO[3]+",xPosition:"+argsO[4]+",yPosition:"+argsO[5]+",zIndex:"+argsO[6]+")");
						} else if(argsO[0].equalsIgnoreCase("reload") || argsO[0].equalsIgnoreCase("mute") || argsO[0].equalsIgnoreCase("unMute")) {
							Operation reload = event.addOperation(argsO[0]);
							reload.addParameter(Cell.ID, argsO[1]);		
							logger.info(argsO[0]+"("+"id:"+argsO[1]+")");
						} else if(argsO[0].equalsIgnoreCase("run")) {
							moreOperations = false;
						} else if(argsO[0].equalsIgnoreCase("help")) {
							System.out.println("\nSupported operations:");
							System.out.println("  setContent <id> <url>");
							System.out.println("  resizeAndMove <id> <width> <height> <xPosition> <yPosition> <zIndex> (value 'n' means no change)");
							System.out.println("  reload <id>");
							System.out.println("  mute <id>");
							System.out.println("  unMute <id>");
							System.out.println("  run\n");
						}else {
							logger.info("Wrong syntax!");
						}
					}		
					logger.info(client.processEvent(event));
				} else if (args2[0].equalsIgnoreCase(Event.EVENT_CONFIGURE)) { 
					Event event = new Event(args2[0], "", args2[1]);
					logger.info(args2[0]+" for "+args2[1]);
					boolean moreOperations = true;
					while(moreOperations) {
						logger.info("Add operation: ");
						String inputO = in.readLine();
						String argsO[] = inputO.split("\\s");
						if(argsO[0].equalsIgnoreCase(Event.SET_STATEMACHINE)) {
							Operation setContent = event.addOperation(argsO[0]);
							setContent.addParameter(StateMachine.STATEMACHINE_URL, argsO[1]);											
							logger.info(argsO[0]+"("+"url:"+argsO[1]+")");
						} else if(argsO[0].equalsIgnoreCase("run")) {
							moreOperations = false;
						} else if(argsO[0].equalsIgnoreCase("help")) {
							System.out.println("\nSupported operations:");
							System.out.println("  "+Event.SET_STATEMACHINE+" <url>");							
							System.out.println("  run\n");
						}else {
							logger.info("Wrong syntax!");
						}
					}		
					logger.info(client.processEvent(event));
				} else if (args2[0].equalsIgnoreCase(Event.EVENT_GET_STATE)) { 
					Event event = new Event(args2[0], "", args2[1]);
					logger.info(args2[0] +" for "+args2[1]);	
					logger.info(client.processEvent(event));					
				} else if (args2[0].equalsIgnoreCase(Event.EVENT_GET_STATEMACHINE)) { 
					Event event = new Event(args2[0], "", args2[1]);
					logger.info(args2[0]+" for " +args2[1]);	
					logger.info(client.processEvent(event));					
				} else if (args2[0].equalsIgnoreCase("exit")) {
					exit = true;
				} else if (args2[0].equalsIgnoreCase("help")) {
					System.out.println("\nSupported commands:");
					System.out.println("  "+Event.EVENT_CHANGE_STATE+" <transition_name>");
					System.out.println("  "+Event.EVENT_MODIFY_STATE);
					System.out.println("  "+Event.EVENT_GET_STATE);
					System.out.println("  "+Event.EVENT_GET_STATEMACHINE);
					System.out.println("  "+Event.EVENT_CONFIGURE);
					System.out.println("  exit\n");
				} else {
					logger.info("Wrong syntax!");
				}
			}
					
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String notifyScreenEvent(String eventType) {
		logger.info("Got " +eventType);
		return "Ok";
	}
}