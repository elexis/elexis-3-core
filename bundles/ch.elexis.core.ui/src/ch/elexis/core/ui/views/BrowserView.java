package ch.elexis.core.ui.views;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.part.ViewPart;

import com.equo.chromium.swt.Browser;

import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.icons.Images;

public class BrowserView extends ViewPart {

	public static final String ID = "ch.elexis.core.ui.chromium.views.BrowserView";
	private Browser browser;
	private Combo addressBarCombo;
	private LinkedList<String> history = new LinkedList<>();
	private int currentHistoryIndex = -1;
	private static final String BROWSER_HISTORY_KEY = "browserHistory";
	private static final String BROWSER_FAVORIT_KEY = "browserFavorites";
	private static final String LAST_URL_KEY = "browserLastUrl";
	private static final String SEARCH_ENGINE_URL = "https://www.google.com/search?q=";
	private LinkedList<String> favorites = new LinkedList<>();
	private Composite favoritesBar;
	private static final int MAX_FAVORITES = 10;
	private static final int MAX_HISTORY_SIZE = 20;

	@Override
	public void createPartControl(Composite parent) {

		parent.setLayout(new GridLayout(5, false));
		Button backButton = createButton(parent, Images.IMG_PREVIOUS.getImage(), Messages.Web_Button_Back,
				b -> goBack());
		Button forwardButton = createButton(parent, Images.IMG_NEXT.getImage(), Messages.Web_Button_Forward,
				b -> goForward());
		Button reloadButton = createButton(parent, Images.IMG_REFRESH.getImage(), Messages.Web_Button_Reload,
				b -> browser.refresh());
		addressBarCombo = new Combo(parent, SWT.DROP_DOWN);
		addressBarCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		String lastUrl = ConfigServiceHolder.getUser(LAST_URL_KEY, "http://www.google.com");
		addressBarCombo.setText(lastUrl);
		addressBarCombo.addListener(SWT.Selection, event -> {
			int selectedIndex = addressBarCombo.getSelectionIndex();
			if (selectedIndex >= 0) {
				browser.setUrl(addressBarCombo.getItem(selectedIndex));
			}
		});
		addressBarCombo.addListener(SWT.Traverse, event -> {
			if (event.detail == SWT.TRAVERSE_RETURN) {
				String enteredText = addressBarCombo.getText();
				String urlToLoad = isValidUrl(enteredText) ? normalizeUrl(enteredText)
						: SEARCH_ENGINE_URL + encodeURIComponent(enteredText);
				browser.setUrl(urlToLoad);
				if (isValidUrl(urlToLoad)) {
					updateHistory(urlToLoad);
				} else {
					updateHistory(SEARCH_ENGINE_URL + encodeURIComponent(enteredText));
				}
			}
		});
		addressBarCombo.addListener(SWT.Traverse, event -> {
			if (event.detail == SWT.TRAVERSE_RETURN) {
				String enteredText = addressBarCombo.getText();
				loadUrlOrSearch(enteredText);
			}
		});
		addressBarCombo.addListener(SWT.MenuDetect, event -> createAddressBarContextMenu());
		Button favoritesButton = createButton(parent, Images.IMG_STAR.getImage(), Messages.Web_Button_Favoriten,
				b -> addFavorite(browser.getUrl()));
		favoritesBar = new Composite(parent, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1);
		gridData.heightHint = 30;
		favoritesBar.setLayoutData(gridData);
		favoritesBar.setLayout(new RowLayout(SWT.HORIZONTAL));
		browser = new Browser(parent, SWT.NONE);
		GridData browserLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		browserLayoutData.horizontalSpan = 4;
		browser.setLayoutData(browserLayoutData);
		browser.setUrl(addressBarCombo.getText());
		browser.addLocationListener(new LocationListener() {
			@Override
			public void changed(LocationEvent event) {
				Display.getCurrent().asyncExec(() -> {
					String newUrl = event.location;
					addressBarCombo.setText(newUrl);
					if (currentHistoryIndex >= 0 && !newUrl.equals(history.get(currentHistoryIndex))) {
						history.set(currentHistoryIndex, newUrl);
					} else {
						updateHistory(newUrl);
					}
					ConfigServiceHolder.setUser(LAST_URL_KEY, newUrl);
				});
			}

			@Override
			public void changing(LocationEvent event) {
			}
		});
		loadHistory();
		loadFavorites();
	}

