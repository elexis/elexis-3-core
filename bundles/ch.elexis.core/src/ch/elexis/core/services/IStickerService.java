package ch.elexis.core.services;

import java.util.List;
import java.util.Optional;

import ch.elexis.core.model.ISticker;
import ch.elexis.core.model.Identifiable;

public interface IStickerService {

	/**
	 * Get all stickers for the {@link Identifiable}.
	 * 
	 * @param identifiable
	 * @return
	 */
	public List<ISticker> getStickers(Identifiable identifiable);

	/**
	 * Get the sticker with the highest importance for the {@link Identifiable}.
	 * 
	 * @param identifiable
	 * @return
	 */
	public Optional<ISticker> getSticker(Identifiable identifiable);

	public void addSticker(ISticker sticker, Identifiable identifiable);

	public void removeSticker(ISticker sticker, Identifiable identifiable);

	/**
	 * Determine whether an ISticker is applicable to the given clazz
	 * 
	 * @param clazz
	 * @param sticker
	 * @return
	 */
	public boolean isStickerAddableToClass(Class<?> clazz, ISticker sticker);

	public void setStickerAddableToClass(Class<?> clazz, ISticker sticker);
	
	/**
	 * Find all Stickers applicable for a given class
	 * 
	 * @param clazz
	 * @return
	 */
	public List<ISticker> getStickersForClass(Class<?> clazz);
	
}
