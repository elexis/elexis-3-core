/*******************************************************************************
 * Copyright (c) 2005-2012, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 	  M. Descher - added executable link type
 *    H. Marlovits - added BOOL/BOOLTRISTATE resp CHECHBOX/CHECKBOXTRISTATE
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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
		TEXT, BOOL, BOOLTRISTATE, LIST, LINK, DATE, MONEY, COMBO, EXECLINK
	};
	
	Label lbl;
	Control ctl;
	FormToolkit tk = UiDesk.getToolkit();
	Typ inputFieldType;
	
	/**
	 * simply creates a LabeledInputField of Type {@link LabeledInputField.Typ.TEXT}
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
	 *            the type of field to create. One of {@link LabeledInputField.Typ}
	 */
	public LabeledInputField(Composite parent, String label, Typ typ){
		super(parent, SWT.NONE);
		setLayout(new GridLayout(1, false));
		
		this.inputFieldType = typ;
		
		lbl = new Label(this, SWT.BOLD);
		switch (typ) {
		case BOOL:
		case BOOLTRISTATE:
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
		case BOOL:
			ctl = tk.createButton(this, label, SWT.CHECK);
			((Button) ctl).setText(label);
			ctl.setBackground(this.getBackground());
			ctl.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			break;
		case BOOLTRISTATE:
			ctl = tk.createButton(this, label, SWT.CHECK);
			((Button) ctl).setText(label);
			((Button) ctl).addSelectionListener(new TristateSelectionListener());
			ctl.setBackground(this.getBackground());
			ctl.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			break;
		case EXECLINK:
			lbl.setForeground(UiDesk.getColorRegistry().get(UiDesk.COL_BLUE));
			ctl = tk.createText(this, "", SWT.BORDER);
			ctl.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			break;
		default:
			break;
		}
		lbl.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
	}
	
	/**
	 * Sets the item's text. For List, Combo and RadioGroup it looks for the right item and selects
	 * it. For CheckBoxes you can set "0" or "1". For CheckboxTristate you can set "" for
	 * "undefined".
	 * 
	 * @param text
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
			Button myself = (Button) ctl;
			text = StringTool.unNull(text);
			if (inputFieldType == Typ.BOOL) {
				myself.setSelection(text.equalsIgnoreCase("1") ? true : false);
			} else if (inputFieldType == Typ.BOOLTRISTATE) {
				setTristateStringValue(myself, text);
			}
		}
	}
	
	/**
	 * get the String/the selected item of the control
	 * 
	 * @return
	 */
	public String getText(){
		if (ctl instanceof Text) {
			return ((Text) ctl).getText();
		} else if (ctl instanceof List) {
			List list = (List) ctl;
			String[] sel = list.getSelection();
			if (sel.length == 0) {
				return "";
			} else {
				return StringTool.join(sel, ",");
			}
		} else if (ctl instanceof Combo) {
			return ((Combo) ctl).getText();
		} else if (ctl instanceof DatePickerCombo) {
			return ((DatePickerCombo) ctl).getText();
		} else if (ctl instanceof Button) {
			Button myself = (Button) ctl;
			if (inputFieldType == Typ.BOOL) {
				return (myself.getSelection() == true) ? "1" : "0";
			} else if (inputFieldType == Typ.BOOLTRISTATE) {
				return getTristateStringValue(myself);
			}
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
		case BOOL:
		case BOOLTRISTATE:
			((Button) ctl).setText(text);
			break;
		default:
			lbl.setText(text);
			break;
		}
	}
	
	/**
	 * gets the lable for the LabeledInputField
	 * 
	 * @return
	 */
	public String getLabel(){
		switch (inputFieldType) {
		case BOOL:
		case BOOLTRISTATE:
			return ((Button) ctl).getText();
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
	};
	
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
					ltf = addComponent(def[i].sAnzeige, LabeledInputField.Typ.BOOL);
				} else if (typ == InputData.Typ.CHECKBOXTRISTATE) {
					ltf = addComponent(def[i].sAnzeige, LabeledInputField.Typ.BOOLTRISTATE);
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
									(((Button) (inp.mine.getControl())).getSelection() == true) ? "1"
											: "0";
								break;
							case CHECKBOXTRISTATE:
								val = getTristateStringValue(((Button) (inp.mine.getControl())));
								if (val == null)
									val = "";
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
					((Button) (def[i].mine.getControl())).setSelection(val.equalsIgnoreCase("1"));
					break;
				case CHECKBOXTRISTATE:
					setTristateStringValue(((Button) (def[i].mine.getControl())), val);
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
	
	/**
	 * a class for implementing a tristate checkbox. Just add this as a selection listener to the
	 * checkbox. The states cycle through empty -> checked -> unchecked
	 * 
	 * @author H. Marlovits
	 * 
	 */
	class TristateSelectionListener implements SelectionListener {
		TristateSelectionListener(){}
		
		@Override
		public void widgetSelected(SelectionEvent e){
			Button button = ((Button) e.getSource());
			boolean selection = !button.getSelection();
			boolean grayed = button.getGrayed();
			if (selection) {
				if (grayed) {
					button.setSelection(true);
					button.setGrayed(false);
				} else {
					button.setSelection(false);
					button.setGrayed(false);
				}
			} else {
				button.setSelection(true);
				button.setGrayed(true);
			}
		}
		
		@Override
		public void widgetDefaultSelected(SelectionEvent e){}
	}
	
	/**
	 * returns the selected value of a tristate button.
	 * 
	 * @param checkBox
	 *            the checkbox to test for
	 * @return 0 for "not selected, 1 for "selected", -1 for "not defined", -2 on error
	 */
	public static int getTristateValue(Button checkBox){
		String result = getTristateStringValue(checkBox);
		if (result.equalsIgnoreCase(""))
			return -1;
		if (result.equalsIgnoreCase("1"))
			return 1;
		if (result.equalsIgnoreCase("0"))
			return 0;
		return -2;
	}
	
	/**
	 * returns the selected value of a tristate button.
	 * 
	 * @param checkBox
	 *            the checkbox to test for
	 * @return "0" for "not selected, "1" for "selected", "" for "not defined", null on error
	 */
	public static String getTristateStringValue(Button checkBox){
		if (checkBox == null)
			return null;
		// test if this is a Button
		if (!checkBox.getClass().getSimpleName().equalsIgnoreCase("button"))
			return null;
		// test if this is a checkbox
		if ((checkBox.getStyle() & SWT.CHECK) == 0)
			return null;
		boolean selection = checkBox.getSelection();
		boolean grayed = checkBox.getGrayed();
		if (selection) {
			if (grayed) {
				return "";
			} else {
				return "1";
			}
		} else {
			return "0";
		}
	}
	
	/**
	 * sets the selected value of a tristate button.
	 * 
	 * @param checkBox
	 *            the checkbox for which to set the value
	 * @param value
	 *            new new value "0" = set deselected; "1" = set selected, everything else set undef
	 * @return true on success, false on error
	 */
	public static boolean setTristateStringValue(Button checkBox, String value){
		if (checkBox == null)
			return false;
		// test if this is a Button
		if (!checkBox.getClass().getSimpleName().equalsIgnoreCase("button"))
			return false;
		// test if this is a checkbox
		if ((checkBox.getStyle() & SWT.CHECK) == 0)
			return false;
		// set the right values
		if (value == null) {
			checkBox.setSelection(true);
			checkBox.setGrayed(false);
		} else if (value.equalsIgnoreCase("1")) {
			checkBox.setSelection(true);
			checkBox.setGrayed(false);
		} else if (value.equalsIgnoreCase("0")) {
			checkBox.setSelection(false);
			checkBox.setGrayed(false);
		} else {
			checkBox.setSelection(true);
			checkBox.setGrayed(true);
		}
		return true;
	}
}
