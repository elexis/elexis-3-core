package ch.elexis.core.services;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.jpa.entities.EntityWithId;
import ch.elexis.core.jpa.entities.StickerObjectLink;
import ch.elexis.core.jpa.entities.StickerObjectLinkId;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

@Component
public class StickerService implements IStickerService {

	@Reference(target = "(id=default)")
	private IElexisEntityManager entityManager;
	
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
	
	private List<ISticker> getStickersForId(String id){
		List<StickerObjectLink> stickerObjectLinks = getStickerObjectLinksForId(id);
		return stickerObjectLinks
			.parallelStream().map(sol -> CoreModelServiceHolder.get()
				.load(sol.getEtikette(), ISticker.class).orElse(null))
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
