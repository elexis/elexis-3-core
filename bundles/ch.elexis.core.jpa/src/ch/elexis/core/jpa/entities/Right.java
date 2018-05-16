package ch.elexis.core.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Convert;

@Entity
@Table(name = "RIGHT_")
public class Right extends AbstractDBObjectIdDeleted {

	@Convert("booleanStringConverter")
	@Column(name = "LOG_EXECUTION")
	protected boolean logExecution;

	@Column(length = 255)
	protected String name;

	@OneToOne
	@JoinColumn(name = "PARENTID")
	protected Right parent;

	@Column(length = 255, name = "I18N_NAME")
	protected String i18nName;

	public boolean isLogExecution() {
		return logExecution;
	}

	public void setLogExecution(boolean logExecution) {
		this.logExecution = logExecution;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Right getParent() {
		return parent;
	}

	public void setParent(Right parent) {
		this.parent = parent;
	}

	public String getI18nName() {
		return i18nName;
	}

	public void setI18nName(String i18nName) {
		this.i18nName = i18nName;
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}
}
