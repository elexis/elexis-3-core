package ch.elexis.core.jpa.entities;

import java.util.Hashtable;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Convert;

@Entity
@Table(name = "TARMED_EXTENSION")
public class TarmedExtension {

	@Id
	@Column(length = 14)
	private String code;

	@Basic(fetch = FetchType.LAZY)
	@Convert(value = "ElexisExtInfoMapConverter")
	private Map<String, String> limits = new Hashtable<String, String>();

	@Lob
	private String med_interpret;

	@Lob
	private String tech_interpret;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Map<String, String> getLimits() {
		return limits;
	}

	public void setLimits(Map<String, String> limits) {
		this.limits = limits;
	}

	public String getMed_interpret() {
		return med_interpret;
	}

	public void setMed_interpret(String med_interpret) {
		this.med_interpret = med_interpret;
	}

	public String getTech_interpret() {
		return tech_interpret;
	}

	public void setTech_interpret(String tech_interpret) {
		this.tech_interpret = tech_interpret;
	}
}
