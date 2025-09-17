/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *
 *******************************************************************************/

package ch.elexis.core.ui.dialogs;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.tiff.common.ui.datepicker.DatePicker;

import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ISickCertificate;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Eine AUF erstellen oder ändern
 *
 * @author gerry
 */
public class EditAUFDialog extends TitleAreaDialog {
	private ISickCertificate auf;
	private ICoverage fall;
	private DatePicker dpVon, dpBis;
	private Text tProzent, tGrund, tZusatz;
	TimeTool tt = new TimeTool();

	public EditAUFDialog(Shell shell, ISickCertificate a, ICoverage fall) {
		super(shell);
		auf = a;
		if (auf != null && fall == null) {
			fall = auf.getCoverage();
		}
		this.fall = fall;
		if (this.fall == null) {
			this.close();
			Display.getDefault().asyncExec(() -> {
				MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Neue AUF",
						"Bitte wählen Sie zuerst einen Fall für diese AUF aus");
			});
		}
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout(2, true));
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		new Label(ret, SWT.NONE).setText(Messages.Core_Since); // $NON-NLS-1$
		new Label(ret, SWT.NONE).setText(Messages.Core_Date_Until); // $NON-NLS-1$
		dpVon = new DatePicker(ret, SWT.NONE);
		dpBis = new DatePicker(ret, SWT.NONE);

		dpBis.addVerifyListener(new Listener() {
		    @Override
		    public void handleEvent(Event event) {
		    	checkDateSpan(dpVon.getDate(), (Date) event.data, event);
		    }
		});
		
		dpVon.addVerifyListener(new Listener() {
			 @Override
			    public void handleEvent(Event event) {
			    	checkDateSpan((Date) event.data, dpBis.getDate(), event);
			    }
		});
		
		new Label(ret, SWT.NONE).setText(Messages.EditAUFDialog_percent); // $NON-NLS-1$
		new Label(ret, SWT.NONE).setText(Messages.Core_Reason); // $NON-NLS-1$
		tProzent = new Text(ret, SWT.BORDER);
		tGrund = new Text(ret, SWT.BORDER);
		tProzent.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tGrund.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));

		Label lbZusatz = new Label(ret, SWT.NONE);
		lbZusatz.setText(Messages.EditAUFDialog_additional); // $NON-NLS-1$
		lbZusatz.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1));
		tZusatz = new Text(ret, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		GridData tZusatzLayout = SWTHelper.getFillGridData(2, true, 1, true);
		tZusatzLayout.heightHint = 150;
		tZusatz.setLayoutData(tZusatzLayout);
		if (auf != null) {
			dpVon.setDate(asDate(auf.getStart()));
			dpBis.setDate(asDate(auf.getEnd()));
			
			tGrund.setText(auf.getReason());
			tProzent.setText(Integer.toString(auf.getPercent()));
			tZusatz.setText(auf.getNote());
		} else {
			tGrund.setText(fall.getReason());
			tProzent.setText("100"); //$NON-NLS-1$
			dpVon.setDate(tt.getTime());
			dpBis.setDate(tt.getTime());
		}
		return ret;
	}
	
	private void checkDateSpan(Date start, Date end, Event event) {
		if (!validDateSpan(start, end)) {
			event.doit = false;
			SWTHelper.showError(Messages.EditAUFDialog_invalidDateSpan,
			Messages.EditAUFDialog_checkIfDatesValid);
		}
	}

	private Date asDate(LocalDate localDate) {
		return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	private LocalDate asLocalDate(Date date) {
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

	@Override
	public void create() {
		super.create();
		IPatient patient = ContextServiceHolder.get().getActivePatient().orElse(null);
		String patLabel = (patient != null) ? patient.getLabel() : "missing patient name"; //$NON-NLS-1$
		setTitle(Messages.EditAUFDialog_auf + " - " + patLabel);
		if (auf == null) {
			setMessage(Messages.EditAUFDialog_enterNewAUF); // $NON-NLS-1$
		} else {
			setMessage(Messages.EditAUFDialog_editAufDetails); // $NON-NLS-1$
		}
		getShell().setText(Messages.EditAUFDialog_auf); // $NON-NLS-1$
		setTitleImage(Images.IMG_LOGO.getImage(ImageSize._75x66_TitleDialogIconSize));
	}

	@Override
	protected void okPressed() {
		String zus = tZusatz.getText();
		if (auf == null) {
			auf = CoreModelServiceHolder.get().create(ISickCertificate.class);
			auf.setDate(LocalDate.now());
			auf.setPatient(fall.getPatient());
		}
		
		auf.setCoverage(fall);
		auf.setStart(asLocalDate(dpVon.getDate()));
		auf.setEnd(asLocalDate(dpBis.getDate()));
		auf.setPercent(Integer.parseInt(tProzent.getText()));
		auf.setReason(tGrund.getText());
		
		if (!StringTool.isNothing(zus)) {
			auf.setNote(zus);
		}
		CoreModelServiceHolder.get().save(auf);
		super.okPressed();
		}

	private static boolean validDateSpan(Date startDate, Date endDate) {
		return startDate.before(endDate) || startDate.equals(endDate);
	}

	public ISickCertificate getAuf() {
		return auf;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}
}
