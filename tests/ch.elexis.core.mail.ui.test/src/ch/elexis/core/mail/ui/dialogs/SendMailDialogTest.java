package ch.elexis.core.mail.ui.dialogs;

import static org.junit.Assert.assertEquals;

import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

public class SendMailDialogTest {

	@Test
	public void testConfidentialSend() {
		// Erstellen eines neuen Shell-Objekts als Parent für den Dialog.
		Shell parentShell = new Shell();

		// Initialisieren des SendMailDialogs.
		SendMailDialog sendMailDialog = new SendMailDialog(parentShell);

		// Simulieren der Eingabe des Betreffs.
		sendMailDialog.setSubject("Testbetreff");
		sendMailDialog.create();

		// Fall 1: Prüfen, ob der Betreff korrekt gesetzt wird, ohne das vertrauliche
		// Kontrollkästchen zu markieren.
		sendMailDialog.getConfidentialCheckbox().setSelection(false);
		sendMailDialog.okPressed();
		assertEquals("Testbetreff", sendMailDialog.getSubject());


		// Fall 2: Prüfen, ob der Betreff korrekt gesetzt wird, nachdem das vertrauliche
		// Kontrollkästchen markiert wurde.
		sendMailDialog.getConfidentialCheckbox().setSelection(true);
		sendMailDialog.okPressed();
		assertEquals("Testbetreff (Vertraulich)", sendMailDialog.getSubject());
	}
}
