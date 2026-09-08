package ch.elexis.core.ui.mediorder.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.LoggerFactory;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class MediorderHistoryRenderer {

	private static final String MACRO_TEMPLATE = "online_mediorder_history.ftlh";
	private static final String PAGE_TEMPLATE = "online_mediorder_history_page.ftlh";
	private static final String CSS_RESOURCE = "style.css";
	private static final String RSC_PATH = "rsc/";

	private final Configuration configuration;
	private final String css;
	private final boolean initialized;

	public MediorderHistoryRenderer() {
		configuration = new Configuration(Configuration.VERSION_2_3_34);
		configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
		configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		configuration.setLogTemplateExceptions(false);
		configuration.setWrapUncheckedExceptions(true);
		configuration.setFallbackOnNullLoopVariable(false);

		StringTemplateLoader loader = new StringTemplateLoader();
		boolean loaded = false;
		String loadedCss = "";
		try {
			loader.putTemplate(MACRO_TEMPLATE, readResource(RSC_PATH + MACRO_TEMPLATE));
			loader.putTemplate(PAGE_TEMPLATE, readResource(RSC_PATH + PAGE_TEMPLATE));
			loadedCss = readResource(RSC_PATH + CSS_RESOURCE);
			loaded = true;
		} catch (IOException e) {
			LoggerFactory.getLogger(getClass()).error("Failed to load order history templates", e);
		}
		configuration.setTemplateLoader(loader);
		css = loadedCss;
		initialized = loaded;
	}

	public String renderSections(List<Map<String, Object>> sections) {
		if (initialized) {
			Map<String, Object> model = new HashMap<>();
			model.put("sections", sections != null ? sections : List.of());
			try {
				return process(model);
			} catch (IOException | TemplateException e) {
				LoggerFactory.getLogger(getClass()).error("Failed to render the history.", e);
			}

			try {
				return process(Map.of("error", Boolean.TRUE));
			} catch (IOException | TemplateException e) {
				LoggerFactory.getLogger(getClass()).error("Failed to render the history error message.", e);
			}
		}
		return "Failed to display the history.";
	}

	private String process(Map<String, Object> model) throws IOException, TemplateException {
		Map<String, Object> fullModel = new HashMap<>(model);
		fullModel.put("css", css);
		Template template = configuration.getTemplate(PAGE_TEMPLATE);
		StringWriter writer = new StringWriter();
		template.process(fullModel, writer);
		return writer.toString();
	}

	private String readResource(String path) throws IOException {
		Bundle bundle = FrameworkUtil.getBundle(getClass());
		if (bundle == null) {
			throw new IOException("Bundle for " + getClass().getName() + " is not available");
		}
		URL url = bundle.getEntry(path);
		if (url == null) {
			throw new IOException("Ressource " + path + " not found");
		}
		try (InputStream is = url.openStream()) {
			return new String(is.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
