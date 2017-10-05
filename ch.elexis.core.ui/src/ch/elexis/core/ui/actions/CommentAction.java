package ch.elexis.core.ui.actions;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.ui.dialogs.base.InputDialog;
import ch.elexis.core.ui.icons.Images;

public class CommentAction extends Action {
	private String comment;
	private final Shell shell;
	
	public CommentAction(Shell shell, String comment){
		super("", Action.AS_PUSH_BUTTON);
		Assert.isNotNull(shell);
		this.shell = shell;
		this.comment = comment;
		init();
		
	}
	
	public String getComment(){
		return comment;
	}
	
	public void init(){
		if (comment != null && !comment.isEmpty()) {
			setToolTipText(comment);
			setImageDescriptor(Images.IMG_VIEW_CONSULTATION_DETAIL.getImageDescriptor());
		} else {
			setToolTipText("Kommentar erfassen");
			setImageDescriptor(Images.IMG_COMMENT_ADD.getImageDescriptor());
			
		}
	}
	
	@Override
	public void run(){
		InputDialog inputDialog =
			new InputDialog(shell, "Kommentar Erfassen", "Bitte geben Sie einen Kommentar ein",
				comment, null, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		if (inputDialog.open() == MessageDialog.OK) {
			comment = inputDialog.getValue();
			init();
		}
		super.run();
	}
}