package io.quarkus.runtime;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * JUST A STUB. Replaced in myelexis-server with the real deal!
 */
@Target({ TYPE, METHOD, FIELD })
@Retention(RUNTIME)
public @interface Startup {

	/**
	 *
	 * @return the priority
	 * @see jakarta.annotation.Priority
	 */
	int value() default 0;

}
