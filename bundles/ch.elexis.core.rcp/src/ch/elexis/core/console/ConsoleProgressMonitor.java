package ch.elexis.core.console;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.framework.console.CommandInterpreter;

public class ConsoleProgressMonitor implements IProgressMonitor {

	private CommandInterpreter ci;
	private String name;
	private int totalWork;
	private int worked;
	private boolean cancelled;

	public ConsoleProgressMonitor() {
	}

	public ConsoleProgressMonitor(CommandInterpreter ci) {
		this.ci = ci;
	}

	@Override
	public void beginTask(String name, int totalWork) {
		this.name = name;
		this.totalWork = (totalWork == UNKNOWN) ? 9999 : totalWork;
		this.worked = 0;
		this.cancelled = false;
		printStatus();
	}

	private void printStatus() {
		if (name == null && worked == 0 && totalWork == 0) {
			return;
		}

		String msg = name + " [" + worked + "/" + totalWork + "]";
		if (cancelled) {
			msg = "-CNCLD- " + msg;
		}
		if (ci != null) {
			ci.println(msg);
		} else {
			System.out.println(msg);
		}
	}

	@Override
	public void done() {
		this.worked = this.totalWork;
		printStatus();
	}

	@Override
	public void internalWorked(double work) {
		// nothing to do
	}

	@Override
	public boolean isCanceled() {
		return cancelled;
	}

	@Override
	public void setCanceled(boolean value) {
		this.cancelled = value;
		if (value) {
			printStatus();
		}
	}

	@Override
	public void setTaskName(String name) {
		this.name = name;
	}

	@Override
	public void subTask(String name) {
		this.name = name;
	}

	@Override
	public void worked(int work) {
		worked += work;
		printStatus();
	}

}
