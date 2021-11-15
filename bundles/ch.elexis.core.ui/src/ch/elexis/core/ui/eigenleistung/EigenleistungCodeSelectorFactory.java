/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.eigenleistung;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.model.ICustomService;
import ch.elexis.core.model.localservice.Constants;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.actions.ToggleVerrechenbarFavoriteAction;
import ch.elexis.core.ui.commands.EditEigenleistungUi;
import ch.elexis.core.ui.dialogs.EigenLeistungDialog;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.selectors.FieldDescriptor;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.CommonViewerContentProvider;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.core.ui.util.viewers.SelectorPanelProvider;
import ch.elexis.core.ui.util.viewers.SimpleWidgetProvider;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ContentType;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ControlFieldProvider;
import ch.elexis.core.ui.views.codesystems.CodeSelectorFactory;
import ch.elexis.core.ui.views.codesystems.Messages;

public class EigenleistungCodeSelectorFactory extends CodeSelectorFactory {
	
	private ToggleVerrechenbarFavoriteAction tvfa = new ToggleVerrechenbarFavoriteAction();
	private ISelectionChangedListener selChangeListener = new ISelectionChangedListener() {
		@Override
		public void selectionChanged(SelectionChangedEvent event){
			TableViewer tv = (TableViewer) event.getSource();
			StructuredSelection ss = (StructuredSelection) tv.getSelection();
			tvfa.updateSelection(ss.isEmpty() ? null : ss.getFirstElement());
			
			if (!ss.isEmpty()) {
				ICustomService ea = (ICustomService) ss.getFirstElement();
				ContextServiceHolder.get().getRootContext()
					.setNamed("ch.elexis.core.ui.eigenleistung.selection", ea);
			} else {
				ContextServiceHolder.get().getRootContext()
					.setNamed("ch.elexis.core.ui.eigenleistung.selection", null);
			}
		}
	};
	
	@Override
	public ViewerConfigurer createViewerConfigurer(CommonViewer commonViewer){
		MenuManager menu = new MenuManager();
		menu.add(tvfa);
		menu.add(new Action(
			ch.elexis.core.ui.eigenleistung.Messages.EigenleistungContextMenu_deleteAction) {
			{
				setImageDescriptor(Images.IMG_DELETE.getImageDescriptor());
				setToolTipText(
					ch.elexis.core.ui.eigenleistung.Messages.EigenleistungContextMenu_deleteActionToolTip);
			}
			
			@Override
			public void run(){
				ICustomService act = (ICustomService) ContextServiceHolder.get()
					.getNamed("ch.elexis.core.ui.eigenleistung.selection").orElse(null);
				if (act != null && MessageDialog.openConfirm(
					commonViewer.getViewerWidget().getControl().getShell(),
					ch.elexis.core.ui.eigenleistung.Messages.EigenleistungContextMenu_deleteActionConfirmCaption,
					MessageFormat.format(
						ch.elexis.core.ui.eigenleistung.Messages.EigenleistungContextMenu_deleteConfirmBody,
						act.getText()))) {
					CoreModelServiceHolder.get().delete(act);
					commonViewer.getConfigurer().getControlFieldProvider().fireChangedEvent();
					commonViewer.notify(CommonViewer.Message.update);
				}
			}
			
			@Override
			public boolean isEnabled(){
				return ContextServiceHolder.get()
					.getNamed("ch.elexis.core.ui.eigenleistung.selection").isPresent();
			}
			
		});
		menu.add(new Action(
			ch.elexis.core.ui.eigenleistung.Messages.EigenleistungContextMenu_propertiesAction) {
			{
				setImageDescriptor(Images.IMG_EDIT.getImageDescriptor());
				setToolTipText(
					ch.elexis.core.ui.eigenleistung.Messages.EigenleistungContextMenu_propertiesTooltip);
			}
			
			@Override
			public void run(){
				ICustomService act = (ICustomService) ContextServiceHolder.get()
					.getNamed("ch.elexis.core.ui.eigenleistung.selection").orElse(null);
				if (act != null) {
					EditEigenleistungUi.executeWithParams(NoPoUtil.loadAsPersistentObject(act));
				}
			}
			
			@Override
			public boolean isEnabled(){
				return ContextServiceHolder.get()
					.getNamed("ch.elexis.core.ui.eigenleistung.selection").isPresent();
			}
		});
		menu.addMenuListener(new IMenuListener() {
			
			@Override
			public void menuAboutToShow(IMenuManager manager){
				for (IContributionItem item : manager.getItems()) {
					item.update();
				}
			}
		});
		
		commonViewer.setNamedSelection("ch.elexis.core.ui.eigenleistung.selection");
		commonViewer.setContextMenu(menu);
		commonViewer.setSelectionChangedListener(selChangeListener);
		
		FieldDescriptor<?>[] fieldDescriptors = new FieldDescriptor<?>[] {
			new FieldDescriptor<ICustomService>("Code", "code", null),
			new FieldDescriptor<ICustomService>("Bezeichnung", "description", null)
		};
		
		SelectorPanelProvider slp = new SelectorPanelProvider(fieldDescriptors, true);
		slp.addActions(new Action("neu erstellen") {
			{
				setImageDescriptor(Images.IMG_NEW.getImageDescriptor());
				setToolTipText(Messages.BlockDetailDisplay_addSelfDefinedServices);
			}
			
			@Override
			public void run(){
				Shell parent = PlatformUI.getWorkbench().getDisplay().getActiveShell();
				EigenLeistungDialog dialog = new EigenLeistungDialog(parent, null);
				dialog.open();
				commonViewer.notify(CommonViewer.Message.update);
			}
		});
		ViewerConfigurer vc =
			new ViewerConfigurer(new EigenleistungContentProvider(commonViewer, slp),
			new DefaultLabelProvider(), slp, new ViewerConfigurer.DefaultButtonProvider(),
				new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_LAZYLIST, SWT.NONE,
					commonViewer));
		return vc.setContentType(ContentType.GENERICOBJECT);
	}
	
	private class EigenleistungContentProvider extends CommonViewerContentProvider {
		
		private ControlFieldProvider controlFieldProvider;
		
		public EigenleistungContentProvider(CommonViewer commonViewer,
			ControlFieldProvider controlFieldProvider){
			super(commonViewer);
			this.controlFieldProvider = controlFieldProvider;
		}
		
		@Override
		public Object[] getElements(Object inputElement){
			IQuery<?> query = getBaseQuery();
			
			// apply filters from control field provider
			controlFieldProvider.setQuery(query);
			applyQueryFilters(query);
			query.orderBy("description", ORDER.ASC);
			query.orderBy("code", ORDER.ASC);
			List<?> elements = query.execute();
			
			return elements.toArray(new Object[elements.size()]);
		}
		
		@Override
		protected IQuery<?> getBaseQuery(){
			IQuery<ICustomService> query =
				CoreModelServiceHolder.get().getQuery(ICustomService.class);
			return query;
		}
		
	}
	
	@Override
	public Class<?> getElementClass(){
		return ICustomService.class;
	}
	
	@Override
	public void dispose(){}
	
	@Override
	public String getCodeSystemName(){
		return Constants.TYPE_NAME;
	}
}
