package ch.elexis.core.ui.views.controls;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.data.Interaction;

public class InteractionLink {

	/**
	 *
	 */
	private Link interactionLink = null;
	static Logger logger = LoggerFactory.getLogger(InteractionLink.class);
	private String destUrl = StringUtils.EMPTY;
	private static int lastUpTime;

	public InteractionLink(Composite parent, int style) {
		interactionLink = new Link(parent, style);
		if (ConfigServiceHolder.getUser(Preferences.USR_SUPPRESS_INTERACTION_CHECK, true)) {
			setSuppressed();
		} else {
			// parent.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
			updateAtcs(new ArrayList<>());
		}
	}

	private void setSuppressed() {
		interactionLink.setText(StringUtils.EMPTY);
		interactionLink.setToolTipText(Messages.SuppressInteractionCheckTooltip);
		interactionLink.setForeground(UiDesk.getColorRegistry().get(UiDesk.COL_BLACK));

	}

	public String updateAtcs(List<IArticle> gtins) {
		interactionLink.setText(StringUtils.EMPTY);
		if (ConfigServiceHolder.getUser(Preferences.USR_SUPPRESS_INTERACTION_CHECK, true)) {
			setSuppressed();
			return StringUtils.EMPTY;
		}

		String severity = StringUtils.SPACE;
		Color color = UiDesk.getColor(UiDesk.COL_WHITE);
		String epha = Messages.VerrDetailDialog_InteractionEpha;
		String tooltip = StringUtils.EMPTY;
		StringBuilder buildUrl = new StringBuilder(Messages.VerrDetailDialog_InteractionBaseURL);

		ArrayList<String> atcs = new ArrayList<>();
		gtins.forEach(art -> {
			String atc = art.getAtcCode();
			buildUrl.append(art.getGtin());
			buildUrl.append(",");//$NON-NLS-1$
			if (atc != null && atc.length() >= 0) {
				atcs.add(art.getAtcCode());
			}
		});
		if (atcs.size() > 1) {
			destUrl = buildUrl.toString().replaceAll(",+$", StringUtils.EMPTY); //$NON-NLS-1$
			logger.info("For {} ATCs {} set destUrl to {}", atcs.size(), atcs, destUrl); //$NON-NLS-1$
		} else {
			destUrl = StringUtils.EMPTY;
		}

		// Reset tooltip text and color to nothing
		interactionLink.setText(Messages.VerrDetailDialog_NoInteractionKnown);
		interactionLink.setBackground(color);
		interactionLink.setVisible(true);
		interactionLink.setEnabled(true);
		interactionLink.setToolTipText(tooltip);
		interactionLink.setTouchEnabled(true);
		interactionLink.setForeground(UiDesk.getColorRegistry().get(UiDesk.COL_BLUE));
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
					}
				}
			}
			interactionLink.setToolTipText(tooltip);
			interactionLink.addListener(SWT.MouseUp, new Listener() {

				@Override
				public void handleEvent(Event event) {
					try {
						// Suppress action if event already seen
						if (event.time == lastUpTime) {
							logger.info("{} Skipping: {}", event.toString(), destUrl); //$NON-NLS-1$
							return;
						}
						lastUpTime = event.time;
						// zB. NOLVADEX, PAROXETIN, LOSARTAN, METOPROLOL with GTIN
						// 7680390530474 7680569620074, 7680589810141, 7680659580097 gives
						// https://matrix.epha.ch/#/7680390530474,7680569620074,7680589810141,7680659580097
						// or regnr "https://matrix.epha.ch/#/58392,59131,39053,58643"
						logger.info("{} destURL for external browser is: {}", event.toString(), destUrl); //$NON-NLS-1$
						PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(destUrl));
					} catch (PartInitException | MalformedURLException e) {
						e.printStackTrace();
					}
				}
			});
			if (!severity.contentEquals(StringUtils.SPACE)) {
				color = UiDesk.getColorFromRGB(Interaction.Colors.get(severity));
				interactionLink.setText(epha + ": " + Interaction.Ratings.get(severity)); //$NON-NLS-1$
				interactionLink.setBackground(color);
			}
		}
		return destUrl;
	}

	public void setLayoutData(GridData gridData) {
		interactionLink.setLayoutData(gridData);
	}
}