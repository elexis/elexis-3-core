/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    D. Lutz	 - DBBased Importer
 *
 *******************************************************************************/

package ch.elexis.core.ui.util;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.ac.ObjectEvaluatableACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.services.holder.AccessControlServiceHolder;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.wizards.DBImportWizard;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLinkException;
import ch.rgw.tools.Result;
import ch.rgw.tools.StringTool;

/**
 * Dies ist die Basisklasse für Importfunktionen von Plugins.
 *
 * @author gerry
 *
 */
public abstract class ImporterPage implements IExecutableExtension {

	protected Log log = Log.get("Import"); //$NON-NLS-1$

	public String[] results;

	/** Nur intern gebraucht; kann bei Bedarf überschrieben oder erweitert werden */
	@Override
	public void setInitializationData(final IConfigurationElement config, final String propertyName, final Object data)
			throws CoreException {
	}

	/**
	 * Importer starten
	 *
	 * @param waitUntilFinished true: Kehrt erst zurück, wenn Import beendet
	 */
	public void run(final boolean waitUntilFinished) {
		ImporterJob job = new ImporterJob();
		job.schedule();
		if (waitUntilFinished) {
			try {
				job.join();
			} catch (InterruptedException e) {
				ExHandler.handle(e);
			}
		}
	}

	/**
	 * Hier muss die eigentliche Arbeit erledigt werden
	 */
	public abstract IStatus doImport(IProgressMonitor monitor) throws Exception;

	/** Ein Titel, der auf der Titelzeile des Importers erscheint */
	public abstract String getTitle();

	/** Eine längere Beschreibung für den Message-Bereich des Dialogs */
	public abstract String getDescription();

	/**
	 * @return The class names of the imported object. Only if all of the accounted
	 *         class names have {@link Right#CREATE}, {@link Right#READ},
	 *         {@link Right#UPDATE}, {@link Right#DELETE} and {@link Right#IMPORT}
	 *         will be executed.
	 * @since 3.12
	 */
	public abstract List<String> getObjectClass();

	/**
	 * Allfällige von User eingegebene Daten einsammeln. Die Default-Implementation
	 * tut nichts.
	 */
	public void collect() {
	}

	/**
	 * Die Dialogseite erstellen, um ggf. eine Datenquelle auszuwählen oder weitere
	 * Erläuterungen zu geben.
	 *
	 * @param parent Achtung: Hat schon ein GridlLayout, darf nicht geändert werden.
	 */
	public abstract Composite createPage(Composite parent);

	public class ImporterJob extends Job {
		ImporterJob() {
			super(getTitle());
			setPriority(Job.LONG);
			setUser(true);
		}

		@Override
		protected IStatus run(final IProgressMonitor monitor) {

			try {
				List<String> objectClass = getObjectClass();
				boolean allRightsOk = objectClass.stream()
						.map(type -> AccessControlServiceHolder.get()
								.evaluate(new ObjectEvaluatableACE(type, Right.IMPORT).and(Right.CREATE)
										.and(Right.UPDATE).and(Right.DELETE).and(Right.READ)))
						.allMatch(n -> n == true);
				if (!allRightsOk) {
					throw new Exception("Insufficient rights [requires crudi] on objects " + objectClass);
				}
				return doImport(monitor);
			} catch (Exception e) {
				return new Status(Status.ERROR, Hub.PLUGIN_ID,
						Messages.Core_Error_while_importing + StringUtils.SPACE + e.getMessage(), e); // $NON-NLS-2$
			}
		}
	}

	/**
	 * An importer that lets the user select a file to import. Can be built into
	 * derived ImporterPages
	 *
	 * @author gerry
	 *
	 */
	public static class FileBasedImporter extends Composite {

		private Label lFile;
		public Text tFname;
		private String[] filterExts = { "*" }; //$NON-NLS-1$
		private String[] filterNames = { Messages.Core_All_Files };

		private String result;

		public FileBasedImporter(final Composite parent, final ImporterPage home) {
			this(parent, home, false);
		}

