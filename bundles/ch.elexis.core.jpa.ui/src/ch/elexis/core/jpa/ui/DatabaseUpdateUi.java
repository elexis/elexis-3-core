package ch.elexis.core.jpa.ui;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

import ch.elexis.core.jpa.entitymanager.ui.IDatabaseUpdateUi;

@Component
public class DatabaseUpdateUi implements IDatabaseUpdateUi {
	
	private Shell shell;
	private Label messageLabel;
	private ProgressIndicator progressIndicator;
	private boolean isExecuting;
	
	private void createAndOpenShell(Display display){
		if (display.getActiveShell() != null) {
			shell = new Shell(display.getActiveShell(), SWT.TOOL | SWT.APPLICATION_MODAL);
		} else {
			shell = new Shell(display, SWT.TOOL | SWT.APPLICATION_MODAL);
		}
		shell.setText("Database Update");
		shell.setLayout(new FillLayout());
		
		Composite contentComposite = new Composite(shell, SWT.NONE);
		contentComposite.setLayout(new GridLayout());
		
		messageLabel = new Label(contentComposite, SWT.CENTER | SWT.WRAP);
		messageLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		// progress indicator
		progressIndicator = new ProgressIndicator(contentComposite);
		progressIndicator.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
		
		shell.pack();
		shell.open();
		Rectangle displayBounds = display.getBounds();
		shell.setSize(500, 50);
		shell.setLocation(((displayBounds.width / 2) - (shell.getBounds().width / 2)),
			(displayBounds.height / 2) + (shell.getBounds().height / 2) + 150);
		runEventLoop();
	}
	
	public void closeProgress(){
		Display display = Display.getDefault();
		display.syncExec(() -> {
			if (shell != null) {
				shell.dispose();
				shell = null;
			}
		});
	}
	
	public void openProgress(String message){
		Display display = Display.getDefault();
		display.syncExec(() -> {
			if (shell == null) {
				createAndOpenShell(display);
			}
			progressIndicator.beginAnimatedTask();
			messageLabel.setText(message);
			shell.pack();
		});
	}
	
	public void execute(Runnable updateRunnable){
		try {
			isExecuting = true;
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.execute(updateRunnable);
			executor.execute(() -> {
				isExecuting = false;
			});
			executor.shutdown();
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass());
		}
		
	}
	
	@Override
	public void executeWithProgress(String message, Runnable updateRunnable){
		openProgress(message);
		execute(updateRunnable);
		while (isExecuting()) {
			runEventLoop();
		}
		closeProgress();
	}
	
	public boolean isExecuting(){
		return isExecuting;
	}
	
	public void runEventLoop(){
		Display display = Display.getDefault();
		while (display.readAndDispatch()) {}
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// ignore
		}
	}
	
	@Override
	public void setMessage(String message){
		Display.getDefault().syncExec(() -> {
			if (shell != null && !shell.isDisposed()) {
				shell.setSize(500, 100);
				Rectangle displayBounds = shell.getDisplay().getBounds();
				shell.setLocation(((displayBounds.width / 2) - (shell.getBounds().width / 2)),
					(displayBounds.height / 2) + (shell.getBounds().height / 2) + 150);
				this.messageLabel.setText(message);
				this.messageLabel.getParent().layout();
			}
		});
	}
}