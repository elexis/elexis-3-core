package ch.elexis.core.ui.importer.div.rs232;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.serial.Connection;
import ch.elexis.core.ui.UiDesk;

public class SerialConnectionUi {

	public static Thread awaitFrame(Connection connection, final Shell shell, final String text, final int timeout,
			final boolean background, final boolean adjustEndTime) {
		long endTime = System.currentTimeMillis() + (timeout * 1000);
		Thread watchdogThread = null;
		if (background) {
			watchdogThread = new Thread(new BackgroundWatchdog(connection, endTime));
		} else {
			watchdogThread = new Thread(new MonitoredWatchdog(connection, shell, text, endTime));
		}
		watchdogThread.start();
		return watchdogThread;
	}

	static class BackgroundWatchdog implements Runnable {

		private Connection connection;

		private long endTime;

		public BackgroundWatchdog(Connection connection, long endTime) {
			this.connection = connection;
			this.endTime = endTime;
		}

		public void run() {
			while (System.currentTimeMillis() < endTime && connection.isOpen()) {
				try {
					Thread.sleep(1000); // 1s.
				} catch (InterruptedException ex) {
					return;
				}
			}
			connection.close();
		}
	}

	static class MonitoredWatchdog implements Runnable {

		private Connection connection;

		private long endTime;

		final Shell shell;
		final String text;

		public MonitoredWatchdog(Connection connection, Shell shell, String text, long endTime) {
			super();
			this.connection = connection;
			this.endTime = endTime;
			this.shell = shell;
			this.text = text;
		}

		public void run() {
			final IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
				private int count = 0;

				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.setTaskName(Messages.AbstractConnection_PleaseWait);
					while (!monitor.isCanceled() && System.currentTimeMillis() < endTime && connection.isOpen()) {
						if (count == 160) {
							monitor.beginTask(text, 100);
							count = 0;
						}

						if (monitor.isCanceled()) {
							monitor.done();
							return;
						}

						monitor.worked(1);
						count++;

						Thread.sleep(10); // 0.001s.
					}
					connection.close();
					monitor.done();
				}
			};

			Thread monitorDialogThread = new Thread() {
				public void run() {
					ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
					try {
						dialog.run(true, true, runnableWithProgress);
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};

			UiDesk.getDisplay().asyncExec(monitorDialogThread);
		}
	}
}
