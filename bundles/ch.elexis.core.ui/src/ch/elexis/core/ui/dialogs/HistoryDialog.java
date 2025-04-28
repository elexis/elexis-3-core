package ch.elexis.core.ui.dialogs;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
// --- Jsoup-Imports ---
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import com.google.gson.Gson;

import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOutputLog;
import ch.elexis.core.model.OrderHistoryAction;
import ch.elexis.core.model.OrderHistoryEntry;
import ch.elexis.core.ui.util.OrderManagementUtil;

public class HistoryDialog extends Dialog {


	private static final String BASE_HTML = """
			<html>
			  <head>
			    <meta charset="UTF-8">
			    <style>
			      body {
			        font-family: Arial;
			        margin: 0;
			        padding: 0;
			      }
			      .header {
			        text-align: center;
			        margin-top: 10px;
			        font-size: 1.2em;
			        font-weight: bold;
			      }
			      .description {
			        text-align: center;
			        color: #555;
			        margin: 5px 0 10px 0;
			        font-size: 0.95em;
			      }

			      table {
			        border-collapse: collapse;
			        width: 100%;
			        font-family: Arial;
			        font-size: 14px;
			      }
			      th {
			        background-color: #9D9D9D;
			        color: white;
			      }
			    </style>
			  </head>
			  <body>
			  </body>
			</html>
			"""; //$NON-NLS-1$

    private IOrder order;
    private String historyContent;

    public HistoryDialog(Shell parentShell, IOrder order) {
        super(parentShell);
        this.order = order;
        this.historyContent = generateHistoryContent(order);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(order.getLabel());
        newShell.setSize(1400, 600);
        newShell.setMinimumSize(800, 400);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new FillLayout());
        Browser browser = new Browser(container, SWT.NONE);
        browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		browser.setText(historyContent);
        return container;
    }

    @Override
    protected int getShellStyle() {
        return super.getShellStyle() | SWT.RESIZE;
    }

    private String generateHistoryContent(IOrder order) {
        if (order == null) {
			return String.format("""
                   <html><body style="font-family: Arial;">
					   <p>%s</p>
                   </body></html>
					""", Messages.HistoryDialog_NoHistoryAvailable); //$NON-NLS-1$
        }

        IOutputLog logEntry = OrderManagementUtil.getOrderLogEntry(order);
        String jsonLog = (logEntry != null) ? logEntry.getOutputterStatus() : "[]"; //$NON-NLS-1$

        OrderHistoryEntry[] historyEntries;
        try {
            historyEntries = new Gson().fromJson(jsonLog, OrderHistoryEntry[].class);
        } catch (Exception e) {
            historyEntries = new OrderHistoryEntry[0];
        }

        if (historyEntries.length == 0) {
			return String.format("""
                   <html><body style="font-family: Arial;">
					   <p>%s</p>
                   </body></html>
					""", Messages.HistoryDialog_NoHistoryAvailable); //$NON-NLS-1$
        }

		Document doc = Jsoup.parse(BASE_HTML, StringUtils.EMPTY, Parser.htmlParser());
		doc.outputSettings().prettyPrint(false);
        Element body = doc.body();

		body.appendElement("div").addClass("header").text(Messages.HistoryDialog_Header); //$NON-NLS-1$ //$NON-NLS-2$

		body.appendElement("div").addClass("description").text(Messages.HistoryDialog_Description); //$NON-NLS-1$ //$NON-NLS-2$

        Element table = body.appendElement("table") //$NON-NLS-1$
            .attr("border", "1") //$NON-NLS-1$ //$NON-NLS-2$
            .attr("cellspacing", "0") //$NON-NLS-1$ //$NON-NLS-2$
				.attr("cellpadding", "5"); //$NON-NLS-1$ //$NON-NLS-2$

		Element headerRow = table.appendElement("tr"); //$NON-NLS-1$
		headerRow.appendElement("th").text(Messages.HistoryDialog_Action); //$NON-NLS-1$
		headerRow.appendElement("th").text(Messages.HistoryDialog_Details); //$NON-NLS-1$
		headerRow.appendElement("th").text(Messages.HistoryDialog_Date); //$NON-NLS-1$
		headerRow.appendElement("th").text(Messages.HistoryDialog_User); //$NON-NLS-1$

        fillTableRows(table, historyEntries);

        return doc.html();
    }

    private void fillTableRows(Element table, OrderHistoryEntry[] entries) {
        DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"); //$NON-NLS-1$

        for (int i = 0; i < entries.length; i++) {
            OrderHistoryEntry entry = entries[i];
            String timestamp = LocalDateTime.parse(entry.getTimestamp()).format(displayFormat);
            String userId = entry.getUserId();
			OrderHistoryAction action = entry.getAction(); // kann null sein bei alten Daten
			String icon = StringUtils.EMPTY;
			String actionText = StringUtils.EMPTY;
			if (action != null) {
				icon = action.getIcon();
				actionText = action.getTranslation();
			} else {
				// fallback auf Enum-Name, falls alte Daten
				actionText = ch.elexis.core.l10n.Messages.UNKNOWN;
			}
			String details = (entry.getDetails() != null)
					? entry.getDetails().replace(StringUtils.LF, StringUtils.SPACE)
					: StringUtils.EMPTY;
			String extraInfo = (entry.getExtraInfo() != null)
					? entry.getExtraInfo().replace(StringUtils.LF, StringUtils.SPACE)
					: StringUtils.EMPTY;
			String rowColor = (i % 2 == 0) ? "#f2f2f2" : ""; //$NON-NLS-1$ //$NON-NLS-2$
            Element row = table.appendElement("tr") //$NON-NLS-1$
                    .attr("style", "background-color: " + rowColor + ";"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			row.appendElement("td").html(icon + " <b>" + actionText + "</b>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			String detailsText = details + (!extraInfo.isEmpty() ? " (" + extraInfo + ")" : StringUtils.EMPTY); //$NON-NLS-1$ //$NON-NLS-2$
            row.appendElement("td").text(detailsText); //$NON-NLS-1$
            row.appendElement("td").html("<b>" + timestamp + "</b>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            row.appendElement("td").text(userId); //$NON-NLS-1$
        }
    }
}
