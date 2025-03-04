
package ch.elexis.core.spotlight.ui.command;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.services.IContextService;
import ch.elexis.core.spotlight.ISpotlightService;
import ch.elexis.core.spotlight.ui.internal.ISpotlightResultEntryDetailCompositeService;
import ch.elexis.core.spotlight.ui.internal.SpotlightShell;
import ch.elexis.core.spotlight.ui.internal.ready.SpotlightReadyService;
import jakarta.inject.Named;

public class OpenSpotlightShellHandler {

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell, ParameterizedCommand command,
			EPartService partService, IContextService contextService, ISpotlightService spotlightService,
			SpotlightReadyService spotlightReadyService,
			ISpotlightResultEntryDetailCompositeService resultEntryDetailCompositeService) {

		Map<String, String> spotlightContextParameters = null;
		String filter = (String) command.getParameterMap()
				.get("ch.elexis.core.spotlight.ui.commandparameter.spotlightshellfilter");
		if (filter != null) {
			spotlightContextParameters = new HashMap<>();

			if ("patientFiltered".equals(filter)) {
				String patientId = contextService.getActivePatient().map(p -> p.getId()).orElse(null);
				if (patientId != null) {
					spotlightContextParameters.put(ISpotlightService.CONTEXT_FILTER_PATIENT_ID, patientId);
				}
			}
		}

		SpotlightShell spotlightShell = new SpotlightShell(shell, partService, spotlightService,
				resultEntryDetailCompositeService, spotlightReadyService, spotlightContextParameters);

		// center on the screen
		Monitor primary = spotlightShell.getDisplay().getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = spotlightShell.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;

		y -= 100; // slightly more to the top - dialog will get bigger on opening results

		spotlightShell.setLocation(x, y);

		spotlightShell.open();
	}

	/**
	 * Creates a region that delineates a rounded rectangle:
	 *
	 * @param x the initial X corner (of the original rectangle)
	 * @param y the initial Y corner (of the original rectangle)
	 * @param W the max width of the rectangle
	 * @param H the max height of the rectangle
	 * @param r the radius of the rounding circles
	 * @return the following region:
	 *
	 *         <pre>
	 *       P0 (x,y)
	 *       . ___ _ _ _ _ _ _ _ _ _ _ _ ___
	 *        /   \                     /   \    A
	 *       |  ·  |                   |  ·  |   :
	 *        \___/                     \___/    :
	 *       |   <->                         |   :
	 *            r                              :
	 *       |                               |   :
	 *                                           :
	 *       |                               |   : H
	 *                                           :
	 *       |                               |   :
	 *                                           :
	 *       |                               |   :
	 *                                           :
	 *       | ___                       ___ |   :
	 *        /   \                     /   \    :
	 *       |  ·  |                   |  ·  |   :
	 *        \___/ _ _ _ _ _ _ _ _ _ _ \___/    v
	 *
	 *       <------------------------------->
	 *                       W
	 *         </pre>
	 *
	 * @see https://stackoverflow.com/questions/22431269/swt-shell-with-round-corners
	 */
	public static Region createRoundedRectangle(int x, int y, int W, int H, int r) {
		// TODO apply?
		Region region = new Region();
		int d = (2 * r); // diameter

		region.add(circle(r, (x + r), (y + r)));
		region.add(circle(r, (x + W - r), (y + r)));
		region.add(circle(r, (x + W - r), (y + H - r)));
		region.add(circle(r, (x + r), (y + H - r)));

		region.add((x + r), y, (W - d), H);
		region.add(x, (y + r), W, (H - d));

		return region;
	}

	/**
	 * Defines the coordinates of a circle.
	 *
	 * @param r       radius
	 * @param offsetX x offset of the centre
	 * @param offsetY y offset of the centre
	 * @return the set of coordinates that approximates the circle.
	 * @see https://git.eclipse.org/c/platform/eclipse.platform.swt.git/tree/examples/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet134.java
	 */
	public static int[] circle(int r, int offsetX, int offsetY) {
		int[] polygon = new int[8 * r + 4];
		// x^2 + y^2 = r^2
		for (int i = 0; i < 2 * r + 1; i++) {
			int x = i - r;
			int y = (int) Math.sqrt(r * r - x * x);
			polygon[2 * i] = offsetX + x;
			polygon[2 * i + 1] = offsetY + y;
			polygon[8 * r - 2 * i - 2] = offsetX + x;
			polygon[8 * r - 2 * i - 1] = offsetY - y;
		}
		return polygon;
	}

}