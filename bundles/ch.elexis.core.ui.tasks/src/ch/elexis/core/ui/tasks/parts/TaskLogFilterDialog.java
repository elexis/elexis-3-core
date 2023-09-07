package ch.elexis.core.ui.tasks.parts;

import java.util.List;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
import org.eclipse.jface.viewers.AcceptAllFilter;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;

import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.ITaskService;
import ch.elexis.core.ui.tasks.internal.TaskModelServiceHolder;

public class TaskLogFilterDialog {

	private ITaskDescriptor td;
	private MDirectToolItem item;
	private IFilter showSelectedTask;

	@Execute
	public void execute(Shell shell, ITaskService taskService, MPart part, MDirectToolItem item) {
		TaskLogPart taskResultPart = (TaskLogPart) part.getObject();
		this.item = item;
		if (item.isSelected()) {
			openTaskSelectionDialog();

			showSelectedTask = (object) -> ((ITask) object).getTaskDescriptor().getReferenceId()
					.equals(td.getReferenceId());
			taskResultPart.getContentProvider().setFilter(showSelectedTask);
		} else {
			taskResultPart.getContentProvider().setFilter(AcceptAllFilter.getInstance());
		}
	}

	private ITaskDescriptor openTaskSelectionDialog() {
		IQuery<ITaskDescriptor> taskDescriptorQuery = TaskModelServiceHolder.get().getQuery(ITaskDescriptor.class);
		taskDescriptorQuery.and(ch.elexis.core.tasks.model.ModelPackage.Literals.ITASK__SYSTEM, COMPARATOR.EQUALS,
				false);
		List<ITaskDescriptor> list = taskDescriptorQuery.execute();

		LabelProvider lp = new LabelProvider() {
			@Override
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
