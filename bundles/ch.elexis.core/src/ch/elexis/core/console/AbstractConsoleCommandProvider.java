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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
public abstract class AbstractConsoleCommandProvider implements CommandProvider {
	
	public final Logger logger = LoggerFactory.getLogger(getClass());
	
	private static Map<String, Method> methods = new HashMap<>();
	private static LinkedHashMap<String, String> commandsHelp = new LinkedHashMap<>();
	private String[] arguments;
	protected CommandInterpreter ci;
	
	private String[] subArguments;
	
	private String outcomeMessage;
	
	protected void register(Class<?> clazz){
		for (Method method : clazz.getMethods()) {
			
			if (method.getName().startsWith("__")) {
				methods.put(method.getName(), method);
			} else if (method.getName().startsWith("_")) {
				CmdAdvisor advisor = method.getDeclaredAnnotation(CmdAdvisor.class);
				String description = (advisor != null) ? advisor.description() : "";
				commandsHelp.put(method.getName().substring(1), description);
			}
			
		}
	}
	
	public String getArgument(int i){
		if (arguments.length >= i + 1) {
			return arguments[i];
		}
		return null;
	}
	
	public void executeCommand(String root, CommandInterpreter ci){
		this.ci = ci;
		
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
			outcomeMessage = null;
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
					ci.println("invalid parameter type " + clazz);
				}
			} else {
				result = method.invoke(this);
			}
			if (result instanceof String) {
				ci.println(result);
			}
			if (outcomeMessage != null) {
				ci.println(outcomeMessage);
			}
		} catch (Exception e) {
			if (e.getCause() != null) {
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
	
	public void fail(String message){
		outcomeMessage = "ERR " + message;
	}
	
	public String ok(){
		return "OK";
	}
	
	public void ok(Object object){
		outcomeMessage = "OK " + object;
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
	
	/** Private helper method for getHelp. Formats the help headers. */
	private void addHeader(String header, StringBuilder help){
		help.append("---"); //$NON-NLS-1$
		help.append(header);
		help.append("---"); //$NON-NLS-1$
		help.append("\n");
	}
	
	/** Private helper method for getHelp. Formats the command descriptions. */
	private void addCommand(String command, String description, StringBuilder help){
		help.append("\t");
		help.append(command);
		help.append(" - "); //$NON-NLS-1$
		help.append(description);
		help.append("\n");
	}
	
	public String getHelp(String... sub){
		StringBuilder sb = new StringBuilder();
		if (sub == null) {
			return printOverviewHelp();
		}
		if (StringUtils.isBlank(sub[0])) {
			return "";
		}
		String[] methodSignatures = methods.keySet().toArray(new String[] {});
		
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
		
		// flatten the set (prevents mulitple entries) to a list
		List<String> helpList = new ArrayList<>(relevant);
		Collections.sort(helpList, Comparator.naturalOrder());
		
		sb.append("(" + helpList.stream().reduce((u, t) -> u + " | " + t).orElse("") + ")\n");
		for (String string : helpList) {
			String methodKey = "__"+StringUtils.join(sub, "_")+"_"+string;
			Method method = methods.get(methodKey);
			if(method!=null) {
				CmdAdvisor declaredAnnotation = method.getDeclaredAnnotation(CmdAdvisor.class);
				addCommand(string, (declaredAnnotation!=null) ? declaredAnnotation.description(): "", sb);
			} else {
				sb.append("\t"+string+" - [see subcommand]\n");
			}
		}
		
		return sb.toString();
	}
	
	private String printOverviewHelp(){
		StringBuilder sb = new StringBuilder();
		addHeader("Elexis Admin Commands", sb);
		Iterator<Entry<String, String>> i = commandsHelp.entrySet().iterator();
		while (i.hasNext()) {
			Entry<String, String> entry = i.next();
			String command = entry.getKey();
			String attributes = entry.getValue();
			addCommand(command, attributes, sb);
		}
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
