package ch.elexis.core.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import ch.elexis.core.common.ElexisEventTopics;
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

	@Reference
	private IAccessControlService accessControlService;

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@Reference
	private EventAdmin eventAdmin;

	@Activate
	public void activate() {
		accessControlService.doPrivileged(() -> {
			Optional<ISticker> readOnlySticker = coreModelService.load(STICKER_ID_READONLY, ISticker.class);
			if (readOnlySticker.isEmpty()) {
				ISticker sticker = coreModelService.create(ISticker.class);
				sticker.setId(STICKER_ID_READONLY);
				sticker.setName("Read Only Sticker");
				sticker.setImportance(-1);
				coreModelService.save(sticker);
			}
		});
	}

	private List<StickerObjectLink> getStickerObjectLinksForId(String id) {
		EntityManager em = (EntityManager) entityManager.getEntityManager(true);
		TypedQuery<StickerObjectLink> query = em.createNamedQuery("StickerObjectLink.obj", StickerObjectLink.class);
		query.setParameter("obj", id);
		return query.getResultList();
	}

	private List<StickerObjectLink> getStickerObjectLinksForSticker(ISticker iSticker) {
		EntityManager em = (EntityManager) entityManager.getEntityManager(true);
		TypedQuery<StickerObjectLink> query = em.createNamedQuery("StickerObjectLink.etikette",
				StickerObjectLink.class);
		query.setParameter("etikette", iSticker.getId());
		return query.getResultList();
	}

	private StickerObjectLink getStickerObjectLink(String id, String etikette) {
		EntityManager em = (EntityManager) entityManager.getEntityManager(true);
		return em.find(StickerObjectLink.class, new StickerObjectLinkId(id, etikette));
	}

	@Override
	public boolean hasSticker(Identifiable identifiable, ISticker iSticker) {
		List<StickerObjectLink> entries = findAttachments(identifiable, iSticker);
		return entries.isEmpty() ? false : true;
	}

	private List<StickerObjectLink> findAttachments(Identifiable identifiable, ISticker iSticker) {
		EntityManager em = (EntityManager) entityManager.getEntityManager(true);
		TypedQuery<StickerObjectLink> query = em.createNamedQuery("StickerObjectLink.obj.etikette",
				StickerObjectLink.class);
		query.setParameter("obj", identifiable.getId());
		query.setParameter("etikette", iSticker.getId());
		return query.getResultList();
	}

	@Override
	public <T> List<T> getObjectsWithSticker(ISticker sticker, Class<T> type) {
		List<StickerObjectLink> objectLinks = getStickerObjectLinksForSticker(sticker);
		if (!objectLinks.isEmpty()) {
			return objectLinks.stream().map(ol -> CoreModelServiceHolder.get().load(ol.getObj(), type).orElse(null))
					.filter(o -> o != null).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	@Override
	public List<ISticker> getStickers(Identifiable identifiable) {
		if (identifiable == null) {
			return Collections.emptyList();
		}
		List<StickerObjectLink> stickerObjectLinks = getStickerObjectLinksForId(identifiable.getId());
		List<ISticker> loadedStickers = new ArrayList<>();
		for (StickerObjectLink link : stickerObjectLinks) {
			ISticker sticker = loadStickerForStickerObjectLink(link, identifiable);
			if (sticker != null) {
				loadedStickers.add(sticker);
			}
		}
		loadedStickers.sort(new StickerSorter());
		return loadedStickers;
	}

	@Override
	public ISticker getSticker(Identifiable identifiable, ISticker sticker) {
		List<StickerObjectLink> resultList = findAttachments(identifiable, sticker);
		if (resultList.isEmpty()) {
			return null;
		}
		StickerObjectLink stickerObjectLink = resultList.get(0);
		return loadStickerForStickerObjectLink(stickerObjectLink, identifiable);
	}

	private ISticker loadStickerForStickerObjectLink(StickerObjectLink stickerObjectLink, Identifiable identifiable) {
		ISticker sticker = coreModelService.load(stickerObjectLink.getEtikette(), ISticker.class, false, false)
				.orElse(null);
		if (sticker != null) {
			sticker.setAttachedTo(identifiable);
			sticker.setAttachedToData(stickerObjectLink.getData());
			return sticker;
		}
		return null;
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
	public void addSticker(ISticker sticker, Identifiable identifiable, String data) {
		EntityManager em = (EntityManager) entityManager.getEntityManager(false);
		try {
			StickerObjectLink link = new StickerObjectLink();
			link.setEtikette(sticker.getId());
			link.setObj(identifiable.getId());
			link.setData(data);

			em.getTransaction().begin();
			em.merge(link);
			em.getTransaction().commit();
		} finally {
			entityManager.closeEntityManager(em);
		}

		handleUpdate(identifiable);

	}

	private void handleUpdate(Identifiable identifiable) {
		coreModelService.touch(identifiable);
		Map<String, String> map = new HashMap<>(2);
		map.put(ElexisEventTopics.ECLIPSE_E4_DATA, identifiable.getId());
		map.put(ElexisEventTopics.PROPKEY_PROPERTY, "sticker");
		Event event = new Event(ElexisEventTopics.EVENT_UPDATE, map);
		eventAdmin.postEvent(event);
	}

	@Override
	public void addSticker(ISticker sticker, Identifiable identifiable) {
		addSticker(sticker, identifiable, null);
	}

	@Override
	public void removeSticker(ISticker sticker, Identifiable identifiable) {
		StickerObjectLink stickerObjectLink = getStickerObjectLink(identifiable.getId(), sticker.getId());
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
		handleUpdate(identifiable);
	}

	private List<StickerClassLink> getStickerClassLinksForSticker(String id) {
		EntityManager em = (EntityManager) entityManager.getEntityManager(true);
		TypedQuery<StickerClassLink> query = em.createNamedQuery("StickerClassLink.sticker", StickerClassLink.class);
		query.setParameter("sticker", id);
		return query.getResultList();
	}

	@Override
	public boolean isStickerAddableToClass(Class<?> clazz, ISticker sticker) {
		String type = getTypeForClass(clazz);
		if (type != null) {
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
				em.merge(link);
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
		public int compare(ISticker s1, ISticker s2) {
			return Integer.valueOf(s2.getImportance()).compareTo(Integer.valueOf(s1.getImportance()));
		}
	}

	@Override
	public List<ISticker> getStickersForClass(Class<?> clazz) {
		String type = getTypeForClass(clazz);
		if (type != null) {
			EntityManager em = (EntityManager) entityManager.getEntityManager(true);
			TypedQuery<StickerClassLink> query = em.createNamedQuery("StickerClassLink.objclass",
					StickerClassLink.class);
			query.setParameter("objclass", type);

			List<StickerClassLink> results = query.getResultList();
			List<String> stickerIds = results.parallelStream().map(item -> item.getSticker())
					.collect(Collectors.toList());

			return coreModelService.findAllById(stickerIds, ISticker.class);
		} else {
			throw new IllegalStateException("Could not get type for [" + clazz + "]");
		}
	}

	private String getTypeForClass(Class<?> clazz) {
		if (EntityWithId.class.isAssignableFrom(clazz)) {
			return storeToStringServcie.getTypeForEntity(clazz);
		} else if (Identifiable.class.isAssignableFrom(clazz)) {
			return storeToStringServcie.getTypeForModel(clazz);
		}
		return null;
	}
}
