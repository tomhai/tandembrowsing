package org.tandembrowsing.state;

import java.util.Map;

import org.apache.commons.scxml.model.SCXML;
import org.apache.commons.scxml.model.State;
import org.apache.commons.scxml.model.TransitionTarget;

public class SCXMLUtil {
	
	/**
	 * This checks if the state is found in the given SCXML. There is a limitation though
	 * that only two levels is checked. TODO: Support for infinitive hierarchy
	 * 
	 * @param scxml
	 * @param state
	 * @return
	 */
	public static TransitionTarget hasState(SCXML scxml, String state) {
		if(scxml.getChildren().containsKey(state))
			return (TransitionTarget)scxml.getChildren().get(state);
		else {
			return hasState(scxml.getChildren(), state);
		}
	}
	
	private static TransitionTarget hasState(Map <String, State> states, String state) {
		for(Map.Entry<String, State> entry : states.entrySet()) {
			if(entry.getValue().getChildren().containsKey(state)) {
				return (TransitionTarget)entry.getValue().getChildren().get(state);
			} else {
				TransitionTarget stateCheck = hasState(entry.getValue().getChildren(), state);
				if(stateCheck != null)
					return stateCheck;
			}
		}
		return null;
	}

}
