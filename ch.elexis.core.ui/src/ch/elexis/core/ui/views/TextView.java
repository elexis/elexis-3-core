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

package ch.elexis.core.ui.views;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.Brief;
import ch.elexis.core.data.Fall;
import ch.elexis.core.data.Konsultation;
import ch.elexis.core.data.Kontakt;
import ch.elexis.core.data.Patient;
import ch.elexis.core.data.Person;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.admin.AccessControlDefaults;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.icons.Images;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.GlobalEventDispatcher.IActivationListener;
import ch.elexis.core.ui.dialogs.DocumentSelectDialog;
import ch.elexis.core.ui.dialogs.SelectFallDialog;
import ch.elexis.core.ui.text.ITextPlugin;
import ch.elexis.core.ui.text.TextContainer;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.MimeTool;


public class TextView extends ViewPart implements IActivationListener {
	public final static String ID = "ch.elexis.TextView"; //$NON-NLS-1$
	TextContainer txt;
	// CommonViewer cv;
	Composite textContainer = null;
	private Brief actBrief;
	private Logger log = LoggerFactory.getLogger("TextView");//$NON-NLS-1$
	private IAction briefLadenAction, loadTemplateAction, loadSysTemplateAction,
			saveTemplateAction, showMenuAction, showToolbarAction, importAction, newDocAction,
			exportAction;
	private ViewMenus menus;
	
	public TextView(){}
	
