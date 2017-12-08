/*******************************************************************************
 * Copyright (c) 2015 MEDEVIT and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 *******************************************************************************/
package ch.elexis.core.services;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Interface definition for transformation implementations.
 * 
 * @author thomas
 * 
 */
public interface IFormattedOutput {
	
	/**
	 * Method for transforming the input object into an OutputStream using the org.apache.fop
	 * library. Different transformation implementations are available via the
	 * {@link IFormattedOutputFactory}.
	 * 
	 * @param input
	 * @param xslt
	 * @param output
	 * 
	 * @throws FormattedOutputException
	 */
	public void transform(Object input, InputStream xslt, OutputStream output);
	
	/**
	 * Method for transforming the input object into an OutputStream using the org.apache.fop
	 * library. Different transformation implementations are available via the
	 * {@link IFormattedOutputFactory}.
	 * 
	 * @param input
	 * @param xslt
	 * @param output
	 * @param transformerParameters
	 *            key/value parameters for the transformer
	 * 
	 * @throws FormattedOutputException
	 */
	public void transform(Object input, InputStream xslt, OutputStream output,
		Map<String, String> transformerParameters);
}
