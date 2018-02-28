package ch.elexis.core.findings.util.commands;

import ch.elexis.core.exceptions.ElexisException;

public interface IFindingCommand {
	public void execute() throws ElexisException;
}
