/*******************************************************************************
 * Copyright (c) 2008-2010, G. Weirich and Elexis
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.util.SortedList;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.PersistentObjectDragSource;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.data.Script;
import ch.elexis.scripting.ScriptEditor;
import ch.rgw.tools.ExHandler;

/**
 * Display and edit Beanshell-Scripts
 * 
 * @author gerry
 * 
 */
public class ScriptView extends ViewPart {
	public static final String ID = "ch.elexis.scriptsView"; //$NON-NLS-1$
	private IAction newScriptAction, editScriptAction, removeScriptAction, execScriptAction,
			exportScriptAction, importScriptAction;
	TableViewer tv;
	ScrolledForm form;
	
	public ScriptView(){
		
	}
	
	@Override
	public void createPartControl(Composite parent){
		form = UiDesk.getToolkit().createScrolledForm(parent);
		form.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		form.getBody().setLayout(new FillLayout());
		tv = new TableViewer(form.getBody(), SWT.SINGLE | SWT.FULL_SELECTION);
		tv.setContentProvider(new IStructuredContentProvider() {
			
			@Override
			public Object[] getElements(Object inputElement){
				SortedList<Script> sortedScripts = new SortedList<Script>(new ScriptComparator());
				List<Script> scripts = Script.getScripts();
				for (int i = 0; i < scripts.size(); i++) {
					Script script = scripts.get(i);
					sortedScripts.add(script);
				}
				return sortedScripts.toArray();
			}
			
			@Override
			public void dispose(){}
			
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput){}
		});
		tv.setLabelProvider(new LabelProvider() {
			
			@Override
			public String getText(Object element){
				if (element instanceof Script) {
					return ((Script) element).getLabel();
				} else {
					return element.toString();
				}
			}
			
		});
		new PersistentObjectDragSource(tv);
		makeActions();
		ViewMenus menu = new ViewMenus(getViewSite());
		menu.createToolbar(newScriptAction);
		menu.createViewerContextMenu(tv, editScriptAction, execScriptAction, null,
			exportScriptAction, removeScriptAction);
		menu.createMenu(importScriptAction, newScriptAction);
		tv.setInput(this);
	}
	
	class ScriptComparator implements Comparator<Script> {
		@Override
		public int compare(Script script0, Script script1){
			String script0_ID = ((ch.elexis.data.Script) script0).getId().toUpperCase();
			String script1_ID = ((ch.elexis.data.Script) script1).getId().toUpperCase();
			return script0_ID.compareTo(script1_ID);
		}
	}
	
	@Override
	public void setFocus(){
		// TODO Auto-generated method stub
		
	}
	
	private void makeActions(){
		exportScriptAction = new Action("export script") {
			{
				setImageDescriptor(Images.IMG_EXPORT.getImageDescriptor());
				setToolTipText("export script into a text file");
			}
			
			@Override
			public void run(){
				IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
				if (sel != null && sel.size() != 0) {
					FileDialog fd = new FileDialog(getViewSite().getShell(), SWT.SAVE);
					Script script = (Script) sel.getFirstElement();
					fd.setFileName(script.getLabel());
					String filename = fd.open();
					if (filename != null) {
						try {
							File file = new File(filename);
							FileWriter fw = new FileWriter(file);
							
							fw.write(script.getString());
							fw.close();
						} catch (IOException ex) {
							SWTHelper.showError("IO Error",
								"Could not write file " + filename + " : " + ex.getMessage());
						}
					}
				}
			}
		};
		
		importScriptAction =
			new RestrictedAction(AccessControlDefaults.SCRIPT_EDIT, "Import Script") {
				{
					setImageDescriptor(Images.IMG_IMPORT.getImageDescriptor());
					setToolTipText("Import script from a text file");
				}
				
				@Override
				public void doRun(){
					FileDialog fd = new FileDialog(getViewSite().getShell(), SWT.OPEN);
					String filename = fd.open();
					if (fd != null) {
						try {
							/* Script script= */Script.importFromFile(filename);
							tv.refresh();
						} catch (ElexisException e) {
							SWTHelper.showError("IO Error", e.getMessage());
							
						}
					}
					
				}
				
			};
		newScriptAction = new RestrictedAction(AccessControlDefaults.SCRIPT_EDIT,
			Messages.ScriptView_newScriptAction) { //$NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_NEW.getImageDescriptor());
				setToolTipText(Messages.ScriptView_newScriptTooltip); //$NON-NLS-1$
			}
			
			@Override
			public void doRun(){
				InputDialog inp =
					new InputDialog(getSite().getShell(), Messages.ScriptView_enterNameCaption, //$NON-NLS-1$
						Messages.ScriptView_enterNameBody, null, //$NON-NLS-1$
						null);
				if (inp.open() == Dialog.OK) {
					try {
						Script.create(inp.getValue(), "");
					} catch (ElexisException e) {
						ExHandler.handle(e);
						SWTHelper.showError("Fehler bei Scripterstellung", e.getMessage());
					}
					tv.refresh();
				}
			}
			
		};
		editScriptAction = new RestrictedAction(AccessControlDefaults.SCRIPT_EDIT,
			Messages.ScriptView_editScriptAction) { //$NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_EDIT.getImageDescriptor());
				setToolTipText(Messages.ScriptView_editScriptTooltip); //$NON-NLS-1$
			}
			
			@Override
			public void doRun(){
				IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
				if (sel != null && sel.size() != 0) {
					Script script = (Script) sel.getFirstElement();
					ScriptEditor sce = new ScriptEditor(getSite().getShell(), script.getString(),
						script.getLabel());
					if (sce.open() == Dialog.OK) {
						script.putString(sce.getScript());
					}
				}
				
			}
		};
		removeScriptAction = new RestrictedAction(AccessControlDefaults.SCRIPT_EDIT,
			Messages.ScriptView_deleteScriptAction) { //$NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_DELETE.getImageDescriptor());
				setToolTipText(Messages.ScriptView_deleteScriptTooltip); //$NON-NLS-1$
			}
			
			@Override
			public void doRun(){
				IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
				if (sel != null && sel.size() != 0) {
					Script script = (Script) sel.getFirstElement();
					script.delete();
					tv.refresh();
				}
			}
		};
		execScriptAction = new RestrictedAction(AccessControlDefaults.SCRIPT_EXECUTE,
			Messages.ScriptView_executeScriptAction) { //$NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_GOFURTHER.getImageDescriptor());
				setToolTipText(Messages.ScriptView_executeScriptTooltip); //$NON-NLS-1$
			}
			
			@Override
			public void doRun(){
				IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
				if (sel != null && sel.size() != 0) {
					Script script = (Script) sel.getFirstElement();
					try {
						String contents = script.getString();
						ArrayList<String> vars = new ArrayList<String>();
						Pattern var = Pattern.compile("\\$[0-9a-z]+", Pattern.CASE_INSENSITIVE);
						Matcher m = var.matcher(contents);
						while (m.find()) {
							String varname = m.group();
							if (!vars.contains(varname)) {
								vars.add(varname);
							}
						}
						String varString = null;
						if (vars.size() > 0) {
							SetVarsDlg dlg = new SetVarsDlg(getViewSite().getShell(), vars);
							if (dlg.open() == Dialog.OK) {
								varString = dlg.getResult();
							}
						}
						Object ret = script.execute(varString);
						SWTHelper.showInfo(Messages.ScriptView_ScriptOutput, ret.toString()); //$NON-NLS-1$
					} catch (Exception ex) {
						ExHandler.handle(ex);
						SWTHelper.showError("Fehler beim Ausführen des Scripts", ex.getMessage());
					}
				}
			}
		};
	}
	
	class SetVarsDlg extends TitleAreaDialog {
		List<String> myVars;
		List<Text> inputs;
		String result;
		
		SetVarsDlg(Shell shell, List<String> vars){
			super(shell);
			myVars = vars;
			inputs = new ArrayList<Text>(vars.size());
		}
		
		@Override
		protected Control createDialogArea(Composite parent){
			Composite ret = (Composite) super.createDialogArea(parent);
			Composite cVars = new Composite(ret, SWT.NONE);
			cVars.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			cVars.setLayout(new GridLayout(2, false));
			for (String v : myVars) {
				new Label(cVars, SWT.NONE).setText(v);
				Text text = new Text(cVars, SWT.BORDER);
				text.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
				text.setData("varname", v);
				inputs.add(text);
			}
			return ret;
		}
		
		@Override
		public void create(){
			super.create();
			setMessage("Folgende Variablen sollten gesetzt werden:");
			setTitle("Bitte vervollständigen");
			getShell().setText("Script Ausführung");
		}
		
		@Override
		protected void okPressed(){
			StringBuilder sb = new StringBuilder();
			for (Text text : inputs) {
				String varname = (String) text.getData("varname");
				String varcontents = text.getText();
				sb.append(varname).append("=").append(varcontents).append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
			result = sb.toString();
			super.okPressed();
		}
		
		String getResult(){
			return result;
		}
	}
	
	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT)
	boolean currentState){
		CoreUiUtil.updateFixLayout(part, currentState);
	}
}
