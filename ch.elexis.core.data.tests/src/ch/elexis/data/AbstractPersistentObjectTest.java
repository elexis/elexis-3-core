package ch.elexis.data;

import static org.junit.Assert.assertNotNull;

import org.junit.BeforeClass;

import ch.elexis.ResourceManager;
import ch.elexis.core.data.activator.CoreHub;
import ch.rgw.io.InMemorySettings;
import ch.rgw.tools.JdbcLink;

public abstract class AbstractPersistentObjectTest {
	
	@BeforeClass
	public static void oneTimeSetUp(){
		CoreHub.localCfg = new InMemorySettings();
	}
	
	// create a JdbcLink with an initialized db for elexis
	// the creation script is taken from the rsc directory
	// of the host plugin when running a Plugin-Test
	protected JdbcLink initDB(){
		ResourceManager rsc = ResourceManager.getInstance();
		String pluginPath = rsc.getResourceLocationByName("/createDB.script");
		int end = pluginPath.lastIndexOf('/');
		end = pluginPath.lastIndexOf('/', end - 1);
		pluginPath = pluginPath.substring(0, end);
		/*
		 * PowerMockito.mockStatic(Hub.class);
		 * PowerMockito.when(Hub.getBasePath()).thenReturn(pluginPath);
		 * PowerMockito.when(Hub.getCfgVariant()).thenReturn("default");
		 * 
		 * PowerMockito.mockStatic(PreferenceInitializer.class);
		 * PowerMockito.when(PreferenceInitializer.getDefaultDBPath()).thenReturn(pluginPath);
		 */
		JdbcLink link = new JdbcLink("org.h2.Driver", "jdbc:h2:mem:test_mem", "hsql");
		assertNotNull(link);
		link.connect("", "");
		PersistentObject.connect(link);
		return link;
	}
}
