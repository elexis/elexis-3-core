/*******************************************************************************
 * Copyright (c) 2008-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import ch.elexis.core.constants.TextContainerConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.scripting.Interpreter;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.exceptions.ElexisException;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

/**
 * A script. At this moment only beanshell is supported as interpreter, but others are possible
 * 
 * @author gerry
 * 
 */
public class Script extends NamedBlob2 {
	public static final String INTERPRETER_BEANSHELL = "BSH";
	public static final String INTERPRETER_SCALA = "SCALA";
	public static final String INTERPRETER_DEFAULT = INTERPRETER_BEANSHELL;
	private static final Pattern varPattern = Pattern
		.compile(TextContainerConstants.MATCH_TEMPLATE);
	private static final String PREFIX = "Script:";
	public static final String SCRIPT_MARKER = "SCRIPT:";
	private Interpreter interpreter = null;
	
	/**
	 * 
	 * @param name
	 * @return
	 * @throws ElexisException
	 *             if the script interpreter can not successfully be loaded or is not available.
	 */
	private static Interpreter getInterpreter(String name) throws ElexisException{
		if (name == null)
			name = INTERPRETER_BEANSHELL;
		
		List<IConfigurationElement> scripters = Extensions.getExtensions("ch.elexis.scripting");
		for (IConfigurationElement scripter : scripters) {
			if (scripter.getAttribute("name").equals(name)) {
				try {
					return (Interpreter) scripter.createExecutableExtension("class");
				} catch (CoreException e) {
					ExHandler.handle(e);
					throw new ElexisException(Script.class, "Could not load intepreter "
						+ e.getMessage(), ElexisException.EE_NOT_SUPPORTED);
				}
			}
		}
		throw new ElexisException(Script.class, name + " interpreter plug-in not available",
			ElexisException.EE_NOT_SUPPORTED);
		
	}
	
	/**
	 * create and return an appropriate Interpreter for the given script. If the script declares an
	 * Interpreter as in / * SCALA * /, the metghod will attempr to load this interpreter. If no
	 * such declaration is found, the default interpreter will be returned
	 * 
	 * @param script
	 *            the script contents.
	 * @return the Interpreter
	 * @throws ElexisException
	 *             if the interpreter was not found or could not be instantiated
	 */
	private void loadInterpreter(String script) throws ElexisException{
		if (interpreter == null) {
			if (script == null) {
				script = getString();
			}
			Pattern ip = Pattern.compile("^\\/\\*\\s*!([A-Z]+)!\\s*\\*\\/", Pattern.MULTILINE);
			Matcher m = ip.matcher(script);
			if (m.matches()) {
				interpreter = getInterpreter(m.group(1));
			} else {
				interpreter = getInterpreter(null);
			}
		}
	}
	
	public static Script create(String name, String contents) throws ElexisException{
		String mid = PREFIX + name;
		Script ret = new Script(mid);
		if (ret.state() == INEXISTENT) {
			ret.create(mid);
		} else if (ret.state() == DELETED) {
			ret.undelete();
		}
		if (StringTool.isNothing(contents)) {
			contents = "/* !BSH! */";
		}
		ret.putString(contents);
		return ret;
	}
	
	@Override
	public String getLabel(){
		String[] name = getId().split(":");
		return name[1];
	}
	
	public void init() throws ElexisException{
		loadInterpreter(null);
		interpreter.setValue("finished", false);
		interpreter.setValue("init", true);
		interpreter.run(parse(getString(), new PersistentObject[0]), false);
		interpreter.setValue("init", false);
	}
	
	public void finished() throws ElexisException{
		loadInterpreter(null);
		interpreter.setValue("finished", true);
		interpreter.run(parse(getString(), (PersistentObject[]) null), false);
	}
	
	public void setVariable(String name, Object value) throws ElexisException{
		loadInterpreter(null);
		interpreter.setValue(name, value);
	}
	
