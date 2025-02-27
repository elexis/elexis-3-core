package ch.elexis.core.services;

import java.util.List;
import java.util.Optional;

import ch.elexis.core.model.ISticker;
import ch.elexis.core.model.Identifiable;

public interface IStickerService {

	String STICKER_ID_READONLY = "readOnly";
	String PEA_MEDIORDER_STICKER_ID = "activate_mediorder";

	/**
	 * Whether the identifiable is tagged with the given sticker
	 *
	 * @param identifiable
	 * @param iSticker
	 * @return
	 */
	public boolean hasSticker(Identifiable identifiable, ISticker iSticker);

	/**
	 * Get all stickers for the {@link Identifiable} excluding pea mediorder
	 * sticker. The returned list is sorted by {@link ISticker#getImportance()}.
	 *
	 * @param identifiable
	 * @return
	 */
	public List<ISticker> getStickers(Identifiable identifiable);

	/**
	 * Get all stickers for the {@link Identifiable}.The returned list is sorted by
	 * {@link ISticker#getImportance()}.
	 * 
	 * @param identifiable
	 * @param includeMediorderSticker
	 * @return
	 */
	public List<ISticker> getStickers(Identifiable identifiable, boolean includeMediorderSticker);

	/**
	 * Get the sticker with the highest importance for the {@link Identifiable}.
	 *
	 * @param identifiable
	 * @return
	 */
	public Optional<ISticker> getSticker(Identifiable identifiable);

	/**
	 * Get the attached instance of the given sticker on identifiable, if it exists
	 *
	 * @param identifiable
	 * @param sticker
	 * @return <code>null</code> or the attached instance, with
	 *         {@link ISticker#getAttachedTo()} being non <code>null</code>
	 */
	public ISticker getSticker(Identifiable identifiable, ISticker sticker);

	/**
	 * Add a sticker to a given identifiable with optional detail data. There can
	 * only exist on attachment of a given sticker to identifiable, so multiple
	 * calls overwrite the existing.
	 *
	 * @param sticker           to apply
	 * @param identifiable      to apply the sticker to
	 * @param <code>null</code> or data which only applies to this specific
	 *                          sticker/identifiable attachment
	 */
	public void addSticker(ISticker sticker, Identifiable identifiable, String data);

	/**
	 * Add a sticker to a given identifiable
	 *
	 * @param sticker
	 * @param identifiable
	 * @see IStickerService#addSticker(ISticker, Identifiable, String)
	 */
	default void addSticker(ISticker sticker, Identifiable identifiable) {
		addSticker(sticker, identifiable, null);
	}

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

	/**
	 * Find all objects of the provided type marked with the {@link ISticker}.
	 *
	 * @param <T>
	 * @param sticker
	 * @param type
	 * @return
	 */
	public <T> List<T> getObjectsWithSticker(ISticker sticker, Class<T> type);

}
