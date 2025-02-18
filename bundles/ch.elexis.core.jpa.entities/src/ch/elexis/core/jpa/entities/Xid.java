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

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.converter.XidQualityConverter;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.XidQuality;
import ch.elexis.core.model.util.ElexisIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

/**
 * The persistent class for the Elexis XID database table. Valid from DB Version
 * >1.8.11
 *
 * @author M. Descher, MEDEVIT, Austria
 */
@Entity
@Table(name = "xid")
@EntityListeners(EntityWithIdListener.class)
@Cache(expiry = 15000)
@NamedQuery(name = "Xid.domain.domainid", query = "SELECT xi FROM Xid xi WHERE xi.deleted = false AND xi.domain = :domain AND xi.domainId = :domainid")
@NamedQuery(name = "Xid.domain.objectid", query = "SELECT xi FROM Xid xi WHERE xi.deleted = false AND xi.domain = :domain AND xi.object = :objectid")
@NamedQuery(name = "Xid.domain.objectid.type", query = "SELECT xi FROM Xid xi WHERE xi.deleted = false AND xi.domain = :domain AND xi.object = :objectid AND xi.type = :type")
@NamedQuery(name = "Xid.objectid", query = "SELECT xi FROM Xid xi WHERE xi.deleted = false AND xi.object = :objectid")
@NamedQuery(name = "Xid.ahvdomainid", query = "SELECT xi FROM Xid xi WHERE xi.deleted = false AND xi.domain = 'www.ahv.ch/xid' AND FUNCTION('REPLACE',xi.domainId,'.','') = FUNCTION('REPLACE',:ahvdomainid,'.','')")
public class Xid extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@Column(length = 255)
	protected String domain;

	@Column(name = "domain_id", length = 255)
	protected String domainId;

	@Column(length = 25)
	protected String object;

	@Column(length = 1)
	@Convert(converter = XidQualityConverter.class)
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
	public boolean isDeleted() {
		return deleted;
	}

	@Override
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public Long getLastupdate() {
		return lastupdate;
	}

	@Override
	public void setLastupdate(Long lastupdate) {
		this.lastupdate = lastupdate;
	}
}
