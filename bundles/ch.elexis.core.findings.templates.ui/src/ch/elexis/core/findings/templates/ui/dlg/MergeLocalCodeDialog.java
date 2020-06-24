package ch.elexis.core.findings.templates.ui.dlg;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ILocalCoding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.templates.ui.util.FindingsServiceHolder;
import ch.elexis.core.findings.util.FindingsTextUtil;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;

public class MergeLocalCodeDialog extends TitleAreaDialog {
	
	private TableViewer destinationItems;
	private Text destinationFilterTxt;
	private LocalCodeViewerFilter destinationFilter;
	private TableViewer sourceItems;
	private Text sourceFilterTxt;
	private LocalCodeViewerFilter sourceFilter;
	private LabelProvider labelProvider;
	
	public MergeLocalCodeDialog(Shell parentShell){
		super(parentShell);
		
		labelProvider = new LabelProvider() {
			@Override
			public String getText(Object element){
				ILocalCoding iCoding = (ILocalCoding) element;
				StringBuilder stringBuilder = new StringBuilder();
				for (ICoding mappedCoding : iCoding.getMappedCodes()) {
					
						if (stringBuilder.length() > 0) {
							stringBuilder.append(", ");
						}
						stringBuilder.append(mappedCoding.getSystem());
						stringBuilder.append(": ");
						stringBuilder.append(mappedCoding.getCode());
					
				}
				
				return iCoding != null ? iCoding.getDisplay() + " (" + iCoding.getCode() + ")"
					+ (stringBuilder.length() > 0 ? (" [" + stringBuilder.toString() + "]") : "")
						: "";
			}
		};
		destinationFilter = new LocalCodeViewerFilter(labelProvider);
		sourceFilter = new LocalCodeViewerFilter(labelProvider);
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		getShell().setText("Codes vereinen");
		setTitle("Codes vereinen");
		setMessage("Bitte die zu vereinenden Codes auswählen");
		
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(new GridData(GridData.FILL_BOTH));
		ret.setLayout(new GridLayout(1, false));
		
		Label lbl = new Label(ret, SWT.NONE);
		lbl.setText("Code der nach dem vereinen gesetzt ist");
		
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1);
		layoutData.heightHint = 150;
		
		destinationFilterTxt = new Text(ret, SWT.BORDER);
		destinationFilterTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		destinationFilterTxt.setMessage("Filter"); //$NON-NLS-1$
		destinationFilterTxt.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e){
				if (destinationFilterTxt.getText().length() > 1) {
					destinationFilter.setSearchText(destinationFilterTxt.getText());
					destinationItems.refresh();
				} else {
					destinationFilter.setSearchText(""); //$NON-NLS-1$
					destinationItems.refresh();
				}
			}
		});
		
		destinationItems = new TableViewer(ret, SWT.BORDER);
		destinationItems.getTable().setLayoutData(layoutData);
		destinationItems.setContentProvider(new ArrayContentProvider());
		destinationItems.setLabelProvider(labelProvider);
		destinationItems.setComparator(new LocalCodeViewerComparator(labelProvider));
		destinationItems.addFilter(destinationFilter);
		
		ColumnViewerToolTipSupport.enableFor(destinationItems, ToolTip.NO_RECREATE);
		
		lbl = new Label(ret, SWT.NONE);
		lbl.setText("Code der nach dem vereinen entfernt wird");
		
		sourceFilterTxt = new Text(ret, SWT.BORDER);
		sourceFilterTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		sourceFilterTxt.setMessage("Filter"); //$NON-NLS-1$
		sourceFilterTxt.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e){
				if (sourceFilterTxt.getText().length() > 1) {
					sourceFilter.setSearchText(sourceFilterTxt.getText());
					sourceItems.refresh();
				} else {
					sourceFilter.setSearchText(""); //$NON-NLS-1$
					sourceItems.refresh();
				}
			}
		});
		
		sourceItems = new TableViewer(ret, SWT.BORDER);
		sourceItems.getTable().setLayoutData(layoutData);
		sourceItems.setContentProvider(new ArrayContentProvider());
		sourceItems.setLabelProvider(labelProvider);
		sourceItems.setComparator(new LocalCodeViewerComparator(labelProvider));
		sourceItems.addFilter(sourceFilter);
		
		ColumnViewerToolTipSupport.enableFor(sourceItems, ToolTip.NO_RECREATE);
		
		List<ICoding> codings = FindingsServiceHolder.codingService
			.getAvailableCodes(CodingSystem.ELEXIS_LOCAL_CODESYSTEM.getSystem());
		destinationItems.setInput(codings);
		sourceItems.setInput(codings);
		
		return ret;
	}
	
	@Override
	protected void okPressed(){
		StructuredSelection selection = (StructuredSelection) destinationItems.getSelection();
		if (selection.isEmpty()) {
			setErrorMessage("Kein Code zum setzen selektiert");
			return;
		}
		ILocalCoding destination = (ILocalCoding) selection.getFirstElement();
		
		selection = (StructuredSelection) sourceItems.getSelection();
		if (selection.isEmpty()) {
			setErrorMessage("Kein Code zum entfernen selektiert");
			return;
		}
		ILocalCoding source = (ILocalCoding) selection.getFirstElement();
		
		if (source == destination) {
			setErrorMessage("Selber Code zum setzen und entfernen selektiert");
			return;
		}
		
		boolean confirm =
			MessageDialog.openConfirm(getShell(), "Warnung",
				"Warnung vereinen kann nicht rückgängig gemacht werden.\nSollen die Codes vereint werden?");
		
		if (confirm) {
			mergeLocalCode(source, destination);
			FindingsServiceHolder.codingService.removeLocalCoding(source);
		} else {
			return;
		}
		
		super.okPressed();
	}
	
	private void mergeLocalCode(ILocalCoding source, ILocalCoding destination){
		IQuery<IObservation> obsQuery =
			FindingsServiceHolder.findingsModelService.getQuery(IObservation.class);
		obsQuery.and("content", COMPARATOR.LIKE,
			"%\"system\":\"" + source.getSystem() + "\",\"code\":\"" + source.getCode() + "\"%");
		List<IObservation> obsWithCode = obsQuery.execute();
		for (IObservation iObservation : obsWithCode) {
			List<ICoding> coding = iObservation.getCoding();
			coding = coding.stream().filter(c -> !(c.getSystem().equals(source.getSystem())
				&& c.getCode().equals(source.getCode()))).collect(Collectors.toList());
			coding.add(destination);
			iObservation.setCoding(coding);
			// update the text
			FindingsTextUtil.getObservationText(iObservation, true);
			FindingsServiceHolder.findingsService.saveFinding(iObservation);
		}
		ElexisEventDispatcher.getInstance()
			.fire(new ElexisEvent(null, ICoding.class, ElexisEvent.EVENT_RELOAD));
	}
	
	public void setSource(ILocalCoding source){
		sourceItems.setSelection(new StructuredSelection(source));
	}
}