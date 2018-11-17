package ch.elexis.core.ui.views.controls;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.l10n.Messages;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.data.Interaction;
import ch.elexis.data.Artikel;

public class InteractionLink {

	/**
	 * 
	 */
	private Link interactionLink = null;
	static Logger logger = LoggerFactory.getLogger(InteractionLink.class);
	
	public InteractionLink(Composite parent, int style){
		interactionLink = new Link(parent, style);
		// parent.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		updateAtcs(new ArrayList<Artikel>());
	}
	
	public String updateAtcs(ArrayList<Artikel> gtins){
		String severity = " "; //$NON-NLS-1$
		Color color = UiDesk.getColor(UiDesk.COL_WHITE);
		String epha = Messages.VerrDetailDialog_InteractionEpha;
		String tooltip = ""; //$NON-NLS-1$
		StringBuilder destUrl = new StringBuilder(Messages.VerrDetailDialog_InteractionBaseURL);

		ArrayList<String> atcs = new ArrayList<String>();			
		gtins.forEach(art -> {
			atcs.add(art.getATC_code());
			destUrl.append(art.getGTIN() + ",");
		});
		// Reset tooltip text and color to nothing
		interactionLink.setText(Messages.VerrDetailDialog_NoInteractionKnown);
		interactionLink.setBackground(color);
		interactionLink.setVisible(true);
		interactionLink.setEnabled(true);
		interactionLink.setToolTipText(tooltip);
		interactionLink.setTouchEnabled(true);
		interactionLink.setForeground(UiDesk.getColorRegistry().get(UiDesk.COL_BLUE));
		interactionLink.addListener(SWT.MouseUp, new Listener() {
			@Override
			public void handleEvent(Event event){
				try {
					// zB. NOLVADEX, PAROXETIN, LOSARTAN, METOPROLOL with GTIN
					// 7680390530474 7680569620074, 7680589810141, 7680659580097 gives
					// https://matrix.epha.ch/#/7680390530474,7680569620074,7680589810141,7680659580097
					// or regnr "https://matrix.epha.ch/#/58392,59131,39053,58643"
					logger.info("destURL for external browser is: {}", destUrl.toString()); //$NON-NLS-1$
					PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser()
						.openURL(new URL(destUrl.toString()));
				} catch (PartInitException | MalformedURLException e) {
					e.printStackTrace();
				}
			}
			
		});
		if (atcs.size() > 1) {
			for (int j = 0; j < atcs.size(); j++) {
				for (int k = j + 1; k < atcs.size(); k++) {
					Interaction ia = Interaction.getByATC(atcs.get(j), atcs.get(k));
					if (ia == null) {
						// search also reverse
						ia = Interaction.getByATC(atcs.get(k), atcs.get(j));
					}
					if (ia == null) {
						continue;
					}
					String rating = ia.get(Interaction.FLD_SEVERITY);
					logger.trace("Add: {} {} res {}", rating, severity, //$NON-NLS-1$
						rating.compareTo(severity));
					
					if (severity.compareTo(rating) < 0) {
						severity = rating;
						String info = ia.get(Interaction.FLD_INFO);
						tooltip = String.format("%s\n%s\n%s\n%s", //$NON-NLS-1$
							Interaction.Ratings.get(severity), info, destUrl,
							Messages.VerrDetailDialog_InteractionTooltip);
						interactionLink.setToolTipText(tooltip);
					}
				}
			}
		}
		if (!severity.contentEquals(" ")) {//$NON-NLS-1$
			color = UiDesk.getColorFromRGB(Interaction.Colors.get(severity));
			interactionLink.setText(epha + ": " + Interaction.Ratings.get(severity)); //$NON-NLS-1$
			interactionLink.setBackground(color);
		}
		return destUrl.toString();
	}

	public void setLayoutData(GridData gridData){
		interactionLink.setLayoutData(gridData);
	}
}