	/**
	 * Replace variables of the form [Patient.Name] in the script with their respective values for
	 * the current call
	 * 
	 * @param script
	 *            the script
	 * @param params
	 *            all Variables to replace
	 * @return the parsed Script
	 */
	private static String parse(String script, PersistentObject... params){
		if (params == null) {
			params = new PersistentObject[0];
		}
		Matcher matcher = varPattern.matcher(script);
		// Suche Variablen der Form [Patient.Alter]
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			boolean bMatched = false;
			String var = matcher.group().replaceAll("[\\[\\]]", StringTool.leer);
			String[] fields = var.split("\\.");
			if (fields.length > 1) {
				String fqname = "ch.elexis.data." + fields[0];
				for (PersistentObject o : params) {
					if (o.getClass().getName().equals(fqname)) {
						String repl = o.get(fields[1]);
						repl = repl.replace('\\', '/');
						repl = repl.replace('\"', ' ');
						repl = repl.replace('\n', ' ');
						repl = repl.replace('\r', ' ');
						matcher.appendReplacement(sb, "\"" + repl + "\"");
						bMatched = true;
					}
				}
			}
			if (!bMatched) {
				matcher.appendReplacement(sb, "\"\"");
			}
		}
		matcher.appendTail(sb);
		return sb.toString();
	}
	
	/**
	 * execute a script entered as string with the given interpreter
	 * 
	 * 
	 * @param objects
	 *            optional Objects to replace in Variables like [Fall.Grund] in the script
	 * @param params
	 *            optional parameters. These can be of the form <i>name=value</i> or <i>value</i>.
	 *            if no name is given, the variables will be inserted for $1, $2 ... in the script.
	 *            If a name is given, $names in the script will be replaced with the respective
	 *            values.
	 * @return The result of the script interpreter
	 * @throws ElexisException
	 */
	public static Object execute(Interpreter scripter, String script, String params,
		PersistentObject... objects) throws ElexisException{
		if (!StringTool.isNothing(script)) {
			if (params != null) {
				String var = "\\$";
				String[] parameters = params.split("\\s*,\\s*");
				for (int i = 0; i < parameters.length; i++) {
					String parm = parameters[i].trim();
					String[] p = parm.split("=");
					if (p.length == 2) {
						script = script.replaceAll("\\" + p[0], p[1]);
					} else {
						script = script.replaceAll(var + i, p[0]);
					}
				}
			}
			String parsed = parse(script, objects);
			scripter.setValue("actPatient", ElexisEventDispatcher.getSelectedPatient());
			scripter.setValue("actFall", ElexisEventDispatcher.getSelected(Fall.class));
			scripter.setValue("actKons", ElexisEventDispatcher.getSelected(Konsultation.class));
			scripter.setValue("actMandant", CoreHub.actMandant);
			scripter.setValue("actUser", CoreHub.actUser);
			
			scripter.setValue("Elexis", CoreHub.plugin);
			return scripter.run(parsed, true);
		}
		return null;
	}
	
	public Object execute(String params, PersistentObject... objects) throws ElexisException{
		String script = getString();
		loadInterpreter(script);
		return execute(interpreter, script, params, objects);
	}
	
	public static List<Script> getScripts(){
		Query<Script> qbe = new Query<Script>(Script.class);
		qbe.add("ID", "LIKE", PREFIX + "%");
		return qbe.execute();
	}
	
	/**
	 * Execute a script that is part of the call
	 * 
	 * @param call
	 *            e.g. scriptname(a="foo",b="bar")
	 * @param objects
	 *            some Objects to cinvert in the script
	 * @return the result of the interpreter
	 * @throws ElexisException
	 *             if no such scruiopt was found or an error occurred
	 */
	public static Object executeScript(String call, PersistentObject... objects)
		throws ElexisException{
		call = call.trim();
		String name = call;
		String params = null;
		int x = name.indexOf('(');
		if (x != -1) {
			name = call.substring(0, x);
			params = call.substring(x + 1, call.length() - 1);
		}
		Query<Script> qbe = new Query<Script>(Script.class);
		qbe.add("ID", Query.EQUALS, PREFIX + name);
		List<Script> found = qbe.execute();
		if (found.size() == 0) {
			throw new ElexisException(Script.class,
				"A Script with this name was not found " + name, ElexisException.EE_NOT_FOUND);
		}
		Script script = found.get(0);
		try {
			return script.execute(params, objects);
		} catch (Exception e) {
			ExHandler.handle(e);
			throw new ElexisException(Script.class, "Error while executing " + name + ": "
				+ e.getMessage(), ElexisException.EE_UNEXPECTED_RESPONSE);
		}
	}
	
	@Override
	public boolean isDragOK(){
		return true;
	}
	
	@Override
	public boolean isValid(){
		if (getId().matches(PREFIX + "[a-zA-Z0-9_-]+")) {
			return super.isValid();
		}
		return false;
	}
	
	public static Script load(String id){
		Script ret = new Script(id);
		if (ret.isValid()) {
			return ret;
		}
		return null;
	}
	
	protected Script(String id){
		super(id);
	}
	
	protected Script(){}
	
	public static Interpreter getInterpreterFor(String script) throws ElexisException{
		Script s = new Script();
		s.loadInterpreter(script);
		return s.interpreter;
	}
	
	public static Script importFromFile(String filepath) throws ElexisException{
		File file = new File(filepath);
		if (!file.exists()) {
			file = new File(filepath + ".script");
			if (!file.exists()) {
				throw new ElexisException(Script.class, "Could not find file " + filepath,
					ElexisException.EE_NOT_FOUND);
			}
		}
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			StringBuilder sb = new StringBuilder();
			String in;
			while ((in = br.readLine()) != null) {
				sb.append(in);
			}
			br.close();
			String name = FileTool.getNakedFilename(filepath);
			return create(name, sb.toString());
		} catch (IOException ioex) {
			throw new ElexisException(Script.class, "Error loading file " + filepath + ": "
				+ ioex.getMessage(), ElexisException.EE_FILE_ERROR);
		}
	}
}
