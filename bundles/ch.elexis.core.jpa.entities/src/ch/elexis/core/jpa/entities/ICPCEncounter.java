package ch.elexis.core.jpa.entities;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "CH_ELEXIS_ICPC_ENCOUNTER")
@EntityListeners(EntityWithIdListener.class)
public class ICPCEncounter extends AbstractEntityWithId
		implements EntityWithId, EntityWithDeleted, EntityWithExtInfo {

	// Transparently updated by the EntityListener
	protected Long lastupdate;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();
	
	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;
	
	@Basic(fetch = FetchType.LAZY)
	@Lob
	protected byte[] extInfo;
	
	@ManyToOne()
	@JoinColumn(name = "KONS")
	private Behandlung kons;

	@ManyToOne()
	@JoinColumn(name = "EPISODE")
	private ICPCEpisode episode;

	@Column(length = 4, name = "RFE")
	private String rfe;
	
	@Column(length = 4, name = "DIAG")
	private String diag;
	
	@Column(length = 4, name = "PROC")
	private String proc;
	
	@Override
	public boolean isDeleted(){
		return deleted;
	}
	
	@Override
	public void setDeleted(boolean deleted){
		this.deleted = deleted;
	}
	
	@Override
	public String getId(){
		return id;
	}
	
	@Override
	public void setId(String id){
		this.id = id;
	}
	
	@Override
	public Long getLastupdate(){
		return lastupdate;
	}
	
	@Override
	public void setLastupdate(Long lastupdate){
		this.lastupdate = lastupdate;
	}

	@Override
	public byte[] getExtInfo(){
		return extInfo;
	}
	
	@Override
	public void setExtInfo(byte[] extInfo){
		this.extInfo = extInfo;
	}
	
	public String getRfe(){
		return rfe;
	}
	
	public void setRfe(String rfe){
		this.rfe = rfe;
	}
	
	public String getDiag(){
		return diag;
	}
	
	public void setDiag(String diag){
		this.diag = diag;
	}
	
	public String getProc(){
		return proc;
	}
	
	public void setProc(String proc){
		this.proc = proc;
	}
	
	public Behandlung getKons(){
		return kons;
	}
	
	public void setKons(Behandlung kons){
		this.kons = kons;
	}
	
	public ICPCEpisode getEpisode(){
		return episode;
	}
	
	public void setEpisode(ICPCEpisode episode){
		this.episode = episode;
	}
}
