package ch.elexis.core.ui.usage.settings;

import java.io.IOException;
import java.util.Comparator;

import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapCellLabelProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.usage.model.IStatistic;
import ch.elexis.core.ui.usage.model.ModelPackage;
import ch.elexis.core.ui.usage.util.StatisticsManager;
import ch.rgw.tools.TimeTool;

public class UsageSettings extends PreferencePage implements IWorkbenchPreferencePage {
	

	public static final String CONFIG_USAGE_STATISTICS = "statistics/usage/sentUsageStatistics";
	private Button checkSentStatistics;
	
	@Override
	public void init(IWorkbench workbench){
	}
	
	@Override
	protected Control createContents(Composite parent){
		
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(1, false));
		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		TableViewer viewer =
			new TableViewer(main, SWT.FULL_SELECTION | SWT.BORDER | SWT.SINGLE);
		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.getTable().setHeaderVisible(true);
		
		// create a content provider
		ObservableListContentProvider cp = new ObservableListContentProvider();
		
		// create a new columns
		TableViewerColumn tvc = new TableViewerColumn(viewer, SWT.LEFT);
		tvc.getColumn().setText("Zeitpunkt");
		tvc.getColumn().setWidth(80);
		tvc.setLabelProvider(new ObservableMapCellLabelProvider(EMFProperties
			.value(ModelPackage.Literals.ISTATISTIC__TIME).observeDetail(cp.getKnownElements())) {
			@Override
			public void update(ViewerCell cell){
				IStatistic iStatistic = (IStatistic) cell.getElement();
				if (iStatistic.getTime() != null) {
					TimeTool t = new TimeTool(iStatistic.getTime());
					cell.setText(t.toString(TimeTool.TIME_FULL));
				} else {
					super.update(cell);
				}
			}
		});
		
		tvc = new TableViewerColumn(viewer, SWT.LEFT);
		tvc.getColumn().setText("Typ");
		tvc.getColumn().setWidth(120);
		tvc.setLabelProvider(new ObservableMapCellLabelProvider(EMFProperties
			.value(ModelPackage.Literals.ISTATISTIC__ACTION_TYPE)
			.observeDetail(cp.getKnownElements())));
		
		tvc = new TableViewerColumn(viewer, SWT.LEFT);
		tvc.getColumn().setText("Aktion");
		tvc.getColumn().setWidth(300);
		tvc.setLabelProvider(new ObservableMapCellLabelProvider(EMFProperties
			.value(ModelPackage.Literals.ISTATISTIC__ACTION).observeDetail(cp.getKnownElements())));
		
		tvc = new TableViewerColumn(viewer, SWT.LEFT);
		tvc.getColumn().setText("Wert");
		tvc.getColumn().setWidth(50);
		tvc.setLabelProvider(new ObservableMapCellLabelProvider(EMFProperties
			.value(ModelPackage.Literals.ISTATISTIC__VALUE).observeDetail(cp.getKnownElements())));
		

		viewer.setContentProvider(cp);
		
		ECollections.sort(StatisticsManager.getInstance().getStatistics().getStatistics(),
			new Comparator<IStatistic>() {
				public int compare(IStatistic o1, IStatistic o2){
					int i = o2.getTime().compareTo(o1.getTime());
					// in some cases the time is equal then we sort by action
					if (i == 0) {
						return o1.getActionType().compareTo(o2.getActionType());
					}
					return i;
				}
			});
		viewer.setInput(
			EMFProperties.list(ModelPackage.Literals.STATISTICS__STATISTICS)
				.observe(StatisticsManager.getInstance().getStatistics()));

		addContextMenuSupport(viewer, createMenu(viewer));
		
		checkSentStatistics = new Button(main, SWT.CHECK);
		checkSentStatistics
			.setText(
				"Nutzungsstatistik aktivieren und beim Beenden übermitteln (Neustart erforderlich)");
		checkSentStatistics.setSelection(CoreHub.globalCfg.get(CONFIG_USAGE_STATISTICS, false));
		
		return main;
	}
	
	public void addContextMenuSupport(TableViewer tableViewer, MenuManager menuManager){
		Table table = tableViewer.getTable();
		Menu menu = menuManager.createContextMenu(table);
		table.setMenu(menu);
	}
	
	private MenuManager createMenu(TableViewer tableViewer){
		MenuManager menuManager = new MenuManager();
		
		Action clearAction = new Action("Leeren") { //$NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_DELETE.getImageDescriptor());
				setToolTipText("Statistik Leeren"); //$NON-NLS-1$
			}
			
			@Override
			public void run(){
				StatisticsManager.getInstance().getStatistics().getStatistics().clear();
				tableViewer.refresh();
			}
		};
			
		Action exportAction = new Action("Exportieren") { //$NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_EXPORT.getImageDescriptor());
				setToolTipText("Statistik Exportieren"); //$NON-NLS-1$
			}
			
			@Override
			public void run(){
				FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
				dialog.setFilterNames(new String[] {
					"xml"
				});
				dialog.setFilterExtensions(new String[] {
					"*.xml"
				});
				dialog.setOverwrite(true);
				dialog.setFilterPath(CoreHub.getWritableUserDir().getAbsolutePath()); // Windows path
				dialog.setFileName("statistics_export.xml");
				String path = dialog.open();
				if (path != null) {
					try {
						StatisticsManager.getInstance().exportStatisticsToFile(path);
					} catch (IOException e) {
						LoggerFactory.getLogger(UsageSettings.class)
							.error("statistics export error", e);
						MessageDialog.openError(getShell(), "Fehler",
							"Statistik Export nicht möglich. [" + e.getMessage() + "]");
					}
				}
			}
		};
			
		menuManager.add(clearAction);
		menuManager.add(exportAction);
		return menuManager;
	}
	
	@Override
	protected void performApply(){
		CoreHub.globalCfg.set(CONFIG_USAGE_STATISTICS, checkSentStatistics.getSelection());
	}
	

	@Override
	public boolean performOk(){
		performApply();
		return super.performOk();
	}
}
