/*******************************************************************************
 * Copyright (c) 2007-2018, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    T. Huster - updated
 *******************************************************************************/
package ch.elexis.core.ui.eigendiagnosen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.SWT;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.IDiagnosisTree;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.CommonViewerContentProvider;
import ch.elexis.core.ui.util.viewers.DefaultControlFieldProvider;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.core.ui.util.viewers.SimpleWidgetProvider;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ContentType;
import ch.elexis.core.ui.views.codesystems.CodeSelectorFactory;
import jakarta.inject.Inject;

public class EigendiagnoseSelector extends CodeSelectorFactory {

	private CommonViewer commonViewer;

	@Override
	public ViewerConfigurer createViewerConfigurer(CommonViewer cv) {
		commonViewer = cv;
		ViewerConfigurer vc = new ViewerConfigurer(new EigendiagnoseContentProvider(commonViewer),
				new DefaultLabelProvider(),
				new DefaultControlFieldProvider(commonViewer,
						new String[] { "code=" + Messages.Core_Short_Label, //$NON-NLS-1$
								"title=" + Messages.Core_Text }), //$NON-NLS-1$
				new ViewerConfigurer.DefaultButtonProvider(),
				new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_TREE, SWT.NONE, null));

		commonViewer.setNamedSelection("ch.elexis.core.ui.eigendiagnosen.selection"); //$NON-NLS-1$
		vc.setContentType(ContentType.GENERICOBJECT);
		return vc;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}

	@Override
	public String getCodeSystemName() {
		return Messages.Eigendiagnosen_CodeSystemName;
	}

	@Override
	public Class getElementClass() {
		return IDiagnosisTree.class;
	}

	@Inject
	@Optional
	public void reload(@UIEventTopic(ElexisEventTopics.EVENT_RELOAD) Class<?> clazz) {
		if (IDiagnosisTree.class.equals(clazz)) {
			if (commonViewer != null && !commonViewer.isDisposed()) {
				commonViewer.getViewerWidget().refresh();
			}
		}
	}

	@Optional
	@Inject
	public void update(@UIEventTopic(ElexisEventTopics.EVENT_UPDATE) IDiagnosisTree object) {
		if (commonViewer != null && object != null) {
			commonViewer.getViewerWidget().update(object, null);
		}
	}

	private class EigendiagnoseContentProvider extends CommonViewerContentProvider implements ITreeContentProvider {

		public EigendiagnoseContentProvider(CommonViewer commonViewer) {
			super(commonViewer);
		}

		@Override
		protected IQuery<?> getBaseQuery() {
			return ModelServiceHolder.get().getQuery(IDiagnosisTree.class);
		}

		@Override
		public Object[] getElements(Object inputElement) {
			// CommonViewer inputElement can be ignored
			List<IDiagnosisTree> roots = Collections.emptyList();
			@SuppressWarnings("unchecked")
			IQuery<IDiagnosisTree> query = (IQuery<IDiagnosisTree>) getBaseQuery();
			query.and("id", COMPARATOR.NOT_EQUALS, "VERSION"); //$NON-NLS-1$ //$NON-NLS-2$
			if (hasActiveFilter(fieldFilterValues)) {
				query.startGroup();
				for (String key : fieldFilterValues.keySet()) {
					query.and(key, COMPARATOR.LIKE, "%" + fieldFilterValues.get(key) + "%"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				query.andJoinGroups();
				if (fieldOrderBy != null) {
					query.orderBy(fieldOrderBy, fieldOrder);
				}
				List<IDiagnosisTree> found = query.execute();
				List<IDiagnosisTree> foundCopy = new ArrayList<>(found);
				roots = found.parallelStream().filter(d -> shouldBeVisible(d, foundCopy)).collect(Collectors.toList());
			} else {
				query.startGroup();
				query.or(ModelPackage.Literals.IDIAGNOSIS_TREE__PARENT, COMPARATOR.EQUALS, null);
				query.or(ModelPackage.Literals.IDIAGNOSIS_TREE__PARENT, COMPARATOR.EQUALS, "NIL"); //$NON-NLS-1$
				query.andJoinGroups();
				if (fieldOrderBy != null) {
					query.orderBy(fieldOrderBy, fieldOrder);
				}
				roots = query.execute();
			}
			return roots.toArray();
		}

		private boolean hasActiveFilter(Map<String, String> fieldFilterValues) {
			if (fieldFilterValues != null && !fieldFilterValues.isEmpty()) {
				for (String key : fieldFilterValues.keySet()) {
					String value = fieldFilterValues.get(key);
					if (StringUtils.isNotBlank(value)) {
						return true;
					}
				}
			}
			return false;
		}

		private boolean shouldBeVisible(IDiagnosisTree diagnosis, List<IDiagnosisTree> foundCopy) {
			List<IDiagnosisTree> children = diagnosis.getChildren();
			// leafs are always visible
			if (children.isEmpty()) {
				return true;
			}
			// if child is visible, parent is visible
			for (IDiagnosisTree child : children) {
				if (foundCopy.contains(child)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof IDiagnosisTree) {
				return ((IDiagnosisTree) parentElement).getChildren().toArray();
			}
			return null;
		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof IDiagnosisTree) {
				return ((IDiagnosisTree) element).getParent();
			}
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof IDiagnosisTree) {
				return !((IDiagnosisTree) element).getChildren().isEmpty();
			}
			return false;
		}
	}
}
