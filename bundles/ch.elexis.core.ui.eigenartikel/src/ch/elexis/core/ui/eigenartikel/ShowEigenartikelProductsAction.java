package ch.elexis.core.ui.eigenartikel;

import org.eclipse.jface.action.Action;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.icons.Images;

public class ShowEigenartikelProductsAction extends Action {
	
	public static final String FILTER_CFG = "ShowEigenartikelProductsAction.showProducts";
	
	private EigenartikelTreeContentProvider eal;
	private EigenartikelSelector eigenartikelSelector;
	
	public ShowEigenartikelProductsAction(EigenartikelTreeContentProvider eal,
		EigenartikelSelector eigenartikelSelector){
		super("Produkte anzeigen", Action.AS_CHECK_BOX);
		this.eal = eal;
		this.eigenartikelSelector = eigenartikelSelector;
		setImageDescriptor(Images.IMG_CARDS.getImageDescriptor());
		setToolTipText("");		
		setChecked(CoreHub.userCfg.get(ShowEigenartikelProductsAction.FILTER_CFG, false));
		execute();
	}
	
	@Override
	public void run(){
		CoreHub.userCfg.set(FILTER_CFG, isChecked());
		execute();
	}
	
	private void execute() {
		eal.setShowProducts(isChecked());
		eigenartikelSelector.allowArticleRearrangement(isChecked());
	}
	
}
