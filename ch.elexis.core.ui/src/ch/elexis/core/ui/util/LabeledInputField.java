/*******************************************************************************
 * Copyright (c) 2005-2014, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 	  M. Descher - added executable link type
 *    H. Marlovits - added CHECKBOX/CHECKBOXTRISTATE
 *******************************************************************************/
package ch.elexis.core.ui.util;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.FormToolkit;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Money;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

import com.tiff.common.ui.datepicker.DatePickerCombo;

/**
 * Ein Ein/Ausgabeelement, das aus einem Kästchen mit darin einem Label und darunter einem Control
 * zur Eingabe besteht. Eignet sich besonders zur Verwendung in einem ColumnLayout.
 * 
 * @author Gerry
 * 
 */
public class LabeledInputField extends Composite {
	static public enum Typ {
		TEXT, CHECKBOX, CHECKBOXTRISTATE, LIST, LINK, DATE, MONEY, COMBO, EXECLINK
	};
	
	Label lbl;
	Control ctl;
	FormToolkit tk = UiDesk.getToolkit();
	Typ inputFieldType;
	
	/**
	 * simply creates a LabeledInputField of Type LabeledInputField.Typ.TEXT}
	 * 
	 * @param parent
	 * @param label
	 *            the label to show above the field
	 */
	public LabeledInputField(Composite parent, String label){
		this(parent, label, Typ.TEXT);
	}
	
	/**
	 * creates a LabeledInputField of the desired Type.
	 * 
	 * @param parent
	 * @param label
	 *            the label to show above the field
	 * @param typ
	 *            the type of field to create. One of LabeledInputField.Typ
	 */
	public LabeledInputField(Composite parent, String label, Typ typ){
		super(parent, SWT.NONE);
		setLayout(new GridLayout(1, false));
		
		this.inputFieldType = typ;
		
		lbl = new Label(this, SWT.BOLD);
		switch (typ) {
		case CHECKBOX:
		case CHECKBOXTRISTATE:
			// just a a spacer for nice alignment - label is shown behind the checkbox as usual
			break;
		default:
			lbl.setText(label);
			break;
		}
		
		switch (typ) {
		case LINK:
			lbl.setForeground(UiDesk.getColorRegistry().get(UiDesk.COL_BLUE)); //$NON-NLS-1$
			ctl = tk.createText(this, "", SWT.NONE);
			ctl.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			break;
		case TEXT:
		case MONEY:
			ctl = tk.createText(this, "", SWT.BORDER);
			ctl.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			break;
		case LIST:
			ctl = new List(this, SWT.MULTI | SWT.BORDER);
			ctl.setLayoutData(new GridData(GridData.FILL_BOTH/* |GridData.GRAB_VERTICAL */));
			break;
		case DATE:
			ctl = new DatePickerCombo(this, SWT.NONE);
			ctl.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			setText("");
			break;
		case COMBO:
			ctl = new Combo(this, SWT.SINGLE | SWT.BORDER);
			ctl.setLayoutData(new GridData(GridData.FILL_BOTH/* |GridData.GRAB_VERTICAL */));
			break;
		case CHECKBOX:
			ctl = tk.createButton(this, label, SWT.CHECK);
			((Button) ctl).setText(label);
			ctl.setBackground(this.getBackground());
			ctl.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			break;
		case CHECKBOXTRISTATE:
			ctl = new TristateCheckbox(this, SWT.NONE, true);
			((Button) ctl).setText(label);
			ctl.setBackground(this.getBackground());
			ctl.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			break;
		case EXECLINK:
			lbl.setForeground(UiDesk.getColorRegistry().get(UiDesk.COL_BLUE));
			ctl = tk.createText(this, StringConstants.EMPTY, SWT.BORDER);
			ctl.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			break;
		default:
			break;
		}
		lbl.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
	}
	
