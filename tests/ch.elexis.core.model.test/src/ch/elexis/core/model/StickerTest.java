package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.test.AbstractTest;

public class StickerTest extends AbstractTest {

	@Test
	public void createDelete() {
		ISticker sticker = coreModelService.create(ISticker.class);
		sticker.setBackground("123456");
		sticker.setForeground("123456");
		sticker.setImportance(1000);
		sticker.setName("Sticker 1");
		coreModelService.save(sticker);

		IQuery<ISticker> qre = coreModelService.getQuery(ISticker.class);
		qre.orderBy(ModelPackage.Literals.ISTICKER__IMPORTANCE, ORDER.DESC);
		List<ISticker> stickers = qre.execute();

		assertEquals(3, stickers.size());
		assertEquals("Sticker 1", stickers.get(0).getName());
//			assertEquals(0, sticker.getStickerClassLinks().size());
		//
//			// updates a sticker
//			sticker.setName("Sticker 11");
//			sticker.setBackground("0000");
//			StickerClassLink stickerClassLink = new StickerClassLink();
//			stickerClassLink.setObjclass(Artikel.class.getSimpleName());
//			stickerClassLink.setSticker(sticker);
//			sticker.getStickerClassLinks().add(stickerClassLink);
		//
//			StickerObjectLink stickerObjectLink = new StickerObjectLink();
//			stickerObjectLink.setObj(Artikel.class.getSimpleName());
//			stickerObjectLink.setSticker(sticker);
//			sticker.getStickerObjectLinks().add(stickerObjectLink);
//			sticker = StickerService.save(sticker);
		//
//			assertEquals("Sticker 11", sticker.getName());
//			assertEquals("0000", sticker.getBackground());
//			assertEquals(1, sticker.getStickerClassLinks().size());
//			assertEquals(1, sticker.getStickerObjectLinks().size());
//			for (StickerClassLink stLink : sticker.getStickerClassLinks()) {
//				assertEquals(Artikel.class.getSimpleName(), stLink.getObjclass());
//				assertEquals(stLink.getSticker().getId(), sticker.getId());
//			}
//			for (StickerObjectLink stLink : sticker.getStickerObjectLinks()) {
//				assertEquals(Artikel.class.getSimpleName(), stLink.getObj());
//				assertEquals(stLink.getSticker().getId(), sticker.getId());
//			}
		//
//			// update sticker class links
//			assertEquals(1, sticker.getStickerClassLinks().size());
//			StickerClassLink next = sticker.getStickerClassLinks().iterator().next();
//			sticker.getStickerClassLinks().remove(next);
//			sticker = StickerService.save(sticker);
//			assertEquals(0, sticker.getStickerClassLinks().size());
//			assertEquals(1, sticker.getStickerObjectLinks().size());
//			
//			StickerService.remove(sticker);
//		}
	}

}
