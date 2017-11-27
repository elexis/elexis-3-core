package ch.elexis.core.ui.usage.settings;

import java.util.Date;
import java.util.HashMap;

import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapCellLabelProvider;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.ui.usage.model.ModelFactory;
import ch.elexis.core.ui.usage.model.ModelPackage;
import ch.elexis.core.ui.usage.model.SimpleStatistic;
import ch.elexis.core.ui.usage.model.Statistics;

public class UsageSettings extends PreferencePage implements IWorkbenchPreferencePage {
	
	@Override
	public void init(IWorkbench workbench){
	}
	
	@Override
	protected Control createContents(Composite parent){
		
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(1, false));
		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		

		
		//Extract from text code
		TableViewer viewer =
			new TableViewer(main, SWT.FULL_SELECTION | SWT.BORDER | SWT.SINGLE);
		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.getTable().setHeaderVisible(true);
		
		// create a content provider
		ObservableListContentProvider cp = new ObservableListContentProvider();
		
		// put all attributes (from class Person) that are going to be shown
		// into a map
		// and associate the column title
		HashMap<EAttribute, String> attributeMap = new HashMap<EAttribute, String>();
		attributeMap.put(ModelPackage.Literals.ISTATISTIC__TIME, "Zeitpunkt");
		attributeMap.put(ModelPackage.Literals.ISTATISTIC__TITLE, "Titel");
		attributeMap.put(ModelPackage.Literals.ISTATISTIC__TYPE, "Typ");
		attributeMap.put(ModelPackage.Literals.ISTATISTIC__VALUE, "Wert");
		
		// create a column for each attribute & setup the databinding
		for (EAttribute attribute : attributeMap.keySet()) {
			// create a new column
			TableViewerColumn tvc = new TableViewerColumn(viewer, SWT.LEFT);
			// determine the attribute that should be observed
			IObservableMap map =
				EMFProperties.value(attribute).observeDetail(cp.getKnownElements());
			tvc.setLabelProvider(new ObservableMapCellLabelProvider(map));
			// set the column title & set the size
			tvc.getColumn().setText(attributeMap.get(attribute));
			tvc.getColumn().setWidth(150);
		}
		
		// set the content provider
		viewer.setContentProvider(cp);
		// set the model (which is a list of persons)
		
		Statistics s = ModelFactory.eINSTANCE.createStatistics();
		SimpleStatistic simpleStatistic1 = ModelFactory.eINSTANCE.createSimpleStatistic();
		SimpleStatistic simpleStatistic2 = ModelFactory.eINSTANCE.createSimpleStatistic();
		SimpleStatistic simpleStatistic3 = ModelFactory.eINSTANCE.createSimpleStatistic();
		
		simpleStatistic1.setTime(new Date(System.currentTimeMillis()));
		simpleStatistic1.setTitle("Test Simple1");
		simpleStatistic1.setValue(20);
		simpleStatistic1.setType("Aufruf");
		simpleStatistic2.setTitle("Test Simple2");
		simpleStatistic2.setValue(30);
		simpleStatistic2.setType("Aufruf");
		simpleStatistic2.setTime(new Date(System.currentTimeMillis()));
		
		simpleStatistic3.setTitle("Test Relational");
		simpleStatistic3.setType("PatientenView -> DiagnoseView");
		simpleStatistic3.setValue(10);
		simpleStatistic3.setTime(new Date(System.currentTimeMillis()));
		
		s.getStatistics().add(simpleStatistic1);
		s.getStatistics().add(simpleStatistic2);
		s.getStatistics().add(simpleStatistic3);

		
		viewer.setInput(
			EMFProperties.list(ModelPackage.Literals.STATISTICS__STATISTICS).observe(s));

		
		return main;
	}
	
}
