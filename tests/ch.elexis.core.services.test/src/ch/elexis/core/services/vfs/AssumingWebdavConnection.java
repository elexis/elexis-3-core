package ch.elexis.core.services.vfs;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;

import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class AssumingWebdavConnection implements TestRule {

	private URL connectionUrl;

	public AssumingWebdavConnection(String connection) {
		try {
			String replaced = connection.replaceFirst("davs", "https");
			connectionUrl = new URL(replaced);
		} catch (MalformedURLException e) {
		}
	}

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				URLConnection conn = connectionUrl.openConnection();
				if (connectionUrl.getUserInfo() != null) {
					String basicAuth = "Basic "
							+ new String(Base64.getEncoder().encode(connectionUrl.getUserInfo().getBytes()));
					conn.setRequestProperty("Authorization", basicAuth);
				}
				try (InputStream is = conn.getInputStream()) {
					is.read();
				} catch (Exception e) {
					throw new AssumptionViolatedException("Could not connect. Skipping test!");
				}
				base.evaluate();
			}
		};
	}

}
