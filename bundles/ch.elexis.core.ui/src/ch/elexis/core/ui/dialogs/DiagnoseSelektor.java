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
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.eclipse.ui.internal.WorkbenchMessages;

import ch.elexis.core.data.interfaces.IDiagnose;
import ch.elexis.core.data.service.CodeElementServiceHolder;
import ch.elexis.core.model.IDiagnosis;
import ch.elexis.core.model.IXid;
import ch.elexis.core.services.ICodeElementService.CodeElementTyp;
import ch.elexis.core.services.ICodeElementServiceContribution;

public class DiagnoseSelektor extends FilteredItemsSelectionDialog {
	
	private List<IDiagnosis> diagnoses = new ArrayList<IDiagnosis>();
	
	public DiagnoseSelektor(Shell shell){
		super(shell);
		setTitle(Messages.DiagnoseSelektorDialog_Title);
		
		diagnoses.add(new NoDiagnose());
		
		List<ICodeElementServiceContribution> diagnoseContributions =
			CodeElementServiceHolder.get().getContributionsByTyp(CodeElementTyp.DIAGNOSE);
		
		for (ICodeElementServiceContribution iCodeElementServiceContribution : diagnoseContributions) {
			diagnoses.addAll((Collection<? extends IDiagnosis>) iCodeElementServiceContribution
				.getElements(CodeElementServiceHolder.createContext()));
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
				diagnoses.add((IDiagnosis) object);
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
		
		for (IDiagnosis diagnose : diagnoses) {
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
	
	private class NoDiagnose implements IDiagnosis {
		
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
		public void setCode(String value){
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void setText(String value){
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public boolean addXid(String domain, String id, boolean updateIfExists){
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public IXid getXid(String domain){
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String getDescription(){
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public void setDescription(String value){
			// TODO Auto-generated method stub
			
		}
	}
}
