package ch.elexis.core.ui.preferences.inputs;


import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;

import ch.rgw.tools.TimeTool;

/**
 * A field editor for an date preference.
 */
public class DateTimeFieldEditor extends FieldEditor {

	/**
	 * Text limit constant (value <code>-1</code>) indicating unlimited text
	 * limit and width.
	 */
	public static int UNLIMITED = -1;

	/**
	 * Old text value.
	 */
	private long oldValue;

	/**
	 * The dateTime field, or <code>null</code> if none.
	 */
	DateTime dateTimeField;

	Button checkControl;

	/**
	 * Width of text field in characters; initially unlimited.
	 */
	private int widthInChars = UNLIMITED;

	/**
	 * The name of the preference displayed in this field editor.
	 */
	private String preferenceNameEnabled;
	
	/**
	 * Use String in format TimeTool.COMPACT_DATE in prefs 
	 */
	private boolean useStringInPref;

	/**
	 * Creates a new date field editor
	 */
	protected DateTimeFieldEditor() {
		// do nothing
	}

	/**
	 * Creates a string field editor of unlimited width. Use the method
	 * <code>setTextLimit</code> to limit the text.
	 * 
	 * @param name
	 *            the name of the preference this field editor works on
	 * @param labelText
	 *            the label text of the field editor
	 * @param parent
	 *            the parent of the field editor's control
	 * @param useStringInPref uses string in format TimeTool.COMPACT_DATE in prefs            
	 */
	public DateTimeFieldEditor(String name, String labelText, Composite parent , boolean useStringInPref) {
		this(name, labelText, UNLIMITED, parent, useStringInPref);
	}

	/**
	 * Creates a string field editor of unlimited width. Use the method
	 * <code>setTextLimit</code> to limit the text.
	 * 
	 * @param name
	 *            the name of the preference this field editor works on
	 * @param nameEnabled
	 *            the name of the preference to store if the field is enabled
	 * @param labelText
	 *            the label text of the field editor
	 * @param parent
	 *            the parent of the field editor's control
	 * @param useStringInPref uses string in format TimeTool.COMPACT_DATE in prefs            
	 */
	public DateTimeFieldEditor(String name, String nameEnabled, String labelText, Composite parent, boolean useStringInPref) {
		this(name, nameEnabled, labelText, UNLIMITED, parent, useStringInPref);
	}

	/**
	 * Creates a string field editor. Use the method <code>setTextLimit</code>
	 * to limit the text.
	 * 
	 * @param name
	 *            the name of the preference this field editor works on
	 * @param labelText
	 *            the label text of the field editor
	 * @param width
	 *            the width of the text input field in characters, or
	 *            <code>UNLIMITED</code> for no limit
	 * @param strategy
	 *            either <code>VALIDATE_ON_KEY_STROKE</code> to perform on the
	 *            fly checking (the default), or
	 *            <code>VALIDATE_ON_FOCUS_LOST</code> to perform validation
	 *            only after the text has been typed in
	 * @param parent
	 *            the parent of the field editor's control
	 * @param useStringInPref uses string in format TimeTool.COMPACT_DATE in prefs 
	 * @since 2.0
	 */
	public DateTimeFieldEditor(String name, String labelText, int width, Composite parent, boolean useStringInPref) {
		this(name, null, labelText, width, parent, useStringInPref);
	}

	/**
	 * Creates a string field editor. Use the method <code>setTextLimit</code>
	 * to limit the text.
	 * 
	 * @param name
	 *            the name of the preference this field editor works on
	 * @param nameEnabled
	 *            the name of the preference to store if the field is enabled
	 * @param labelText
	 *            the label text of the field editor
	 * @param width
	 *            the width of the text input field in characters, or
	 *            <code>UNLIMITED</code> for no limit
	 * @param strategy
	 *            either <code>VALIDATE_ON_KEY_STROKE</code> to perform on the
	 *            fly checking (the default), or
	 *            <code>VALIDATE_ON_FOCUS_LOST</code> to perform validation
	 *            only after the text has been typed in
	 * @param parent
	 *            the parent of the field editor's control
	 * @param useStringInPref uses string in format TimeTool.COMPACT_DATE in prefs 
	 * @since 2.0
	 */
	public DateTimeFieldEditor(String name, String nameEnabled, String labelText, int width, Composite parent, boolean useStringInPref) {
		init(name, labelText);
		this.preferenceNameEnabled = nameEnabled;
		this.widthInChars = width;
		this.useStringInPref = useStringInPref;
		createControl(parent);
	}

