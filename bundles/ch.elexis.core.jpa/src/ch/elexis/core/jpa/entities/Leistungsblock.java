package ch.elexis.core.jpa.entities;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.converter.ICodeElementListConverter;
import ch.elexis.core.model.ICodeElement;


@Entity
@Table(name = "leistungsblock")
public class Leistungsblock extends AbstractDBObjectIdDeleted implements ICodeElement {

	public static final String CODESYSTEM_NAME = "Block";

	@OneToOne
	@JoinColumn(name = "MandantId")
	private Kontakt mandator;

	@Column(length = 30)
	private String name;

	@Convert(converter = ICodeElementListConverter.class)
	@Column(name = "leistungen")
	private List<ICodeElement> services;

	@Column(length = 30)
	private String macro;

	public Kontakt getMandator() {
		return mandator;
	}

	public void setMandator(Kontakt mandator) {
		this.mandator = mandator;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ICodeElement> getServices() {
		return services;
	}

	public void setServices(List<ICodeElement> services) {
		this.services = services;
	}

	public String getMacro() {
		return macro;
	}

	public void setMacro(String macro) {
		this.macro = macro;
	}

	@Override
	public String getCodeSystemName() {
		return CODESYSTEM_NAME;
	}

	@Override
	public String getCode() {
		return getName();
	}

	@Override
	public String getText() {
		return getName();
	}

}
