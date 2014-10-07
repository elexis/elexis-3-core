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

package ch.elexis.core.ui.text;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.actions.ActionFactory;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.text.model.SSDRange;
import ch.elexis.core.text.model.Samdas;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.preferences.UserTextPref;
import ch.elexis.core.ui.util.IKonsExtension;
import ch.elexis.core.ui.util.IKonsMakro;
import ch.elexis.core.ui.util.PersistentObjectDropTarget;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Konsultation;
import ch.rgw.tools.GenericRange;
import ch.rgw.tools.StringTool;

/**
 * Ein StyledText mit erweiterten Eigenschaften. Kann XML-Dokumente von SAmDaS-Typ lesen. Aus
 * Kompatibiltätsgründen können auch reine Texteinträge gelesen werden, werden beim Speichern aber
 * nach XML gewandelt.
 * 
 * @author Gerry
 * 
 */
public class EnhancedTextField extends Composite implements IRichTextDisplay {
	public static final String MACRO_ENABLED = "enhancedtextfield/macro_enabled"; //$NON-NLS-1$
	public static final String MACRO_KEY = "enhancedtextfield/macro_key"; //$NON-NLS-1$
	public static final String MACRO_KEY_DEFAULT = "$"; //$NON-NLS-1$
	
	StyledText text;
	Map<String, IKonsExtension> hXrefs;
	ETFDropReceiver dropper;
	private List<Samdas.XRef> links;
	private List<Samdas.Markup> markups;
	private List<Samdas.Range> ranges;
	Samdas samdas;
	Samdas.Record record;
	boolean dirty;
	MenuManager menuMgr;
	private Konsultation actKons;
	private static Pattern outline = Pattern.compile("^\\S+:", Pattern.MULTILINE); //$NON-NLS-1$
	private static Pattern bold = Pattern.compile("\\*\\S+\\*"); //$NON-NLS-1$
	private static Pattern italic = Pattern.compile("\\/\\S+\\/"); //$NON-NLS-1$
	private static Pattern underline = Pattern.compile("_\\S+_"); //$NON-NLS-1$
	private IAction copyAction, cutAction, pasteAction;
	private IMenuListener globalMenuListener;
	private final ElexisEventListener eeli_user = new UserChangeListener();
	private List<IKonsMakro> externalMakros;
	
	public void setExternalMakros(List<IKonsMakro> makros){
		externalMakros = makros;
	}
	
	@Override
	public void setXrefHandlers(Map<String, IKonsExtension> xrefs){
		hXrefs = xrefs;
	}
	
	@Override
	public void addXrefHandler(String id, IKonsExtension xref){
		if (hXrefs == null) {
			hXrefs = new Hashtable<String, IKonsExtension>();
		}
		hXrefs.put(id, xref);
	}
	
	/**
	 * Only needed for billing macros
	 * 
	 * @param k
	 *            kons to bill, can be null then billing macros are disabled
	 */
	
	public void setKons(Konsultation k){
		actKons = k;
	}
	
