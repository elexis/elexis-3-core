package ch.elexis.core.console;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ch.elexis.core.services.IAccessControlService;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CmdAdvisor {

	String description();

	/**
	 * @return whether to execute this method as
	 *         {@link IAccessControlService#doPrivileged(Runnable)}
	 */
	boolean executePrivileged() default false;

}