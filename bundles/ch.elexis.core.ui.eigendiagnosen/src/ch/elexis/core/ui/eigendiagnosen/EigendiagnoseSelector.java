/*******************************************************************************
 * Copyright (c) 2007-2013, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *******************************************************************************/
package ch.elexis.core.ui.eigendiagnosen;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;

import ch.elexis.core.ui.eigendiagnosen.Messages;
import ch.elexis.core.ui.actions.JobPool;
import ch.elexis.core.ui.actions.LazyTreeLoader;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.DefaultControlFieldProvider;
import ch.elexis.core.ui.util.viewers.SimpleWidgetProvider;
import ch.elexis.core.ui.util.viewers.TreeContentProvider;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.views.codesystems.CodeSelectorFactory;
import ch.elexis.data.Eigendiagnose;
import ch.elexis.data.Query;

public class EigendiagnoseSelector extends CodeSelectorFactory {
	private LazyTreeLoader<Eigendiagnose> dataloader;
	private static final String LOADER_NAME = "Eigendiagnosen"; //$NON-NLS-1$
	
	@SuppressWarnings("unchecked")
	public EigendiagnoseSelector(){
		dataloader = (LazyTreeLoader<Eigendiagnose>) JobPool.getJobPool().getJob(LOADER_NAME);
		
		if (dataloader == null) {
			dataloader =
				new LazyTreeLoader<Eigendiagnose>(LOADER_NAME, new Query<Eigendiagnose>(
					Eigendiagnose.class),
					"parent", new String[] { Eigendiagnose.FLD_CODE, Eigendiagnose.FLD_TEXT}); //$NON-NLS-1$
			dataloader.setParentField(Eigendiagnose.FLD_CODE);
			JobPool.getJobPool().addJob(dataloader);
		}
		JobPool.getJobPool().activate(LOADER_NAME, Job.SHORT); //$NON-NLS-1$
		
	}
	
	@Override
	public ViewerConfigurer createViewerConfigurer(CommonViewer cv){
		ViewerConfigurer vc =
			new ViewerConfigurer(new TreeContentProvider(cv, dataloader),
				new ViewerConfigurer.TreeLabelProvider(), new DefaultControlFieldProvider(cv,
					new String[] {
						Messages.EigendiagnoseSelector_Shortcut_Label,
						Messages.EigendiagnoseSelector_Text_Label
					}), new ViewerConfigurer.DefaultButtonProvider(), new SimpleWidgetProvider(
					SimpleWidgetProvider.TYPE_TREE, SWT.NONE, null));
		return vc;
		
	}
	
	@Override
	public void dispose(){
		// TODO Auto-generated method stub
	}
	
	@Override
	public String getCodeSystemName(){
		return Messages.Eigendiagnosen_CodeSystemName;
	}
	
	@Override
	public Class getElementClass(){
		return Eigendiagnose.class;
	}
	
}