	public void connectGlobalActions(IViewSite site){
		makeActions();
		IActionBars actionBars = site.getActionBars();
		actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), copyAction);
		actionBars.setGlobalActionHandler(ActionFactory.CUT.getId(), cutAction);
		actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(), pasteAction);
		globalMenuListener = new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager){
				if (text.getSelectionCount() == 0) {
					copyAction.setEnabled(false);
					cutAction.setEnabled(false);
				} else {
					copyAction.setEnabled(true);
					cutAction.setEnabled(true);
				}
				
			}
		};
		// TODO
		// ApplicationActionBarAdvisor.editMenu.addMenuListener(globalMenuListener);
		ElexisEventDispatcher.getInstance().addListeners(eeli_user);
	}
	
	public void disconnectGlobalActions(IViewSite site){
		IActionBars actionBars = site.getActionBars();
		actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), null);
		actionBars.setGlobalActionHandler(ActionFactory.CUT.getId(), null);
		actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(), null);
		// TODO
		// ApplicationActionBarAdvisor.editMenu.removeMenuListener(globalMenuListener);
		ElexisEventDispatcher.getInstance().removeListeners(eeli_user);
		
	}
	
	@Override
	public void addDropReceiver(Class clazz, IKonsExtension ext){
		dropper.addReceiver(clazz, ext);
	}
	
	public void removeDropReceiver(Class clazz, IKonsExtension ext){
		dropper.removeReceiver(clazz, ext);
	}
	
	public EnhancedTextField(final Composite parent){
		super(parent, SWT.NONE);
		setLayout(new GridLayout());
		text = new StyledText(this, SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		text.setFont(UiDesk.getFont(Preferences.USR_DEFAULTFONT));
		text.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		text.addVerifyListener(new ETFVerifyListener());
		text.addVerifyKeyListener(new ShortcutListener(this));
		dropper = new ETFDropReceiver(this);
		menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			
			@Override
			public void menuAboutToShow(IMenuManager manager){
				manager.add(GlobalActions.cutAction);
				manager.add(GlobalActions.copyAction);
				manager.add(GlobalActions.pasteAction);
				manager.add(new Separator());
				manager.add(new Action(Messages.EnhancedTextField_asMacro) {
					String tx;
					{
						tx = text.getSelectionText();
						if (StringTool.isNothing(tx)) {
							setEnabled(false);
						} else {
							setEnabled(true);
						}
					}
					
					@Override
					public void run(){
						
						InputDialog in =
							new InputDialog(parent.getShell(), Messages.EnhancedTextField_newMacro,
								Messages.EnhancedTextField_enterNameforMacro, null, null);
						if (in.open() == Dialog.OK) {
							StringBuilder name = new StringBuilder(in.getValue());
							name.reverse();
							CoreHub.userCfg.set("makros/" + name, tx); //$NON-NLS-1$
						}
					}
					
				});
				if (hXrefs != null) {
					boolean bAdditions = false;
					for (IKonsExtension k : hXrefs.values()) {
						IAction[] acs = k.getActions();
						if (acs != null) {
							for (IAction ac : acs) {
								manager.add(ac);
								bAdditions = true;
							}
						}
					}
					if (bAdditions) {
						manager.add(new Action(Messages.EnhancedTextField_RemoveXref) {
							Samdas.XRef actRef = null;
							{
								setEnabled(false);
								int cp = text.getCaretOffset();
								actRef = findLinkRef(cp);
								if (actRef != null) {
									setEnabled(true);
								}
							}
							
							@Override
							public void run(){
								List<Samdas.XRef> xrefs = record.getXrefs();
								Samdas.XRef eRemove = null;
								for (Samdas.XRef xref : xrefs) {
									if ((xref.getProvider().equals(actRef.getProvider()))
										&& (xref.getID().equals(actRef.getID()))) {
										IKonsExtension ex = hXrefs.get(actRef.getProvider());
										if (ex != null) {
											eRemove = xref;
											text.replaceTextRange(actRef.getPos(),
												actRef.getLength(), StringTool.leer);
											ex.removeXRef(actRef.getProvider(), actRef.getID());
										}
									}
									
								}
								record.remove(eRemove);
								doFormat(getContentsAsXML());
							}
							
						});
					}
				}
			}
		});
		Menu menu = menuMgr.createContextMenu(text);
		text.setMenu(menu);
		text.setWordWrap(true);
		text.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseDoubleClick(MouseEvent e){
				// System.out.println("Line="+e.y/text.getLineHeight());
				// System.out.println("Caret="+text.getCaretOffset());
				if (e.button != 1) {
					super.mouseDoubleClick(e);
				} else {
					if (links != null) {
						try {
							int ch = text.getOffsetAtLocation(new Point(e.x, e.y));
							Samdas.XRef lr = findLinkRef(ch);
							if (lr != null) {
								IKonsExtension xr = hXrefs.get(lr.getProvider());
								xr.doXRef(lr.getProvider(), lr.getID());
							}
							
						} catch (IllegalArgumentException iax) {
							/* Klick ausserhalb des Textbereichs: egal */
						}
					}
				}
			}
		});
		text.addExtendedModifyListener(new RangeTracker());
		new PersistentObjectDropTarget(text, dropper);
		
		dirty = false;
	}
	
	public boolean isDirty(){
		return dirty;
	}
	
	public void setDirty(boolean d){
		dirty = d;
	}
	
	/**
	 * Text formatieren (d.h. Style-Ranges erstellen. Es wird unterschieden zwischen dem KG-Eintrag
	 * alten Stils und dem neuen XML-basierten format.
	 */
	void doFormat(String tx){
		text.setStyleRange(null);
		if (tx.startsWith("<")) { //$NON-NLS-1$
			doFormatXML(tx);
			tx = text.getText();
		} else {
			samdas = new Samdas(tx);
			record = samdas.getRecord();
			text.setText(tx);
		}
		
		// Überschriften formatieren
		
		// obsoleted by markups!
		Matcher matcher = outline.matcher(tx);
		while (matcher.find() == true) {
			StyleRange n = new StyleRange();
			n.start = matcher.start();
			n.length = matcher.end() - n.start;
			n.fontStyle = SWT.BOLD;
			text.setStyleRange(n);
		}
		
		matcher = bold.matcher(tx);
		while (matcher.find() == true) {
			StyleRange n = new StyleRange();
			n.start = matcher.start();
			n.length = matcher.end() - n.start;
			n.fontStyle = SWT.BOLD;
			text.setStyleRange(n);
		}
		matcher = italic.matcher(tx);
		while (matcher.find() == true) {
			StyleRange n = new StyleRange();
			n.start = matcher.start();
			n.length = matcher.end() - n.start;
			n.fontStyle = SWT.ITALIC;
			text.setStyleRange(n);
		}
		
		matcher = underline.matcher(tx);
		while (matcher.find() == true) {
			StyleRange n = new StyleRange();
			n.start = matcher.start();
			n.length = matcher.end() - n.start;
			n.underline = true;
			text.setStyleRange(n);
		}
		// Obsoleted, do not rely
	}
	
	void doFormatXML(String tx){
		samdas = new Samdas(tx);
		record = samdas.getRecord();
		List<Samdas.XRef> xrefs = record.getXrefs();
		text.setText(record.getText());
		int textlen = text.getCharCount();
		markups = record.getMarkups();
		links = new ArrayList<Samdas.XRef>(xrefs.size());
		ranges = new ArrayList<Samdas.Range>(xrefs.size() + markups.size());
		for (Samdas.Markup m : markups) {
			String type = m.getType();
			StyleRange n = new StyleRange();
			n.start = m.getPos();
			n.length = m.getLength();
			if (type.equalsIgnoreCase("emphasized")) { //$NON-NLS-1$
				n.strikeout = true;
			} else if (type.equalsIgnoreCase("bold")) { //$NON-NLS-1$
				n.fontStyle = SWT.BOLD;
			} else if (type.equalsIgnoreCase("italic")) { //$NON-NLS-1$
				n.fontStyle = SWT.ITALIC;
			} else if (type.equalsIgnoreCase("underlined")) { //$NON-NLS-1$
				n.underline = true;
			}
			if ((n.start + n.length) > textlen) {
				n.length = textlen - n.start;
			}
			if ((n.length > 0) && (n.start >= 0)) {
				text.setStyleRange(n);
				ranges.add(m);
			} else {
				// fehlerhaftes Markup entfernen.
				record.remove(m);
			}
			
		}
		if (hXrefs != null) {
			for (Samdas.XRef xref : xrefs) {
				IKonsExtension xProvider = hXrefs.get(xref.getProvider());
				if (xProvider == null) {
					continue;
				}
				StyleRange n = new StyleRange();
				n.start = xref.getPos();
				n.length = xref.getLength();
				if (xProvider.doLayout(n, xref.getProvider(), xref.getID()) == true) {
					links.add(xref);
				}
				
				if ((n.start + n.length) > text.getCharCount()) {
					n.length = text.getCharCount() - n.start;
				}
				if ((n.length > 0) && (n.start >= 0)) {
					text.setStyleRange(n);
					ranges.add(xref);
				} else {
					xref.setPos(0);
				}
			}
		}
		
	}
	
	/**
	 * Querverweis einfügen.
	 * 
	 * @param pos
	 *            Einfügeposition im Text oder -1: An Caretposition
	 * @param string
	 *            der einzufügende Bezeichner.
	 * @param provider
	 *            XRef-Provider wie beim Extensionpoint XREf angegeben
	 * @param id
	 *            vom Provider vergebene Identifikation für diesen Querverweis (beliebiger String)
	 */
	@Override
	public void insertXRef(int pos, String string, String provider, String id){
		if (pos == -1) {
			pos = text.getCaretOffset();
		} else {
			text.setCaretOffset(pos);
		}
		int len = string.trim().length();
		text.insert(string);
		record.setText(text.getText());
		
		Samdas.XRef xref = new Samdas.XRef(provider, id, pos, len);
		record.add(xref);
		setDirty(true);
		doFormat(getContentsAsXML());
	}
	
	/**
	 * Markup erstellen
	 * 
	 * @param type
	 *            '*' bold, '/' italic, '_', underline
	 */
	public void createMarkup(char type, int pos, int len){
		String typ = "bold"; //$NON-NLS-1$
		switch (type) {
		case '/':
			typ = "italic"; //$NON-NLS-1$
			break;
		case '_':
			typ = "underline"; //$NON-NLS-1$
			break;
		}
		Samdas.Markup markup = new Samdas.Markup(pos, len, typ);
		record.add(markup);
		doFormat(getContentsAsXML());
	}
	
	/**
	 * Den Text mit len zeichen ab start durch nt ersetzen
	 */
	public void replace(int start, int len, String nt){
		text.replaceTextRange(start, len, nt);
	}
	
	class ETFVerifyListener implements VerifyListener {
		@Override
		public void verifyText(VerifyEvent e){
			
			// if(e.text.length()<2){ wieso das??? weiss nicht mehr, was ich
			// damit wollte
			dirty = true;
			// }
			
			String macroKey = CoreHub.userCfg.get(MACRO_KEY, MACRO_KEY_DEFAULT);
			
			// Wenn der macroKey gedrückt wurde, das Wort rückwärts von der
			// aktuellen Position
			// bis zum letzten whitespace scannen.
			if (e.text.equals(macroKey)) {
				StringBuilder s = new StringBuilder();
				int start = e.start;
				while (--start >= 0) {
					String t = text.getTextRange(start, 1);
					if (t.matches("\\S")) { //$NON-NLS-1$
						s.append(t);
					} else {
						break;
					}
				}
				// Dann prüfen, ob dieses Wort einem Makronamen entspricht
				String code = s.toString();
				String comp = CoreHub.userCfg.get("makros/" + code, null); //$NON-NLS-1$
				if (comp != null) { // Ja -> Makri umwandeln
					start += 1;
					text.replaceTextRange(start, (e.end - start), comp);
					e.doit = false;
					doFormat(getContentsAsXML());
					text.setCaretOffset(start + comp.length());
				} else { // Nein -> aufruf externer makros
					start += 1;
					String makro = s.reverse().toString();
					StringBuilder replace = new StringBuilder();
					
					for (IKonsMakro extMakro : externalMakros) {
						if (isMakroEnabled(extMakro)) {
							replace.append(extMakro.executeMakro(makro));
						}
					}
					
					text.replaceTextRange(start, (e.end - start), replace.toString());
					e.doit = false;
					doFormat(getContentsAsXML());
					text.setCaretOffset(start + replace.toString().length());
					ElexisEventDispatcher.update(actKons);
				}
				// Wenn ein : gedrückt wurde, prüfen, ob es ein Wort am
				// Zeilenanfang ist und ggf.
				// fett formatieren.
			} else if (e.text.equals(":")) { //$NON-NLS-1$
				int lineStart = text.getOffsetAtLine(text.getLineAtOffset(e.start));
				String line = text.getText(lineStart, e.start - 1);
				if (line.matches("^\\S+")) { //$NON-NLS-1$
					/*
					 * StyleRange n=new StyleRange(); n.start=lineStart; n.length=line.length();
					 * n.fontStyle=SWT.BOLD; text.setStyleRange(n);
					 */
					createMarkup('*', lineStart, line.length());
				}
			}
			// Wenn ein *, _ oder / gedrückt wurde, prüfen, ob vor dem aktuellen
			// Wort dasselbe
			// Zeichen steht
			// und wenn ja, entsprechende Formatierung anwenden.
			else if (e.text.matches("[\\*/_]")) { //$NON-NLS-1$
				int start = e.start;
				String t = ""; //$NON-NLS-1$
				while (--start >= 0) {
					t = text.getTextRange(start, 1);
					if (t.equals(e.text)) {
						createMarkup(t.charAt(0), start, e.start - start);
						e.doit = true;
						break;
					}
					if (t.matches(Messages.EnhancedTextField_5)) {
						break;
					}
				}
				/*
				 * e.doit=true; Desk.theDisplay.asyncExec(new Runnable(){
				 * 
				 * public void run() { int off=text.getCaretOffset();
				 * actKons.updateEintrag(getDocumentAsText(), false); setDirty(false);
				 * //GlobalEvents.getInstance().fireObjectEvent(actKons,
				 * GlobalEvents.CHANGETYPE.update); setText(getDocumentAsText());
				 * text.setCaretOffset(off); }t });
				 */
				
			}
			
		}
		
		private boolean isMakroEnabled(IKonsMakro extMakro){
			UserTextPref.setMakroEnabledDefaults();
			return CoreHub.userCfg.get(EnhancedTextField.MACRO_ENABLED + "/"
				+ extMakro.getClass().getName(), false);
		}
		
	}
	
	public void setText(String ntext){
		doFormat(ntext);
		setDirty(false);
	}
	
	public void putCaretToEnd(){
		text.setCaretOffset(text.getCharCount());
		text.setFocus();
	}
	
	/**
	 * Alle Änderungen seit dem letzten speichern zurücknehmen
	 * 
	 * @TODO: multi-undo
	 */
	public void undo(){
		XMLOutputter xo = new XMLOutputter(Format.getRawFormat());
		String oldText = xo.outputString(samdas.getDocument());
		setText(oldText);
	}
	
	/**
	 * Liefert das dem Textfeld zugrundeliegende Samdas
	 */
	public Samdas getContents(){
		return samdas;
	}
	
	/**
	 * Liefert den Inhalt des Textfields als jdom-Document zurück
	 */
	public Document getDocument(){
		record.setText(text.getText());
		// StyleRange[] rgs=text.getStyleRanges();
		return samdas.getDocument();
	}
	
	/**
	 * Liefert den Inhalt des Textfelds als XML-Text zurück
	 */
	@Override
	public String getContentsAsXML(){
		XMLOutputter xo = new XMLOutputter(Format.getRawFormat());
		return xo.outputString(getDocument());
	}
	
	/**
	 * Liefert den Selektierten Inhalt des Textfelds zurück
	 * 
	 * @return Den Selektierten Text, <code>String.empty</code> falls nichts ausgewählt
	 */
	public String getSelectedText(){
		return text.getSelectionText();
	}
	
	/**
	 * Gibt das Wort des Inhalts zurück das durch den Cursor berührt wird
	 * 
	 * @return Das mit dem Cursor berührte Wort des Textfelds, <code>String.empty</code> falls kein
	 *         Wort berührt wird
	 */
	@Override
	public String getWordUnderCursor(){
		return StringTool.getWordAtIndex(text.getText(), text.getCaretOffset());
	}
	
	Samdas.XRef findLinkRef(int cp){
		Samdas.XRef ret = null;
		if (links != null) {
			for (Samdas.XRef lr : links) {
				if ((lr.getPos() <= cp) && ((lr.getPos() + lr.getLength()) >= cp)) {
					ret = lr;
					break;
				}
			}
		}
		return ret;
	}
	
	/**
	 * Liefert das zugrundeliegende Text-Control zurueck
	 * 
	 * @return das zugrundeliegende Text-Control
	 */
	public Control getControl(){
		return text;
	}
	
	/**
	 * Wenn Änderungen des Texts stattfinden, müssen unsere xref- und markup- EInträge ggf
	 * mitverschoben werden. Leider können wir dazu nicht die sowieso immer nachgeführten
	 * StyleRanges verwenden, weil StyledText da immer nur Kopien rausgibt :-(
	 * 
	 * @author gerry
	 * 
	 */
	class RangeTracker implements ExtendedModifyListener {
		
		@Override
		public void modifyText(ExtendedModifyEvent event){
			if (ranges != null) {
				int pos = event.start;
				int len = event.length;
				String text = event.replacedText;
				int diff = len - text.length();
				for (Samdas.Range r : ranges) {
					int spos = r.getPos();
					if (spos >= pos) {
						r.setPos(spos + diff);
					}
				}
			}
		}
		
	}
	
	private void makeActions(){
		// copyAction=ActionFactory.COPY.create();
		cutAction = new Action(Messages.EnhancedTextField_cutAction) {
			@Override
			public void run(){
				text.cut();
			}
			
		};
		pasteAction = new Action(Messages.EnhancedTextField_pasteAction) {
			@Override
			public void run(){
				text.paste();
			}
		};
		copyAction = new Action(Messages.EnhancedTextField_copyAction) {
			@Override
			public void run(){
				text.copy();
			}
		};
		
	}
	
	class UserChangeListener implements ElexisEventListener {
		ElexisEvent filter = new ElexisEvent(null, null, ElexisEvent.EVENT_USER_CHANGED);
		
		@Override
		public void catchElexisEvent(ElexisEvent ev){
			UiDesk.asyncExec(new Runnable() {
				@Override
				public void run(){
					text.setFont(UiDesk.getFont(Preferences.USR_DEFAULTFONT));
					
				}
			});
		}
		
		@Override
		public ElexisEvent getElexisEventFilter(){
			return filter;
		}
		
	}
	
	@Override
	public String getContentsPlaintext(){
		return text.getText();
	}
	
	@Override
	public GenericRange getSelectedRange(){
		Point pt = text.getSelection();
		GenericRange gr = new GenericRange(pt.x);
		gr.setEnd(pt.y);
		return gr;
	}
	
	@Override
	public void insertRange(SSDRange range){
		// TODO Auto-generated method stub
		
	}
	
}
