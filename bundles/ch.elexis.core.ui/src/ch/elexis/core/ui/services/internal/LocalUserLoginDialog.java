/*******************************************************************************
 * Copyright (c) 2005-2019, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *    
 *******************************************************************************/

package ch.elexis.core.ui.services.internal;

import java.util.List;
import java.util.Optional;

import javax.security.auth.login.LoginException;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IUser;
import ch.elexis.core.ui.ILoginNews;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Anwender;
import ch.elexis.data.Query;
import ch.rgw.tools.ExHandler;

public class LocalUserLoginDialog extends TitleAreaDialog {
	
	private Text usr, pwd;
	private boolean hasUsers;
	private IUser user;
	
	public LocalUserLoginDialog(Shell parentShell){
		super(parentShell);
		
		Query<Anwender> qbe = new Query<Anwender>(Anwender.class);
		List<Anwender> list = qbe.execute();
		hasUsers = (list.size() > 1);
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout(2, false));
		Label lu = new Label(ret, SWT.NONE);
		
		lu.setText(Messages.LoginDialog_0);
		usr = new Text(ret, SWT.BORDER);
		usr.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		new Label(ret, SWT.NONE).setText(Messages.LoginDialog_1);
		pwd = new Text(ret, SWT.BORDER | SWT.PASSWORD);
		pwd.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		if (hasUsers == false) {
			usr.setText("Administrator"); //$NON-NLS-1$
			pwd.setText("admin"); //$NON-NLS-1$
		}
		
		@SuppressWarnings("unchecked")
		List<ILoginNews> newsModules =
			Extensions.getClasses(ExtensionPointConstantsUi.LOGIN_NEWS, "class");
		
		if (newsModules.size() > 0) {
			Composite cNews = new Composite(ret, SWT.NONE);
			cNews.setLayoutData(SWTHelper.getFillGridData(2, true, 1, true));
			cNews.setLayout(new GridLayout());
			for (ILoginNews lm : newsModules) {
				try {
					Composite comp = lm.getComposite(cNews);
					comp.setLayoutData(SWTHelper.getFillGridData());
				} catch (Exception ex) {
					// Note: This is NOT a fatal error. It just means, that the Newsmodule could not
					// load. Maybe we are offline.
					ExHandler.handle(ex);
					
				}
			}
			
		}
		
		return ret;
	}
	
	@Override
	protected void okPressed(){
		
		// load by local database
		String username = usr.getText();
		IUser _user = null;
		Optional<IUser> dbUser = CoreModelServiceHolder.get().load(username, IUser.class);
		if (dbUser.isPresent()) {
			_user = dbUser.get().login(username, pwd.getTextChars());
		}
		if (_user != null && _user.isActive()) {
			Anwender anwender = Anwender.load(_user.getAssignedContact().getId());
			if (anwender != null) {
				if (anwender.isValid()) {
					if (anwender.istAnwender()) {
						user = _user;
						super.okPressed();
						return;
					} else {
						LoggerFactory.getLogger(getClass()).error("username: {}", username,
							new LoginException("anwender is not a istAnwender"));
					}
				} else {
					LoggerFactory.getLogger(getClass()).error("username: {}", username,
						new LoginException("anwender is invalid or deleted"));
				}
				
			} else {
				LoggerFactory.getLogger(getClass()).error("username: {}", username,
					new LoginException("anwender is null"));
			}
		}
		
		setMessage(Messages.LoginDialog_4, IMessageProvider.ERROR);
		
	}
	
	@Override
	protected void cancelPressed(){
		// GlobalActions.exitAction?
		ContextServiceHolder.get().setActiveUser(null);
		CoreHub.actMandant = null;
		super.cancelPressed();
	}
	
	@Override
	public void create(){
		super.create();
		getButton(IDialogConstants.OK_ID).setText(Messages.LoginDialog_login);
		getButton(IDialogConstants.CANCEL_ID).setText(Messages.LoginDialog_terminate);
		// getButton(IDialogConstants.OK_ID).setEnabled(false);
		
	}
	
	public IUser getUser(){
		return user;
	}
	
}
