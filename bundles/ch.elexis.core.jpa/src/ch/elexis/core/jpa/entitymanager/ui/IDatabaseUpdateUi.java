package ch.elexis.core.jpa.entitymanager.ui;

public interface IDatabaseUpdateUi {

	void executeWithProgress(String message, Runnable runnable);

	void setMessage(String message);

}