	@Override
	public void createPartControl(Composite parent){
		txt = new TextContainer(getViewSite());
		textContainer = txt.getPlugin().createContainer(parent, new SaveHandler());
		if (textContainer == null) {
			SWTHelper
				.showError(
					Messages.getString("TextView.couldNotCreateTextView"), Messages.getString("TextView.couldNotLoadTextPlugin")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			makeActions();
			menus = new ViewMenus(getViewSite());
			// menus.createToolbar(briefNeuAction);
			menus.createMenu(newDocAction, briefLadenAction, loadTemplateAction,
				loadSysTemplateAction, saveTemplateAction, null, showMenuAction, showToolbarAction,
				null, importAction, exportAction);
			GlobalEventDispatcher.addActivationListener(this, this);
			setName();
		}
	}
	
	@Override
	public void setFocus(){
		if (textContainer != null) {
			textContainer.setFocus();
		}
	}
	
	public TextContainer getTextContainer(){
		return txt;
	}
	
	@Override
	public void dispose(){
		GlobalEventDispatcher.removeActivationListener(this, this);
		actBrief = null;
		super.dispose();
	}
	
	public boolean openDocument(Brief doc){
		if (txt.open(doc) == true) {
			log.debug("TextView.openDocument: "); //$NON-NLS-1$		
			actBrief = doc;
			setName();
			return true;
		} else {
			actBrief = null;
			setName();
			String ext = MimeTool.getExtension(doc.getMimeType());
			if (ext.length() == 0) {
				ext = "odt"; //$NON-NLS-1$
			}
			try {
				log.debug("TextView.openDocument createTempFile: "); //$NON-NLS-1$		
				File tmp = File.createTempFile("elexis", "brief." + ext); //$NON-NLS-1$ //$NON-NLS-2$
				tmp.deleteOnExit();
				byte[] buffer = doc.loadBinary();
				if (buffer == null) {
					return false;
				}
				ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
				FileOutputStream fos = new FileOutputStream(tmp);
				FileTool.copyStreams(bais, fos);
				return Program.launch(tmp.getAbsolutePath());
			} catch (IOException e) {
				ExHandler.handle(e);
			}
			
			return false;
		}
	}
	
	/**
	 * Ein Document von Vorlage erstellen.
	 * 
	 * @param template
	 *            die Vorlage
	 * @param subject
	 *            Titel, kann null sein
	 * @return true bei erfolg
	 */
	public boolean createDocument(Brief template, String subject){
		log.debug("TextView.createDocument: " + subject); //$NON-NLS-1$		
		if (template == null) {
			SWTHelper
				.showError(
					Messages.getString("TextView.noTemplateSelected"), Messages.getString("TextView.pleaseSelectTemplate")); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
		actBrief =
			txt.createFromTemplate(Konsultation.getAktuelleKons(), template, Brief.UNKNOWN, null,
				subject);
		setName();
		if (actBrief == null) {
			return false;
		}
		return true;
	}
	
	/**
	 * Ein Document von Vorlage erstellen. Adressat kann hier angegeben werden
	 * 
	 * @param template
	 *            die Vorlage
	 * @param subject
	 *            Titel, kann null sein
	 * @param adressat
	 *            der Adressat, der im Dokument angezeigt werden soll
	 * @return true bei erfolg
	 */
	public boolean createDocument(Brief template, String subject, Kontakt adressat){
		log.debug("TextView.createDocument: " + subject + " Kontakt"); //$NON-NLS-1$ //$NON-NLS-2$		
		if (template == null) {
			SWTHelper
				.showError(
					Messages.getString("TextView.noTemplateSelected"), Messages.getString("TextView.pleaseSelectTemplate")); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
		actBrief =
			txt.createFromTemplate(Konsultation.getAktuelleKons(), template, Brief.UNKNOWN,
				adressat, subject);
		setName();
		if (actBrief == null) {
			return false;
		}
		return true;
	}
	
	private void makeActions(){
		briefLadenAction = new Action(Messages.getString("TextView.openLetter")) { //$NON-NLS-1$
				@Override
				public void run(){
					Patient actPatient = (Patient) ElexisEventDispatcher.getSelected(Patient.class);
					DocumentSelectDialog bs =
						new DocumentSelectDialog(getViewSite().getShell(), actPatient,
							DocumentSelectDialog.TYPE_LOAD_DOCUMENT);
					if (bs.open() == Dialog.OK) {
						openDocument(bs.getSelectedDocument());
					}
				}
				
			};
		
		loadSysTemplateAction = new Action(Messages.getString("TextView.openSysTemplate")) { //$NON-NLS-1$
				@Override
				public void run(){
					DocumentSelectDialog bs =
						new DocumentSelectDialog(getViewSite().getShell(), CoreHub.actMandant,
							DocumentSelectDialog.TYPE_LOAD_SYSTEMPLATE);
					if (bs.open() == Dialog.OK) {
						openDocument(bs.getSelectedDocument());
					}
				}
			};
		loadTemplateAction = new Action(Messages.getString("TextView.openTemplate")) { //$NON-NLS-1$
				@Override
				public void run(){
					DocumentSelectDialog bs =
						new DocumentSelectDialog(getViewSite().getShell(), CoreHub.actMandant,
							DocumentSelectDialog.TYPE_LOAD_TEMPLATE);
					if (bs.open() == Dialog.OK) {
						openDocument(bs.getSelectedDocument());
					}
				}
			};
		saveTemplateAction = new Action(Messages.getString("TextView.saveAsTemplate")) { //$NON-NLS-1$
				@Override
				public void run(){
					if (actBrief != null) {
						txt.saveTemplate(actBrief.get(Messages.getString("TextView.Subject"))); //$NON-NLS-1$
					} else {
						txt.saveTemplate(null);
					}
				}
			};
		
		showMenuAction = new Action(Messages.getString("TextView.showMenu"), Action.AS_CHECK_BOX) { //$NON-NLS-1$			
				public void run(){
					txt.getPlugin().showMenu(isChecked());
				}
			};
		
		showToolbarAction =
			new Action(Messages.getString("TextView.Toolbar"), Action.AS_CHECK_BOX) { //$NON-NLS-1$
				public void run(){
					txt.getPlugin().showToolbar(isChecked());
				}
			};
		importAction = new Action(Messages.getString("TextView.importText")) { //$NON-NLS-1$
				@Override
				public void run(){
					try {
						FileDialog fdl = new FileDialog(getViewSite().getShell());
						String filename = fdl.open();
						if (filename != null) {
							File file = new File(filename);
							if (file.exists()) {
								actBrief = null;
								setPartName(filename);
								FileInputStream fis = new FileInputStream(file);
								txt.getPlugin().loadFromStream(fis, false);
							}
							
						}
						
					} catch (Throwable ex) {
						ExHandler.handle(ex);
					}
				}
			};
		
		exportAction = new Action(Messages.getString("TextView.exportText")) { //$NON-NLS-1$
				@Override
				public void run(){
					try {
						if (actBrief == null) {
							SWTHelper.alert("Fehler", //$NON-NLS-1$
								"Es ist kein Dokument zum exportieren geladen"); //$NON-NLS-1$
						} else {
							FileDialog fdl = new FileDialog(getViewSite().getShell(), SWT.SAVE);
							fdl.setFilterExtensions(new String[] {
								"*.odt", "*.xml", "*.*" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							});
							fdl.setFilterNames(new String[] {
								"OpenOffice.org Text", "XML File", "All files" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							});
							String filename = fdl.open();
							if (filename != null) {
								if (FileTool.getExtension(filename).equals("")) { //$NON-NLS-1$
									filename += ".odt"; //$NON-NLS-1$
								}
								File file = new File(filename);
								byte[] contents = actBrief.loadBinary();
								ByteArrayInputStream bais = new ByteArrayInputStream(contents);
								FileOutputStream fos = new FileOutputStream(file);
								FileTool.copyStreams(bais, fos);
								fos.close();
								bais.close();
								
							}
						}
						
					} catch (Throwable ex) {
						ExHandler.handle(ex);
					}
				}
			};
		newDocAction = new Action(Messages.getString("TextView.newDocument")) { //$NON-NLS-1$
				{
					setImageDescriptor(Images.IMG_NEW.getImageDescriptor());
				}
				
				public void run(){
					Patient pat = ElexisEventDispatcher.getSelectedPatient();
					if (pat != null) {
						Fall selectedFall = (Fall) ElexisEventDispatcher.getSelected(Fall.class);
						if (selectedFall == null) {
							SelectFallDialog sfd = new SelectFallDialog(UiDesk.getTopShell());
							sfd.open();
							if (sfd.result != null) {
								ElexisEventDispatcher.fireSelectionEvent(sfd.result);
							} else {
								MessageDialog
									.openInformation(UiDesk.getTopShell(),
										Messages.getString("TextView.NoCaseSelected"), //$NON-NLS-1$
										Messages
											.getString("TextView.SaveNotPossibleNoCaseAndKonsSelected")); //$NON-NLS-1$
								return;
							}
						}
						Konsultation selectedKonsultation =
							(Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
						if (selectedKonsultation == null) {
							Konsultation k = pat.getLetzteKons(false);
							if (k == null) {
								k =
									((Fall) ElexisEventDispatcher.getSelected(Fall.class))
										.neueKonsultation();
								k.setMandant(CoreHub.actMandant);
							}
							ElexisEventDispatcher.fireSelectionEvent(k);
						}
						actBrief = null;
						setName();
						txt.getPlugin().createEmptyDocument();
					} else {
						MessageDialog.openInformation(UiDesk.getTopShell(),
							Messages.getString("BriefAuswahlNoPatientSelected"), //$NON-NLS-1$
							Messages.getString("BriefAuswahlNoPatientSelected")); //$NON-NLS-1$
					}
				}
				
			};
		briefLadenAction.setImageDescriptor(Images.IMG_MAIL.getImageDescriptor());
		briefLadenAction.setToolTipText("Brief zum Bearbeiten öffnen"); //$NON-NLS-1$
		// briefNeuAction.setImageDescriptor(Hub.getImageDescriptor("rsc/schreiben.gif"));
		// briefNeuAction.setToolTipText("Einen neuen Brief erstellen");
		showMenuAction.setToolTipText(Messages.getString("TextView.showMenuBar")); //$NON-NLS-1$
		showMenuAction.setImageDescriptor(Images.IMG_MENUBAR.getImageDescriptor());
		showMenuAction.setChecked(true);
		showToolbarAction.setImageDescriptor(Images.IMG_TOOLBAR.getImageDescriptor());
		showToolbarAction.setToolTipText(Messages.getString("TextView.showToolbar")); //$NON-NLS-1$
		showToolbarAction.setChecked(true);
	}
	
	class SaveHandler implements ITextPlugin.ICallback {
		
		public void save(){
			log.debug("TextView.save"); //$NON-NLS-1$
			if (actBrief != null) {
				actBrief.save(txt.getPlugin().storeToByteArray(), txt.getPlugin().getMimeType());
			}
		}
		
		public boolean saveAs(){
			log.debug("TextView.saveAs"); //$NON-NLS-1$
			InputDialog il =
				new InputDialog(
					getViewSite().getShell(),
					Messages.getString("TextView.saveText"), Messages.getString("TextView.enterTitle"), "", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (il.open() == Dialog.OK) {
				actBrief.setBetreff(il.getValue());
				return actBrief.save(txt.getPlugin().storeToByteArray(), txt.getPlugin()
					.getMimeType());
			}
			return false;
		}
		
	}
	
	public void activation(boolean mode){
		if (mode == false) {
			if (actBrief != null) {
				actBrief.save(txt.getPlugin().storeToByteArray(), txt.getPlugin().getMimeType());
			}
			// txt.getPlugin().clear();
		} else {
			loadSysTemplateAction.setEnabled(CoreHub.acl
				.request(AccessControlDefaults.DOCUMENT_SYSTEMPLATE));
			saveTemplateAction.setEnabled(CoreHub.acl.request(AccessControlDefaults.DOCUMENT_TEMPLATE));
		}
	}
	
	public void visible(boolean mode){
		
	}
	
	void setName(){
		String n = ""; //$NON-NLS-1$
		if (actBrief == null) {
			setPartName(Messages.getString("TextView.noLetterSelected")); //$NON-NLS-1$
		} else {
			Person pat = actBrief.getPatient();
			if (pat != null) {
				n = pat.getLabel() + ": "; //$NON-NLS-1$
			}
			n += actBrief.getBetreff();
			setPartName(n);
		}
		log.debug("TextView.setName: " + getPartName()); //$NON-NLS-1$		
	}
	
}
