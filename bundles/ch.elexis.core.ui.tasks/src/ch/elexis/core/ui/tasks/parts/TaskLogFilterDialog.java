package ch.elexis.core.ui.tasks.parts;

import java.util.Collections;
import java.util.List;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;

import ch.elexis.core.services.IQuery;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.ITaskService;
import ch.elexis.core.ui.e4.parts.IRefreshablePart;
import ch.elexis.core.ui.tasks.internal.TaskModelServiceHolder;

public class TaskLogFilterDialog {

	public static final String SHOW_FILTERED_TASK = "sft";
	private ITaskDescriptor td;
	private MDirectToolItem item;

	@Execute
	public void execute(Shell shell, ITaskService taskService, MPart part, MDirectToolItem item) {
		this.item = item;
		if (item.isSelected()) {
			openTaskSelectionDialog();
		} else {
			td = null;
		}

		((IRefreshablePart) part.getObject()).refresh(Collections.singletonMap(SHOW_FILTERED_TASK, this.td));
	}

	private ITaskDescriptor openTaskSelectionDialog() {
		IQuery<ITaskDescriptor> taskDescriptorQuery = TaskModelServiceHolder.get().getQuery(ITaskDescriptor.class);
		List<ITaskDescriptor> list = taskDescriptorQuery.execute();

		LabelProvider lp = new LabelProvider() {
			public String getText(Object element) {
				ITaskDescriptor iTaskDescriptor = (ITaskDescriptor) element;
				return iTaskDescriptor.getReferenceId();
			};
		};

		ListDialog listDialog = new ListDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		listDialog.setContentProvider(ArrayContentProvider.getInstance());
		listDialog.setInput(list);
		listDialog.setLabelProvider(lp);
		listDialog.setMessage("Nach Task filtern");

		int open = listDialog.open();
		if (open == ListDialog.OK) {
			Object[] selection = listDialog.getResult();
			if (selection != null) {
				td = (ITaskDescriptor) selection[0];
			}
		} else {
			item.setSelected(false);
		}

		return td;
	}

}