	/**
	 * Sets the item's text for TEXT, LIST, LINK, DATE, MONEY, COMBO, EXECLINK. For COMBO and LIST
	 * it sets the new text only if present as a possible selection. For BOOL, BOOLTRISTATE it sets
	 * the label of the control.
	 * 
	 * @param text
	 *            the text to be set
	 * 
	 */
	public void setText(String text){
		if (ctl instanceof Text) {
			((Text) ctl).setText(text);
		} else if (ctl instanceof List) {
			List list = (List) ctl;
			list.deselectAll();
			if (!StringTool.isNothing(text)) {
				String[] sel = text.split(",");
				int[] selidx = new int[sel.length];
				String[] items = list.getItems();
				for (int i = 0; i < sel.length; i++) {
					int idx = StringTool.getIndex(items, sel[i]);
					if (idx != -1) {
						selidx[i] = idx;
					}
				}
				list.select(selidx);
			}
		} else if (ctl instanceof Combo) {
			Combo combo = (Combo) ctl;
			if (!StringTool.isNothing(text)) {
				int idx = StringTool.getIndex(combo.getItems(), text);
				if (idx != -1) {
					combo.select(idx);
				}
			}
		} else if (ctl instanceof DatePickerCombo) {
			DatePickerCombo dp = (DatePickerCombo) ctl;
			dp.setDate(new TimeTool(text).getTime());
		} else if (ctl instanceof Button) {
			((Button) ctl).setText(text);
		}
	}
	
	/**
	 * get the String/the selected item/text for TEXT, MONEY, LINK, EXECLINK, LIST, COMBO, DATE. For
	 * BOOL, BOOLTRISTATE it returns the label of the control.
	 * 
	 * @return
	 */
	public String getText(){
		if (ctl instanceof Text) {
			// for TEXT, MONEY, LINK, EXECLINK
			return ((Text) ctl).getText();
		} else if (ctl instanceof List) {
			List list = (List) ctl;
			String[] sel = list.getSelection();
			if (sel.length == 0) {
				return "";
			} else {
				return StringTool.join(sel, StringConstants.COMMA);
			}
		} else if (ctl instanceof Combo) {
			return ((Combo) ctl).getText();
		} else if (ctl instanceof DatePickerCombo) {
			return ((DatePickerCombo) ctl).getText();
		} else if (ctl instanceof Button) {
			return ((Button) ctl).getText();
		}
		return "";
	}
	
	/**
	 * sets the label for the LabeledInputField
	 * 
	 * @param text
	 *            the new label
	 */
	public void setLabel(String text){
		switch (inputFieldType) {
		case CHECKBOX:
			((Button) ctl).setText(text);
		case CHECKBOXTRISTATE:
			((TristateCheckbox) ctl).setText(text);
			break;
		default:
			lbl.setText(text);
			break;
		}
	}
	
	/**
	 * gets the label for the LabeledInputField
	 * 
	 * @return the label for this LabeledInputField
	 */
	public String getLabel(){
		switch (inputFieldType) {
		case CHECKBOX:
			return ((Button) ctl).getText();
		case CHECKBOXTRISTATE:
			return ((TristateCheckbox) ctl).getText();
		default:
			return lbl.getText();
		}
	}
	
	public Label getLabelComponent(){
		return lbl;
	}
	
	public Control getControl(){
		return ctl;
	}
	
	public static class Tableau extends Composite {
		public Tableau(Composite parent, int minColumns, int maxColumns){
			super(parent, SWT.BORDER);
			ColumnLayout cl = new ColumnLayout();
			cl.maxNumColumns = maxColumns;
			cl.minNumColumns = minColumns;
			setLayout(cl);
		}
		
		public Tableau(Composite parent){
			this(parent, 1, 5);
			
		}
		
		public LabeledInputField addComponent(String l){
			return new LabeledInputField(this, l);
		}
		
		public LabeledInputField addComponent(String l, LabeledInputField.Typ typ){
			return new LabeledInputField(this, l, typ);
		}
	}
	
