package ch.elexis.core.ui.medication.views;

import ch.elexis.core.ui.UiDesk;

public abstract class UIFinishingThread {
	
	public abstract Object preparation();
	
	/**
	 * will be exececuted in an asyncExec UI
	 * @param input
	 */
	public abstract void finalization(Object input);
	
	public synchronized void start(){
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run(){
				final Object output = preparation();
				
				UiDesk.asyncExec(new Runnable() {
					
					@Override
					public void run(){
						finalization(output);
					}
				});
			}
		});
		t.start();
	}
}
