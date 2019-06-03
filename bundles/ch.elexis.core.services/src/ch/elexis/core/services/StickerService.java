package ch.elexis.core.services;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.jpa.entities.EntityWithId;
import ch.elexis.core.jpa.entities.StickerClassLink;
import ch.elexis.core.jpa.entities.StickerObjectLink;
import ch.elexis.core.jpa.entities.StickerObjectLinkId;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

@Component
public class StickerService implements IStickerService {

	@Reference(target = "(id=default)")
	private IElexisEntityManager entityManager;
	
	@Reference
	private IStoreToStringService storeToStringServcie;
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService iModelService;
	
	private List<StickerObjectLink> getStickerObjectLinksForId(String id){
		EntityManager em = (EntityManager) entityManager.getEntityManager(true);
		TypedQuery<StickerObjectLink> query =
			em.createNamedQuery("StickerObjectLink.obj", StickerObjectLink.class);
		query.setParameter("obj", id);
		return query.getResultList();
	}
	
	private StickerObjectLink getStickerObjectLink(String id, String etikette){
		EntityManager em = (EntityManager) entityManager.getEntityManager(true);
		return em.find(StickerObjectLink.class, new StickerObjectLinkId(id, etikette));
	}
	
	/**
	 * Get all {@link ISticker} linked to the provided object id. The returned list is sorted by
	 * {@link ISticker#getImportance()}.
	 * 
	 * @param id
	 * @return
	 */
	private List<ISticker> getStickersForId(String id){
		List<StickerObjectLink> stickerObjectLinks = getStickerObjectLinksForId(id);
		List<ISticker> loadedStickers = stickerObjectLinks
			.stream().map(sol -> CoreModelServiceHolder.get()
				.load(sol.getEtikette(), ISticker.class).orElse(null))
			.filter(Objects::nonNull).collect(Collectors.toList());
		loadedStickers.sort(new StickerSorter());
		return loadedStickers;
	}
	
	@Override
	public List<ISticker> getStickers(Identifiable identifiable) {
		return getStickersForId(identifiable.getId());
	}

	@Override
	public Optional<ISticker> getSticker(Identifiable identifiable) {
		List<ISticker> stickers = getStickers(identifiable);
		if (stickers != null && !stickers.isEmpty()) {
			return Optional.of(stickers.get(0));
		}
		return Optional.empty();
	}

	@Override
	public void addSticker(ISticker sticker, Identifiable identifiable) {
		EntityManager em = (EntityManager) entityManager.getEntityManager(false);
		try {
			StickerObjectLink link = new StickerObjectLink();
			link.setEtikette(sticker.getId());
			link.setObj(identifiable.getId());
			
			em.getTransaction().begin();
			EntityWithId merged = em.merge(link);
			em.getTransaction().commit();
		} finally {
			entityManager.closeEntityManager(em);
		}
	}

	@Override
	public void removeSticker(ISticker sticker, Identifiable identifiable) {
		StickerObjectLink stickerObjectLink =
			getStickerObjectLink(identifiable.getId(), sticker.getId());
		if (stickerObjectLink != null) {
			EntityManager em = (EntityManager) entityManager.getEntityManager(false);
			try {
				em.getTransaction().begin();
				EntityWithId object = em.merge(stickerObjectLink);
				em.remove(object);
				em.getTransaction().commit();
			} finally {
				entityManager.closeEntityManager(em);
			}
		}
	}

	private List<StickerClassLink> getStickerClassLinksForSticker(String id){
		EntityManager em = (EntityManager) entityManager.getEntityManager(true);
		TypedQuery<StickerClassLink> query =
			em.createNamedQuery("StickerClassLink.sticker", StickerClassLink.class);
		query.setParameter("sticker", id);
		return query.getResultList();
	}
	
	@Override
	public boolean isStickerAddableToClass(Class<?> clazz, ISticker sticker) {
		String type = getTypeForClass(clazz);
		if(type != null) {
			List<StickerClassLink> classLinks = getStickerClassLinksForSticker(sticker.getId());
			for (StickerClassLink stickerClassLink : classLinks) {
				if (type.equals(stickerClassLink.getObjclass())) {
					return true;
				}
			}			
		} else {
			throw new IllegalStateException("Could not get type for [" + clazz + "]");
		}
		return false;
	}

	@Override
	public void setStickerAddableToClass(Class<?> clazz, ISticker sticker) {
		String type = getTypeForClass(clazz);
		if (type != null) {
			EntityManager em = (EntityManager) entityManager.getEntityManager(false);
			try {
				StickerClassLink link = new StickerClassLink();
				link.setObjclass(type);
				link.setSticker(sticker.getId());
				
				em.getTransaction().begin();
				EntityWithId merged = em.merge(link);
				em.getTransaction().commit();
			} finally {
				entityManager.closeEntityManager(em);
			}
		} else {
			throw new IllegalStateException("Could not get type for [" + clazz + "]");
		}
	}

	private class StickerSorter implements Comparator<ISticker> {
		@Override
		public int compare(ISticker s1, ISticker s2){
			return Integer.valueOf(s2.getImportance())
				.compareTo(Integer.valueOf(s1.getImportance()));
		}
	}

	@Override
	public List<ISticker> getStickersForClass(Class<?> clazz){
		String type = getTypeForClass(clazz);
		if (type != null) {
			EntityManager em = (EntityManager) entityManager.getEntityManager(true);
			TypedQuery<StickerClassLink> query =
				em.createNamedQuery("StickerClassLink.objclass", StickerClassLink.class);
			query.setParameter("objclass", type);
			
			List<StickerClassLink> results = query.getResultList();
			Set<String> stickerIds = new HashSet<>();
			results.forEach(item -> stickerIds.add(item.getSticker()));
			
			INamedQuery<ISticker> queryAllStickers =
				iModelService.getNamedQuery(ISticker.class, "ids");
			return queryAllStickers
				.executeWithParameters(queryAllStickers.getParameterMap("ids", stickerIds));
		} else {
			throw new IllegalStateException("Could not get type for [" + clazz + "]");
		}
	}

	private String getTypeForClass(Class<?> clazz){
		if (EntityWithId.class.isAssignableFrom(clazz)) {
			return storeToStringServcie.getTypeForEntity(clazz);
		} else if (Identifiable.class.isAssignableFrom(clazz)) {
			return storeToStringServcie.getTypeForModel(clazz);
		}
		return null;
	}
}