	/**
	 * class for storing display details for use with LabeledInputField
	 */
	public static class InputData {
		public enum Typ {
			STRING, INT, CURRENCY, LIST, HYPERLINK, DATE, COMBO, EXECSTRING, CHECKBOX,
				CHECKBOXTRISTATE
		};
		
		String sAnzeige, sFeldname, sHashname;
		Typ tFeldTyp;
		Object ext;
		LabeledInputField mine;
		
		/**
		 * create control of different types.
		 * 
		 * @param anzeige
		 *            String, the label shown above the field
		 * @param feldname
		 *            the database field name
		 * @param feldtyp
		 *            field type to use, one of InputData.Typ
		 * @param hashname
		 *            the name of the field in the hashfield, set to null if feldname is not a hash
		 */
		public InputData(String anzeige, String feldname, Typ feldtyp, String hashname){
			sAnzeige = anzeige;
			sFeldname = feldname;
			tFeldTyp = feldtyp;
			sHashname = hashname;
		}
		
		/**
		 * create control of type STRING. label and fieldType are the same
		 * 
		 * @param all
		 *            the fieldname, also used for the label
		 */
		public InputData(String all){
			sAnzeige = all;
			sFeldname = all;
			tFeldTyp = Typ.STRING;
			sHashname = null;
		}
		
		/**
		 * create control of type HYPERLINK
		 * 
		 * @param anzeige
		 *            String, the label shown above the field
		 * @param feldname
		 *            the database field name
		 * @param cp
		 *            an IContentProvider used to display the contents
		 */
		public InputData(String anzeige, String feldname, IContentProvider cp){
			sAnzeige = anzeige;
			sFeldname = feldname;
			ext = cp;
			tFeldTyp = Typ.HYPERLINK;
		}
		
		/**
		 * Provide an executable link input field; that is, on click on the resp. label (like the
		 * hyperlink), an executable can be called by a callback on cp
		 * 
		 * @param anzeige
		 *            the name of the label (will be clickable, calling cp)
		 * @param feldname
		 *            the field name
		 * @param feldtyp
		 *            the field type
		 * @param cp
		 *            executable callback
		 * @author M. Descher
		 */
		public InputData(String anzeige, String feldname, IExecLinkProvider cp){
			sAnzeige = anzeige;
			sFeldname = feldname;
			sHashname = null;
			ext = cp;
			tFeldTyp = Typ.EXECSTRING;
		}
		
		/**
		 * create control of type LIST
		 * 
		 * @param anzeige
		 *            String, the label shown above the field
		 * @param feldname
		 *            the database field name
		 * @param hashname
		 *            the name of the field in the hashfield, set to null if feldname is not a hash
		 * @param choices
		 *            the items to be displayed in the list
		 */
		public InputData(String anzeige, String feldname, String hashname, String[] choices){
			sAnzeige = anzeige;
			sFeldname = feldname;
			sHashname = hashname;
			tFeldTyp = Typ.LIST;
			ext = choices;
		}
		
		/**
		 * create control of type COMBO
		 * 
		 * @param anzeige
		 *            String, the label shown above the field
		 * @param feldname
		 *            the database field name
		 * @param hashname
		 *            the name of the field in the hashfield, set to null if feldname is not a hash
		 * @param comboItems
		 *            the items to be displayed in the combo
		 * @param bDropDown
		 *            just to select this method - not used actually...
		 */
		public InputData(String anzeige, String feldname, String hashname, String[] comboItems,
			boolean bDropDown){
			sAnzeige = anzeige;
			sFeldname = feldname;
			sHashname = hashname;
			tFeldTyp = Typ.COMBO;
			ext = comboItems;
		}
		
		public void setParent(LabeledInputField p){
			mine = p;
		}
		
		public void setLabel(String lbl){
			if (mine != null) {
				mine.setLabel(lbl);
			}
		}
		
