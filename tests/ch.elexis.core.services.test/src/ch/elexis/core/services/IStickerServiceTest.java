package ch.elexis.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.Ignore;
import org.junit.Test;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.test.TestEntities;
import ch.elexis.core.utils.OsgiServiceUtil;

public class IStickerServiceTest extends AbstractServiceTest {
	
	private IStickerService service = OsgiServiceUtil.getService(IStickerService.class).get();
	
	@Test
	public void getStickers(){
		Optional<IPatient> load =
			coreModelService.load(TestEntities.PATIENT_MALE_ID, IPatient.class);
		List<ISticker> stickers = service.getStickers(load.get());
		assertEquals(2, stickers.size());
	}
	
	@Test
	public void getSticker(){
		Optional<IPatient> load =
			coreModelService.load(TestEntities.PATIENT_MALE_ID, IPatient.class);
		Optional<ISticker> sticker = service.getSticker(load.get());
		assertEquals("verstorben", sticker.get().getName());
	}
	
	@Test
	public void addSticker(){
		Optional<IPatient> patient =
			coreModelService.load(TestEntities.PATIENT_MALE_ID, IPatient.class);
		ISticker newSticker = coreModelService.create(ISticker.class);
		newSticker.setName("test sticker");
		coreModelService.save(newSticker);
		
		service.addSticker(newSticker, patient.get());
		List<ISticker> patientStickers = service.getStickers(patient.get());
		assertFalse(patientStickers.isEmpty());
		assertTrue(patientStickers.contains(newSticker));
		for (ISticker iSticker : patientStickers) {
			assertNotNull(iSticker.getLastupdate());
		}
		
		service.removeSticker(newSticker, patient.get());
	}
	
	@Test
	public void removeSticker(){
		Optional<IPatient> patient =
			coreModelService.load(TestEntities.PATIENT_MALE_ID, IPatient.class);
		ISticker newSticker = coreModelService.create(ISticker.class);
		newSticker.setName("test sticker");
		coreModelService.save(newSticker);
		
		service.addSticker(newSticker, patient.get());
		List<ISticker> patientStickers = service.getStickers(patient.get());
		assertTrue(patientStickers.contains(newSticker));
		assertTrue(service.hasSticker(patient.get(), newSticker));
		
		service.removeSticker(newSticker, patient.get());
		patientStickers = service.getStickers(patient.get());
		assertFalse(patientStickers.contains(newSticker));
		assertFalse(service.hasSticker(patient.get(), newSticker));
	}
	
	@Ignore(value = "Not yet implemented the required mapping")
	@Test
	public void isStickerAddableToClass(){
		ISticker sticker = coreModelService.load("T3811f3656cea91c2080136", ISticker.class).get();
		assertTrue(service.isStickerAddableToClass(IPatient.class, sticker));
	}
	
	//
	//	@Test
	//	public void testCreateAndDeleteSticker() {
	//		// creates a sticker
	//		Sticker sticker = new Sticker();
	//		sticker.setBackground("0");
	//		sticker.setForeground("0");
	//		sticker.setName("Sticker 2");
	//
	//		StickerClassLink stickerClassLink = new StickerClassLink();
	//		stickerClassLink.setObjclass(ArtikelstammItem.class.getSimpleName());
	//		stickerClassLink.setSticker(sticker);
	//		sticker.getStickerClassLinks().add(stickerClassLink);
	//
	//		StickerObjectLink stickerObjectLink = new StickerObjectLink();
	//		stickerObjectLink.setObj(Artikel.class.getSimpleName());
	//		stickerObjectLink.setSticker(sticker);
	//
	//		sticker.getStickerObjectLinks().add(stickerObjectLink);
	//
	//		sticker = StickerService.save(sticker);
	//
	//		assertEquals(1, StickerService.findAll(Sticker.class, false).size());
	//		assertEquals("Sticker 2", sticker.getName());
	//		assertEquals("0", sticker.getBackground());
	//		assertEquals(1, sticker.getStickerClassLinks().size());
	//		assertEquals(1, sticker.getStickerObjectLinks().size());
	//		for (StickerClassLink stLink : sticker.getStickerClassLinks()) {
	//			assertEquals(ArtikelstammItem.class.getSimpleName(), stLink.getObjclass());
	//			assertEquals(stLink.getSticker().getId(), sticker.getId());
	//		}
	//		for (StickerObjectLink stLink : sticker.getStickerObjectLinks()) {
	//			assertEquals(Artikel.class.getSimpleName(), stLink.getObj());
	//			assertEquals(stLink.getSticker().getId(), sticker.getId());
	//		}
	//
	//		// removes a sticker
	//		StickerService.delete(StickerService.load(sticker.getId()).get());
	//		assertEquals(0, StickerService.findAll(Sticker.class, false).size());
	//		assertEquals(1, StickerService.findAll(Sticker.class, true).size());
	//	}
	//
	@Test
	public void findStickersApplicableToPatients(){
		ISticker newSticker = coreModelService.create(ISticker.class);
		newSticker.setName("Sticker 2");
		coreModelService.save(newSticker);
		
		service.setStickerAddableToClass(IPatient.class, newSticker);
		
		assertTrue(service.isStickerAddableToClass(IPatient.class, newSticker));
		assertFalse(service.isStickerAddableToClass(IArticle.class, newSticker));
		assertFalse(service.isStickerAddableToClass(IOrganization.class, newSticker));
	}
	
