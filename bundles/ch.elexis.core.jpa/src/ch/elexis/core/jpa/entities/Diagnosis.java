package ch.elexis.core.jpa.entities;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "DIAGNOSEN")
public class Diagnosis extends AbstractDBObjectIdDeleted {

	@Column(length = 255, name = "DG_TXT")
	private String text;

	@Column(length = 25, name = "DG_CODE")
	private String code;

	@Column(length = 80, name = "KLASSE")
	private String diagnosisClass;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDiagnosisClass() {
		return diagnosisClass;
	}

	public void setDiagnosisClass(String diagnosisClass) {
		this.diagnosisClass = diagnosisClass;
	}

	@Override
	public int hashCode() {
		return Objects.hash(code, diagnosisClass);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Diagnosis other = (Diagnosis) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (diagnosisClass == null) {
			if (other.diagnosisClass != null)
				return false;
		} else if (!diagnosisClass.equals(other.diagnosisClass))
			return false;
		return true;
	}

	@Override
	public String getLabel() {
		return getText() + " (" + getCode() + ")";
	}

	@Override
	public String toString() {
		return super.toString() + "text=[" + getText() + "] code=[" + getCode() + "]";
	}
}
