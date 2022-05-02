package ch.elexis.core.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import ch.elexis.core.jpa.entities.BriefVorlage;
import ch.elexis.core.jpa.entities.Heap;
import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.model.util.DocumentLetterUtil;
import ch.elexis.core.model.util.internal.ModelUtil;
import ch.elexis.core.services.INativeQuery;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.StickerServiceHolder;
import ch.elexis.core.time.TimeUtil;
import ch.elexis.core.types.DocumentStatus;
import ch.elexis.core.types.DocumentStatusMapper;

public class DocumentTemplate extends AbstractIdDeleteModelAdapter<BriefVorlage>
		implements IdentifiableWithXid, IDocumentTemplate {

	private ICategory category;
	private String storeId = StringUtils.EMPTY;
	private List<IHistory> history;
	private String keywords;

	public DocumentTemplate(BriefVorlage entity) {
		super(entity);
	}

	@Override
	public String getTitle() {
		return getEntity().getSubject();
	}

	@Override
	public void setTitle(String value) {
		getEntityMarkDirty().setSubject(value);
	}

	@Override
	public String getDescription() {
		return getEntity().getNote();
	}

	@Override
	public void setDescription(String value) {
		getEntityMarkDirty().setNote(value);
	}

	@Override
	public List<DocumentStatus> getStatus() {
		int status = getEntity().getStatus();
		Set<DocumentStatus> map = DocumentStatusMapper.map(status);
		if (getEntity().getRecipient() != null) {
			map.add(DocumentStatus.SENT);
		}
		return new ArrayList<DocumentStatus>(map);
	}

	@Override
	public void setStatus(DocumentStatus _status, boolean active) {
		Set<DocumentStatus> _statusSet = new HashSet<>(getStatus());
		if (active) {
			_statusSet.add(_status);
		} else {
			_statusSet.remove(_status);
		}
		int value = DocumentStatusMapper.map(_statusSet);
		getEntity().setStatus(value);
	}

	@Override
	public Date getCreated() {
		LocalDateTime creationDate = getEntity().getCreationDate();
		return creationDate != null ? toDate(creationDate) : getLastchanged();
	}

	@Override
	public void setCreated(Date value) {
		getEntityMarkDirty().setCreationDate(TimeUtil.toLocalDateTime(value));
	}

	@Override
	public Date getLastchanged() {
		if (getEntity().getModifiedDate() != null) {
			return toDate(getEntity().getModifiedDate());
		}
		if (getEntity().getLastupdate() != null) {
			return new Date(getEntity().getLastupdate().longValue());
		}
		return new Date(0);
	}

	@Override
	public void setLastchanged(Date value) {
		getEntityMarkDirty().setModifiedDate(TimeUtil.toLocalDateTime(value));
	}

	@Override
	public String getMimeType() {
		return getEntity().getMimetype();
	}

	@Override
	public void setMimeType(String value) {
		getEntityMarkDirty().setMimetype(value);
	}

	@Override
	public ICategory getCategory() {
		if (this.category == null && getEntity().getTyp() != null) {
			this.category = new TransientCategory(getEntity().getTyp());
		}
		return this.category;
	}

	@Override
	public void setCategory(ICategory value) {
		this.category = value;
		getEntityMarkDirty().setTyp(value.getName());
	}

	@Override
	public List<IHistory> getHistory() {
		if (history == null) {
			history = new ArrayList<>();
			if (getEntity().getRecipient() != null) {
				history.add(new TransientHistory(getCreated(), DocumentStatus.SENT,
						ModelUtil.getPersonalia(getEntity().getRecipient())));
			}
		}
		return history;
	}

	@Override
	public String getStoreId() {
		return StringUtils.isNotEmpty(storeId) ? storeId : "ch.elexis.data.store.brief";
	}

	@Override
	public void setStoreId(String value) {
		this.storeId = value;
	}

	@Override
	public String getExtension() {
		return DocumentLetterUtil.evaluateFileExtension(getEntity().getMimetype());
	}

	@Override
	public void setExtension(String value) {
		getEntity().setMimetype(value);
	}

	@Override
	public String getKeywords() {
		return this.keywords;
	}

	@Override
	public void setKeywords(String value) {
		this.keywords = value;
	}

	@Override
	public IPatient getPatient() {
		return null;
	}

	@Override
	public void setPatient(IPatient value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IContact getAuthor() {
		return null;
	}

	@Override
	public void setAuthor(IContact value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public InputStream getContent() {
		// try to open from external file if applicable
		IVirtualFilesystemHandle vfsHandle = DocumentLetterUtil.getExternalHandleIfApplicable(this);
		if (vfsHandle != null && vfsHandle.canRead()) {
			try {
				return vfsHandle.openInputStream();
			} catch (IOException e) {
				LoggerFactory.getLogger(getClass()).warn("Exception getting InputStream for Letter [{}]", getId(), e);
			}
		}

		// read from heap content
		Heap content = getEntity().getContent();
		if (content != null) {
			byte[] inhalt = content.getInhalt();
			if (inhalt != null) {
				return new ByteArrayInputStream(inhalt);
			}
		}
		return null;
	}

	@Override
	public long getContentLength() {
		try {
			IVirtualFilesystemHandle vfsHandle = DocumentLetterUtil.getExternalHandleIfApplicable(this);
			if (vfsHandle != null && vfsHandle.canRead()) {
				return vfsHandle.getContentLenght();
			}
		} catch (IOException e) {
		}

		INativeQuery nativeQuery = CoreModelServiceHolder.get()
				.getNativeQuery("SELECT LENGTH(INHALT) FROM HEAP WHERE ID = ?1");
		Iterator<?> result = nativeQuery
				.executeWithParameters(nativeQuery.getIndexedParameterMap(Integer.valueOf(1), getId())).iterator();
		if (result.hasNext()) {
			Object next = result.next();
			if (next != null) {
				return Long.parseLong(next.toString());
			}
		}
		return -1;
	}

	@Override
	public void setContent(InputStream content) {
		setStatus(DocumentStatus.PREPROCESSED, false);
		setStatus(DocumentStatus.INDEXED, false);
		setLastchanged(new Date());
		IVirtualFilesystemHandle vfsHandle = DocumentLetterUtil.getExternalHandleIfApplicable(this);
		if (vfsHandle != null) {
			if (content == null) {
				try {
					vfsHandle.delete();
					return;
				} catch (IOException e) {
					LoggerFactory.getLogger(getClass()).error("Error deleting document content", e);
				}
			} else {
				try (OutputStream outputStream = vfsHandle.openOutputStream()) {
					IOUtils.copy(content, outputStream);
					return;
				} catch (IOException e) {
					LoggerFactory.getLogger(getClass()).error("Error setting document content, will write to HEAP", e);
				}
			}
		}

		try {
			if (content == null) {
				getEntity().setContent(null);
			} else {
				getEntity().getOrCreateContent().setInhalt(IOUtils.toByteArray(content));
				addChanged(ModelUtil.getAdapter(getEntity().getOrCreateContent(), IBlob.class));
			}
		} catch (IOException e) {
			LoggerFactory.getLogger(getClass()).error("Error setting document content", e);
		} finally {
			IOUtils.closeQuietly(content);
		}
	}

	@Override
	public String getLabel() {
		return new SimpleDateFormat("dd.MM.yyyy").format(getCreated()) + StringUtils.SPACE + getTitle();
	}

	@Override
	public String getTemplateTyp() {
		return getEntity().getTemplateTyp();
	}

	@Override
	public void setTemplateTyp(String value) {
		getEntityMarkDirty().setTemplateTyp(value);
	}

	@Override
	public IMandator getMandator() {
		return ModelUtil.getAdapter(getEntity().getRecipient(), IMandator.class);
	}

	@Override
	public void setMandator(IMandator value) {
		if (value instanceof AbstractIdDeleteModelAdapter) {
			getEntityMarkDirty().setRecipient((Kontakt) ((AbstractIdDeleteModelAdapter<?>) value).getEntity());
		} else if (value == null) {
			getEntityMarkDirty().setRecipient(null);
		}
	}

	@Override
	public boolean isAskForAddressee() {
		return !StickerServiceHolder.get().getStickers(this).stream()
				.filter(s -> s.getName().equals(BriefConstants.DONT_ASK_FOR_ADDRESS_STICKER)).findFirst().isPresent();
	}

	@Override
	public void setAskForAddressee(boolean value) {
		throw new UnsupportedOperationException();
	}
}
