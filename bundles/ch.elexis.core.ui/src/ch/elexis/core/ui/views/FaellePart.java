
package ch.elexis.core.ui.views;

import static ch.elexis.core.ui.actions.GlobalActions.delFallAction;
import static ch.elexis.core.ui.actions.GlobalActions.makeBillAction;
import static ch.elexis.core.ui.actions.GlobalActions.neuerFallAction;
import static ch.elexis.core.ui.actions.GlobalActions.openFallaction;
import static ch.elexis.core.ui.actions.GlobalActions.reopenFallAction;

import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.osgi.service.event.Event;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.ObjectFilterRegistry;
import ch.elexis.core.ui.actions.ObjectFilterRegistry.IObjectFilterProvider;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.FallComparator;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.views.provider.FaelleLabelProvider;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;

public class FaellePart {
	
	@Inject
	EPartService partService;
	
	private TableViewer tv;
	private IAction konsFilterAction;
	private IAction filterClosedAction;
	private final FallKonsFilter filter = new FallKonsFilter();
	
	public FaellePart(){
		makeActions();
	}
	
	@PostConstruct
	public void postConstruct(Composite parent, MPart mPart, EModelService modelService){
		parent.setLayout(new GridLayout());
		
		tv = new TableViewer(parent);
		Table table = tv.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tv.getControl().setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		tv.setContentProvider(ArrayContentProvider.getInstance());
		tv.setLabelProvider(new FaelleLabelProvider());
		tv.addSelectionChangedListener(GlobalEventDispatcher.getInstance().getDefaultListener());
		tv.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event){
				MPart findPart = partService.findPart(FallDetailView.ID);
				partService.activate(findPart);
			}
		});
		
//		final MToolBar mBar = modelService.createModelElement(MToolBar.class);
//		mPart.setToolbar(mBar);
//		mBar.getChildren().add(modelService.createM)
		
//		menus = new ViewMenus(getViewSite());
//		menus.createToolbar(neuerFallAction, konsFilterAction, filterClosedAction);
//		menus.createViewerContextMenu(tv, delFallAction, openFallaction, reopenFallAction,
//			makeBillAction);
	}
	
	@Inject
	@Optional
	private void patientChangeListener(
		@UIEventTopic(ElexisEventTopics.CONTEXT_EVENT_SELECTION + "/patient") Event object,
		MPart thisPart, EPartService partService){
		
		if (partService.isPartVisible(thisPart)) {
			Patient patient = (Patient) object.getProperty(ElexisEventTopics.PROPKEY_OBJECT);
			if (patient != null) {
				Fall[] cases = patient.getFaelle();
				Arrays.sort(cases, new FallComparator());
				tv.setInput(cases);
				
				Fall currentFall = (Fall) ElexisEventDispatcher.getSelected(Fall.class);
				if (currentFall != null) {
					tv.setSelection(new StructuredSelection(currentFall));
				}
			} else {
				tv.setInput(new Object[0]);
			}
		}
	}
	
	@Inject
	@Optional
	private void fallChangeListener(
		@UIEventTopic(ElexisEventTopics.CONTEXT_EVENT_SELECTION + "/fall") Event object,
		MPart thisPart, EPartService partService){
		
	}
	
	private void makeActions(){
		konsFilterAction = new Action(Messages.FaelleView_FilterConsultations, //$NON-NLS-1$
			Action.AS_CHECK_BOX) {
			{
				setToolTipText(Messages.FaelleView_ShowOnlyConsOfThisCase); //$NON-NLS-1$
				setImageDescriptor(Images.IMG_FILTER.getImageDescriptor());
			}
			
			@Override
			public void run(){
				if (!isChecked()) {
					ObjectFilterRegistry.getInstance().unregisterObjectFilter(Konsultation.class,
						filter);
				} else {
					ObjectFilterRegistry.getInstance().registerObjectFilter(Konsultation.class,
						filter);
					filter.setFall((Fall) ElexisEventDispatcher.getSelected(Fall.class));
				}
			}
			
		};
		filterClosedAction = new Action("", Action.AS_CHECK_BOX) {
			private ViewerFilter closedFilter;
			{
				setToolTipText(Messages.FaelleView_ShowOnlyOpenCase); //$NON-NLS-1$
				setImageDescriptor(Images.IMG_DOCUMENT_WRITE.getImageDescriptor());
				closedFilter = new ViewerFilter() {
					@Override
					public boolean select(Viewer viewer, Object parentElement, Object element){
						if (element instanceof Fall) {
							Fall fall = (Fall) element;
							return fall.isOpen();
						}
						return false;
					}
				};
			}
			
			@Override
			public void run(){
				if (!isChecked()) {
					tv.removeFilter(closedFilter);
				} else {
					tv.addFilter(closedFilter);
				}
			}
		};
	}
	
	class FallKonsFilter implements IObjectFilterProvider, IFilter {
		
		Fall mine;
		boolean bDaempfung;
		
		void setFall(final Fall fall){
			mine = fall;
			ElexisEventDispatcher.reload(Konsultation.class);
		}
		
		public void activate(){
			bDaempfung = true;
			konsFilterAction.setChecked(true);
			bDaempfung = false;
		}
		
		public void changed(){
			// don't mind
		}
		
		public void deactivate(){
			bDaempfung = true;
			konsFilterAction.setChecked(false);
			bDaempfung = false;
		}
		
		public IFilter getFilter(){
			return this;
		}
		
		public String getId(){
			return "ch.elexis.FallFilter"; //$NON-NLS-1$
		}
		
		public boolean select(final Object toTest){
			if (mine == null) {
				return true;
			}
			if (toTest instanceof Konsultation) {
				Konsultation k = (Konsultation) toTest;
				if (k.getFall().equals(mine)) {
					return true;
				}
			}
			return false;
		}
		
	}
	
}