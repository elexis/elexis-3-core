package ch.elexis.core.findings.templates.ui.composite;

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.templates.ui.views.FindingsTemplateView;

public class CodesSystemsComposite extends Composite {
	
	public CodesSystemsComposite(Composite parent){
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout(2, false));
		this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}
	
	public void createContens(){
		Label lblCodeSystem = new Label(this, SWT.NONE);
		lblCodeSystem.setText("Code System");
		ComboViewer comboViewer = new ComboViewer(this);
		comboViewer.setContentProvider(new ArrayContentProvider());
		comboViewer.setLabelProvider(new LabelProvider());
		comboViewer.setInput(FindingsTemplateView.codingService.getAvailableCodeSystems());
		
		TableViewer tableViewer = new TableViewer(this);
		tableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				ICoding iCoding = (ICoding) element;
				return iCoding != null ? iCoding.getCode() : "";
			}
		});
		
		comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				StructuredSelection s = (StructuredSelection) event.getSelection();
				String system = (String) s.getFirstElement();
				tableViewer.setInput(FindingsTemplateView.codingService.getAvailableCodes(system));
			
			}
		});
		
		comboViewer.setSelection(new StructuredSelection(((List<String>)comboViewer.getInput()).get(0)));
	}
}