	/**
	 * Sets the name of the preference to store if the field is enabled.
	 * 
	 * @param name
	 *            the name of the preference
	 */
	public void setPreferenceNameEnabled(String name) {
		this.preferenceNameEnabled = name;
	}

	/**
	 * Returns the name of the preference to store if the field is enabled.
	 * 
	 * @return the name of the preference
	 */
	public String getPreferenceNameEnabled() {
		return this.preferenceNameEnabled;
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	protected void adjustForNumColumns(int numColumns) {
		GridData gd = (GridData) this.dateTimeField.getLayoutData();
		gd.horizontalSpan = numColumns - 2;
		// We only grab excess space if we have to
		// If another field editor has more columns then
		// we assume it is setting the width.
		gd.grabExcessHorizontalSpace = gd.horizontalSpan == 1;
	}

	/**
	 * Fills this field editor's basic controls into the given parent.
	 * <p>
	 * The string field implementation of this <code>FieldEditor</code>
	 * framework method contributes the text field. Subclasses may override but
	 * must call <code>super.doFillIntoGrid</code>.
	 * </p>
	 */
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		getLabelControl(parent);

		Composite composite = new Composite(parent, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = numColumns - 2;
		if (this.widthInChars != UNLIMITED) {
			GC gc = new GC(this.dateTimeField);
			try {
				Point extent = gc.textExtent("X");//$NON-NLS-1$
				gd.widthHint = this.widthInChars * extent.x;
			} finally {
				gc.dispose();
			}
		} else {
			gd.horizontalAlignment = GridData.FILL;
			gd.grabExcessHorizontalSpace = true;
		}
		composite.setLayoutData(gd);

		boolean displayCheckControl = getPreferenceNameEnabled() != null;

		GridLayout gridLayout = new GridLayout(displayCheckControl ? 2 : 1, false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		composite.setLayout(gridLayout);

		if (displayCheckControl) {
			this.checkControl = new Button(composite, SWT.CHECK);
			this.checkControl.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
			this.checkControl.setSelection(true);
			this.checkControl.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					valueChanged();
				}
			});
		}

		this.dateTimeField = getDateTimeControl(composite);
		this.dateTimeField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	protected void doLoad() {
		if (this.dateTimeField != null) {
			long value = getLongByPrefName(getPreferenceName(), false);
			doLoad(value);
			if (this.checkControl != null) {
				this.checkControl.setSelection(getPreferenceStore().getBoolean(getPreferenceNameEnabled()));
			}
			this.oldValue = value;
			valueChanged();
		}
	}
	
	private Date getDateByPrefName(String prefName) {
		if (useStringInPref) {
			String val = getPreferenceStore().getString(prefName);
			TimeTool t = new TimeTool(val);
			return t.getGregorianChange();
		} else {
			return new Date(getPreferenceStore().getLong(prefName));
		}
	}
	
	private long getLongByPrefName(String prefName, boolean isDefault){
		if (useStringInPref) {
			String val = isDefault ? getPreferenceStore().getDefaultString(prefName)
					: getPreferenceStore().getString(prefName);
			TimeTool t = new TimeTool(val);
			return t.getTimeAsLong();
		} else {
			return getPreferenceStore().getLong(prefName);
		}
	}
	
