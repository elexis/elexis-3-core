/*******************************************************************************
 * Copyright (c) 2005, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * Helferklasse zur Erzeugung von SWT-Widgets selbständig oder innerhalb einer org.eclipse.ui.forms
 * Form. Die in der Form erstellten Elemente erhalten automatisch das Eclipse-look&feel und fügen
 * sich dadurch gut in Eclipse ein.
 * 
 * @author Gerry
 */
public class WidgetFactory {
	private Composite parent = null;
	private ScrolledForm form = null;
	private FormToolkit tk = null;
	
	/**
	 * Label erzeugen
	 * 
	 * @param parent
	 *            �bergeoirdnetes Composite
	 * @param Text
	 *            Anzuzeigender Text
	 * @return ein Standardlabel mit dem angegebenen Text
	 */
	public static Label createLabel(Composite parent, String Text){
		Label ret = new Label(parent, SWT.NONE);
		ret.setText(Text);
		return ret;
	}
	
	/** Der einzige �ffenliche Konstruktor */
	public WidgetFactory(Composite parent){
		this.parent = parent;
	}
	
	/** Button mit SWT.PUSH erzeugen. Parent wie im Kostruktor der Factory angegeben */
	public Button createPushButton(String Text){
		Button ret = new Button(parent, SWT.PUSH);
		ret.setText(Text);
		return ret;
	}
	
	public Button createCheckbox(String Text){
		Button ret = new Button(parent, SWT.CHECK);
		if (Text != null) {
			ret.setText(Text);
		}
		return ret;
	}
	
	/** Simples Label erzeugen */
	public Label createLabel(String Text){
		Label ret = new Label(parent, SWT.NONE);
		ret.setText(Text);
		return ret;
	}
	
	/** Simples Eingabefeld mit Vorgabetext pre erzeugen */
	public Text createText(String pre){
		Text inp = new Text(parent, SWT.BORDER);
		inp.setText(pre);
		return inp;
	}
	
	/** Parent der WidgetFactory neu setzen */
	public void setParent(Composite parent){
		this.parent = parent;
	}
	
	/** Forms-Form-Objekt erzeugen */
	public void createForm(boolean withBorders){
		if (tk == null) {
			tk = new FormToolkit(parent.getDisplay());
		}
		if (withBorders == true) {
			tk.setBorderStyle(SWT.BORDER);
		}
		form = tk.createScrolledForm(parent);
	}
	
	/** Form-Objekt zurückliefern */
	public ScrolledForm getForm(){
		return form;
	}
	
	/** Body der Form zurückgeben */
	public Composite getBody(){
		return form.getBody();
	}
	
	/** Toolkit, das die Form erzeugt hat liefern */
	public FormToolkit getToolkit(){
		return tk;
	}
	
	/** Toolkit neu setzen */
	public void setToolkit(FormToolkit tk){
		this.tk = tk;
	}
	
	/**
	 * ExpandableComposite (Aufklapp-Feld) in der Form erzeugen
	 * 
	 * @param client
	 *            das Element, das aufgeklappt werden soll
	 * @param Text
	 *            Der Text, der auf dem Composite stehen soll
	 */
	public ExpandableComposite createExpandableComposite(Control client, String Text){
		ExpandableComposite ret =
			tk.createExpandableComposite(form.getBody(), ExpandableComposite.TWISTIE);
		ret.setText(Text);
		client.setParent(ret);
		ret.setClient(client);
		ret.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e){
				form.reflow(true);
			}
		});
		return ret;
	}
	
	public static ExpandableComposite createExpandableComposite(final FormToolkit t,
		final ScrolledForm f, String text){
		ExpandableComposite ret =
			t.createExpandableComposite(f.getBody(), ExpandableComposite.TWISTIE);
		ret.setText(text);
		ret.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e){
				f.reflow(true);
			}
		});
		return ret;
	}
	
	/** Label in der Form erzeugen */
	public Label createFormLabel(String text){
		return tk.createLabel(form.getBody(), text, SWT.WRAP);
	}
	
	/** Texteingabefeld in der Form erzeugen */
	public Text createFormText(String predef, Object align){
		Text ret = tk.createText(form.getBody(), predef);
		
		if (align != null) {
			ret.setLayoutData(align);
		}
		return ret;
	}
	
	/** Mehrzeiliges Textfeld in der Form erzeugen */
	public Text createFormTextField(String predef){
		Text ret = tk.createText(form.getBody(), predef, SWT.MULTI | SWT.WRAP);
		return ret;
	}
	
	/** Button in der Form erzeugen */
	public Button createFormButton(String Text){
		Button ret = tk.createButton(form.getBody(), Text, SWT.PUSH);
		return ret;
	}
	
	/**
	 * Hyperlink in der Form erzeugen
	 * 
	 * @param text
	 *            Angezeigter und anklickbarer Text
	 * @param lis
	 *            HyperlinkListener oder (einfacher) HyperlinkAdapter, der die Klicks verarbeiten
	 *            kann
	 */
	public Hyperlink createHyperlink(String text, IHyperlinkListener lis){
		Hyperlink ret = tk.createHyperlink(form.getBody(), text, SWT.WRAP);
		ret.addHyperlinkListener(lis);
		return ret;
	}
	
	/**
	 * Trennlinie in der Form erzeugen. Achtung:Breite und Höhe muss noch mit entsprechenden
	 * LayoutData festgelegt werden.
	 */
	public Composite createFormSeparator(){
		Composite ret = tk.createCompositeSeparator(form.getBody());
		return ret;
	}
	
	/** Composite in der Form erzeugen */
	public Composite createFormComposite(int style){
		Composite ret = tk.createComposite(form.getBody(), style);
		return ret;
	}
	
	/*
	 * obsolete public static GridData getFillGridData() { GridData ret=new
	 * GridData(GridData.FILL_HORIZONTAL
	 * |GridData.FILL_VERTICAL|GridData.GRAB_HORIZONTAL|GridData.GRAB_VERTICAL); return ret; }
	 * public static GridData getHorzFillGridData(){ return new
	 * GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL); }
	 */
}
