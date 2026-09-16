package ch.elexis.core.findings.ui.composites;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
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

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.ICondition.ConditionCategory;
import ch.elexis.core.findings.ICondition.ConditionStatus;
import ch.elexis.core.findings.ui.composites.CodingListComposite.CodingAdapter;
import ch.elexis.core.findings.ui.composites.NoteListComposite.NotesAdapter;
import ch.elexis.core.findings.ui.model.ConditionBeanAdapter;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.services.LocalConfigService;
import ch.elexis.core.text.docx.util.TextUtil;
import ch.elexis.core.ui.views.controls.RichTextEditorComposite;

public class ConditionComposite extends Composite {

	private ConditionCategory category;

	private Optional<ICondition> condition;
	private WritableValue<ConditionBeanAdapter> conditionValue;

	private ComboViewer statusViewer;

	private TabFolder textOrCodingFolder;

	private Text startTxt;
	private Text endTxt;

	/** Set when the alternative formatting is active, {@link #textTxt} is set otherwise. */
	private RichTextEditorComposite textEditor;
	private Text textTxt;

	/** The markup {@link #textTxt} was filled from, kept to detect an untouched text. */
	private String plainSourceMarkup;

	private CodingListComposite codingComposite;

	private NoteListComposite notesComposite;

	public ConditionComposite(ConditionCategory category, Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));

		this.category = category;
		condition = Optional.empty();

		statusViewer = new ComboViewer(this);
		statusViewer.setContentProvider(new ArrayContentProvider());
		statusViewer.setInput(ConditionStatus.values());
		statusViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ConditionStatus) {
					return ((ConditionStatus) element).getLocalized();
				}
				return super.getText(element);
			}
		});

		startTxt = new Text(this, SWT.BORDER);
		startTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		startTxt.setMessage(Messages.ConditionComposite_StartHint);

		endTxt = new Text(this, SWT.BORDER);
		endTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		endTxt.setMessage(Messages.ConditionComposite_EndHint);

		textOrCodingFolder = new TabFolder(this, SWT.NONE);

		boolean useRichText = LocalConfigService.get(Preferences.P_TEXT_DIAGNOSE_EXPORT_WORD_FORMAT, false);

		TabItem textItem = new TabItem(textOrCodingFolder, SWT.NONE, 0);
		textItem.setText(Messages.ConditionComposite_TabText);
		// only the alternative formatting needs the browser based editor, everyone else keeps
		// the plain text widget and does not pay for starting the browser engine
		if (useRichText) {
			textEditor = new RichTextEditorComposite(textOrCodingFolder, SWT.NONE);
			textItem.setControl(textEditor);
		} else {
			textTxt = new Text(textOrCodingFolder, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
			textItem.setControl(textTxt);
		}

		TabItem codingItem = new TabItem(textOrCodingFolder, SWT.NONE, 1);
		codingItem.setText(Messages.ConditionComposite_TabCoding);
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

	private void initDataBinding() {
		conditionValue = new WritableValue<>();
		DataBindingContext bindingContext = new DataBindingContext();

		IObservableValue<?> targetObservable = ViewerProperties.singleSelection().observe(statusViewer);
		IObservableValue<?> modelObservable = PojoProperties
				.value(ConditionBeanAdapter.class, "status", ConditionStatus.class)
				.observeDetail(conditionValue);
		bindingContext.bindValue(targetObservable, modelObservable);

		targetObservable = WidgetProperties.text(SWT.Modify).observe(startTxt);
		modelObservable = PojoProperties.value(ConditionBeanAdapter.class, "start", String.class)
				.observeDetail(conditionValue);
		bindingContext.bindValue(targetObservable, modelObservable);

		targetObservable = WidgetProperties.text(SWT.Modify).observe(endTxt);
		modelObservable = PojoProperties.value(ConditionBeanAdapter.class, "end", String.class)
				.observeDetail(conditionValue);
		bindingContext.bindValue(targetObservable, modelObservable);

		setCondition(null);
	}

	public Optional<ICondition> getCondition() {
		if (conditionValue.getValue() != null) {
			conditionValue.getValue().setText(getTextValue());
		}
		return condition;
	}

	/**
	 * Reads the text tab. With the alternative formatting active this is the editor markup as
	 * it is. Without it the text was shown without its markup, so an unchanged text has to be
	 * stored unchanged as well - otherwise opening and confirming a diagnosis at a workstation
	 * that does not use the formatting would silently drop the formatting for everybody else.
	 */
	private String getTextValue() {
		if (textEditor != null) {
			return textEditor.getText();
		}
		// SWT hands back the platform line delimiter, which is \r\n on Windows, while the
		// markup is converted with \n - so both sides have to be normalized before comparing
		String plain = normalizeLineDelimiters(textTxt.getText());
		if (plainSourceMarkup != null && plain.equals(TextUtil.htmlToPlainText(plainSourceMarkup))) {
			return plainSourceMarkup;
		}
		return plain;
	}

	private static String normalizeLineDelimiters(String text) {
		return text != null ? text.replace("\r\n", StringUtils.LF).replace("\r", StringUtils.LF) : null;
	}

	private void setTextValue(String markup) {
		if (textEditor != null) {
			textEditor.setText(markup);
			return;
		}
		plainSourceMarkup = markup;
		textTxt.setText(StringUtils.defaultString(TextUtil.htmlToPlainText(markup)));
	}

	public void setCondition(final ICondition condition) {
		this.condition = Optional.ofNullable(condition);
		if (this.condition.isPresent()) {
			condition.setCategory(category);
			conditionValue.setValue(new ConditionBeanAdapter(condition));
			// show coding if present
			List<ICoding> coding = this.condition.get().getCoding();
			textOrCodingFolder.setSelection(coding != null && !coding.isEmpty() ? 1 : 0);
		} else {
			ICondition emptyCondition = FindingsServiceComponent.getService().create(ICondition.class);
			emptyCondition.setStatus(ConditionStatus.ACTIVE);
			emptyCondition.setDateRecorded(LocalDate.now());
			emptyCondition.setCategory(category);
			conditionValue.setValue(new ConditionBeanAdapter(emptyCondition));
			this.condition = Optional.of(emptyCondition);
		}

		setTextValue(conditionValue.getValue() != null ? conditionValue.getValue().getText() : null);

		// provide access adapter to notes composite
		notesComposite.setInput(new NotesAdapter() {
			@Override
			public void removeNote(String note) {
				if (conditionValue.getValue() != null) {
					conditionValue.getValue().removeNote(note);
				}
			}

			@Override
			public List<String> getNotes() {
				if (conditionValue.getValue() != null) {
					return conditionValue.getValue().getNotes();
				}
				return Collections.emptyList();
			}

			@Override
			public void addNote(String note) {
				if (conditionValue.getValue() != null) {
					conditionValue.getValue().addNote(note);
				}
			}
		});
		codingComposite.setInput(new CodingAdapter() {
			@Override
			public List<ICoding> getCoding() {
				if (conditionValue.getValue() != null) {
					return conditionValue.getValue().getCoding();
				}
				return Collections.emptyList();
			}

			@Override
			public void setCoding(List<ICoding> coding) {
				if (conditionValue.getValue() != null) {
					conditionValue.getValue().setCoding(coding);
				}
			}
		});
	}
}
