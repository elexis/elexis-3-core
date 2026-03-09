package ch.elexis.core.tasks.model;

import com.cronutils.builder.CronBuilder;

import ch.elexis.core.model.tasks.IIdentifiedRunnable;

public class TaskTriggerTypeParameter {

	/**
	 * @see TaskTriggerType#CRON
	 */
	public static final class CRON {

		/**
		 * Configuration schema key for crontab. Use {@link CronBuilder} to generate the
		 * respective value
		 */
		public static final String SCHEMA = "cron";

	};

	/**
	 * @see TaskTriggerType#FILESYSTEM_CHANGE
	 */
	public static final class FILESYSTEM_CHANGE {
		/**
		 * The URL to watch
		 */
		public static final String URL = IIdentifiedRunnable.RunContextParameter.STRING_URL;
		/**
		 * The list filter to apply. E.g. <code>pdf</code> for all files ending with pdf
		 */
		public static final String FILE_EXTENSION_FILTER = "fileExtensionFilter";
	}

}