		/**
		 * @since 3.7
		 */
		public FileBasedImporter(final Composite parent, final ImporterPage home, boolean supportMultiFileSelection) {
			super(parent, SWT.BORDER);
			setLayout(new GridLayout(1, false));
			lFile = new Label(this, SWT.NONE);
			tFname = new Text(this, SWT.BORDER);
			tFname.setText(CoreHub.localCfg.get("ImporterPage/" + home.getTitle() + "/filename", StringUtils.EMPTY)); //$NON-NLS-1$ //$NON-NLS-2$
			home.results = new String[1];
			home.results[0] = tFname.getText();
			lFile.setText(Messages.ImporterPage_file); // $NON-NLS-1$
			lFile.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			tFname.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			tFname.addModifyListener(new ModifyListener() {
				/** {@inheritDoc} */
				@Override
				public void modifyText(ModifyEvent event) {
					String filename = tFname.getText();
					if (new File(filename).isFile()) {
						home.results[0] = filename;
					}
				}
			});

			Button bFile = new Button(this, SWT.PUSH);
			bFile.setText(Messages.ImporterPage_browse); // $NON-NLS-1$
			// bFile.setLayoutData(SWTHelper.getFillGridData(2,true,1,false));
			bFile.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					int style = (supportMultiFileSelection) ? SWT.OPEN | SWT.MULTI : SWT.OPEN;
					FileDialog fdl = new FileDialog(parent.getShell(), style);
					fdl.setFilterExtensions(filterExts);
					fdl.setFilterNames(filterNames);
					fdl.open();
					String[] fileNames = fdl.getFileNames();
					if (fileNames != null && fileNames.length > 0) {
						if (fileNames.length > 1) {
							tFname.setText(fileNames[0] + " and more ...");
						} else {
							tFname.setText(fileNames[0]);
						}

						home.results = new String[fileNames.length];
						for (int i = 0; i < fileNames.length; i++) {
							home.results[i] = fdl.getFilterPath() + File.separator + fileNames[i];
							result = home.results[i];
						}

						CoreHub.localCfg.set("ImporterPage/" + home.getTitle() + "/filename", fileNames[0]); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			});

		}

		public void setFilter(final String[] extensions, final String[] names) {
			filterExts = extensions;
			filterNames = names;
		}

		public void setText(String text) {
			lFile.setText(text);
		}

