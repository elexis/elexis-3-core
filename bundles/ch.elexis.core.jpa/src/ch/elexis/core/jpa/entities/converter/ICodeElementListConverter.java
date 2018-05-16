package ch.elexis.core.jpa.entities.converter;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.model.ICodeElement;
import ch.rgw.compress.CompEx;

@Converter
public class ICodeElementListConverter implements AttributeConverter<List<ICodeElement>, byte[]> {

	private Logger log = LoggerFactory.getLogger(ICodeElementListConverter.class);

	@Override
	public byte[] convertToDatabaseColumn(List<ICodeElement> list) {
		if (list == null) {
			return null;
		}

		StringBuilder st = new StringBuilder();
		for (ICodeElement v : list) {
			//			String sts = StoreToStringService.storeToString((AbstractDBObjectIdDeleted) v);
			//			st.append(sts).append(StringConstants.COMMA);
		}
		String storable = st.toString().replaceFirst(",$", StringConstants.EMPTY);
		return CompEx.Compress(storable, CompEx.ZIP);
	}

	@Override
	public List<ICodeElement> convertToEntityAttribute(byte[] compressed) {
		if (compressed == null) {
			return Collections.emptyList();
		}
		List<ICodeElement> ret = new ArrayList<>();
		try {
			String storable = new String(CompEx.expand(compressed), "UTF-8"); //$NON-NLS-1$
			for (String storeToString : storable.split(StringConstants.COMMA)) {
				//				Optional<AbstractDBObjectIdDeleted> created = StoreToStringService.INSTANCE
				//						.createDetachedFromString(storeToString);
				//				if (created.isPresent()) {
				//					if (created.get() instanceof ICodeElement) {
				//						ret.add((ICodeElement) created.get());
				//					} else {
				//						log.error("[{}] is not an instanceof ICodeElement", storeToString);
				//					}
				//				} else {
				//					log.warn("Could not load [{}]", storeToString);
				//				}
			}
		} catch (UnsupportedEncodingException uoe) {
			log.error("Error initializing", uoe);
		}
		return ret;
	}

}
