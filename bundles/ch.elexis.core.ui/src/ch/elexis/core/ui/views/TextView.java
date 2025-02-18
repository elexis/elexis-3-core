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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
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

import ch.elexis.core.ac.EvACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IDocumentLetter;
import ch.elexis.core.services.holder.AccessControlServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.text.ITextPlugin.Parameter;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.dialogs.DocumentSelectDialog;
import ch.elexis.core.ui.dialogs.SelectFallDialog;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.LockResponseHelper;
import ch.elexis.core.ui.text.EditLocalDocumentUtil;
import ch.elexis.core.ui.text.ITextPlugin;
import ch.elexis.core.ui.text.TextContainer;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.data.Brief;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Mandant;
import ch.elexis.data.Patient;
import ch.elexis.data.Person;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.MimeTool;
import jakarta.inject.Inject;
import jakarta.inject.Named;

public class TextView extends ViewPart implements IActivationListener {
	public final static String ID = "ch.elexis.TextView"; //$NON-NLS-1$
	TextContainer txt;
	// CommonViewer cv;
	Composite textContainer = null;
	private Brief actBrief;
	private Logger log = LoggerFactory.getLogger("TextView");//$NON-NLS-1$
	private IAction briefLadenAction, loadTemplateAction, loadSysTemplateAction, saveTemplateAction, showMenuAction,
			showToolbarAction, importAction, newDocAction, exportAction;
	private ViewMenus menus;

