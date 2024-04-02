/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *
 *******************************************************************************/

package ch.elexis.core.ui.views.rechnung;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.rgw.tools.Money;
import ch.rgw.tools.Tree;

public class VerrDetailDialog extends TitleAreaDialog {
	Patient pat;
	Tree tree;
	Hashtable<Fall, List<Konsultation>> faelle;

	@SuppressWarnings("unchecked")
	public VerrDetailDialog(Shell shell, Tree subTree) {
		super(shell);
		Object o = subTree.contents;
		this.tree = subTree;
		if (o instanceof Patient) {
			pat = (Patient) o;
		} else if (o instanceof Fall) {
			pat = ((Fall) o).getPatient();
			this.tree = subTree.getParent();
		} else if (o instanceof Konsultation) {
			Fall fall = ((Konsultation) o).getFall();
			pat = fall.getPatient();
			this.tree = subTree.getParent().getParent();
		}
		faelle = new Hashtable<>();
		for (Object of : this.tree.getChildren()) {
			Tree tFall = (Tree) of;
			Fall fall = (Fall) tFall.contents;
			Collection<Tree> c = tFall.getChildren();
			LinkedList<Konsultation> lKons = new LinkedList<>();
			for (Tree tKons : c) {
				lKons.add((Konsultation) tKons.contents);
			}
			faelle.put(fall, lKons);
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new FillLayout());
		TreeViewer tv = new TreeViewer(ret, SWT.V_SCROLL);
		tv.setContentProvider(new ITreeContentProvider() {

			public Object[] getChildren(Object parentElement) {
				Tree[] ret = (Tree[]) ((Tree) parentElement).getChildren().toArray(new Tree[0]);
				Arrays.sort(ret, new Comparator<Tree>() {
					public int compare(Tree t1, Tree t2) {
						if (t1.contents instanceof Konsultation) {
							Konsultation k1 = (Konsultation) t1.contents;
							return k1.compareTo((Konsultation) t2.contents);
						}
						return 0;
					}
				});
				return ret;
			}

			public Object getParent(Object element) {
				return ((Tree) element).getParent();
			}

			public boolean hasChildren(Object element) {
				return ((Tree) element).hasChildren();
			}

			public Object[] getElements(Object inputElement) {
				Tree[] ret = (Tree[]) tree.getChildren().toArray(new Tree[0]);
				return ret;
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

		});
		tv.setLabelProvider(new LabelProvider() {

			@Override
			public String getText(Object element) {
				Object o = ((Tree) element).contents;
				if (o instanceof Fall) {
					Fall f = (Fall) o;
					Money sum = new Money();
					List<Konsultation> list = faelle.get(f);
					if (list != null) {
						for (Konsultation k : list) {
							sum.addMoney(calcKons(k));
						}
					}
					return f.getLabel() + " - " + sum.getAmountAsString(); //$NON-NLS-1$
				} else if (o instanceof Konsultation) {
					Konsultation k = (Konsultation) o;
					return k.getLabel() + " - " + calcKons(k).getAmountAsString(); //$NON-NLS-1$
				}
				return super.getText(element);
			}

		});
		tv.setInput(this);
		return ret;
	}

	private Money calcKons(Konsultation k) {
		IEncounter encounter = NoPoUtil.loadAsIdentifiable((Konsultation) k, IEncounter.class).get();
		Money ret = new Money();
		for (IBilled v : encounter.getBilled()) {
			ret.addMoney(v.getTotal());
		}
		return ret;
	}

	@Override
	public void create() {
		super.create();
		if (pat != null) {
			setTitle(pat.getLabel());
		} else {
			setTitle(Messages.Core_No_patient_selected); // $NON-NLS-1$
		}
		setMessage(Messages.VerrDetailDialog_detailsOfOpenKons); // $NON-NLS-1$
		getShell().setText(Messages.VerrDetailDialog_billingData); // $NON-NLS-1$
	}

	@Override
	protected boolean isResizable() {
		return true;
	}
}
