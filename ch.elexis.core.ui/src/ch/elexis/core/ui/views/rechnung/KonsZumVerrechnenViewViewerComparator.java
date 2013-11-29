/*******************************************************************************
 * Copyright (c) 2012 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.views.rechnung;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.Tree;

public class KonsZumVerrechnenViewViewerComparator extends ViewerComparator {
	@Override
	public int compare(Viewer viewer, Object e1, Object e2){
		Tree lt1 = (Tree) e1;
		Tree lt2 = (Tree) e2;
		
		if (lt1.contents instanceof Patient && lt2.contents instanceof Patient) {
			Patient p1 = (Patient) lt1.contents;
			Patient p2 = (Patient) lt2.contents;
			return (p1.getName().compareTo(p2.getName()));
		} else if (lt1.contents instanceof Fall && lt2.contents instanceof Fall) {
			Fall f1 = (Fall) lt1.contents;
			Fall f2 = (Fall) lt2.contents;
			TimeTool bd1 = new TimeTool(f1.getBeginnDatum());
			TimeTool bd2 = new TimeTool(f2.getBeginnDatum());
			return bd1.compareTo(bd2);
		} else if (lt1.contents instanceof Konsultation && lt2.contents instanceof Konsultation) {
			Konsultation k1 = (Konsultation) lt1.contents;
			Konsultation k2 = (Konsultation) lt2.contents;
			TimeTool kt1 = new TimeTool(k1.getDatum());
			TimeTool kt2 = new TimeTool(k2.getDatum());
			return kt1.compareTo(kt2);
		}
		return super.compare(viewer, e1, e2);
	}
}
