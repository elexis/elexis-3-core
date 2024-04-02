/*******************************************************************************
 * Copyright (c) 2009-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *******************************************************************************/

package ch.elexis.core.ui.util.viewers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.ui.selectors.ActiveControl;
import ch.elexis.core.ui.selectors.ActiveControlListener;
import ch.elexis.core.ui.selectors.ComboField;
import ch.elexis.core.ui.selectors.DateField;
import ch.elexis.core.ui.selectors.FieldDescriptor;
import ch.elexis.core.ui.selectors.IntegerField;
import ch.elexis.core.ui.selectors.MoneyField;
import ch.elexis.core.ui.selectors.SelectorPanel;
import ch.elexis.core.ui.selectors.TextField;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ControlFieldListener;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ControlFieldProvider;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.rgw.tools.IFilter;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.Tree;

/**
 * A ControlFieldProvider that creates a SelectorPanel
 *
 * @author Gerry Weirich
 *
 */
public class SelectorPanelProvider implements ControlFieldProvider {
	private LinkedList<ControlFieldListener> listeners = new LinkedList<>();
	private SelectorPanel panel;
	private FieldDescriptor<?>[] fields;
	private boolean bExclusive = false;
	private IAction[] actions = null;

	private int changeDelay;

	public SelectorPanelProvider(FieldDescriptor<?>[] fields, boolean bExlusive) {
		this.fields = fields;
		this.bExclusive = bExlusive;
		this.changeDelay = -1;
	}

	public void addActions(IAction... actions) {
		this.actions = actions;
	}

	public void addChangeListener(ControlFieldListener cl) {
		listeners.add(cl);
	}

	public void clearValues() {
		if (panel != null) {
			panel.clearValues();
		}
	}

	public Composite createControl(Composite parent) {
		if (actions == null) {
			panel = new SelectorPanel(parent);
		} else {
			panel = new SelectorPanel(parent, actions);
		}
		for (FieldDescriptor<?> field : fields) {
			ActiveControl ac = null;
			switch (field.getFieldType()) {
			case HYPERLINK:
			case STRING:
				ac = new TextField(panel.getFieldParent(), 0, field.getLabel(), field.getAssignedListenerEventType(),
						field.getAssignedListener());
				break;
			case CURRENCY:
				ac = new MoneyField(panel.getFieldParent(), 0, field.getLabel());
				break;
			case DATE:
				ac = new DateField(panel.getFieldParent(), 0, field.getLabel());
				break;

			case COMBO:
				ac = new ComboField(panel.getFieldParent(), 0, field.getLabel(), (String[]) field.getExtension());
				break;
			case INT:
				ac = new IntegerField(panel.getFieldParent(), 0, field.getLabel());
			}
			if (ac != null) {
				ac.setData(ActiveControl.PROP_FIELDNAME, field.getFieldname());
				ac.setData(ActiveControl.PROP_HASHNAME, field.getHashname());
				panel.addField(ac);
			}
		}
		/*
		 * if (actions != null) { panel.addActions(actions); }
		 */
		panel.setExclusive(bExclusive);
		panel.addSelectorListener(new ActiveControlListener() {

			public void contentsChanged(ActiveControl field) {
				fireChangedEvent();
			}

			public void titleClicked(ActiveControl field) {
				fireClickedEvent(field.getLabelText());
			}

			public void invalidContents(ActiveControl field) {
				// TODO Auto-generated method stub

			}
		});
		return panel;
	}

	public IFilter createFilter() {
		return new DefaultFilter(panel);
	}

	public void fireClickedEvent(final String fieldname) {
		for (ControlFieldListener cl : listeners) {
			cl.reorder(fieldname);
		}
	}

	private volatile Runnable delayedChanged;

	public void fireChangedEvent() {
		if (changeDelay > 0) {
			if (delayedChanged == null) {
				delayedChanged = new Runnable() {
					@Override
					public void run() {
						doFireChangedEvent();
						delayedChanged = null;
					}
				};
				Display.getDefault().timerExec(changeDelay, delayedChanged);
			}
		} else {
			doFireChangedEvent();
		}
	}

	private void doFireChangedEvent() {
		HashMap<String, String> hv = panel.getValues();
		for (ControlFieldListener cl : listeners) {
			cl.changed(hv);
		}
	}

	public void fireSortEvent(String text) {
		for (ControlFieldListener cl : listeners) {
			cl.reorder(text);
		}
	}

	public String[] getValues() {
		HashMap<String, String> vals = panel.getValues();
		String[] ret = new String[fields.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = vals.get(fields[i].getLabel());
		}
		return ret;
	}

	public boolean isEmpty() {
		HashMap<String, String> vals = panel.getValues();
		for (FieldDescriptor<?> fd : fields) {
			if (vals.get(fd.getLabel()).length() > 0) {
				return false;
			}
		}
		return true;
	}

	public void removeChangeListener(ControlFieldListener cl) {
		listeners.remove(cl);
	}

	public void setFocus() {
		List<ActiveControl> controls = panel.getControls();
		if (controls != null && !controls.isEmpty()) {
			// if available set the focus on the first control
			controls.get(0).getCtl().setFocus();
		} else {
			panel.setFocus();
		}
	}

	public void setQuery(final Query<? extends PersistentObject> q) {
		HashMap<String, String> vals = panel.getValues();
		for (FieldDescriptor<?> field : fields) {
			String name = field.getFieldname();
			String value = vals.get(name);
			if (!StringTool.isNothing(value)) {
				q.add(name, Query.LIKE, (field.isValueToLower() ? value.toLowerCase() : value) + "%", true); //$NON-NLS-1$
			}
		}
	}

	@Override
	public void setQuery(IQuery<?> query) {
		HashMap<String, String> vals = panel.getValues();
		for (FieldDescriptor<?> field : fields) {
			String name = field.getFieldname();
			String value = vals.get(name);
			if (!StringTool.isNothing(value)) {
				query.and(name, COMPARATOR.LIKE, (field.isValueToLower() ? value.toLowerCase() : value) + "%", //$NON-NLS-1$
						field.isIgnoreCase());
			}
		}
	}

	public SelectorPanel getPanel() {
		return panel;
	}

	static class DefaultFilter extends ViewerFilter implements IFilter {
		SelectorPanel slp;

		/**
		 * @param fields
		 */
		/**
		 * @param fields
		 */
		public DefaultFilter(SelectorPanel panel) {
			slp = panel;
		}

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			return select(element);
		}

		public boolean select(Object element) {
			PersistentObject po = null;
			if (element instanceof Tree) {
				po = (PersistentObject) ((Tree) element).contents;
			} else if (element instanceof PersistentObject) {
				po = (PersistentObject) element;
			} else {
				return false;
			}
			HashMap<String, String> vals = slp.getValues();
			if (po.isMatching(vals, PersistentObject.MATCH_START, true)) {
				return true;
			} else {
				if (element instanceof Tree) {
					Tree p = ((Tree) element).getParent();
					if (p == null) {
						return false;
					}
					return select(p);
				} else {
					return false;
				}
			}
		}

	}

	/**
	 * Set the delay of calling {@link SelectorPanelProvider#fireChangedEvent()}.
	 * Setting to 0 or -1 means no delay.
	 *
	 * @param delay
	 */
	public void setChangeDelay(int delay) {
		this.changeDelay = delay;
	}
}
