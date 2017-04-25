package ch.elexis.core.data.service.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.ICategory;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.ITag;
import ch.elexis.core.services.IDocumentStore;
import ch.elexis.data.Brief;
import ch.elexis.data.Kontakt;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.data.dto.BriefDocumentDTO;
import ch.elexis.data.dto.CategoryDocumentDTO;
import ch.rgw.tools.JdbcLink.Stm;
import ch.rgw.tools.TimeTool;

@Component
public class BriefDocumentStore implements IDocumentStore {
	
	private static final String STORE_ID = "ch.elexis.data.store.brief";
	private static Logger log = LoggerFactory.getLogger(BriefDocumentStore.class);
	
	@Override
	public String getId(){
		return STORE_ID;
	}
	
	@Override
	public String getName(){
		return "Briefe";
	}
	
	@Override
	public List<IDocument> getDocuments(String patientId, String authorId, ICategory category,
		List<ITag> tag){
		Query<Brief> query = new Query<>(Brief.class);
		query.add(Brief.FLD_PATIENT_ID, Query.EQUALS, patientId);
		
		if (authorId != null) {
			query.add(Brief.FLD_SENDER_ID, Query.EQUALS, authorId);
		}
		if (category != null && category.getName() != null) {
			query.add(Brief.FLD_TYPE, Query.EQUALS, category.getName());
		}
		
		List<Brief> briefe = query.execute();
		List<IDocument> results = new ArrayList<>();
		for (Brief brief : briefe) {
			results.add(new BriefDocumentDTO(brief, STORE_ID));
		}
		return results;
	}
	
	@Override
	public List<ICategory> getCategories(){
		Stm stm = PersistentObject.getDefaultConnection().getStatement();
		ResultSet rs = stm.query("select distinct " + Brief.FLD_TYPE + " from " + Brief.TABLENAME + " order by " + Brief.FLD_TYPE);
		List<ICategory> categories = new ArrayList<>();
		try {
			while (rs.next()) {
				String typ = rs.getString(Brief.FLD_TYPE );
				if (typ != null) {
					categories.add(new CategoryDocumentDTO(typ));
				}
			}
		} catch (SQLException e) {
			log.error("Error executing distinct brief category selection", e);
		}
		PersistentObject.getDefaultConnection().releaseStatement(stm);
		
		return categories;
	}
	
	@Override
	public ICategory addCategory(String name){
		return new CategoryDocumentDTO(name);
	}
	
	@Override
	public void removeCategory(ICategory category) throws IllegalStateException{
		
	}
	
	@Override
	public List<ITag> getTags(){
		return Collections.emptyList();
	}
	
	@Override
	public ITag addTag(String name){
		return null;
	}
	
	@Override
	public void removeTag(ITag tag){
		
	}


	@Override
	public void removeDocument(IDocument document){
		Brief brief = Brief.load(document.getId());
		if (brief.exists()) {
			brief.delete();
		}
	}
	
	@Override
	public IDocument saveDocument(IDocument document){
		return save(document, null);
	}
	
	@Override
	public IDocument saveDocument(IDocument document, InputStream content){
		return save(document, content);
	}
	
	private IDocument save(IDocument document, InputStream content){
		Brief brief = Brief.load(document.getId());
		if (brief.exists()) {
			String category =
				document.getCategory() != null ? document.getCategory().getName() : null;
			// update an existing document
			String[] fetch = new String[] {
				Brief.FLD_PATIENT_ID, Brief.FLD_SENDER_ID, Brief.FLD_NOTE, Brief.FLD_SUBJECT,
				Brief.FLD_MIME_TYPE, Brief.FLD_TYPE
			};
			String[] data = new String[] {
				document.getPatientId(), document.getAuthorId(), document.getDescription(),
				document.getTitle(), document.getMimeType(), category
			};
			brief.set(fetch, data);
		} else {
			// persist a new document
			brief = new Brief(document.getTitle(),
				document.getCreated() != null ? new TimeTool(document.getCreated()) : null,
				Kontakt.load(document.getAuthorId()), null, null, document.getCategory().getName());
			brief.set(new String[] {
				Brief.FLD_PATIENT_ID, Brief.FLD_MIME_TYPE, Brief.FLD_NOTE
			}, new String[] {
				document.getPatientId(), document.getMimeType(), document.getDescription()
			});
		}
		
		if (content != null) {
			try {
				brief.save(IOUtils.toByteArray(content), document.getMimeType());
			} catch (IOException e) {
				log.error("cannot save document contents", e);
			}
		}
		return new BriefDocumentDTO(brief, STORE_ID);
	}
	
	@Override
	public Optional<IDocument> loadDocument(String id){
		Brief brief = Brief.load(id);
		if (brief.exists()) {
			return Optional.of(new BriefDocumentDTO(brief, STORE_ID));
		}
		return Optional.empty();
	}
	
	@Override
	public Optional<InputStream> loadContent(IDocument document){
		Brief brief = Brief.load(document.getId());
		if (brief.exists()) {
			return Optional.of(new ByteArrayInputStream(brief.loadBinary()));
		}
		return Optional.empty();
	}
	
	@Override
	public boolean isAllowed(Capability restricted){
		if (Capability.CATEGORY.equals(restricted) || Capability.TAG.equals(restricted)) {
			return false;
		}
		return IDocumentStore.super.isAllowed(restricted);
	}
}
