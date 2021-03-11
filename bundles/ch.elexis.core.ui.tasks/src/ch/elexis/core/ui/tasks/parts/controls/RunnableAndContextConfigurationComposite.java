package ch.elexis.core.ui.tasks.parts.controls;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.ITaskService;

public class RunnableAndContextConfigurationComposite
		extends AbstractTaskDescriptorConfigurationComposite {
	
	private ITaskService taskService;
	private IIdentifiedRunnable selectedRunnable;
	
	private Text txtRunnableId;
	private Composite compAssisted;
	private Composite compRaw;
	private Text txtRunContextRaw;
	
	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 * @param taskService
	 */
	public RunnableAndContextConfigurationComposite(Composite parent, int style,
		ITaskService taskService){
		super(parent, style);
		this.taskService = taskService;
		setLayout(new GridLayout(2, false));
		
		Label lblRunnable = new Label(this, SWT.NONE);
		lblRunnable.setText("runnable");
		
		Composite composite = new Composite(this, SWT.NONE);
		GridLayout gl_composite = new GridLayout(2, false);
		gl_composite.marginWidth = 0;
		gl_composite.marginHeight = 0;
		composite.setLayout(gl_composite);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		txtRunnableId = new Text(composite, SWT.BORDER);
		txtRunnableId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Link linkRunnableSelect = new Link(composite, SWT.NONE);
		linkRunnableSelect.setBounds(0, 0, 54, 15);
		linkRunnableSelect.setText("<a>...</a>");
		linkRunnableSelect.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e){
				openRunnableSelectionDialog();
			}
			
		});
		
		TabFolder tabFolder = new TabFolder(this, SWT.BOTTOM);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		TabItem tbtmAssisted = new TabItem(tabFolder, SWT.NONE);
		tbtmAssisted.setText("Assisted");
		
		compAssisted = new Composite(tabFolder, SWT.NONE);
		tbtmAssisted.setControl(compAssisted);
		GridLayout gl_compAssisted = new GridLayout(2, false);
		gl_compAssisted.marginWidth = 0;
		gl_compAssisted.marginHeight = 0;
		compAssisted.setLayout(gl_compAssisted);
		
		TabItem tbtmRaw = new TabItem(tabFolder, SWT.NONE);
		tbtmRaw.setText("Raw");
		
		compRaw = new Composite(tabFolder, SWT.NONE);
		tbtmRaw.setControl(compRaw);
		compRaw.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		txtRunContextRaw = new Text(compRaw, SWT.BORDER | SWT.MULTI);
		
	}
	
	@Override
	public void setSelection(ITaskDescriptor taskDescriptor){
		super.setSelection(taskDescriptor);
		selectedRunnable = null;
		if (taskDescriptor != null) {
			String identifiedRunnableId = taskDescriptor.getIdentifiedRunnableId();
			if (taskService != null && identifiedRunnableId != null) {
				List<IIdentifiedRunnable> identifiedRunnables =
					taskService.getIdentifiedRunnables();
				for (IIdentifiedRunnable iIdentifiedRunnable : identifiedRunnables) {
					if (identifiedRunnableId.equalsIgnoreCase(iIdentifiedRunnable.getId())) {
						selectedRunnable = iIdentifiedRunnable;
						break;
					}
				}
			}
			txtRunnableId.setText(identifiedRunnableId);
			// TODO json runContextRaw
			refreshAssistedConfigurationComposite();
		} else {
			txtRunnableId.setText("");
			txtRunContextRaw.setText("");
		}
	}
	
	private void refreshAssistedConfigurationComposite(){
		Control[] children = compAssisted.getChildren();
		for (Control control : children) {
			control.dispose();
		}
		
		if (selectedRunnable != null) {
			Label description = new Label(compAssisted, SWT.NONE);
			description.setText(selectedRunnable.getLocalizedDescription());
			description.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 2, 1));
			new Label(compAssisted, SWT.NONE);
			new Label(compAssisted, SWT.NONE);
			
			Map<String, Serializable> defaultRunContext = selectedRunnable.getDefaultRunContext();
			Map<String, Serializable> configuredRunContext = taskDescriptor.getRunContext();
			Set<String> keySet = defaultRunContext.keySet();
			for (String key : keySet) {
				Label keyLabel = new Label(compAssisted, SWT.NONE);
				keyLabel.setText(key);
				keyLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
				
				Serializable defaultValue = defaultRunContext.get(key);
				Serializable configuredValue = configuredRunContext.get(key);
				
				if (defaultValue == null) {
					defaultValue = "null";
				}
				
				if (defaultValue instanceof Boolean) {
					RunContextCheckButtonWithDefaultValue btnValue =
						new RunContextCheckButtonWithDefaultValue(compAssisted, this, key,
							(Boolean) defaultValue, (Boolean) configuredValue);
					btnValue.setText("active");
					btnValue.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
					
				} else if (defaultValue instanceof String) {
					RunContextTextWithDefaultValue txtValue = new RunContextTextWithDefaultValue(
						compAssisted, this, key, Objects.toString(defaultValue, null),
						Objects.toString(configuredValue, null));
					txtValue.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
				}
				
			}
			
		} else {
			Label description = new Label(compAssisted, SWT.NONE);
			description.setText("No assist available");
			description.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		}
		
		compAssisted.layout(true);
	}
	
	private void openRunnableSelectionDialog(){
		ListDialog listDialog = new ListDialog(getShell());
		listDialog.setContentProvider(ArrayContentProvider.getInstance());
		List<IIdentifiedRunnable> identifiedRunnables = taskService.getIdentifiedRunnables();
		Collections.sort(identifiedRunnables, Comparator.comparing(IIdentifiedRunnable::getId));
		listDialog.setInput(identifiedRunnables);
		listDialog.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				IIdentifiedRunnable runnable = (IIdentifiedRunnable) element;
				return runnable.getId();
			}
		});
		listDialog.setMessage("Select an action");
		int open = listDialog.open();
		if (open == ListSelectionDialog.OK) {
			Object[] result = listDialog.getResult();
			if (result != null && result.length >= 1) {
				IIdentifiedRunnable selection = (IIdentifiedRunnable) result[0];
				taskDescriptor.setIdentifiedRunnableId(selection.getId());
				setSelection(taskDescriptor);
			}
		}
	}
}
