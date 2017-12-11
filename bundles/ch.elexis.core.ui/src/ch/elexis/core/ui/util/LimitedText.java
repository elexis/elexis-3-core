package ch.elexis.core.ui.util;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * A SWT text widget that allows passing a max. allowed text length. It will decorate the textfield
 * in case the max. length is exceeded. By passing a control it will automatically be
 * enabled/disabled depending on the textfields validity
 * 
 * @author lucia
 *
 */
public class LimitedText {
	private ControlDecoration decorator;
	private Control control;
	private Text txt;
	private int limit;
	
	/**
	 * 
	 * @param parent
	 * @param style
	 * @param maxLength
	 *            max. allowed text length
	 */
	public LimitedText(Composite parent, int style, int maxLength){
		this(parent, style, maxLength, null);
	}
	
	/**
	 * 
	 * @param parent
	 * @param style
	 * @param maxLength
	 *            max. allowed text length
	 * @param disableControl
	 *            control to enable/disable dependent on {@link LimitedText}
	 */
	public LimitedText(Composite parent, int style, int maxLength, Control disableControl){
		this.control = disableControl;
		this.limit = maxLength;
		
		txt = new Text(parent, style);
		txt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		// update decorator and control enabling
		txt.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e){
				if (txt.getText().length() > limit) {
					decorator.show();
					enableControl(false);
				} else {
					decorator.hide();
					enableControl(true);
				}
			}
		});
		// stop user from typing more letters
		txt.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e){
				// limit reached
				if (txt.getText().length() > limit) {
					// only allow modification if backspace (8) or del (127) key was pressed
					if (e.keyCode == 8 || e.keyCode == 127) {
						e.doit = true;
					} else {
						e.doit = false;
					}
				}
			}
		});
		
		// initialize the decorator
		decorator = new ControlDecoration(txt, SWT.TOP | SWT.LEFT);
		decorator.setDescriptionText(Messages.LimitedText_MaxLengthReached);
		Image errorImg =
			FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage();
		decorator.setImage(errorImg);
	}
	
	private void enableControl(boolean enable){
		if (control == null) {
			return;
		}
		control.setEnabled(enable);
	}
	
	public void setDisableControl(Control control){
		this.control = control;
	}
	
	public void setText(String text){
		txt.setText(text);
	}
	
	public String getText(){
		return txt.getText();
	}
	
}
