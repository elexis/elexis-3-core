package ch.elexis.core.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.views.rechnung.KonsZumVerrechnenView;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.rgw.tools.Tree;

public class KonsZumVerrechnenLinkCommand extends AbstractHandler {
	public static final String CMD_ID = "ch.elexis.core.command.linkViews";
	private TreeSelectionChangedListener leftSideSelChangeListener, rightSideSelChangeListener;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		boolean alreadyToggled = HandlerUtil.toggleCommandState(event.getCommand());
		
		IWorkbenchPage activePage =
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		KonsZumVerrechnenView kzvView =
			(KonsZumVerrechnenView) activePage.findView(KonsZumVerrechnenView.ID);
		
		final CommonViewer cv = kzvView.getLeftSide();
		final TreeViewer tvSel = kzvView.getRightSide();
		
		// goes from toggled to not toggled
		if (alreadyToggled) {
			cv.getViewerWidget().removeSelectionChangedListener(leftSideSelChangeListener);
			tvSel.removeSelectionChangedListener(rightSideSelChangeListener);
		} else {
			// toggled
			leftSideSelChangeListener = new TreeSelectionChangedListener(tvSel);
			rightSideSelChangeListener =
				new TreeSelectionChangedListener((TreeViewer) cv.getViewerWidget());
			
			cv.getViewerWidget().addSelectionChangedListener(leftSideSelChangeListener);
			tvSel.addSelectionChangedListener(rightSideSelChangeListener);
		}
		
		return null;
	}
	
	class TreeSelectionChangedListener implements ISelectionChangedListener {
		private TreeViewer treeViewer;
		
		public TreeSelectionChangedListener(TreeViewer treeViewer){
			this.treeViewer = treeViewer;
		}
		
		@Override
		public void selectionChanged(SelectionChangedEvent event){
			IStructuredSelection selection = (IStructuredSelection) event.getSelection();
			Tree treeElement = (Tree) selection.getFirstElement();
			
			if (treeElement != null) {
				Object selObj = treeElement.contents;
				Patient selPatient = null;
				
				// get belonging patient
				if (selObj instanceof Patient) {
					selPatient = (Patient) selObj;
				} else if (selObj instanceof Fall) {
					Fall fall = (Fall) selObj;
					selPatient = fall.getPatient();
				} else if (selObj instanceof Konsultation) {
					Konsultation kons = (Konsultation) selObj;
					Fall fall = kons.getFall();
					if (fall != null && fall.exists()) {
						selPatient = fall.getPatient();
					}
				}
				
				if (selPatient != null) {
					for (TreeItem i : treeViewer.getTree().getItems()) {
						Patient p = (Patient) ((Tree) i.getData()).contents;
						if (p.getId().equals(selPatient.getId())) {
							treeViewer.getTree().setSelection(i);
						}
					}
					treeViewer.refresh();
				}
			}
		}
	}
	
}
