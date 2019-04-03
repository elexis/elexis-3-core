package ch.elexis.core.console;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This abstract class eases implementation of console commands. In order to use it efficiently the
 * implementing class should only have ONE root method (starting with <code>_rootMethodName</code>.
 * Every method starting with <code>__submethod</code> is then treated as a parameter to the root
 * method. And every sub-method to sub-method (separated with a single underscore) is treated same.
 * 
 */
public class AbstractConsoleCommandProvider implements CommandProvider {
	
	public final Logger logger = LoggerFactory.getLogger(getClass());
	
	private Map<String, Method> methods;
	private String[] arguments;
	protected CommandInterpreter ci;
	
	private String[] subArguments;
	
	public String getArgument(int i){
		if (arguments.length >= i + 1) {
			return arguments[i];
		}
		return null;
	}
	
	public void executeCommand(String root, CommandInterpreter ci){
		this.ci = ci;
		
		if (methods == null) {
			initializeClassMethods();
		}
		
		arguments = collectArguments(root, ci);
		if (arguments.length == 0) {
			ci.println(getHelp(root));
			return;
		}
		
		// match method by longest signature
		Method method = null;
		int i;
		for (i = arguments.length; i > 0; i--) {
			String key = "__" + StringUtils.join(Arrays.copyOfRange(arguments, 0, i), "_");
			method = methods.get(key);
			if (method != null) {
				break;
			}
		}
		
		if (method == null) {
			printHelp(arguments);
			return;
		}
		
		subArguments = Arrays.copyOfRange(arguments, i, arguments.length);
		
		String joinedArguments = String.join(" ", arguments);
		ci.println("--( " + new Date() + " )---[cmd: " + joinedArguments + "]"
			+ getRelativeFixedLengthSeparator(joinedArguments, 100, "-"));
		
		try {
			Object result = null;
			if (method.getParameterCount() > 0) {
				Class<?> clazz = method.getParameterTypes()[0];
				if (clazz.equals(Iterator.class)) {
					NoThrowExceptionIterator<String> nullContinueIterator =
						new NoThrowExceptionIterator<>(Arrays.asList(subArguments).iterator());
					result = method.invoke(this, nullContinueIterator);
				} else if (clazz.equals(List.class)) {
					result = method.invoke(this, Arrays.asList(subArguments));
				} else if (clazz.equals(String.class)) {
					result = method.invoke(this, subArguments.length > 0 ? subArguments[0] : "");
				} else {
					ci.println("invalid parameter type "+clazz);
				}
			} else {
				result = method.invoke(this);
			}
			if (result instanceof String) {
				ci.println(result);
			}
		} catch (Exception e) {
			if(e.getCause()!= null) {
				ci.println("Execution error on argument: " + e.getCause().getMessage());
				logger.warn("Execution error on argument [{}]: ", arguments, e.getCause());
			} else {
				ci.println("Execution error on argument: " + e.getMessage());
				logger.warn("Execution error on argument [{}]: ", arguments, e);
			}
		}
	}
	
	private String[] collectArguments(String root, CommandInterpreter ci){
		String argument;
		List<String> argumentQ = new ArrayList<>();
		argumentQ.add(root);
		while ((argument = ci.nextArgument()) != null) {
			argumentQ.add(argument);
		}
		return argumentQ.toArray(new String[] {});
	}
	
	private void initializeClassMethods(){
		methods = new HashMap<>();
		for (Method method : this.getClass().getMethods()) {
			if (method.getName().startsWith("__")) {
				methods.put(method.getName(), method);
			}
		}
	}
	
	public String getRelativeFixedLengthSeparator(String value, int determinedLength,
		String separator){
		if (value == null) {
			return "";
		}
		if (value.length() > determinedLength) {
			determinedLength = value.length() + 1;
		}
		return String.join("", Collections.nCopies(determinedLength - value.length(), separator));
	}
	
	public String ok(){
		return "OK";
	}
	
	public String ok(Object object){
		return "OK [" + object + "]";
	}
	
	public String missingArgument(String string){
		return "Missing argument: " + string;
	}
	
	@Override
	public String getHelp(){
		return getHelp("");
	}
	
	public void printHelp(String... sub){
		ci.println(getHelp(sub));
	}
	
	public String getHelp(String... sub){
		String[] methodSignatures = methods.keySet().toArray(new String[] {});
		
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtils.join(sub, " "));
		sb.append(" ");
		
		Set<String> relevant = new HashSet<>();
		for (int i = 0; i < methodSignatures.length; i++) {
			String key = methodSignatures[i];
			String[] splitMethodNames = key.substring(2).split("_");
			
			List<String> subList = Arrays.asList(sub);
			List<String> asList = new ArrayList<>(Arrays.asList(splitMethodNames));
			int indexOfSubList = Collections.indexOfSubList(asList, subList);
			asList.removeAll(subList);
			if (indexOfSubList >= 0 && !asList.isEmpty()) {
				relevant.add(asList.get(0));
			}
		}
		
		if (relevant.isEmpty()) {
			return "Sub/Command not found: " + StringUtils.join(sub, " ");
		}
		
		sb.append("(" + relevant.stream().sorted(Comparator.naturalOrder())
			.reduce((u, t) -> u + " | " + t).orElse("") + ")");
		
		return sb.toString();
	}
	
	/**
	 * An {@link Iterator} that does not throw a {@link NoSuchElementException} but simply returns
	 * <code>null</code>.
	 *
	 * @param <E>
	 */
	private class NoThrowExceptionIterator<E> implements Iterator<E> {
		
		private final Iterator<E> iterator;
		
		public NoThrowExceptionIterator(Iterator<E> iterator){
			this.iterator = iterator;
		}
		
		@Override
		public boolean hasNext(){
			return iterator.hasNext();
		}
		
		@Override
		public E next(){
			try {
				return iterator.next();
			} catch (NoSuchElementException nse) {
				return null;
			}
		}
		
	}
	
}
