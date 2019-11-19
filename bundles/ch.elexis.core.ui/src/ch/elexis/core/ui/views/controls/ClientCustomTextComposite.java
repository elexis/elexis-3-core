/*******************************************************************************
 * Copyright (c) 2012 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.views.controls;

import java.awt.Desktop;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.dialogs.AddBuchungDialog;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;

import ch.rgw.tools.StringTool;
public class ClientCustomTextComposite extends Composite {
	
	private StyledText txtClientCustomText;
	private Button btnEditCustomText;
	private DropTarget txtClientCustomTextdropTarget;
	private List<TokenMap> tokenMap = new ArrayList<TokenMap>();
	private List<StyleRange> styleList = new ArrayList<StyleRange>();
	private ScrolledForm scrldfrm;
	
	public ClientCustomTextComposite(Composite parent, int style, FormToolkit toolkit,
		ScrolledForm scrldfrm){
		super(parent, style);
		this.scrldfrm = scrldfrm;
		setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP, 1, 1));
		TableWrapLayout twl = new TableWrapLayout();
		twl.numColumns = 2;
		setLayout(twl);
		
		txtClientCustomText = new StyledText(this, SWT.WRAP | SWT.MULTI);
		TableWrapData twd_txtClientCustomText =
			new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 1, 1);
		twd_txtClientCustomText.valign = TableWrapData.MIDDLE;
		twd_txtClientCustomText.grabHorizontal = true;
		txtClientCustomText.setLayoutData(twd_txtClientCustomText);
		txtClientCustomText.setEditable(false);
		txtClientCustomText.setDoubleClickEnabled(false);
		txtClientCustomText.addListener(SWT.Modify, new StyledTextMultiLineAutoGrowListener());
		txtClientCustomText.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e){
				// "execute" the value, if applicable
				StyledText st = (StyledText) e.widget;
				TokenMap token = getTokenFromCarePosition(st.getCaretOffset());
				if (token == null)
					return;
				String tokenAttribute = token.token.split("\\.")[1];
				if (tokenAttribute.equals(Kontakt.FLD_E_MAIL)) {
					try {
						URI uriMailTo = new URI("mailto", token.value, null);
						Desktop.getDesktop().mail(uriMailTo);
					} catch (Exception xe) {}
				} else if (tokenAttribute.equals("Balance")) {
					final Patient actPatient = ElexisEventDispatcher.getSelectedPatient();
					if (new AddBuchungDialog(getShell(), actPatient).open() == Dialog.OK) {
						updateClientCustomArea();
					}
				}
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e){
				// edit the value
				StyledText st = (StyledText) e.widget;
				String token = getTokenFromCarePosition(st.getCaretOffset()).token;
				if (token != null) {
					ClientCustomTextTokenEditDialog cctted =
						new ClientCustomTextTokenEditDialog(PlatformUI.getWorkbench().getDisplay()
							.getActiveShell(), token);
					cctted.open();
					updateClientCustomArea();
				}
			}
		});
		
		txtClientCustomTextdropTarget = new DropTarget(txtClientCustomText, DND.DROP_COPY);
		txtClientCustomTextdropTarget.setTransfer(new Transfer[] {
			TextTransfer.getInstance()
		});
		txtClientCustomTextdropTarget.addDropListener(new DropTargetAdapter() {
			@Override
			public void dragEnter(DropTargetEvent event){
				if (btnEditCustomText.getSelection()) {
					event.detail = DND.DROP_COPY;
				} else {
					event.detail = DND.DROP_NONE;
				}
			}
			
			@Override
			public void drop(DropTargetEvent event){
				txtClientCustomText.setText(txtClientCustomText.getText() + event.data);
			}
		});
		
		btnEditCustomText = toolkit.createButton(this, StringTool.leer, SWT.TOGGLE);
		TableWrapData twd_btnEditCustomText =
			new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 1, 1);
		twd_btnEditCustomText.valign = TableWrapData.MIDDLE;
		btnEditCustomText.setLayoutData(twd_btnEditCustomText);
		btnEditCustomText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				if (btnEditCustomText.getSelection()) {
					txtClientCustomText.setBackground(UiDesk.getDisplay().getSystemColor(
						SWT.COLOR_INFO_BACKGROUND));
				} else {
					txtClientCustomText.setBackground(null);
				}
				txtClientCustomText.setEditable(btnEditCustomText.getSelection());
				setClientCustomAreaContent(btnEditCustomText.getSelection());
			}
		});
		btnEditCustomText.setImage(Images.IMG_PENCIL_8PX.getImage());
		GridData gd_btnEditCustomText = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
		gd_btnEditCustomText.heightHint = 10;
		gd_btnEditCustomText.widthHint = 10;
		
		if (toolkit != null) {
			toolkit.adapt(this);
			toolkit.adapt(txtClientCustomText, true, true);
			toolkit.adapt(btnEditCustomText, false, false);
		}
	}
	
	private void setClientCustomAreaContent(boolean editMode){
		if (editMode) {
			// Edit the variable usage
			txtClientCustomText.setText(CoreHub.globalCfg.get(
				ClientCustomTextComposite.class.getName(), StringTool.leer));
		} else {
			CoreHub.globalCfg.set(ClientCustomTextComposite.class.getName(),
				txtClientCustomText.getText());
			updateClientCustomArea();
		}
		
	}
	
	public void updateClientCustomArea(){
		tokenMap.clear();
		styleList.clear();
		String outputParsed =
			findAndReplaceTemplates(CoreHub.globalCfg.get(
				ClientCustomTextComposite.class.getName(), StringTool.leer));
		String output = initializeStyleRanges(outputParsed);
		txtClientCustomText.setText(output);
		txtClientCustomText.setStyleRanges(styleList.toArray(new StyleRange[0]));
	}
	
	/**
	 * parse the format relevant tokens and add them to the StyledRange entries
	 * 
	 * @param output
	 * @return
	 */
	private String initializeStyleRanges(String output){
		// TODO produces error as we need to remove ALL formatting tokens so conflicted
		// with tokenMap, need to rework this
		// StringBuilder sb = new StringBuilder();
		// char[] rs = output.toCharArray();
		//
		// boolean boldActivated = false;
		// StyleRange tempStyleRange = null;
		//
		// for (int i = 0; i < rs.length; i++) {
		// switch (rs[i]) {
		// case '*':
		// if (boldActivated) {
		// tempStyleRange.length = i - tempStyleRange.start;
		// styleList.add(tempStyleRange);
		// boldActivated = false;
		// tempStyleRange = null;
		// } else {
		// tempStyleRange = new StyleRange();
		// tempStyleRange.fontStyle = SWT.BOLD;
		// tempStyleRange.start = i;
		// boldActivated = true;
		// }
		// break;
		// default:
		// sb.append(rs[i]);
		// break;
		// }
		// }
		
		// automatic styles for FLD_EMAIL and FLD_BALANCE
		for (TokenMap tm : tokenMap) {
			String tokenAttribute = tm.token.split("\\.")[1];
			if (tokenAttribute.equals(Kontakt.FLD_E_MAIL) || tokenAttribute.equals("Balance")) {
				StyleRange sr = new StyleRange();
				sr.start = tm.start;
				sr.length = tm.end - tm.start;
				sr.foreground =
					PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_BLUE);
				styleList.add(sr);
			}
		}
		return output;
		// return sb.toString();
	}
	
	/**
	 * Finds templates in the form of "[class.property]" in a string and returns a string with the
	 * respective replaced values
	 * 
	 * @param replaceString
	 *            the string containing the wild-cards
	 * @return string with instantiated wild-cards
	 */
	private String findAndReplaceTemplates(String replaceString){
		StringBuilder sb = new StringBuilder();
		char[] rs = replaceString.toCharArray();
		StringBuilder replace = new StringBuilder();
		TokenMap tempTM = new TokenMap();
		boolean variable = false;
		
		for (int i = 0; i < rs.length; i++) {
			switch (rs[i]) {
			case '[':
				tempTM = new TokenMap();
				tempTM.start = sb.length();
				replace = new StringBuilder();
				variable = true;
				break;
			case ']':
				variable = false;
				tempTM.token = replace.toString();
				tempTM.value = replaceValue(replace.toString());
				sb.append(replaceValue(replace.toString()));
				tempTM.end = sb.length();
				tokenMap.add(tempTM);
				tempTM = new TokenMap();
				break;
			default:
				if (variable) {
					replace.append(rs[i]);
				} else {
					sb.append(rs[i]);
				}
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * Replace an occurence of "class.property" with the real value
	 * 
	 * @param string
	 * @return COMPAT Code -- ugly
	 */
	private String replaceValue(String replace){
		String[] arr = replace.split("\\.");
		if (arr == null || arr.length < 2)
			return "ERR";
		if (arr[0].equalsIgnoreCase("Patient")) {
			if (arr[1] == null)
				return StringTool.leer;
			Patient pat = ElexisEventDispatcher.getSelectedPatient();
			if (pat == null)
				return StringTool.leer;
			String result = pat.get(arr[1]);
			return (result != null) ? result : StringTool.leer;
		} else {
			return "ERR";
		}
	}
	
	/**
	 * resolves the position of the caret within the {@link StyledText} widget to the associated
	 * {@link TokenMap}
	 * 
	 * @param caretPosition
	 * @return the {@link TokenMap} or <code>null</code> if none found
	 */
	private TokenMap getTokenFromCarePosition(int caretPosition){
		for (TokenMap tm : tokenMap) {
			if (tm.start <= caretPosition && tm.end >= caretPosition)
				return tm;
		}
		return null;
	}
	
	/**
	 * provide an accessor for the text control (to listen for modifications)
	 * 
	 * @return
	 */
	public StyledText getTxtClientCustomText(){
		return txtClientCustomText;
	}
	
	/**
	 * stores tokens used within the custom text composite. This is used to provide a back-forth
	 * mapping between the values and tokens according to their position.
	 */
	private class TokenMap {
		public int start;
		public int end;
		public String token;
		public String value;
	}
	
	/**
	 * This {@link Listener} automatically grows and shrinks a {@link StyledText} according to the
	 * number of lines contained. It handles {@link SWT#Modify} events only.
	 * 
	 * @see http://stackoverflow.com/questions/8287853/text-widget-with-self-
	 *      adjusting-height-based-on-interactively-entered-text
	 * 
	 */
	private final class StyledTextMultiLineAutoGrowListener implements Listener {
		protected int lines = 0;
		
		@Override
		public void handleEvent(Event event){
			if (event.type != SWT.Modify)
				return;
			if (txtClientCustomText.getLineCount() != lines) {
				lines = txtClientCustomText.getLineCount();
				
				txtClientCustomText.setSize(txtClientCustomText.getSize().x, lines
					* (int) txtClientCustomText.getFont().getFontData()[0].height);
			}
			scrldfrm.reflow(true);
		}
	}
}
