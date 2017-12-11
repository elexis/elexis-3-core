package ch.elexis.core.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;

public class FavoritenCTabItem extends CTabItem {
	
	private FavoritenComposite fc;
	
	public FavoritenCTabItem(CTabFolder parent, int style){
		super(parent, style);
		init();
	}
	
	public FavoritenCTabItem(CTabFolder parent, int style, int idx){
		super(parent, style, idx);
		init();
	}
	
	private void init(){
		setText("Favoriten");
		
		fc = new FavoritenComposite(this.getParent(), SWT.None);
		setControl(fc);
	}

	public void update(){
		if (fc != null)
			fc.update();
	}
	
	@Override
	public boolean isShowing(){
		update();
		return super.isShowing();
	}
}
