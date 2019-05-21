package ch.elexis.core.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "net_medshare_percentile_checklist_question")
@EntityListeners(EntityWithIdListener.class)
@Cache(expiry = 15000)
public class PercentileChecklistQuestion extends AbstractEntityWithId
		implements EntityWithId, EntityWithDeleted {

	// Transparently updated by the EntityListener
	protected Long lastupdate;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();
	
	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;
	
	@ManyToOne
	@JoinColumn(name = "TitleId")
	private PercentileChecklistTitle title;
	
	@ManyToOne
	@JoinColumn(name = "SubTitleId")
	private PercentileChecklistTitle subtitle;
	
	@Column(length = 255)
	private String question;
	
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

	public PercentileChecklistTitle getTitle(){
		return title;
	}
	
	public void setTitle(PercentileChecklistTitle title){
		this.title = title;
	}
	
	public PercentileChecklistTitle getSubtitle(){
		return subtitle;
	}
	
	public void setSubtitle(PercentileChecklistTitle subtitle){
		this.subtitle = subtitle;
	}
	
	public String getQuestion(){
		return question;
	}
	
	public void setQuestion(String question){
		this.question = question;
	}
}
