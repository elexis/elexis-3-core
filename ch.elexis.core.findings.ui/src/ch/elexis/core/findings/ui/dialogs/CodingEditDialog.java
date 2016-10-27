package ch.elexis.core.findings.ui.dialogs;

import java.util.Optional;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ui.composites.CodingComposite;

public class CodingEditDialog extends TitleAreaDialog {
	private CodingComposite codingComposite;
	
	private Optional<ICoding> coding = Optional.empty();
	
	public CodingEditDialog(Shell parentShell){
		super(parentShell);
	}
	
	public CodingEditDialog(ICoding coding, Shell parentShell){
		super(parentShell);
		this.coding = Optional.of(coding);
	}
	
	@Override
	public void create(){
		super.create();
		setTitle("Kodierung " + (coding.isPresent() ? "editieren" : "anlegen") + ".");
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(new GridData(GridData.FILL_BOTH));
		ret.setLayout(new FillLayout());
		codingComposite = new CodingComposite(ret, SWT.NONE);
		coding.ifPresent(c -> codingComposite.setCoding(c));
		return ret;
	}
	
	@Override
	protected void okPressed(){
		coding = codingComposite.getCoding();
		super.okPressed();
	}
	
	public Optional<ICoding> getCoding(){
		return coding;
	}
}
