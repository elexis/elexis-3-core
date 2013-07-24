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
package ch.elexis.core.ui.dialogs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.eclipse.ui.internal.WorkbenchMessages;

import ch.elexis.core.data.PersistentObject;
import ch.elexis.core.data.PersistentObjectFactory;
import ch.elexis.core.data.Query;
import ch.elexis.core.data.interfaces.IDiagnose;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.ui.constants.ExtensionPointConstants;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.views.codesystems.CodeSelectorFactory;
import ch.rgw.tools.ExHandler;

public class DiagnoseSelektor extends FilteredItemsSelectionDialog {
	
	private ArrayList<IDiagnose> diagnoses = new ArrayList<IDiagnose>();
	
	public DiagnoseSelektor(Shell shell){
		super(shell);
		setTitle(Messages.DiagnoseSelektorDialog_Title);
		
		// create a list of all diagnoses
		java.util.List<IConfigurationElement> list =
			Extensions.getExtensions(ExtensionPointConstants.DIAGNOSECODE);
		
		diagnoses.add(new NoDiagnose());
		
		if (list != null) {
			for (IConfigurationElement ic : list) {
				try {
					PersistentObjectFactory po =
						(PersistentObjectFactory) ic.createExecutableExtension("ElementFactory"); //$NON-NLS-1$
					CodeSelectorFactory codeSelectorFactory =
						(CodeSelectorFactory) ic.createExecutableExtension("CodeSelectorFactory"); //$NON-NLS-1$
					// get all available diagnoses available (TI can not be Queried as it is
					// not in the database)
					if (!(codeSelectorFactory.getCodeSystemName().equalsIgnoreCase("TI-Code"))) { //$NON-NLS-1$
						Query<IDiagnose> qd =
							new Query<IDiagnose>(codeSelectorFactory.getElementClass());
						diagnoses.addAll(qd.execute());
					} else {
						// get TI Code via content provider
						CommonViewer cv = new CommonViewer();
						ViewerConfigurer vc =
							codeSelectorFactory.createViewerConfigurer(new CommonViewer());
						cv.create(vc, shell, SWT.NONE, this);
						
						ITreeContentProvider tcp = (ITreeContentProvider) vc.getContentProvider();
						Object[] roots = tcp.getElements(null);
						addDiagnoses(tcp, roots);
						
						cv.dispose();
					}
				} catch (CoreException ex) {
					ExHandler.handle(ex);
				}
			}
		}
		
		setListLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				if (element == null) {
					return "";
				}
				return ((IDiagnose) element).getLabel();
			}
		});
		
		setDetailsLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				if (element == null) {
					return "";
				}
				return ((IDiagnose) element).getCodeSystemName() + " "
					+ ((IDiagnose) element).getLabel();
			}
		});
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		String oldListLabel = WorkbenchMessages.FilteredItemsSelectionDialog_listLabel;
		
		setMessage(Messages.DiagnoseSelektorDialog_Message);
		WorkbenchMessages.FilteredItemsSelectionDialog_listLabel = ""; //$NON-NLS-1$
		Control ret = super.createDialogArea(parent);
		
		WorkbenchMessages.FilteredItemsSelectionDialog_listLabel = oldListLabel;
		return ret;
	}
	
	private void addDiagnoses(ITreeContentProvider tcp, Object[] roots){
		for (Object object : roots) {
			if (tcp.hasChildren(object)) {
				addDiagnoses(tcp, tcp.getChildren(object));
			} else {
				diagnoses.add((IDiagnose) object);
			}
		}
	}
	
	@Override
	protected Control createExtendedContentArea(Composite parent){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected IDialogSettings getDialogSettings(){
		return new DialogSettings("diagnoseselektor"); //$NON-NLS-1$
	}
	
	@Override
	protected IStatus validateItem(Object item){
		return Status.OK_STATUS;
	}
	
	@Override
	protected ItemsFilter createFilter(){
		return new ItemsFilter() {
			@Override
			public boolean isConsistentItem(Object item){
				return true;
			}
			
			@Override
			public boolean matchItem(Object item){
				IDiagnose diag = (IDiagnose) item;
				
				return matches(diag.getLabel());
			}
		};
	}
	
	@Override
	protected Comparator getItemsComparator(){
		return new Comparator<IDiagnose>() {
			
			public int compare(IDiagnose o1, IDiagnose o2){
				return o1.getLabel().compareTo(o2.getLabel());
			}
		};
	}
	
	@Override
	protected void fillContentProvider(AbstractContentProvider contentProvider,
		ItemsFilter itemsFilter, IProgressMonitor progressMonitor) throws CoreException{
		
		for (IDiagnose diagnose : diagnoses) {
			if (progressMonitor.isCanceled()) {
				return;
			}
			contentProvider.add(diagnose, itemsFilter);
		}
	}
	
	@Override
	public String getElementName(Object item){
		IDiagnose diag = (IDiagnose) item;
		return diag.getLabel();
	}
	
	private class NoDiagnose extends PersistentObject implements IDiagnose {
		
		@Override
		public String getCodeSystemName(){
			return "";
		}
		
		@Override
		public String getCodeSystemCode(){
			return "";
		}
		
		@Override
		public String getId(){
			return "";
		}
		
		@Override
		public String getCode(){
			return "";
		}
		
		@Override
		public String getText(){
			return " keine ";
		}

		
		@Override
		public String getLabel(){
			return getText();
		}
		
		@Override
		protected String getTableName(){
			return "";
		}

		@Override
		public List<Object> getActions(Object kontext) {
			// TODO Auto-generated method stub
			return null;
		}

	}
}