	@Override
	public void setFocus() {
		addressBarCombo.setFocus();
	}

	private void updateHistory(String url) {
		if (currentHistoryIndex < 0 || !history.get(currentHistoryIndex).equals(url)) {
			if (history.size() >= MAX_HISTORY_SIZE) {
				history.removeLast();
				addressBarCombo.remove(addressBarCombo.getItemCount() - 1);
			}
			history.addFirst(url);
			currentHistoryIndex = 0;
			if (addressBarCombo.indexOf(url) < 0) {
				addressBarCombo.add(url, 0);
			}
		}
	}

	public void goBack() {

		if (currentHistoryIndex < history.size() - 1) {
			currentHistoryIndex++;
			String url = history.get(currentHistoryIndex);
			browser.setUrl(url);
			addressBarCombo.setText(url);
		}
	}

	public void goForward() {
		if (currentHistoryIndex > 0) {
			currentHistoryIndex--;
			String url = history.get(currentHistoryIndex);
			browser.setUrl(url);
			addressBarCombo.setText(url);
		}
	}

	@Override
	public void dispose() {
		saveHistory();
		super.dispose();
	}

	private void saveHistory() {
		String historyString = String.join(";", history);
		ConfigServiceHolder.setUser(BROWSER_HISTORY_KEY, historyString);
	}

	private void loadHistory() {
		String historyString = ConfigServiceHolder.getUser(BROWSER_HISTORY_KEY, "");
		if (!historyString.isEmpty()) {
			String[] urls = historyString.split(";");
			for (int i = urls.length - 1; i >= 0; i--) {
				String url = urls[i];
				history.addFirst(url);
				addressBarCombo.add(url, 0);
			}
			currentHistoryIndex = 0;
		}
	}

	private void addFavorite(String url) {
		if (favorites.size() >= MAX_FAVORITES) {
			Display.getCurrent().asyncExec(() -> {
				org.eclipse.swt.widgets.MessageBox messageBox = new org.eclipse.swt.widgets.MessageBox(
						Display.getCurrent().getActiveShell(), SWT.ICON_WARNING | SWT.OK);
				messageBox.setText(Messages.Web_Favorit_Limit);
				messageBox.setMessage(
						Messages.Web_Favorit_Limit_Text);
				messageBox.open();
			});
			return;
		}

		if (!favorites.contains(url)) {
			favorites.add(url);
			Button favoriteButton = createButton(favoritesBar, Images.IMG_STAR.getImage(), url, b -> {
				browser.setUrl(url);
				updateHistory(url);
			});
			favoriteButton.setText(getShortenedUrl(url));
			favoriteButton.setMenu(createFavoriteContextMenu(favoriteButton, url));
			favoritesBar.layout();
		}
		saveFavorites();
		}

	private String getShortenedUrl(String url) {
		try {
			java.net.URI uri = new java.net.URI(url);
			String domain = uri.getHost();
			domain = domain.startsWith("www.") ? domain.substring(4) : domain;
			if (domain.length() > 20) {
				return domain.substring(0, 13) + "...";
			} else {
				return domain;
			}
		} catch (Exception e) {
			if (url.length() > 15) {
				return url.substring(0, 13) + "...";
			} else {
				return url;
			}
		}
	}

	private void saveFavorites() {
		String favoritesString = String.join(";", favorites);
		ConfigServiceHolder.setUser(BROWSER_FAVORIT_KEY, favoritesString);
	}

