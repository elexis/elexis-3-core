package ch.elexis.core.ui.views;

import java.util.Optional;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.service.StoreToStringServiceHolder;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.ICodeElementBlock;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.actions.ICodeSelectorTarget;
import ch.elexis.core.ui.actions.Messages;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.data.VerrechenbarFavorites;
import ch.elexis.data.VerrechenbarFavorites.Favorite;

public class FavoritenComposite extends Composite {
	
	private TableViewer tv;
	private Transfer[] types = new Transfer[] {
		TextTransfer.getInstance()
	};
	
	private ElexisUiEventListenerImpl reloadListener =
		new ElexisUiEventListenerImpl(Favorite.class, ElexisEvent.EVENT_RELOAD) {
			@Override
			public void runInUi(ElexisEvent ev){
				update();
			}
		};
	
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
		
		tv = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
		final Table table = tv.getTable();
		table.setHeaderVisible(true);
		table.addListener(SWT.MeasureItem, new Listener() {
			
			@Override
			public void handleEvent(Event event){
				int clientWidth = table.getClientArea().width;
				event.height = (event.gc.getFontMetrics().getHeight() * 2) + 2;
				event.width = clientWidth * 2;
			}
		});
		
		table.addListener(SWT.PaintItem, new Listener() {
			@Override
			public void handleEvent(Event event){
				TableItem item = (TableItem) event.item;
				
				Favorite fav = (Favorite) item.getData();
				Optional<Identifiable> cfs = fav.getObject();
				
				String simpleName = "?";
				String label = "?";
				if (cfs.isPresent()) {
					simpleName = cfs.get().getClass().getSimpleName();
					label = cfs.get().getLabel();
				}
				
				/* center column 1 vertically */
				int yOffset = 0;
				
				Font defaultFont = event.gc.getFont();
				FontDescriptor boldDescriptor =
					FontDescriptor.createFrom(defaultFont).setStyle(SWT.BOLD);
				Font boldFont = boldDescriptor.createFont(event.display);
				
				switch (event.index) {
				case 0:
					event.gc.setFont(defaultFont);
					event.gc.drawText(fav.getMacroString(), event.x + 3, event.y + yOffset, true);
					break;
				case 1:
					event.gc.setFont(boldFont);
					event.gc.drawText(simpleName, event.x + 3, event.y + yOffset, true);
					event.gc.setFont(defaultFont);
					event.gc.drawText(label, event.x + 3,
						event.y + event.gc.getFontMetrics().getHeight(), true);
					break;
				default:
					break;
				}
				
			}
		});
		tv.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event){
				ICodeSelectorTarget target =
					CodeSelectorHandler.getInstance().getCodeSelectorTarget();
				if (target != null) {
					StructuredSelection ss = (StructuredSelection) tv.getSelection();
					if (!ss.isEmpty()) {
						Favorite fav = (Favorite) ss.getFirstElement();
						Optional<Identifiable> po = fav.getObject();
						if (po.isPresent()) {
							if (po.get() instanceof ICodeElementBlock) {
								ICodeElementBlock lb = (ICodeElementBlock) po.get();
								for (ch.elexis.core.model.ICodeElement iCodeElement : lb
									.getElements()) {
									target.codeSelected(iCodeElement);
								}
							} else {
								target.codeSelected(po.get());
							}
						}
					}
				}
			}
		});
		
		TableViewerColumn tvcMacro = new TableViewerColumn(tv, SWT.NONE);
		TableColumn tblclmnMakro = tvcMacro.getColumn();
		tcl_composite.setColumnData(tblclmnMakro, new ColumnPixelData(50, true, true));
		tblclmnMakro.setText("Makro");
		tvcMacro.setEditingSupport(new EditingSupport(tvcMacro.getViewer()) {
			
			TextCellEditor editor = new TextCellEditor(table);
			
			@Override
			protected void setValue(Object element, Object value){
				Favorite fav = (Favorite) element;
				fav.setMacroString((String) value);
				
				VerrechenbarFavorites.storeFavorites();
			}
			
			@Override
			protected Object getValue(Object element){
				Favorite fav = (Favorite) element;
				return fav.getMacroString();
			}
			
			@Override
			protected CellEditor getCellEditor(Object element){
				return editor;
			}
			
			@Override
			protected boolean canEdit(Object element){
				return true;
			}
		});
		
		TableViewerColumn tvcElement = new TableViewerColumn(tv, SWT.NONE);
		TableColumn tblclmnNewColumn = tvcElement.getColumn();
		tcl_composite.setColumnData(tblclmnNewColumn,
			new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, true));
		tblclmnNewColumn.setText("Element");
		
		MenuManager mgr = new MenuManager();
		mgr.add(new Action() {
			{
				setText(Messages.ToggleVerrechenbarFavoriteAction_DeFavorize);
			}
			
			@Override
			public void run(){
				StructuredSelection selection = (StructuredSelection) tv.getSelection();
				Favorite fav = (Favorite) selection.getFirstElement();
				fav.getObject().ifPresent(obj -> {
					VerrechenbarFavorites.setFavorite(obj, false);
				});
				tv.refresh();
			}
		});
		Menu menu = mgr.createContextMenu(tv.getControl());
		table.setMenu(menu);
		
		DragSource mine = new DragSource(tv.getControl(), DND.DROP_COPY);
		mine.setTransfer(types);
		mine.addDragListener(new DragSourceAdapter() {
			@Override
			public void dragSetData(DragSourceEvent event){
				StructuredSelection ss = (StructuredSelection) tv.getSelection();
				if (ss.isEmpty()) {
					event.data = null;
				} else {
					Favorite fav = (Favorite) ss.getFirstElement();
					Optional<Identifiable> favObj = fav.getObject();
					if (favObj.isPresent()) {
						event.data = StoreToStringServiceHolder.getStoreToString(favObj.get());
					}
				}
			}
			
			@Override
			public void dragStart(final DragSourceEvent event){
				// TODO ...
				//				StructuredSelection ss = (StructuredSelection) tv.getSelection();
				//				if (ss.isEmpty()) {
				//					PersistentObjectDragSource.setDraggedObject(null);
				//					event.doit = false;
				//				} else {
				//					Favorite fav = (Favorite) ss.getFirstElement();
				//					if (fav.getPersistentObject() instanceof Leistungsblock) {
				//						Leistungsblock lb = (Leistungsblock) fav.getPersistentObject();
				//						PersistentObjectDragSource.setDraggedObject(lb);
				//					} else {
				//						PersistentObjectDragSource
				//							.setDraggedObject((PersistentObject) fav.getPersistentObject());
				//					}
				//					event.doit = true;
				//				}
			}
		});
		
		tv.setContentProvider(new ArrayContentProvider());
		tv.setLabelProvider(new ColorizedLabelProvider());
		tv.setInput(VerrechenbarFavorites.getFavorites());
		
		ElexisEventDispatcher.getInstance().addListeners(reloadListener);
	}
	
	@Override
	public void dispose(){
		ElexisEventDispatcher.getInstance().removeListeners(reloadListener);
	}
	
	@Override
	protected void checkSubclass(){
		// Disable the check that prevents subclassing of SWT components
	}
	
	@Override
	public void update(){
		if (tv != null) {
			tv.setInput(VerrechenbarFavorites.getFavorites());
		}
	}
	
	private class ColorizedLabelProvider extends LabelProvider implements IColorProvider {
		@Override
		public String getText(Object element){
			return "";
		}
		
		@Override
		public Color getBackground(Object element){
			Favorite fav = (Favorite) element;
			Optional<Identifiable> v = fav.getObject();
			if (!v.isPresent()) {
				return null;
			}
			String codeSystemName = ((ICodeElement) v.get()).getCodeSystemName();
			if (codeSystemName == null) {
				return null;
			}
			
			String rgbColor =
				ConfigServiceHolder.getGlobal(Preferences.LEISTUNGSCODES_COLOR + codeSystemName, "ffffff");
			return UiDesk.getColorFromRGB(rgbColor);
			
		}
		
		@Override
		public Color getForeground(Object element){
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