	public TextView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		txt = new TextContainer(getViewSite());
		textContainer = txt.getPlugin().createContainer(parent, new SaveHandler());
		if (textContainer == null) {
			SWTHelper.showError(Messages.TextView_couldNotCreateTextView, Messages.TextView_couldNotLoadTextPlugin);
		} else {
			makeActions();
			menus = new ViewMenus(getViewSite());
			// menus.createToolbar(briefNeuAction);
			menus.createMenu(newDocAction, briefLadenAction, loadTemplateAction, loadSysTemplateAction,
					saveTemplateAction, null, showMenuAction, showToolbarAction, null, importAction, exportAction);
			GlobalEventDispatcher.addActivationListener(this, this);
			setName();
		}
	}

	@Override
	public void setFocus() {
		if (textContainer != null) {
			textContainer.setFocus();
		}
	}

	public TextContainer getTextContainer() {
		return txt;
	}

	@Override
	public void dispose() {
		if (actBrief != null) {
			LocalLockServiceHolder.get().releaseLock(actBrief);
		}
		GlobalEventDispatcher.removeActivationListener(this, this);
		actBrief = null;
		super.dispose();
	}

	public boolean openDocument(Brief doc) {
		if (actBrief != null) {
			LocalLockServiceHolder.get().releaseLock(actBrief);
			actBrief.save(txt.getPlugin().storeToByteArray(), txt.getPlugin().getMimeType());
		}
		if (doc == null) {
			return false;
		} else {
			// test lock and set read only before opening the Brief
			LockResponse result = LocalLockServiceHolder.get().acquireLock(doc);
			if (result.isOk()) {
				txt.getPlugin().setParameter(null);
			} else {
				LockResponseHelper.showInfo(result, doc, log);
				txt.getPlugin().setParameter(Parameter.READ_ONLY);
			}
		}
		if (txt.open(doc) == true) {
			log.debug("TextView.openDocument: "); //$NON-NLS-1$
			actBrief = doc;
			setName();
			return true;
		} else {
			actBrief = null;
			LocalLockServiceHolder.get().releaseLock(doc);
			if (CoreHub.localCfg.get(Preferences.P_TEXT_SUPPORT_LEGACY, false) == true) {
				setName();
				String ext = MimeTool.getExtension(doc.getMimeType());
				if (ext.length() == 0) {
					log.warn("TextView.openDocument no extension found for mime type: " + doc.getMimeType()); //$NON-NLS-1$
					ext = "odt"; //$NON-NLS-1$
				}
				try {
					File tmp = File.createTempFile("elexis", "brief." + ext); //$NON-NLS-1$ //$NON-NLS-2$
					log.debug("TextView.openDocument createTempFile: " + tmp.getAbsolutePath() + " mime " //$NON-NLS-1$ //$NON-NLS-2$
							+ doc.getMimeType());
					tmp.deleteOnExit();
					byte[] buffer = doc.loadBinary();
					if (buffer == null) {
						log.warn("TextView.openDocument createTempFile [{}] -> loadBinary returned null array", //$NON-NLS-1$
								doc.getId());
						return false;
					}
					try (ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
							FileOutputStream fos = new FileOutputStream(tmp);) {
						FileTool.copyStreams(bais, fos);
					}
					File file = new File(tmp.getAbsolutePath());
					file.setReadable(true);
					file.setExecutable(false);
					file.setWritable(false);
					return Program.launch(tmp.getAbsolutePath());
				} catch (IOException e) {
					ExHandler.handle(e);
				}
				return false;
			} else {
				log.warn(
						"TextView.openDocument: Preferences do not allow alternative method of documents created with legacy text-plugins"); //$NON-NLS-1$
				// Do not show a message box, as this happens often when you load a document
				// with an invalid content. Eg. with demoDB and Rezept of Absolut Erfunden
				return false;
			}
		}
	}

	/**
	 * Ein Document von Vorlage erstellen.
	 *
	 * @param template die Vorlage
	 * @param subject  Titel, kann null sein
	 * @return true bei erfolg
	 */
	public boolean createDocument(Brief template, String subject) {
		log.debug("TextView.createDocument [{}]: {}", (template != null) ? template.getLabel() : "null", subject); //$NON-NLS-1$ //$NON-NLS-2$
		if (template == null) {
			SWTHelper.showError(Messages.TextView_noTemplateSelected, Messages.TextView_pleaseSelectTemplate);
			return false;
		}
		actBrief = txt.createFromTemplate(Konsultation.getAktuelleKons(), template, Brief.UNKNOWN, null, subject);
		setName();
		if (actBrief == null) {
			log.debug("TextView.createDocument: createFromTemplate -> null"); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	/**
	 * Ein Document von Vorlage erstellen. Adressat kann hier angegeben werden
	 *
	 * @param template die Vorlage
	 * @param subject  Titel, kann null sein
	 * @param adressat der Adressat, der im Dokument angezeigt werden soll
	 * @return true bei erfolg
	 */
	public boolean createDocument(Brief template, String subject, Kontakt adressat) {
		log.debug("TextView.createDocument [{}]: {} Kontakt", (template != null) ? template.getLabel() : "null", //$NON-NLS-1$ //$NON-NLS-2$
				subject);
		if (template == null) {
			SWTHelper.showError(Messages.TextView_noTemplateSelected, Messages.TextView_pleaseSelectTemplate);
			return false;
		}
		actBrief = txt.createFromTemplate(Konsultation.getAktuelleKons(), template, Brief.UNKNOWN, adressat, subject);

		setRecipientName(subject);

		EditLocalDocumentUtil.startEditLocalDocument(this, actBrief);
		setName();
		if (actBrief == null) {
			log.debug("TextView.createDocument: createFromTemplate -> null"); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	/**
	 * Setzt und speichert Adressangaben wie Vorname (nur bei natürliche Personen),
	 * Nachname und Ort im Brieftitel.
	 *
	 * @param subject
	 */
	private void setRecipientName(String subject) {
		String adressatLabel = null;
		if (actBrief != null && actBrief.getAdressat() != null) {
			String[] names = new String[3];
			actBrief.getAdressat().get(new String[] { Kontakt.FLD_NAME2, Kontakt.FLD_NAME1, Kontakt.FLD_PLACE }, names);

			List<String> nonEmptyNames = new ArrayList<>();
			String nameValue = ""; //$NON-NLS-1$

			for (int i = 0; i < names.length; i++) {
				String name = names[i];
				if (i == 0 && StringUtils.isNoneEmpty(name)) {
					nameValue = nameValue + name + " "; //$NON-NLS-1$
				} else if (i == 1 && StringUtils.isNoneEmpty(name)) {
					nameValue = nameValue + name;
				} else {
					nameValue = name;
				}
				if (i >= 1 && StringUtils.isNoneEmpty(nameValue)) {
					nonEmptyNames.add(nameValue);
				}
			}

			if (!nonEmptyNames.isEmpty()) {
				adressatLabel = String.join(", ", nonEmptyNames); //$NON-NLS-1$
			}
		}

		if (adressatLabel != null) {
			actBrief.set(new String[] { Brief.FLD_SUBJECT },
					new String[] { subject + " " + Messages.TextView_To + " " + adressatLabel }); //$NON-NLS-1$ //$NON-NLS-2$
		}

		txt.saveBrief(actBrief, Brief.UNKNOWN);
	}

	private void makeActions() {
		briefLadenAction = new Action(Messages.TextView_openLetter) { // $NON-NLS-1$
			@Override
			public void run() {
				DocumentSelectDialog bs = new DocumentSelectDialog(getViewSite().getShell(),
						DocumentSelectDialog.TYPE_LOAD_DOCUMENT);
				if (bs.open() == Dialog.OK) {
					openDocument(bs.getSelectedDocument());
				}
			}

		};

		loadSysTemplateAction = new Action(Messages.TextView_openSysTemplate) { // $NON-NLS-1$
			@Override
			public void run() {
				DocumentSelectDialog bs = new DocumentSelectDialog(getViewSite().getShell(),
						DocumentSelectDialog.TYPE_LOAD_SYSTEMPLATE);
				if (bs.open() == Dialog.OK) {
					openDocument(bs.getSelectedDocument());
				}
			}
		};
		loadTemplateAction = new Action(Messages.Core_Open_Template) { // $NON-NLS-1$
			@Override
			public void run() {
				DocumentSelectDialog bs = new DocumentSelectDialog(getViewSite().getShell(),
						DocumentSelectDialog.TYPE_LOAD_TEMPLATE);
				if (bs.open() == Dialog.OK) {
					openDocument(bs.getSelectedDocument());
				}
			}
		};
		saveTemplateAction = new Action(Messages.TextView_saveAsTemplate) { // $NON-NLS-1$
			@Override
			public void run() {
				if (actBrief != null) {
					txt.saveTemplate(actBrief.get(Messages.Core_Subject)); // $NON-NLS-1$
				} else {
					txt.saveTemplate(null);
				}
			}
		};

		showMenuAction = new Action(Messages.TextView_showMenu, Action.AS_CHECK_BOX) { // $NON-NLS-1$
			@Override
			public void run() {
				txt.getPlugin().showMenu(isChecked());
			}
		};

		showToolbarAction = new Action(Messages.TextView_Toolbar, Action.AS_CHECK_BOX) { // $NON-NLS-1$
			@Override
			public void run() {
				txt.getPlugin().showToolbar(isChecked());
			}
		};
		importAction = new Action(Messages.TextView_importText) { // $NON-NLS-1$
			@Override
			public void run() {
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

		exportAction = new Action(Messages.TextView_exportText) { // $NON-NLS-1$
			@Override
			public void run() {
				try {
					if (actBrief == null) {
						SWTHelper.alert("Fehler", //$NON-NLS-1$
								"Es ist kein Dokument zum exportieren geladen"); //$NON-NLS-1$
					} else {
						FileDialog fdl = new FileDialog(getViewSite().getShell(), SWT.SAVE);
						fdl.setFilterExtensions(new String[] { "*.odt", "*.xml", "*.*" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						});
						fdl.setFilterNames(new String[] { "OpenOffice.org Text", "XML File", "All files" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						});
						String filename = fdl.open();
						if (filename != null) {
							if (FileTool.getExtension(filename).equals(StringUtils.EMPTY)) {
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
		newDocAction = new Action(Messages.TextView_newDocument) { // $NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_NEW.getImageDescriptor());
			}

			@Override
			public void run() {
				Patient pat = ElexisEventDispatcher.getSelectedPatient();
				if (pat != null) {
					Fall selectedFall = (Fall) ElexisEventDispatcher.getSelected(Fall.class);
					if (selectedFall == null) {
						SelectFallDialog sfd = new SelectFallDialog(UiDesk.getTopShell());
						sfd.open();
						if (sfd.result != null) {
							ElexisEventDispatcher.fireSelectionEvent(sfd.result);
						} else {
							MessageDialog.openInformation(UiDesk.getTopShell(), Messages.TextView_NoCaseSelected, // $NON-NLS-1$
									Messages.TextView_SaveNotPossibleNoCaseAndKonsSelected); // $NON-NLS-1$
							return;
						}
					}
					Konsultation selectedKonsultation = (Konsultation) ElexisEventDispatcher
							.getSelected(Konsultation.class);
					if (selectedKonsultation == null) {
						Konsultation k = pat.getLetzteKons(false);
						if (k == null) {
							k = ((Fall) ElexisEventDispatcher.getSelected(Fall.class)).neueKonsultation();
							k.setMandant(Mandant.load(ContextServiceHolder.getActiveMandatorOrNull().getId()));
						}
						ElexisEventDispatcher.fireSelectionEvent(k);
					}
					actBrief = null;
					setName();
					txt.getPlugin().createEmptyDocument();
				} else {
					MessageDialog.openInformation(UiDesk.getTopShell(), Messages.Core_No_patient_selected, // $NON-NLS-1$
							Messages.Core_No_patient_selected); // $NON-NLS-1$
				}
			}

		};
		briefLadenAction.setImageDescriptor(Images.IMG_MAIL.getImageDescriptor());
		briefLadenAction.setToolTipText("Brief zum Bearbeiten öffnen"); //$NON-NLS-1$
		// briefNeuAction.setImageDescriptor(Hub.getImageDescriptor("rsc/schreiben.gif"));
		// briefNeuAction.setToolTipText("Einen neuen Brief erstellen");
		showMenuAction.setToolTipText(Messages.TextView_showMenuBar); // $NON-NLS-1$
		showMenuAction.setImageDescriptor(Images.IMG_MENUBAR.getImageDescriptor());
		showMenuAction.setChecked(true);
		showToolbarAction.setImageDescriptor(Images.IMG_TOOLBAR.getImageDescriptor());
		showToolbarAction.setToolTipText(Messages.TextView_showToolbar); // $NON-NLS-1$
		showToolbarAction.setChecked(true);
	}

	class SaveHandler implements ITextPlugin.ICallback {

		@Override
		public void save() {
			log.debug("TextView.save"); //$NON-NLS-1$
			if (actBrief != null) {
				actBrief.save(txt.getPlugin().storeToByteArray(), txt.getPlugin().getMimeType());
			}
		}

		@Override
		public boolean saveAs() {
			log.debug("TextView.saveAs"); //$NON-NLS-1$
			InputDialog il = new InputDialog(getViewSite().getShell(), Messages.TextView_saveText,
					Messages.TextView_enterTitle, StringUtils.EMPTY, null); // $NON-NLS-1$ //$NON-NLS-2$
			if (il.open() == Dialog.OK) {
				actBrief.setBetreff(il.getValue());
				return actBrief.save(txt.getPlugin().storeToByteArray(), txt.getPlugin().getMimeType());
			}
			return false;
		}

	}

	@Override
	public void activation(boolean mode) {
		if (mode == false) {
			if (actBrief != null) {
				actBrief.save(txt.getPlugin().storeToByteArray(), txt.getPlugin().getMimeType());
			}
			// txt.getPlugin().clear();
		} else {
			loadSysTemplateAction.setEnabled(AccessControlServiceHolder.get()
					.evaluate(EvACE.of(IDocumentLetter.class, Right.CREATE).and(Right.UPDATE).and(Right.EXECUTE)));
			saveTemplateAction.setEnabled(AccessControlServiceHolder.get()
					.evaluate(EvACE.of(IDocumentLetter.class, Right.CREATE).and(Right.UPDATE)));
		}
	}

	@Override
	public void visible(boolean mode) {

	}

	public void setName() {
		String n = StringUtils.EMPTY;
		if (actBrief == null) {
			setPartName(Messages.TextView_noLetterSelected); // $NON-NLS-1$
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

	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState) {
		CoreUiUtil.updateFixLayout(part, currentState);
	}

}
