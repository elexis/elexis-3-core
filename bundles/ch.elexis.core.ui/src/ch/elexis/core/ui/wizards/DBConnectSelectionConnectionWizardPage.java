/*******************************************************************************
 * Copyright (c) 2015 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.wizards;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.StringTool;
import jakarta.xml.bind.JAXBException;

public class DBConnectSelectionConnectionWizardPage extends DBConnectWizardPage {
	private Label lblConnection, lblUser, lblDriver, lblTyp;
	private ComboViewer cViewerConns;
	private Button btnDelStoredConn;
	private Button btnCopyStoredConn;

	/**
	 * @wbp.parser.constructor
	 */
	public DBConnectSelectionConnectionWizardPage(String dBConnectWizard_typeOfDB) {
		super(Messages.Core_Connection);

		setTitle(Messages.Core_Connection);
		setMessage(Messages.DBConnectSelectionConnectionWizardPage_this_message);
	}

	@Override
	public void createControl(Composite parent) {
		Composite area = new Composite(parent, SWT.NONE);
		area.setLayout(new GridLayout(1, false));

		setControl(area);

		Group grpStatCurrentConnection = new Group(area, SWT.NONE);
		grpStatCurrentConnection.setLayout(new GridLayout(2, true));
		grpStatCurrentConnection.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		grpStatCurrentConnection.setText(Messages.DBConnectWizardPage_grpStatCurrentConnection_text);
		createEntityArea(grpStatCurrentConnection);

		Composite cmpExistConnSelector = new Composite(area, SWT.BORDER);
		cmpExistConnSelector.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		cmpExistConnSelector.setLayout(new GridLayout(3, false));

		Label lblGespeicherteVerbindungen = new Label(cmpExistConnSelector, SWT.NONE);
		lblGespeicherteVerbindungen.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		lblGespeicherteVerbindungen.setText(Messages.DBConnectWizardPage_lblGespeicherteVerbindungen_text);

		cViewerConns = new ComboViewer(cmpExistConnSelector, SWT.READ_ONLY);
		Combo combo = cViewerConns.getCombo();
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cViewerConns.setContentProvider(ArrayContentProvider.getInstance());
		cViewerConns.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				DBConnection dbc = (DBConnection) element;
				if (dbc.username != null && dbc.connectionString != null) {
					return dbc.username + "@" + dbc.connectionString; //$NON-NLS-1$
				} else {
					return "Neue Verbindung erstellen";
				}
			}
		});
		cViewerConns.setInput(getDBConnectWizard().getStoredConnectionList());

		btnDelStoredConn = new Button(cmpExistConnSelector, SWT.FLAT);
		btnDelStoredConn.setImage(Images.IMG_DELETE.getImage());
		btnDelStoredConn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) cViewerConns.getSelection();
				if (selection.size() > 0) {
					Object firstElement = selection.getFirstElement();
					if (firstElement != null) {
						getDBConnectWizard().removeConnection((DBConnection) firstElement);
						cViewerConns.setInput(getDBConnectWizard().getStoredConnectionList());
						setCurrentSelection();
					}
				}
			}
		});

		btnCopyStoredConn = new Button(cmpExistConnSelector, SWT.FLAT);
		btnCopyStoredConn.setImage(Images.IMG_COPY.getImage());
		btnCopyStoredConn.setToolTipText("Verbindungsdaten in Zwischenablage kopieren");
		btnCopyStoredConn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) cViewerConns.getSelection();
				if (selection.size() > 0) {
					Object firstElement = selection.getFirstElement();
					if (firstElement != null) {
						DBConnection dbc = (DBConnection) firstElement;
						Clipboard cb = new Clipboard(UiDesk.getDisplay());
						TextTransfer textTransfer = TextTransfer.getInstance();
						try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
							dbc.marshall(bos);
							cb.setContents(new Object[] { bos.toString() }, new Transfer[] { textTransfer });
						} catch (JAXBException | IOException e1) {
							MessageDialog.openError(UiDesk.getTopShell(), "Error", e1.getMessage());
						}
					}
				}
			}
		});

		cViewerConns.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection.size() > 0) {
					Object firstElement = selection.getFirstElement();
					if (firstElement != null) {
						DBConnection targetedConnection = (DBConnection) firstElement;
						getDBConnectWizard().setTargetedConnection(targetedConnection);
						btnDelStoredConn
								.setEnabled(!targetedConnection.equals(getDBConnectWizard().getCurrentConnection()));
					}
				}
			}
		});
		setCurrentSelection();

		Label lblOderAufDer = new Label(cmpExistConnSelector, SWT.NONE);
		lblOderAufDer.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		lblOderAufDer.setText(Messages.DBConnectSelectionConnectionWizardPage_lblOderAufDer_text);

		tdbg = new TestDBConnectionGroup(area, SWT.NONE, getDBConnectWizard());
		tdbg.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	}

	private void setCurrentSelection() {
		if (getDBConnectWizard().getCurrentConnection() == null)
			return;
		cViewerConns.setSelection(new StructuredSelection(getDBConnectWizard().getCurrentConnection()));
	}

	private void createEntityArea(Group grpEntity) {
		String driver = StringUtils.EMPTY;
		String user = StringUtils.EMPTY;
		String typ = StringUtils.EMPTY;
		String connection = StringUtils.EMPTY;

		Composite typArea = new Composite(grpEntity, SWT.NONE);
		typArea.setLayout(new GridLayout(2, false));
		Label lTyp = new Label(typArea, SWT.NONE);
		lTyp.setImage(Images.IMG_TABLE.getImage());
		lblTyp = new Label(typArea, SWT.NONE);

		Composite driverArea = new Composite(grpEntity, SWT.NONE);
		driverArea.setLayout(new GridLayout(2, false));
		Label lDriver = new Label(driverArea, SWT.NONE);
		lDriver.setImage(Images.IMG_GEAR.getImage());
		lblDriver = new Label(driverArea, SWT.NONE);

		Composite userArea = new Composite(grpEntity, SWT.NONE);
		userArea.setLayout(new GridLayout(2, false));
		Label lUser = new Label(userArea, SWT.NONE);
		lUser.setImage(Images.IMG_USER_SILHOUETTE.getImage());
		lblUser = new Label(userArea, SWT.NONE);

		Composite connArea = new Composite(grpEntity, SWT.NONE);
		connArea.setLayout(new GridLayout(2, false));
		Label lConnection = new Label(connArea, SWT.NONE);
		lConnection.setImage(Images.IMG_NODE.getImage());
		lblConnection = new Label(connArea, SWT.NONE);

		Hashtable<Object, Object> conn = readRunningEntityInfos();
		if (conn != null) {
			driver = PersistentObject.checkNull(conn.get(Preferences.CFG_FOLDED_CONNECTION_DRIVER));
			connection = PersistentObject.checkNull(conn.get(Preferences.CFG_FOLDED_CONNECTION_CONNECTSTRING));
			user = PersistentObject.checkNull(conn.get(Preferences.CFG_FOLDED_CONNECTION_USER));
			typ = PersistentObject.checkNull(conn.get(Preferences.CFG_FOLDED_CONNECTION_TYPE));
		}

		lblTyp.setText(typ);
		lblDriver.setText(driver);
		lblUser.setText(user);
		lblConnection.setText(connection);
	}

	private Hashtable<Object, Object> readRunningEntityInfos() {
		Hashtable<Object, Object> conn = null;
		String cnt = CoreHub.localCfg.get(Preferences.CFG_FOLDED_CONNECTION, null);
		if (cnt != null) {
			conn = PersistentObject.fold(StringTool.dePrintable(cnt));
			return conn;
		}
		return null;
	}

	@Override
	public void setPageComplete(boolean complete) {
		super.setPageComplete(complete);
	}

	private DBConnectWizard getDBConnectWizard() {
		return (DBConnectWizard) getWizard();
	}

}
