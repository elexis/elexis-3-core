package ch.elexis.core.findings;

import java.time.LocalDateTime;
import java.util.Optional;

import ch.elexis.core.model.IDocument;

public interface IDocumentReference extends IFinding {

	public IDocument getDocument();

	public void setDocument(IDocument document);

	public String getCategory();

	public void setCategory(String value);

	public ICoding getDocumentClass();

	public void setDocumentClass(ICoding coding);

	public ICoding getPracticeSetting();

	public void setPracticeSetting(ICoding coding);

	public ICoding getFacilityType();

	public void setFacilityType(ICoding coding);

	public String getAuthorId();

	public void setAuthorId(String authorId);

	public String getKeywords();

	public void setKeywords(String keywords);

	public Optional<LocalDateTime> getDate();

	public void setDate(LocalDateTime date);
}