		public String getLabel(){
			return mine == null ? "" : mine.getLabel();
		}
		
		public String getText(){
			return mine == null ? "" : mine.getText();
		}
		
		public void setText(String t){
			if (mine != null) {
				mine.setText(t);
			}
		}
		
		public LabeledInputField getWidget(){
			return mine;
		}
		
		public void setEditable(boolean ed){
			mine.ctl.setEnabled(ed);
		}
		
		public void setChoices(String... strings){
			ext = strings;
		}
	}
	
	/**
	 * Create an automatically maintained form out of an array of InpuData[].<br>
	 * Usage: <code><nowrap><br><ul>
	 * InputData[] id=new InputData[]{ // ... };<br>
	 * TableWrapLayout twl=new TableWrapLayout();<br>
	 * setLayout(twl);<br>
	 * AutoForm af=new LabeledInputField.AutoForm(parent,id));<br>
	 * TableWrapData twd=new TableWrapData(TableWrapData.FILL_GRAB);<br>
	 * twd.grabHorizontal=true;<br>af.setLayoutData(twd);</ul><br></code>
	 */
	public static class AutoForm extends Tableau {
		InputData[] def;
		Control[] cFields;
		PersistentObject act;
		DecimalFormat df = new DecimalFormat(Messages.LabeledInputField_7); //$NON-NLS-1$
		LabeledInputField ltf;
		
		public AutoForm(Composite parent, InputData[] fields){
			this(parent, fields, 1, 5);
		}
		
