package ch.elexis.core.ui.views;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.viewers.TableLabelProvider;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.PersistentObjectFactory;
import ch.elexis.data.VerrechenbarFavorites;
import ch.elexis.data.VerrechenbarFavorites.Favorite;

public class FavoritenComposite extends Composite {
	
	private TableViewer tv;
	
	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public FavoritenComposite(Composite parent, int style){
		super(parent, style);
		setLayout(new GridLayout(1, false));
		
		CLabel lblHeader = new CLabel(this, SWT.NONE);

		lblHeader.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		lblHeader.setText("Favoriten");
		lblHeader.setImage(Images.IMG_STAR.getImage(ImageSize._75x66_TitleDialogIconSize));
		
		Composite composite = new Composite(this, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		TableColumnLayout tcl_composite = new TableColumnLayout();
		composite.setLayout(tcl_composite);
		
		TableViewer tv = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
		final Table table = tv.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.addListener(SWT.MeasureItem, new Listener() {
			
			@Override
			public void handleEvent(Event event){
				int clientWidth = table.getClientArea().width;
				event.height = event.gc.getFontMetrics().getHeight() * 3;
				event.width = clientWidth * 3;
			}
		});
		
		table.addListener(SWT.PaintItem, new Listener() {
			public void handleEvent(Event event){
				TableItem item = (TableItem) event.item;
				
				Favorite fav = (Favorite) item.getData();
				
				PersistentObject cfs =
					new PersistentObjectFactory().createFromString(fav.getStoreToString());
				/* center column 1 vertically */
				int yOffset = 0;
				if (event.index == 1) {
					Rectangle bounds = item.getBounds(1);
					System.out.println(event.gc.textExtent("abc4").x);
					System.out.println(item.getBounds(event.index));
					
					Font defaultFont = event.gc.getFont();
					FontDescriptor boldDescriptor =
						FontDescriptor.createFrom(defaultFont).setStyle(SWT.BOLD);
					Font boldFont = boldDescriptor.createFont(event.display);
					event.gc.setFont(boldFont);
					
//					Point size = event.gc.textExtent(cfs.getClass().getName());
					
					event.gc.drawText(cfs.getClass().getSimpleName(), event.x + 3, event.y
						+ yOffset, true);
					event.gc.setFont(defaultFont);
					event.gc.drawText(cfs.getLabel(), event.x + 3, event.y
						+ event.gc.getFontMetrics().getHeight(), true);
				}
				
			}
		});

		TableViewerColumn tableViewerColumn = new TableViewerColumn(tv, SWT.NONE);
		TableColumn tblclmnMakro = tableViewerColumn.getColumn();
		tcl_composite.setColumnData(tblclmnMakro, new ColumnPixelData(50, true, true));
		tblclmnMakro.setText("Makro");
		
		TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(tv, SWT.NONE);
		TableColumn tblclmnNewColumn = tableViewerColumn_1.getColumn();
		tcl_composite.setColumnData(tblclmnNewColumn, new ColumnWeightData(1,
			ColumnWeightData.MINIMUM_WIDTH, true));
		tblclmnNewColumn.setText("Element");
		
		tv.setContentProvider(new ArrayContentProvider());
		tv.setLabelProvider(new TableLabelProvider() {
			@Override
			public String getColumnText(Object element, int columnIndex){
				return "";
//				Favorite fav = (Favorite) element;
//				PersistentObject cfs =
//					new PersistentObjectFactory().createFromString(fav.getStoreToString());
//				if (cfs == null)
//					return "ERR: " + element;
//				
//				switch (columnIndex) {
//				case 1:
//					return cfs.getLabel();
//				default:
//					return fav.getMacroString();
//				}
			}
		});
		
		tv.setInput(VerrechenbarFavorites.getFavorites());
	}
	
	@Override
	protected void checkSubclass(){
		// Disable the check that prevents subclassing of SWT components
	}
	
	@Override
	public void update(){
		super.update();
		System.out.println("User is " + CoreHub.actUser.getId());
		if (tv != null)
			tv.setInput(VerrechenbarFavorites.getFavorites());
	}
	
}