	@Test
	public void applyAndRemoveStickerWithData(){
		ISticker sticker = coreModelService.create(ISticker.class);
		sticker.setName("Sticker With Data");
		coreModelService.save(sticker);
		
		service.setStickerAddableToClass(IPatient.class, sticker);
		IPatient patient = coreModelService.load(TestEntities.PATIENT_MALE_ID, IPatient.class)
			.orElseThrow(() -> new IllegalStateException());
		assertNull(sticker.getAttachedTo());
		assertNull(sticker.getAttachedToData());
		
		int size = service.getStickers(patient).size();
		
		service.addSticker(sticker, patient, "abcd-1234-efgh-8765-ijkl");
		service.addSticker(sticker, patient, "abcd-1234-efgh-5678-ijkl");
		// addSticker should be idempotent
		assertEquals(size + 1, service.getStickers(patient).size());
		
		ISticker boundSticker = service.getSticker(patient, sticker);
		assertNotNull(boundSticker.getLastupdate());
		assertEquals(patient, boundSticker.getAttachedTo());
		assertEquals("abcd-1234-efgh-5678-ijkl", boundSticker.getAttachedToData());
		
		service.removeSticker(sticker, patient);
		boundSticker = service.getSticker(patient, sticker);
		assertNull(boundSticker);
	}
	
	//
	//	@Test
	//	public void testApplyRemoveStickerToPatient() {
	//		
	//		Sticker sticker = new StickerService.StickerBuilder("Sticker 2", "123456", "123456", ElexisTypeMap.TYPE_PATIENT)
	//				.buildAndSave();
	//
	//		assertEquals(1, StickerService.findAll(Sticker.class, false).size());
	//		assertEquals("Sticker 2", sticker.getName());
	//		assertEquals(1, sticker.getStickerClassLinks().size());
	//		assertEquals(ElexisTypeMap.TYPE_PATIENT, sticker.getStickerClassLinks().iterator().next().getObjclass());
	//		Optional<Kontakt> findById = KontaktService.load(TestEntities.PATIENT_MALE_ID);
	//
	//		List<Sticker> findStickersOnObject = StickerService.findStickersOnObject(findById.get());
	//		assertEquals(0, findStickersOnObject.size());
	//		
	//		boolean stickerApplied = StickerService.applyStickerToObject(sticker, findById.get());
	//		assertTrue(stickerApplied);
	//		// deliberately try twice to see that the sum still is 1
	//		stickerApplied = StickerService.applyStickerToObject(sticker, findById.get());
	//		assertTrue(stickerApplied);
	//		
	//		findStickersOnObject = StickerService.findStickersOnObject(findById.get());
	//		assertEquals(1, findStickersOnObject.size());
	//		
	//		StickerService.removeAllStickersFromObject(findById.get());
	//
	//		findStickersOnObject = StickerService.findStickersOnObject(findById.get());
	//		assertEquals(0, findStickersOnObject.size());
	//		
	//		StickerService.remove(sticker);
	//	}
	
}