		/**
		 * Get filename including path for single file selection result.
		 * 
		 * @return
		 */
		public String getResult() {
			return result;
		}
	}

	/**
	 * An Importer that lets the user select a directory to import from. Can be
	 * built into derived ImporterPages
	 *
	 * @author gerry
	 *
	 */
	public static class DirectoryBasedImporter extends Composite {
		public Text tFname;

		public DirectoryBasedImporter(final Composite parent, final ImporterPage home) {
			super(parent, SWT.NONE);
			setLayout(new GridLayout(1, false));
			final Label lFile = new Label(this, SWT.NONE);
			tFname = new Text(this, SWT.BORDER);
			lFile.setText(Messages.ImporterPage_dir); // $NON-NLS-1$
			lFile.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			tFname.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			tFname.setText(CoreHub.localCfg.get("ImporterPage/" + home.getTitle() + "/dirname", StringUtils.EMPTY)); //$NON-NLS-1$ //$NON-NLS-2$
			home.results = new String[1];
			home.results[0] = tFname.getText();
			Button bFile = new Button(this, SWT.PUSH);
			bFile.setText(Messages.ImporterPage_browse); // $NON-NLS-1$
			// bFile.setLayoutData(SWTHelper.getFillGridData(2,true,1,false));
			bFile.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					DirectoryDialog fdl = new DirectoryDialog(parent.getShell(), SWT.OPEN);
					String filename = fdl.open();
					if (filename != null) {
						tFname.setText(filename);
						home.results[0] = filename;
						CoreHub.localCfg.set("ImporterPage/" + home.getTitle() + "/dirname", filename); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}

			});

		}
	}

	/**
	 * An importer that lets the user select an ODBC data source to import. Can be
	 * build into derived ImporterPages
	 *
	 * @author danlutz
	 *
	 */
	static public class ODBCBasedImporter extends Composite {
		public Text tSource;

		public ODBCBasedImporter(final Composite parent, final ImporterPage home) {
			super(parent, SWT.NONE);
			setLayout(new GridLayout());
			final Label lSource = new Label(this, SWT.NONE);
			tSource = new Text(this, SWT.BORDER);
			lSource.setText(Messages.ImporterPage_source); // $NON-NLS-1$
			tSource.setEditable(false);
			lSource.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			tSource.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			tSource.setText(
					CoreHub.localCfg.get("ImporterPage/" + home.getTitle() + "/ODBC-Source", StringUtils.EMPTY)); //$NON-NLS-1$ //$NON-NLS-2$
			home.results = new String[1];
			home.results[0] = tSource.getText();
			Button bSource = new Button(this, SWT.PUSH);
			bSource.setText(Messages.ImporterPage_enter); // $NON-NLS-1$
			bSource.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					InputDialog in = new InputDialog(parent.getShell(), Messages.ImporterPage_odbcSource, // $NON-NLS-1$
							Messages.ImporterPage_pleaseEnterODBC, null, null); // $NON-NLS-1$
					if (in.open() == Dialog.OK) {
						tSource.setText(in.getValue());
						home.results[0] = in.getValue();
						CoreHub.localCfg.set("ImporterPage/" + home.getTitle() + "/ODBC-Source", home.results[0]); //$NON-NLS-1$ //$NON-NLS-2$
					}

				}

			});

		}
	};

	static public class DBBasedImporter extends Composite {
		public Text tSource;
		ImporterPage h;

		public DBBasedImporter(final Composite parent, final ImporterPage home) {
			super(parent, SWT.NONE);
			h = home;
			setLayout(new GridLayout());
			final Label lSource = new Label(this, SWT.NONE);
			final String[] preset = CoreHub.localCfg.getStringArray("ImporterPage/" + home.getTitle() + "/database"); //$NON-NLS-1$ //$NON-NLS-2$
			tSource = new Text(this, SWT.BORDER);
			lSource.setText(Messages.ImporterPage_source); // $NON-NLS-1$
			tSource.setEditable(false);
			lSource.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			tSource.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			tSource.setText(StringUtils.EMPTY);
			if (preset != null) {
				tSource.setText(preset[0]);
			}
			home.results = new String[5];
			for (int i = 0; i < home.results.length; i++) {
				home.results[i] = null;
			}
			Button bSource = new Button(this, SWT.PUSH);
			bSource.setText(Messages.ImporterPage_selectDB); // $NON-NLS-1$
			bSource.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					DBImportWizard wizard = new DBImportWizard(preset);
					WizardDialog wd = new WizardDialog(getShell(), wizard);
					if (wd.open() == Dialog.OK) {
						String type = wizard.getType();
						tSource.setText(type);
						home.results[0] = type;
						home.results[1] = wizard.getServer();
						home.results[2] = wizard.getDb();
						home.results[3] = wizard.getUser();
						home.results[4] = wizard.getPassword();
						CoreHub.localCfg.set("ImporterPage/" + home.getTitle() + "/database", //$NON-NLS-1$ //$NON-NLS-2$
								StringTool.join(home.results, ",")); //$NON-NLS-1$
						CoreHub.localCfg.flush();
					}
				}
			});
		}

		public Result<JdbcLink> getConnection() {
			JdbcLink ret = null;
			if (h.results[0].equalsIgnoreCase("mysql")) { //$NON-NLS-1$
				ret = JdbcLink.createMySqlLink(h.results[1], h.results[2]);
			} else if (h.results[0].equalsIgnoreCase("postgresql")) { //$NON-NLS-1$
				ret = JdbcLink.createPostgreSQLLink(h.results[1], h.results[2]);
			} else if (h.results[0].equalsIgnoreCase("h2")) { //$NON-NLS-1$
				ret = JdbcLink.createH2Link(h.results[1]);
			} else if (h.results[0].equalsIgnoreCase("odbc")) { //$NON-NLS-1$
				ret = JdbcLink.createODBCLink(h.results[1]);
			} else {
				return new Result<>(Result.SEVERITY.ERROR, 1, Messages.ImporterPage_unknownType, null, true); // $NON-NLS-1$
			}
			if (ret != null) {
				try {
					ret.connect(h.results[3], h.results[4]);
					return new Result<>(ret);
				} catch (JdbcLinkException je) {
					// ignore this and fallback to next return statement
				}
			}
			return new Result<>(Result.SEVERITY.ERROR, 2, Messages.ImporterPage_couldntConnect, ret, true); // $NON-NLS-1$
		}
	}
}
