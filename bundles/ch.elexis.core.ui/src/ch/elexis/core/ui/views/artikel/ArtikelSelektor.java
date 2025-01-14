/*******************************************************************************
 * Copyright (c) 2005-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *
 *******************************************************************************/

package ch.elexis.core.ui.views.artikel;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.service.StockServiceHolder;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.ui.commands.EditEigenartikelUi;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.views.codesystems.CodeSystemDescription;
import ch.elexis.core.ui.views.provider.StockEntryLabelProvider;
import ch.elexis.data.PersistentObject;
import jakarta.inject.Inject;
import jakarta.inject.Named;

public class ArtikelSelektor extends ViewPart {
	public ArtikelSelektor() {
	}

	public static final String ID = "ch.elexis.ArtikelSelektor"; //$NON-NLS-1$
	CTabFolder ctab;
	TableViewer tv;

	@Override
	public void createPartControl(final Composite parent) {
		parent.setLayout(new GridLayout());
		ctab = new CTabFolder(parent, SWT.NONE);
		ctab.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		java.util.List<IConfigurationElement> list = Extensions
				.getExtensions(ExtensionPointConstantsUi.VERRECHNUNGSCODE); // $NON-NLS-1$
		ctab.addSelectionListener(new TabSelectionListener());
		for (IConfigurationElement ice : list) {
			if ("Artikel".equals(ice.getName())) { //$NON-NLS-1$
				Optional<CodeSystemDescription> description = CodeSystemDescription.of(ice);
				if (description.isPresent()) {
					CTabItem ci = new CTabItem(ctab, SWT.NONE);
					ci.setText(description.get().getCodeSystemName());
					ci.setData(description.get()); // $NON-NLS-1$
				}
			}
		}
		CTabItem ci = new CTabItem(ctab, SWT.NONE);
		Composite c = new Composite(ctab, SWT.NONE);
		c.setLayout(new GridLayout());
		ci.setControl(c);
		ci.setText(Messages.ArtikelSelector_stockArticles);
		Table table = new Table(c, SWT.SIMPLE | SWT.V_SCROLL);
		table.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		tv = new TableViewer(table);
		tv.setContentProvider(ArrayContentProvider.getInstance());
		tv.setLabelProvider(new StockEntryLabelProvider() {
			@Override
			public String getColumnText(Object element, int columnIndex) {
				IStockEntry se = (IStockEntry) element;
				if (se.getArticle() != null) {
					String ret = se.getArticle().getName();
					Long amount = StockServiceHolder.get().getCumulatedStockForArticle(se.getArticle());
					if (amount != null) {
						ret += " (" + Long.toString(amount) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
					}
					return ret;
				} else {
					return se.getLabel();
				}
			}
		});
		StockEntryLoader loader = new StockEntryLoader(tv);
		loader.schedule();
	}

	@Override
	public void setFocus() {
		// TODO Automatisch erstellter Methoden-Stub

	}

	@Override
	public void dispose() {

	}

	// class LagerLabelProvider extends DefaultLabelProvider implements
	// ITableLabelProvider {
	//
	// @Override
	// public Image getColumnImage(final Object element, final int columnIndex){
	// if (element instanceof Artikel) {
	// return null;
	// } else {
	// return Images.IMG_ACHTUNG.getImage();
	// }
	// }
	//
	// @Override
	// public String getColumnText(final Object element, final int columnIndex){
	// if (element instanceof Artikel) {
	// Artikel art = (Artikel) element;
	// Availability availability =
	// CoreHub.getStockService().getCumulatedAvailabilityForArticle(art);
	// String ret = art.getInternalName();
	// if (availability!=null) {
	// ret += " (" + availability.toString() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	// }
	// return ret;
	// }
	// return super.getColumnText(element, columnIndex);
	// }
	//
	// }

	@org.eclipse.e4.core.di.annotations.Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState) {
		CoreUiUtil.updateFixLayout(part, currentState);
	}

	private class TabSelectionListener extends SelectionAdapter {

		@Override
		public void widgetSelected(SelectionEvent e) {
			CTabItem top = ctab.getSelection();
			if (top != null) {
				if (top.getControl() == null) {
					CommonViewer cv = new CommonViewer();
					CodeSystemDescription description = (CodeSystemDescription) top.getData(); // $NON-NLS-1$
					ViewerConfigurer vc = description.getCodeSelectorFactory().createViewerConfigurer(cv);
					Composite c = new Composite(ctab, SWT.NONE);
					c.setLayout(new GridLayout());
					cv.create(vc, c, SWT.V_SCROLL, getViewSite());
					top.setControl(c);
					top.setData(cv);

					cv.addDoubleClickListener(new CommonViewer.PoDoubleClickListener() {

						@Override
						public void doubleClicked(final PersistentObject obj, final CommonViewer cv) {
							EditEigenartikelUi.executeWithParams(obj);
						}
					});
					vc.getContentProvider().startListening();
				}
			}

		}
	}

	private static class StockEntryLoader extends Job {
		private Viewer viewer;

		private List<ch.elexis.core.model.IStockEntry> loaded;

		public StockEntryLoader(Viewer viewer) {
			super("Stock loading ...");
			this.viewer = viewer;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("Stock loading ...", IProgressMonitor.UNKNOWN);
			loaded = StockServiceHolder.get().getAllStockEntries(false);
			loaded.sort(compareArticleLabel());

			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			monitor.done();
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					viewer.setInput(loaded);
				}
			});
			return Status.OK_STATUS;
		}

		private Comparator<IStockEntry> compareArticleLabel() {
			return Comparator.comparing(o -> o.getArticle() != null ? o.getArticle().getLabel() : null,
					Comparator.nullsLast(Comparator.naturalOrder()));
		}

	}
}
