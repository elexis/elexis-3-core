package ch.elexis.core.tasks.test;

import java.time.LocalDate;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.model.builder.IUserBuilder;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.tasks.internal.model.service.CoreModelServiceHolder;
import ch.elexis.core.types.Gender;
import ch.elexis.core.utils.OsgiServiceUtil;

@RunWith(Suite.class)
@SuiteClasses({ TaskServiceTest.class, FilesystemChangeTriggerTest.class })
public class AllTests {

	private static IModelService taskModelService;
	private static TaskServiceTestUtil taskServiceTestUtil;

	private static IUser owner;

	@BeforeClass
	public static void beforeClass() {
		IPerson contact = new IContactBuilder.PersonBuilder(CoreModelServiceHolder.get(), "first", "last",
				LocalDate.now(), Gender.MALE).mandator().buildAndSave();
		owner = new IUserBuilder(CoreModelServiceHolder.get(), "testUser", contact).buildAndSave();

		taskServiceTestUtil = new TaskServiceTestUtil(getTaskModelService());
	}

	public static IModelService getTaskModelService() {
		if (taskModelService == null) {
			taskModelService = OsgiServiceUtil.getService(IModelService.class,
					"(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.tasks.model)").orElse(null);
		}
		return taskModelService;
	}

	public static IUser getOwner() {
		return owner;
	}

	public static TaskServiceTestUtil getTaskServiceTestUtil() {
		return taskServiceTestUtil;
	}

}
