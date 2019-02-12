package ch.elexis.core.ui.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaTray;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.ui.dialogs.base.InputDialog;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;

public class TextTemplatePrintSettingsDialog extends TitleAreaDialog {
	private ComboViewer cvPrinters, cvTrays;
	private List<PrintService> printServices;
	private List<MediaTray> mediaTrays;
	
	private String selPrinter, selTray;
	
	public TextTemplatePrintSettingsDialog(Shell parentShell, String printer, String tray){
		super(parentShell);
		printServices = Arrays.asList(PrintServiceLookup.lookupPrintServices(null, null));
		mediaTrays = new ArrayList<MediaTray>();
		
		this.selPrinter = printer;
		this.selTray = tray;
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		setTitle("Textvorlagen Druckeinstellungen");
		setMessage("Bitte Drucker und Druckschacht für diese Vorlage definieren");
		setTitleImage(Images.IMG_PRINTER_BIG.getImage());
		
		Composite area = new Composite(parent, SWT.NONE);
		area.setLayout(new GridLayout(3, false));
		area.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		
		Label lblPrinter = new Label(area, SWT.NONE);
		lblPrinter.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPrinter.setText("Drucker");
		
		cvPrinters = new ComboViewer(area, SWT.READ_ONLY);
		Combo comboPrinters = cvPrinters.getCombo();
		comboPrinters.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		cvPrinters.setContentProvider(ArrayContentProvider.getInstance());
		cvPrinters.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof PrintService) {
					PrintService ps = (PrintService) element;
					return ps.getName();
				}
				return super.getText(element);
			}
		});
		cvPrinters.setInput(printServices);
		cvPrinters.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection != null && !selection.isEmpty()) {
					PrintService printService = (PrintService) selection.getFirstElement();
					loadAvailableTrays(printService);
					cvTrays.setInput(mediaTrays);
					cvTrays.refresh();
				}
			}
		});
		
		Label lblTray = new Label(area, SWT.NONE);
		lblTray.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTray.setText("Schacht");
		
		cvTrays = new ComboViewer(area, SWT.READ_ONLY);
		Combo comboTrays = cvTrays.getCombo();
		comboTrays.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cvTrays.setContentProvider(ArrayContentProvider.getInstance());
		cvTrays.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof MediaTray) {
					MediaTray mt = (MediaTray) element;
					return mt.toString();
				}
				return super.getText(element);
			}
		});
		cvTrays.setInput(mediaTrays);
		
		Button addTrayButton = new Button(area, SWT.PUSH);
		addTrayButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		addTrayButton.setImage(Images.IMG_ADDITEM.getImage());
		addTrayButton.setToolTipText("Zusätzlicher Schacht");
		addTrayButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				InputDialog dlg = new InputDialog(getParentShell(), "Zusätzlicher Schacht",
					"Bitten den Namen des zusätzlichen Schacht konfigurieren.", "", null, SWT.NONE);
				if (dlg.open() == Window.OK) {
					if (dlg.getValue() != null && !dlg.getValue().isEmpty()) {
						addCustomMediaTray(dlg.getValue());
					}
				}
			}
		});
		
		initSelection();
		return area;
	}
	
	private void initSelection(){
		if (selPrinter == null) {
			PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();
			if (defaultPrintService != null && printServices.contains(defaultPrintService)) {
				cvPrinters.setSelection(new StructuredSelection(defaultPrintService));
			}
		} else {
			for (PrintService ps : printServices) {
				if (ps.getName().equals(selPrinter)) {
					cvPrinters.setSelection(new StructuredSelection(ps));
				}
			}
		}
		
		if (!mediaTrays.isEmpty()) {
			if (selTray == null) {
				cvTrays.setSelection(new StructuredSelection(mediaTrays.get(0)));
			} else {
				boolean foundTray = false;
				for (MediaTray mt : mediaTrays) {
					if (mt.toString().equals(selTray)) {
						cvTrays.setSelection(new StructuredSelection(mt));
						foundTray = true;
						break;
					}
				}
				if (!foundTray) {
					addCustomMediaTray(selTray);
				}
			}
		}
	}
	
	private void addCustomMediaTray(String name){
		CustomMediaTray customTray = new CustomMediaTray(name);
		mediaTrays.add(customTray);
		cvTrays.setInput(mediaTrays);
		cvTrays.refresh();
		cvTrays.setSelection(new StructuredSelection(customTray));
	}
	
	private List<MediaTray> loadAvailableTrays(PrintService printService){
		mediaTrays = new ArrayList<MediaTray>();
		Object attributes =
			printService.getSupportedAttributeValues(Media.class,
				DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
		if (attributes != null && attributes.getClass().isArray()) {
			for (Media media : (Media[]) attributes) {
				if (media instanceof MediaTray) {
					mediaTrays.add((MediaTray) media);
				}
			}
		}
		return mediaTrays;
	}
	
	@Override
	protected void okPressed(){
		IStructuredSelection selPrintService = (IStructuredSelection) cvPrinters.getSelection();
		if (selPrintService != null && !selPrintService.isEmpty()) {
			selPrinter = ((PrintService) selPrintService.getFirstElement()).getName();
		}
		
		IStructuredSelection selMediaTray = (IStructuredSelection) cvTrays.getSelection();
		if (selMediaTray != null) {
			if (selMediaTray.isEmpty()) {
				selTray = "";
			} else {
				selTray = ((MediaTray) selMediaTray.getFirstElement()).toString();
			}
		}
		super.okPressed();
	}
	
	public String getPrinter(){
		return selPrinter;
	}
	
	public String getMediaTray(){
		return selTray;
	}
	
	private class CustomMediaTray extends MediaTray {
		
		private String name;
		
		protected CustomMediaTray(String name){
			super(-1);
			this.name = name;
		}
		
		@Override
		public String toString(){
			return name;
		}
	}
}