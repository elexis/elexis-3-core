package ch.elexis.core.services;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.model.ISticker;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

@Component
public class StickerService implements IStickerService {

	private static final String QUERY_STICKERS_FOR_OBJECT = "SELECT etikette FROM ETIKETTEN_OBJECT_LINK WHERE obj=?1";

	private static final String QUERY_STICKER_APPLICABLE = "SELECT COUNT FROM ETIKETTEN_OBJCLASS_LINK WHERE objclass=?1 AND sticker=?2";
	
	private List<ISticker> getStickersForId(String id) {
		INativeQuery stickerQuery = CoreModelServiceHolder.get().getNativeQuery(QUERY_STICKERS_FOR_OBJECT);
		return stickerQuery.executeWithParameters(stickerQuery.getIndexedParameterMap(1, id)).parallel()
				.filter(resultId -> resultId instanceof String)
				.map(resultId -> CoreModelServiceHolder.get().load((String) resultId, ISticker.class).orElse(null))
				.filter(Objects::nonNull).collect(Collectors.toList());
	}

	@Override
	public List<ISticker> getStickers(Identifiable identifiable) {
		return getStickersForId(identifiable.getId());
	}

	@Override
	public Optional<ISticker> getSticker(Identifiable identifiable) {
		List<ISticker> stickers = getStickers(identifiable);
		return stickers.stream().sorted().findFirst();
	}

	@Override
	public void addSticker(ISticker sticker, Identifiable identifiable) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeSticker(ISticker sticker, Identifiable identifiable) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isStickerAddableToClass(Class<?> clazz, ISticker sticker) {
//		INativeQuery stickerQuery = CoreModelServiceHolder.get().getNativeQuery(QUERY_STICKER_APPLICABLE);
//		return stickerQuery.executeWithParameters(stickerQuery.getIndexedParameterMap(1, id)).parallel()
//				.filter(resultId -> resultId instanceof String)
//				.map(resultId -> CoreModelServiceHolder.get().load((String) resultId, ISticker.class).orElse(null))
//				.filter(Objects::nonNull).collect(Collectors.toList());
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setStickerAddableToClass(Class<?> clazz, ISticker sticker) {
		// TODO Auto-generated method stub

	}

}
