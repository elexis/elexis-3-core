package ch.elexis.core.mail.ui.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

public class TextTemplateDialog extends Dialog {
	
	private IMandator mandator;
	
	private String name;
	
	private List<Object> list;
	
	public TextTemplateDialog(Shell parentShell){
		super(parentShell);
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite container = (Composite) super.createDialogArea(parent);
		
		Text nameTxt = new Text(container, SWT.BORDER);
		nameTxt.setMessage("Vorlagenname");
		nameTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		nameTxt.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e){
				name = nameTxt.getText();
			}
		});

		ComboViewer comboViewer = new ComboViewer(container, SWT.BORDER);
		comboViewer.setContentProvider(ArrayContentProvider.getInstance());
		comboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof IMandator) {
					return ((IMandator) element).getLabel();
				}
				return super.getText(element);
			}
		});
		List<Object> content = new ArrayList<>();
		content.add("Alle Mandanten");
		IQuery<IMandator> query = CoreModelServiceHolder.get().getQuery(IMandator.class);
		query.and(ModelPackage.Literals.ICONTACT__MANDATOR, COMPARATOR.EQUALS, Boolean.TRUE);
		content.addAll(query.execute());
		if (ContextServiceHolder.get().getActiveUser().isPresent()) {
			if (!ContextServiceHolder.get().getActiveUser().get().isAdministrator()) {
				content = content.stream().filter(o -> isAllOrCurrentMandator(o))
					.collect(Collectors.toList());
			}
		}
		comboViewer.setInput(content);
		comboViewer.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				if (event.getStructuredSelection() != null
					&& !event.getStructuredSelection().isEmpty()) {
					if (event.getStructuredSelection().getFirstElement() instanceof IMandator) {
						mandator = (IMandator) event.getStructuredSelection().getFirstElement();
					} else {
						mandator = null;
					}
				}
			}
		});
		return container;
	}
	
	private boolean isAllOrCurrentMandator(Object o){
		if (o instanceof IMandator) {
			if (ContextServiceHolder.get().getActiveMandator().isPresent()) {
				return ContextServiceHolder.get().getActiveMandator().get().equals(o);
			}
		}
		return true;
	}
	
	public IMandator getMandator(){
		return mandator;
	}
	
	public String getName(){
		return name;
	}
}
