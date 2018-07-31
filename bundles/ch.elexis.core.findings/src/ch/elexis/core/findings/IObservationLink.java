/*******************************************************************************
 * Copyright (c) 2016 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.findings;

import java.util.Optional;

import ch.elexis.core.model.Deleteable;
import ch.elexis.core.model.Identifiable;

public interface IObservationLink extends Identifiable, Deleteable {
	public enum ObservationLinkType {
			REF
	}
	
	public Optional<IObservation> getSource();
	
	public Optional<IObservation> getTarget();
	
	public void setTarget(IObservation observation);
	
	public void setSource(IObservation observation);
	
	public void setType(ObservationLinkType type);
}
