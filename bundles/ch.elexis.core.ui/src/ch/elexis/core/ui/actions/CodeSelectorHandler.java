/*******************************************************************************
 * Copyright (c) 2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation, based on original Code by D.Lutz
 *
 *******************************************************************************/

package ch.elexis.core.ui.actions;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

public class CodeSelectorHandler {
	private static CodeSelectorHandler theInstance;
	private ICodeSelectorTarget codeSelectorTarget;

	private CodeSelectorHandler() {
	}

	public static CodeSelectorHandler getInstance() {
		if (theInstance == null) {
			theInstance = new CodeSelectorHandler();
		}
		return theInstance;
	}

	/**
	 * Register a ICodeSelectorTarget. This is informed when an alement is chosen in
	 * a CodeSelector.
	 *
	 * @param target the ICodeSelectorTarget to set.
	 */
	public void setCodeSelectorTarget(final ICodeSelectorTarget target) {
		if (codeSelectorTarget != null) {
			codeSelectorTarget.registered(false);
		}
		codeSelectorTarget = target;
		codeSelectorTarget.registered(true);
	}

	/**
	 * Unregister the currently registered ICodeSelectorTarget.
	 */
	public void removeCodeSelectorTarget() {
		if (codeSelectorTarget != null) {
			codeSelectorTarget.registered(false);
		}

		codeSelectorTarget = null;
	}

	/**
	 * Reeturns the currently registered ICodeSelectorTarget.
	 *
	 * @return the registered ICodeSelectorTarget
	 */
	public ICodeSelectorTarget getCodeSelectorTarget() {
		return codeSelectorTarget;
	}

	public static abstract class CodeSelectorAdapter implements ICodeSelectorTarget {

		private StructuredViewer viewer;
		private Color normalColor;
		private Color highlightColor;

		public CodeSelectorAdapter(StructuredViewer viewer) {
			this.viewer = viewer;
			this.normalColor = viewer.getControl().getBackground();
			this.highlightColor = viewer.getControl().getDisplay().getSystemColor(SWT.COLOR_RED);
		}

		@Override
		public void registered(boolean registered) {
			if (!viewer.getControl().isDisposed()) {
				if (registered) {
					viewer.getControl().setBackground(highlightColor);
				} else {
					viewer.getControl().setBackground(normalColor);
				}
			}
		}
	}
}
