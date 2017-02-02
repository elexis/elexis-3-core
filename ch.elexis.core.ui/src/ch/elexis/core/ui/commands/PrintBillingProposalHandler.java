package ch.elexis.core.ui.commands;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.LoggerFactory;

import ch.elexis.core.services.IFormattedOutput;
import ch.elexis.core.services.IFormattedOutputFactory;
import ch.elexis.core.services.IFormattedOutputFactory.ObjectType;
import ch.elexis.core.services.IFormattedOutputFactory.OutputType;
import ch.elexis.core.ui.views.rechnung.BillingProposalView;
import ch.elexis.core.ui.views.rechnung.BillingProposalView.ProposalLetter;

public class PrintBillingProposalHandler extends AbstractHandler implements IHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		BillingProposalView view = getOpenView(event);
		if (view != null) {
			ProgressMonitorDialog progress =
				new ProgressMonitorDialog(HandlerUtil.getActiveShell(event));
			try {
				progress.run(true, false, new IRunnableWithProgress() {
					
					@Override
					public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException{
						monitor.beginTask("PDF erzeugen", IProgressMonitor.UNKNOWN);
						ProposalLetter letter = view.getToPrint();
						BundleContext bundleContext =
							FrameworkUtil.getBundle(getClass()).getBundleContext();
						ServiceReference<IFormattedOutputFactory> serviceRef =
							bundleContext.getServiceReference(IFormattedOutputFactory.class);
						if (serviceRef != null) {
							IFormattedOutputFactory service = bundleContext.getService(serviceRef);
							IFormattedOutput outputter = service
								.getFormattedOutputImplementation(ObjectType.JAXB, OutputType.PDF);
							ByteArrayOutputStream pdf = new ByteArrayOutputStream();
							Map<String, String> parameters = new HashMap<>();
							parameters.put("current-date",
								LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
							
							outputter.transform(letter,
								getClass().getResourceAsStream("/rsc/xslt/proposal2fo.xslt"), pdf,
								parameters);
							bundleContext.ungetService(serviceRef);
							// save and open the file ...
							File file = null;
							FileOutputStream fout = null;
							try {
								file = File.createTempFile("proposal_", ".pdf");
								fout = new FileOutputStream(file);
								fout.write(pdf.toByteArray());
							} catch (IOException e) {
								Display.getDefault().syncExec(() -> {
									MessageDialog.openError(HandlerUtil.getActiveShell(event),
										"Fehler", "Fehler beim PDF anlegen.\n" + e.getMessage());
								});
								LoggerFactory.getLogger(getClass()).error("Error creating PDF", e);
							} finally {
								if (fout != null) {
									try {
										fout.close();
									} catch (IOException e) {
										// ignore
									}
								}
							}
							if (file != null) {
								Program.launch(file.getAbsolutePath());
							}
						}
						monitor.done();
					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				MessageDialog.openError(HandlerUtil.getActiveShell(event), "Fehler",
					"Fehler beim PDF erzeugen.\n" + e.getMessage());
				LoggerFactory.getLogger(getClass()).error("Error creating PDF", e);
			}
		}
		return null;
	}
	
	private BillingProposalView getOpenView(ExecutionEvent event){
		try {
			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
			IWorkbenchPage page = window.getActivePage();
			return (BillingProposalView) page.showView(BillingProposalView.ID);
		} catch (PartInitException e) {
			MessageDialog.openError(HandlerUtil.getActiveShell(event), "Fehler",
				"Konnte Rechnungs-Vorschlag View nicht öffnen");
		}
		return null;
	}
	
	@Override
	public boolean isEnabled(){
		return isFopServiceAvailable();
	}
	
	private boolean isFopServiceAvailable(){
		BundleContext bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
		return bundleContext.getServiceReference(IFormattedOutputFactory.class) != null;
	}
}