	private void doStore(long value){
		if (useStringInPref) {
			getPreferenceStore().setValue(getPreferenceName(), new TimeTool(getValue()).toString(TimeTool.DATE_COMPACT));
		} else {
			getPreferenceStore().setValue(getPreferenceName(), getValue());
		}
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	protected void doLoadDefault() {
		if (this.dateTimeField != null) {
			long value = getLongByPrefName(getPreferenceName(), true);
			doLoad(value);
			if (this.checkControl != null) {
				this.checkControl.setSelection(getPreferenceStore().getDefaultBoolean(getPreferenceNameEnabled()));
			}
		}
		valueChanged();
	}

	protected void doLoad(long value) {
		setValue(value);
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	protected void doStore() {
		doStore(getValue());
		if (this.checkControl != null) {
			getPreferenceStore().setValue(getPreferenceNameEnabled(), this.checkControl.getSelection());
		}
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	public int getNumberOfControls() {
		return 2;
	}

	/**
	 * Returns the field editor's value.
	 * 
	 * @return the current value
	 */
	public Date getDateValue() {
		if (this.dateTimeField != null) {
			return new Date(getValue());
		}
		return getDateByPrefName(getPreferenceName());
	}

	/**
	 * Returns if the date control is enabled.
	 * 
	 * @return the current value
	 */
	public boolean getSelection() {
		if (this.checkControl != null) {
			return this.checkControl.getSelection();
		}
		return getPreferenceStore().getBoolean(getPreferenceNameEnabled());
	}

	/**
	 * Returns this field editor's text control.
	 * 
	 * @return the text control, or <code>null</code> if no text field is
	 *         created yet
	 */
	protected DateTime getDateTimeControl() {
		return this.dateTimeField;
	}

	/**
	 * Returns this field editor's text control.
	 * <p>
	 * The control is created if it does not yet exist
	 * </p>
	 * 
	 * @param parent
	 *            the parent
	 * @return the text control
	 */
	public DateTime getDateTimeControl(Composite parent) {
		if (this.dateTimeField == null) {
			this.dateTimeField = new DateTime(parent, SWT.DATE | SWT.MEDIUM);
			this.dateTimeField.setFont(parent.getFont());
			this.dateTimeField.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					valueChanged();
				}
			});
			this.dateTimeField.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					DateTimeFieldEditor.this.dateTimeField = null;
				}
			});
		} else {
			checkParent(this.dateTimeField, parent);
		}
		return this.dateTimeField;
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	public void setFocus() {
		if (this.dateTimeField != null) {
			this.dateTimeField.setFocus();
		}
	}

	/**
	 * Sets this field editor's value.
	 * 
	 * @param value
	 *            the new value, or <code>null</code> meaning the empty string
	 */
	public void setDateValue(long value) {
		if (this.dateTimeField != null) {
			this.oldValue = getValue();
			if (this.oldValue != value) {
				setValue(value);
				valueChanged();
			}
		}
	}

	/**
	 * Sets this field editor enabled or disabled.
	 * 
	 * @param value
	 *            the new value
	 */
	public void setSelection(boolean selection) {
		if (this.checkControl != null) {
			this.checkControl.setSelection(selection);
			valueChanged();
		}
	}

	private long getValue() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, this.dateTimeField.getDay());
		calendar.set(Calendar.MONTH, this.dateTimeField.getMonth());
		calendar.set(Calendar.YEAR, this.dateTimeField.getYear());
		return calendar.getTimeInMillis();
	}

	private void setValue(long value) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(value);
		this.dateTimeField.setDay(calendar.get(Calendar.DAY_OF_MONTH));
		this.dateTimeField.setMonth(calendar.get(Calendar.MONTH));
		this.dateTimeField.setYear(calendar.get(Calendar.YEAR));
	}

	/**
	 * Informs this field editor's listener, if it has one, about a change to
	 * the value (<code>VALUE</code> property) provided that the old and new
	 * values are different.
	 * <p>
	 * This hook is <em>not</em> called when the text is initialized (or reset
	 * to the default value) from the preference store.
	 * </p>
	 */
	protected void valueChanged() {
		setPresentsDefaultValue(false);

		if (this.checkControl != null) {
			this.dateTimeField.setEnabled(this.checkControl.getSelection());
		}

		long newValue = getValue();
		if (newValue != this.oldValue) {
			fireValueChanged(VALUE, new Long(this.oldValue), new Long(newValue));
			this.oldValue = newValue;
		}
	}

	/*
	 * @see FieldEditor.setEnabled(boolean,Composite).
	 */
	public void setEnabled(boolean enabled, Composite parent) {
		super.setEnabled(enabled, parent);
		if (this.checkControl != null) {
			this.checkControl.setEnabled(enabled);
		}
		getDateTimeControl(parent).setEnabled(enabled);
	}

}