	private void loadFavorites() {
		String favoritesString = ConfigServiceHolder.getUser(BROWSER_FAVORIT_KEY, "");
		if (!favoritesString.isEmpty()) {
			String[] urls = favoritesString.split(";");
			for (String url : urls) {
				addFavorite(url);
			}
		}
	}

	private Menu createFavoriteContextMenu(Button favoriteButton, String url) {
		Menu contextMenu = new Menu(favoriteButton);
		MenuItem deleteItem = new MenuItem(contextMenu, SWT.NONE);
		deleteItem.setText(Messages.Core_Delete_ellipsis);
		Image deleteImage = Images.IMG_FEHLER.getImage();
		deleteItem.setImage(deleteImage);
		deleteItem.addListener(SWT.Selection, e -> removeFavorite(url, favoriteButton));
		return contextMenu;
	}

	private void createAddressBarContextMenu() {
		Menu contextMenu = new Menu(addressBarCombo);
		MenuItem clearHistoryItem = new MenuItem(contextMenu, SWT.NONE);
		clearHistoryItem.setText(Messages.Web_History_Delete);
		clearHistoryItem.addListener(SWT.Selection, e -> clearHistory());
		addressBarCombo.setMenu(contextMenu);
	}

	private void clearHistory() {
		history.clear();
		addressBarCombo.removeAll();
		ConfigServiceHolder.setUser(BROWSER_HISTORY_KEY, null);
		currentHistoryIndex = -1;
	}

	private void removeFavorite(String url, Button favoriteButton) {
		favorites.remove(url);
		favoriteButton.dispose();
		favoritesBar.layout();
		saveFavorites();
	}

	private boolean isValidUrl(String url) {
		if (url.matches("^(http|https)://.*")) {
			try {
				new java.net.URL(url).toURI();
				return true;
			} catch (Exception e) {
				return false;
			}
		} else {
			return url.matches("[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
		}
	}

	private String normalizeUrl(String url) {
		if (!url.matches("^(http|https)://.*")) {
			return "https://" + url;
		}
		return url;
	}

	private String encodeURIComponent(String component) {
	   try {
	       return java.net.URLEncoder.encode(component, java.nio.charset.StandardCharsets.UTF_8.toString());
	   } catch (java.io.UnsupportedEncodingException e) {
			return component;
		}
	}

	private Button createButton(Composite parent, Image image, String toolTip, Consumer<Button> onClick) {
		return createButton(parent, image, toolTip, onClick, null);
	}

	private Button createButton(Composite parent, Image image, String toolTip, Consumer<Button> onClick,
			Object layoutData) {
		Button button = new Button(parent, SWT.PUSH);
		button.setImage(image);
		if (toolTip != null && !toolTip.isEmpty()) {
			button.setToolTipText(toolTip);
		}
		if (layoutData != null) {
			button.setLayoutData(layoutData);
		}
		button.addListener(SWT.Selection, e -> onClick.accept(button));
		return button;
	}

	private void loadUrlOrSearch(String enteredText) {
		String urlToLoad = isValidUrl(enteredText) ? normalizeUrl(enteredText)
				: SEARCH_ENGINE_URL + encodeURIComponent(enteredText);
		if (isUrlReachable(urlToLoad)) {
			browser.setUrl(urlToLoad);
			updateHistory(urlToLoad);
		} else {
			browser.setUrl(SEARCH_ENGINE_URL + encodeURIComponent(enteredText));
		}
	}

	private boolean isUrlReachable(String urlString) {
		try {
			URL url = new URL(urlString);
			HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
			urlConn.setConnectTimeout(1000);
			urlConn.setReadTimeout(1500);
			urlConn.connect();
			return (urlConn.getResponseCode() >= 200 && urlConn.getResponseCode() < 300);
		} catch (IOException e) {
			return false;
		}
	}
}
