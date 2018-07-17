/*******************************************************************************
 * Copyright (c) 2015 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import ch.elexis.core.model.XidQuality;

/**
 * The persistent class for the Elexis XID database table. Valid from DB Version
 * >1.8.11
 * 
 * @author M. Descher, MEDEVIT, Austria
 */
@Entity
@Table(name = "xid")
public class Xid extends AbstractDBObjectIdDeleted {

	@Column(length = 255)
	protected String domain;

	@Column(name = "domain_id", length = 255)
	protected String domainId;

	@Column(length = 25)
	protected String object;
	
	@Enumerated
	protected XidQuality quality;

	@Column(length = 80)
	protected String type;

	public Xid() {
	}

	public String getDomain() {
		return this.domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getDomainId() {
		return this.domainId;
	}

	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}

	public String getObject() {
		return this.object;
	}

	public void setObject(String object) {
		this.object = object;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public XidQuality getQuality() {
		return quality;
	}

	public void setQuality(XidQuality quality) {
		this.quality = quality;
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}

}
