package ch.elexis.core.services;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.model.ISticker;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

@Component
public class StickerService implements IStickerService {
	
	private final String QUERY_STICKERS_FOR_OBJECT =
		"SELECT etikette FROM ETIKETTEN_OBJECT_LINK WHERE obj=?1";
	
	private List<ISticker> getStickersForId(String id){
		INativeQuery stickerQuery =
			CoreModelServiceHolder.get().getNativeQuery(QUERY_STICKERS_FOR_OBJECT);
		return stickerQuery
			.executeWithParameters(stickerQuery.getIndexedParameterMap(1, id)).parallel()
			.filter(resultId -> resultId instanceof String).map(resultId -> CoreModelServiceHolder
				.get().load((String) resultId, ISticker.class).orElse(null))
			.filter(st -> st != null).collect(Collectors.toList());
	}
	
	@Override
	public List<ISticker> getStickers(Identifiable identifiable){
		return getStickersForId(identifiable.getId());
	}
	
	@Override
	public Optional<ISticker> getSticker(Identifiable idnetifiable){
		List<ISticker> stickers = getStickers(idnetifiable);
		// sort by importance
		Collections.sort(stickers, new Comparator<ISticker>() {
			@Override
			public int compare(ISticker left, ISticker right){
				return Integer.valueOf(left.getImportance())
					.compareTo(Integer.valueOf(right.getImportance()));
			}
			
		});
		return stickers.isEmpty() ? Optional.empty() : Optional.of(stickers.get(0));
	}
	
	@Override
	public void addSticker(ISticker sticker, Identifiable identifiable){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void removeSticker(ISticker sticker, Identifiable identifiable){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean isStickerAddableToClass(Class<?> clazz, ISticker sticker){
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void setStickerAddableToClass(Class<?> clazz, ISticker sticker){
		// TODO Auto-generated method stub
		
	}
	
}
