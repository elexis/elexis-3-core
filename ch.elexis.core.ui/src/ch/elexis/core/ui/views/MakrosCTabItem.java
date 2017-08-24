package ch.elexis.core.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;

public class MakrosCTabItem extends CTabItem {
	
	private MakrosComposite composite;
	
	public MakrosCTabItem(CTabFolder parent, int style){
		super(parent, style);
		init();
	}
	
	public MakrosCTabItem(CTabFolder parent, int style, int idx){
		super(parent, style, idx);
		init();
	}
	
	private void init(){
		setText("Makros");
		
		composite = new MakrosComposite(this.getParent(), SWT.NONE);
		setControl(composite);
	}

	public void update(){
		if (composite != null)
			composite.update();
	}
	
	@Override
	public boolean isShowing(){
		update();
		return super.isShowing();
	}
}
