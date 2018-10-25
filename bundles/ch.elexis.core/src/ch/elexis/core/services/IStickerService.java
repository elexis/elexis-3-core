package ch.elexis.core.services;

import java.util.List;
import java.util.Optional;

import ch.elexis.core.model.ISticker;
import ch.elexis.core.model.Identifiable;

public interface IStickerService {
	
	public List<ISticker> getStickers(Identifiable identifiable);
	
	public Optional<ISticker> getSticker(Identifiable identifiable);
	
	public void addSticker(ISticker sticker, Identifiable identifiable);
	
	public void removeSticker(ISticker sticker, Identifiable identifiable);
	
	public boolean isStickerAddableToClass(Class<?> clazz, ISticker sticker);
	
	public void setStickerAddableToClass(Class<?> clazz, ISticker sticker);
}
