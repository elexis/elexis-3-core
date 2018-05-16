package ch.elexis.core.jpa.entities;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

import ch.elexis.core.model.ICodeElement;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

@Entity
@Table(name = "CH_MEDELEXIS_LABORTARIF2009")
public class Labor2009Tarif extends AbstractDBObjectIdDeleted implements ICodeElement {

	public static final String CODESYSTEM_NAME = "EAL 2009";

	@Column(length = 255)
	private String chapter;

	@Column(length = 12)
	private String code;

	@Column(length = 10)
	private String tp;

	@Column(length = 255)
	private String name;

	@Lob
	private String limitatio;

	@Column(length = 10)
	private String fachbereich;

	@Column(length = 8)
	private LocalDate gueltigVon;

	@Column(length = 8)
	private LocalDate gueltigBis;

	@Column(length = 2)
	private String praxistyp;

	public String getChapter() {
		return chapter;
	}

	public void setChapter(String chapter) {
		this.chapter = chapter;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getTp() {
		return tp;
	}

	public void setTp(String tp) {
		this.tp = tp;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLimitatio() {
		return limitatio;
	}

	public void setLimitatio(String limitatio) {
		this.limitatio = limitatio;
	}

	public String getFachbereich() {
		return fachbereich;
	}

	public void setFachbereich(String fachbereich) {
		this.fachbereich = fachbereich;
	}

	public LocalDate getGueltigVon() {
		return gueltigVon;
	}

	public void setGueltigVon(LocalDate gueltigVon) {
		this.gueltigVon = gueltigVon;
	}

	public LocalDate getGueltigBis() {
		return gueltigBis;
	}

	public void setGueltigBis(LocalDate gueltigBis) {
		this.gueltigBis = gueltigBis;
	}

	public String getPraxistyp() {
		return praxistyp;
	}

	public void setPraxistyp(String praxistyp) {
		this.praxistyp = praxistyp;
	}

	@Override
	public String getLabel() {
		String code = getCode();
		String text = StringTool.getFirstLine(getName(), 80);

		if (!StringTool.isNothing(code)) {
			StringBuilder sb = new StringBuilder(code).append(" ").append(text) //$NON-NLS-1$
					.append(" (").append(getFachbereich()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$

			TimeTool validFrom = null;
			TimeTool validTo = null;
			if (getGueltigVon() != null) {
				validFrom = new TimeTool(getGueltigVon());
			}
			if (getGueltigBis() != null) {
				validTo = new TimeTool(getGueltigBis());
			}

			if (validFrom != null) {
				sb.append(" (").append(validFrom.toString(TimeTool.DATE_GER));
				if (validTo != null) {
					sb.append("-").append(validTo.toString(TimeTool.DATE_GER)).append(")");
				} else {
					sb.append("-").append(" ").append(")");
				}
			}

			return sb.toString();
		} else {
			return "?"; //$NON-NLS-1$
		}
	}

	@Override
	public String getCodeSystemName() {
		return CODESYSTEM_NAME;
	}

	@Override
	public String getText() {
		return StringTool.getFirstLine(getName(), 80);
	}

	@Override
	public String getCodeSystemCode() {
		return "317";
	}
}
