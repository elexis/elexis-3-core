package ch.elexis.core.findings.ui.composites;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.ICondition.ConditionCategory;
import ch.elexis.core.findings.ICondition.ConditionStatus;
import ch.elexis.core.findings.ui.composites.CodingListComposite.CodingAdapter;
import ch.elexis.core.findings.ui.composites.NoteListComposite.NotesAdapter;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;

public class ConditionComposite extends Composite {
	
	private ConditionCategory category;
	
	private Optional<ICondition> condition;
	private WritableValue transientConditionValue;
	
	private ComboViewer statusViewer;
	
	private TabFolder textOrCodingFolder;
	
	private Text startTxt;
	private Text endTxt;
	
	private Text textTxt;
	
	private CodingListComposite codingComposite;
	
	private NoteListComposite notesComposite;
	
	public ConditionComposite(ConditionCategory category, Composite parent, int style){
		super(parent, style);
		setLayout(new GridLayout(1, false));
		
		this.category = category;
		condition = Optional.empty();
		
		statusViewer = new ComboViewer(this);
		statusViewer.setContentProvider(new ArrayContentProvider());
		statusViewer.setInput(ConditionStatus.values());
		statusViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof ConditionStatus) {
					return ((ConditionStatus) element).getLocalized();
				}
				return super.getText(element);
			}
		});
		
		startTxt = new Text(this, SWT.BORDER);
		startTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		startTxt.setMessage("Beginn Datum oder Beschreibung");
		
		endTxt = new Text(this, SWT.BORDER);
		endTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		endTxt.setMessage("Ende Datum oder Beschreibung");
		
		textOrCodingFolder = new TabFolder(this, SWT.NONE);
		
		TabItem textItem = new TabItem(textOrCodingFolder, SWT.NONE, 0);
		textItem.setText("Text");
		textTxt = new Text(textOrCodingFolder, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		textItem.setControl(textTxt);
		
		TabItem codingItem = new TabItem(textOrCodingFolder, SWT.NONE, 1);
		codingItem.setText("Kodierung");
		codingComposite = new CodingListComposite(textOrCodingFolder, SWT.NONE);
		codingComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		codingItem.setControl(codingComposite);
		
		GridData folderGd = new GridData(GridData.FILL_BOTH);
		textOrCodingFolder.setLayoutData(folderGd);

		notesComposite = new NoteListComposite(this, SWT.NONE);
		notesComposite.showTitle(true);
		notesComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		initDataBinding();
	}
	
	private void initDataBinding(){
		transientConditionValue = new WritableValue();
		DataBindingContext bindingContext = new DataBindingContext();
		
		IObservableValue targetObservable =
			ViewersObservables.observeSingleSelection(statusViewer);
		IObservableValue modelObservable = PojoObservables
			.observeDetailValue(transientConditionValue, "status", TransientCondition.class);
		bindingContext.bindValue(targetObservable, modelObservable);
		
		targetObservable = SWTObservables.observeText(startTxt, SWT.Modify);
		modelObservable = PojoObservables.observeDetailValue(transientConditionValue, "start",
			TransientCondition.class);
		bindingContext.bindValue(targetObservable, modelObservable);
		
		targetObservable = SWTObservables.observeText(endTxt, SWT.Modify);
		modelObservable = PojoObservables.observeDetailValue(transientConditionValue, "end",
			TransientCondition.class);
		bindingContext.bindValue(targetObservable, modelObservable);
		
		targetObservable = SWTObservables.observeText(textTxt, SWT.Modify);
		modelObservable =
			PojoObservables.observeDetailValue(transientConditionValue, "text",
				TransientCondition.class);
		bindingContext.bindValue(targetObservable, modelObservable);
		
		setCondition(null);
	}
	
	public Optional<ICondition> getCondition(){
		return condition;
	}
	
	public void setCondition(final ICondition condition){
		this.condition = Optional.ofNullable(condition);
		if (this.condition.isPresent()) {
			transientConditionValue
				.setValue(TransientCondition.fromCondition(this.condition.get()));
			// show coding if present
			List<ICoding> coding = this.condition.get().getCoding();
			if (coding != null && !coding.isEmpty()) {
				textOrCodingFolder.setSelection(1);
			}
		} else {
			TransientCondition emptyCondition = new TransientCondition();
			emptyCondition.setStatus(ConditionStatus.ACTIVE);
			emptyCondition.setDateRecorded(LocalDate.now());
			transientConditionValue.setValue(emptyCondition);
		}
		
		// provide access adapter to notes composite 
		notesComposite.setInput(new NotesAdapter() {
			@Override
			public void removeNote(String note){
				if (transientConditionValue.getValue() != null) {
					((TransientCondition) transientConditionValue.getValue()).removeNote(note);
				}
			}
			
			@Override
			public List<String> getNotes(){
				if (transientConditionValue.getValue() != null) {
					return ((TransientCondition) transientConditionValue.getValue()).getNotes();
				}
				return Collections.emptyList();
			}
			
			@Override
			public void addNote(String note){
				if (transientConditionValue.getValue() != null) {
					((TransientCondition) transientConditionValue.getValue()).addNote(note);
				}
			}
		});
		codingComposite.setInput(new CodingAdapter() {
			@Override
			public List<ICoding> getCoding(){
				if (transientConditionValue.getValue() != null) {
					return ((TransientCondition) transientConditionValue.getValue()).getCoding();
				}
				return Collections.emptyList();
			}

			@Override
			public void setCoding(List<ICoding> coding){
				if (transientConditionValue.getValue() != null) {
					((TransientCondition) transientConditionValue.getValue()).setCoding(coding);
				}
			}
		});
	}
	
	/**
	 * Creates a new ICondition if none set, and updates it with the actual values.
	 */
	public void udpateModel(){
		if (!condition.isPresent()) {
			condition = Optional
				.of(FindingsServiceComponent.getService().create(ICondition.class));
			condition.get().setCategory(category);
		}
		if (transientConditionValue.getValue() instanceof TransientCondition) {
			((TransientCondition) transientConditionValue.getValue()).toCondition(condition.get());
		}
	}
	
	private static class TransientCondition {
		
		private String text;
		private List<ICoding> coding = new ArrayList<>();
		private ConditionStatus status;
		private LocalDate dateRecorded;
		private List<String> notes = new ArrayList<>();
		
		private String start;
		private String end;
		
		public List<ICoding> getCoding(){
			return coding;
		}
		

		public void setCoding(List<ICoding> coding){
			this.coding = coding;
		}
		
		public String getText(){
			return text;
		}
		

		public void setText(String text){
			this.text = text;
		}
		
		
		public ConditionStatus getStatus(){
			return status;
		}
		

		public void setStatus(ConditionStatus status){
			this.status = status;
		}
		
		public void setDateRecorded(LocalDate dateRecorded){
			this.dateRecorded = dateRecorded;
		}
		
		public LocalDate getDateRecorded(){
			return dateRecorded;
		}
		
		private void setNotes(List<String> notes){
			this.notes = notes;
		}
		
		public List<String> getNotes(){
			return notes;
		}
		
		public void addNote(String note){
			notes.add(note);
		}
		
		public void removeNote(String note){
			notes.remove(note);
		}
		
		public String getStart(){
			return start;
		}
		
		public void setStart(String start){
			this.start = start;
		}
		
		public String getEnd(){
			return end;
		}
		
		public void setEnd(String end){
			this.end = end;
		}
		
		public static TransientCondition fromCondition(ICondition condition){
			TransientCondition ret = new TransientCondition();
			ret.setStatus(condition.getStatus());
			ret.setCoding(condition.getCoding());
			condition.getDateRecorded().ifPresent(d -> ret.setDateRecorded(d));
			condition.getText().ifPresent(t -> ret.setText(t));
			ret.setNotes(new ArrayList<String>(condition.getNotes()));
			condition.getStart().ifPresent(string -> ret.setStart(string));
			condition.getEnd().ifPresent(string -> ret.setEnd(string));
			return ret;
		}
		

		public void toCondition(ICondition condition){
			condition.setStatus(getStatus());
			condition.setCoding(getCoding());
			if(dateRecorded != null) {
				condition.setDateRecorded(getDateRecorded());
			}
			if(text != null) {
				condition.setText(getText());
			}
			if (start != null) {
				condition.setStart(start);
			}
			if (end != null) {
				condition.setEnd(end);
			}
			if (notes != null) {
				List<String> existingNotes = condition.getNotes();
				for (String string : existingNotes) {
					// remove no longer contained notes
					if (!notes.contains(string)) {
						condition.removeNote(string);
					}
				}
				for (String string : notes) {
					// add new notes
					if (!existingNotes.contains(string)) {
						condition.addNote(string);
					}
				}
			}
		}
	}
}
