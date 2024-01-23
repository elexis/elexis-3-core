package ch.elexis.core.jpa.entitymanager.ui;

public interface IDatabaseUpdateUi {

	/**
	 * Show the message to the user and execute the {@link Runnable} while showing
	 * undefined progress.
	 * 
	 * @param message
	 * @param runnable
	 */
	void executeWithProgress(String message, Runnable runnable);

	/**
	 * Show the message to the user.
	 * 
	 * @param message
	 */
	void setMessage(String message);

	/**
	 * Show message to the user and open database connection config afterwards.
	 * 
	 * @param message
	 */
	void requestDatabaseConnectionConfiguration(String message);
}
