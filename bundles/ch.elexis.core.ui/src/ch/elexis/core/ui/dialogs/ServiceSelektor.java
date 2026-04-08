/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.dialogs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.eclipse.ui.internal.WorkbenchMessages;

import ch.elexis.core.data.service.CodeElementServiceHolder;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.services.ICodeElementService.CodeElementTyp;
import ch.elexis.core.services.ICodeElementServiceContribution;

public class ServiceSelektor extends FilteredItemsSelectionDialog {

	private List<ICodeElement> services = new ArrayList<>();

	private List<String> filterCodes;

	private String info;

	public ServiceSelektor(Shell shell) {
		this(shell, null, false);
	}

	public ServiceSelektor(Shell shell, String codeSystemName, boolean multi) {
		super(shell, multi);
		setTitle(Messages.DiagnoseSelektorDialog_Title);

		List<ICodeElementServiceContribution> serviceContributions = CodeElementServiceHolder.get()
				.getContributionsByTyp(CodeElementTyp.SERVICE);

		for (ICodeElementServiceContribution iCodeElementServiceContribution : serviceContributions) {
			if (codeSystemName == null
					|| codeSystemName.equalsIgnoreCase(iCodeElementServiceContribution.getSystem())) {
				services.addAll(iCodeElementServiceContribution.getElements(CodeElementServiceHolder.createContext()));
			}
		}

		setListLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element == null) {
					return StringUtils.EMPTY;
				}
				return getLabel((ICodeElement) element);
			}
		});

		setDetailsLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element == null) {
					return StringUtils.EMPTY;
				}
				if (element instanceof String) {
					return (String) element;
				}
				return ((ICodeElement) element).getCodeSystemName() + StringUtils.SPACE
						+ getLabel((ICodeElement) element);
			}
		});
	}

	private String getLabel(ICodeElement element) {
		if (element != null) {
			return element.getCode() + StringUtils.SPACE + element.getText();
		}
		return StringUtils.EMPTY;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		if (StringUtils.isNotBlank(info)) {
			Label infoLabel = new Label(parent, SWT.WRAP);
			GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd.horizontalIndent = 5;
			gd.verticalIndent = 5;
			infoLabel.setLayoutData(gd);
			infoLabel.setText(info);
		}

		String oldListLabel = WorkbenchMessages.FilteredItemsSelectionDialog_listLabel;

		setMessage(Messages.DiagnoseSelektorDialog_Message);
		WorkbenchMessages.FilteredItemsSelectionDialog_listLabel = StringUtils.EMPTY;
		Control ret = super.createDialogArea(parent);

		WorkbenchMessages.FilteredItemsSelectionDialog_listLabel = oldListLabel;
		return ret;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	@Override
	protected Control createExtendedContentArea(Composite parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected IDialogSettings getDialogSettings() {
		return new DialogSettings("serviceselektor"); //$NON-NLS-1$
	}

	@Override
	protected IStatus validateItem(Object item) {
		return Status.OK_STATUS;
	}

	@Override
	protected ItemsFilter createFilter() {
		return new DefaultSubstringItemsFilter();
	}

	private class DefaultSubstringItemsFilter extends ItemsFilter {
		
		public DefaultSubstringItemsFilter() {
			super();
			if (!(patternMatcher.getPattern().startsWith("*") || patternMatcher.getPattern().startsWith("?"))) {
				patternMatcher.setPattern("*" + patternMatcher.getPattern());
			}
		}

		@Override
		public boolean isConsistentItem(Object item) {
			return true;
		}

		@Override
		public boolean matchItem(Object item) {
			ICodeElement element = (ICodeElement) item;
			if (filterCodes != null) {
				if (!filterCodes.contains(element.getCode())) {
					return false;
				}
			}
			return matches(getLabel(element));
		}

	}

	public void setFilterCodes(List<String> filterCodes) {
		this.filterCodes = filterCodes;
	}

	@Override
	protected Comparator<ICodeElement> getItemsComparator() {
		return new Comparator<>() {

			@Override
			public int compare(ICodeElement o1, ICodeElement o2) {
				return getLabel(o1).compareTo(getLabel(o2));
			}
		};
	}

	@Override
	protected void fillContentProvider(AbstractContentProvider contentProvider, ItemsFilter itemsFilter,
			IProgressMonitor progressMonitor) throws CoreException {

		for (ICodeElement diagnose : services) {
			if (progressMonitor.isCanceled()) {
				return;
			}
			contentProvider.add(diagnose, itemsFilter);
		}
	}

	@Override
	public String getElementName(Object item) {
		ICodeElement element = (ICodeElement) item;
		return getLabel(element);
	}

	public void filterServices(Predicate<? super ICodeElement> filter) {
		if (services != null) {
			services = services.stream().filter(filter).toList();
		}
	}
}
