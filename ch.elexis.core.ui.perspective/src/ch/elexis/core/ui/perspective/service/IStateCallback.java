package ch.elexis.core.ui.perspective.service;

public interface IStateCallback {
	
	public enum State {
			OVERRIDE;
	}
	
	/**
	 * Defines the state handling
	 * 
	 * @param state
	 * @return
	 */
	public boolean state(State state);
}
