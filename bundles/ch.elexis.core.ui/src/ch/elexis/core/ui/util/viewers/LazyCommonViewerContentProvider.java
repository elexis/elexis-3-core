package ch.elexis.core.ui.util.viewers;

import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.AbstractTableViewer;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.ui.util.viewers.CommonViewer.Message;

public abstract class LazyCommonViewerContentProvider extends CommonViewerContentProvider
		implements ILazyContentProvider {
	
	protected Object[] loadedElements;
	
	public LazyCommonViewerContentProvider(CommonViewer commonViewer){
		super(commonViewer);
	}

	@Override
	public void changed(HashMap<String, String> values){
		if (commonViewer.getConfigurer().getControlFieldProvider().isEmpty()) {
			commonViewer.notify(CommonViewer.Message.empty);
		} else {
			commonViewer.notify(CommonViewer.Message.notempty);
		}
		fieldFilterValues = values;
		// trigger loading
		asyncReload();
	}
	
	protected String getJobName() {
		return "Loading";
	}
	
	protected void asyncReload(){
		Job job = Job.create(getJobName(), new ICoreRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException{
				loadedElements = getElements(null);
				Display.getDefault().asyncExec(() -> {
					// virtual table ...
					AbstractTableViewer viewer =
						(AbstractTableViewer) commonViewer.getViewerWidget();
					if (viewer != null && !viewer.getControl().isDisposed()) {
						viewer.setItemCount(loadedElements.length);
					}
					// trigger viewer refresh
					commonViewer.notify(Message.update);
				});
			}
		});
		job.setPriority(Job.SHORT);
		job.schedule();
	}
	
	@Override
	public void updateElement(int index){
		if (loadedElements != null && loadedElements.length > 0) {
			Object[] copy = new Object[loadedElements.length];
			System.arraycopy(loadedElements, 0, copy, 0, loadedElements.length);
			if (index >= 0 && index < copy.length) {
				Object o = copy[index];
				if (o != null) {
					TableViewer tv = (TableViewer) commonViewer.getViewerWidget();
					tv.replace(o, index);
				}
			}
		}
	}
}