		public AutoForm(Composite parent, InputData[] fields, int minColumns, int maxColumns){
			super(parent, minColumns, maxColumns);
			def = fields;
			cFields = new Control[def.length];
			for (int i = 0; i < def.length; i++) {
				ltf = null;
				InputData.Typ typ = def[i].tFeldTyp;
				if (typ == InputData.Typ.LIST) {
					ltf = addComponent(def[i].sAnzeige, LabeledInputField.Typ.LIST);
					((List) ltf.getControl()).setItems((String[]) def[i].ext);
				} else if (typ == InputData.Typ.COMBO) {
					ltf = addComponent(def[i].sAnzeige, LabeledInputField.Typ.COMBO);
					((Combo) ltf.getControl()).setItems((String[]) def[i].ext);
				} else if (typ == InputData.Typ.CHECKBOX) {
					ltf = addComponent(def[i].sAnzeige, LabeledInputField.Typ.CHECKBOX);
				} else if (typ == InputData.Typ.CHECKBOXTRISTATE) {
					ltf = addComponent(def[i].sAnzeige, LabeledInputField.Typ.CHECKBOXTRISTATE);
				} else if (typ == InputData.Typ.EXECSTRING) {
					ltf = addComponent(def[i].sAnzeige, LabeledInputField.Typ.EXECLINK);
					ltf.lbl.setData(i);
					ltf.lbl.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseDown(MouseEvent e){
							Label l = (Label) e.getSource();
							int i = (Integer) l.getData();
							((IExecLinkProvider) def[i].ext).executeString(def[i]);
							super.mouseDown(e);
						}
					});
				} else {
					if (typ == InputData.Typ.HYPERLINK) {
						ltf = addComponent(def[i].sAnzeige, LabeledInputField.Typ.LINK);
						ltf.lbl.setData(i);
						ltf.lbl.addMouseListener(new MouseAdapter() {
							@Override
							public void mouseDown(MouseEvent e){
								Label l = (Label) e.getSource();
								int i = (Integer) l.getData();
								((IContentProvider) def[i].ext).reloadContent(act, def[i]);
								super.mouseDown(e);
							}
							
						});
						((Text) ltf.ctl).setEditable(false);
					} else {
						ltf = addComponent(def[i].sAnzeige);
					}
					
				}
				def[i].setParent(ltf);
				cFields[i] = ltf.getControl();
				cFields[i].addFocusListener(new FocusListener() {
					public void focusGained(FocusEvent e){}
					
					@SuppressWarnings("unchecked")
					public void focusLost(FocusEvent e){
						if (act != null) {
							Control src = (Control) e.getSource();
							InputData inp = (InputData) src.getData();
							String val = StringTool.leer;
							switch (inp.tFeldTyp) {
							
							case STRING:
							case COMBO:
							case INT:
							case EXECSTRING:
							case DATE:
								val = inp.getText();
								break;
							case CURRENCY:
								try {
									Money money = new Money(inp.getText());
									val = money.getCentsAsString();
								} catch (ParseException e1) {
									ExHandler.handle(e1);
									val = "";
								}
								// double betr=Double.parseDouble(inp.getText())*100.0;
								// val=Long.toString(Math.round(betr));
								break;
							case LIST:
								val = inp.getText();
								break;
							case CHECKBOX:
								val =
									(((Button) (inp.mine.getControl())).getSelection() == true) ? StringConstants.ONE
											: StringConstants.ZERO;
								break;
							case CHECKBOXTRISTATE:
								val =
									((TristateCheckbox) (inp.mine.getControl()))
										.getTristateDbValue();
								break;
							default:
								break;
							}
							if (inp.sHashname == null) {
								act.set(inp.sFeldname, val);
							} else {
								Map ext = act.getMap(inp.sFeldname);
								ext.put(inp.sHashname, val);
								act.setMap(inp.sFeldname, ext);
							}
						}
					}
				});
				cFields[i].setData(def[i]);
			}
		}
		
		/**
		 * Angezeigte Daten aus DB neu laden
		 * 
		 * @param o
		 *            Das Objekt aus dem Daten geladen werden
		 */
		public void reload(PersistentObject o){
			act = o;
			if (o == null) {
				for (int i = 0; i < def.length; i++) {
					def[i].setText(StringTool.leer);
				}
				return;
			}
			act = o;
			String val = StringTool.leer;
			for (int i = 0; i < def.length; i++) {
				if (def[i].tFeldTyp == InputData.Typ.HYPERLINK) {
					((IContentProvider) def[i].ext).displayContent(o, def[i]);
					continue;
				} else {
					if (def[i].sHashname == null) {
						val = o.get(def[i].sFeldname);
					} else {
						Map ext = o.getMap(def[i].sFeldname);
						val = (String) ext.get(def[i].sHashname);
						if (val == null) {
							val = o.get(def[i].sHashname);
						}
					}
				}
				switch (def[i].tFeldTyp) {
				case STRING:
				case INT:
				case LIST:
				case COMBO:
				case EXECSTRING:
				case DATE:
					if (StringTool.isNothing(val)) {
						val = StringTool.leer;
					}
					def[i].setText(val);
					break;
				case CURRENCY:
					Money money = new Money(PersistentObject.checkZero(val));
					def[i].setText(money.getAmountAsString());
					// double betr=PersistentObject.checkZeroDouble(val);
					
					// def[i].setText(Double.toString(betr/100.0));
					break;
				case CHECKBOX:
					val = StringTool.unNull(val);
					((Button) (def[i].mine.getControl())).setSelection(val
						.equalsIgnoreCase(StringConstants.ONE));
					break;
				case CHECKBOXTRISTATE:
					val = StringTool.unNull(val);
					((TristateCheckbox) (def[i].mine.getControl())).setTristateDbValue(val);
					break;
				}
			}
		}
	}
	
	public interface IContentProvider {
		/** fetch the Content from the defining PersistentObject and display it in ltf */
		public void displayContent(PersistentObject po, InputData ltf);
		
		/** Let the user modify the content and load in in po and ltf */
		public void reloadContent(PersistentObject po, InputData ltf);
	}
	
	public interface IExecLinkProvider {
		/** Execute the string within the InputData */
		public void executeString(InputData ltf);
	}
}
