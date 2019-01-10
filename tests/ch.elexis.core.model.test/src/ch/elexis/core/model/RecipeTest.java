package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.builder.IPrescriptionBuilder;
import ch.elexis.core.test.AbstractTest;

public class RecipeTest extends AbstractTest {
	
	private IPrescription prescription;
	
	@Before
	public void before(){
		super.before();
		createPatient();
		createMandator();
		createLocalArticle();
		
		prescription = new IPrescriptionBuilder(coreModelService, localArticle, patient, "1-0-0-1")
			.buildAndSave();
	}
	
	@After
	public void after(){
		coreModelService.remove(prescription);
		
		super.after();
	}
	
	@Test
	public void create(){
		IRecipe recipe = coreModelService.create(IRecipe.class);
		assertNotNull(recipe);
		assertTrue(recipe instanceof IRecipe);
		
		recipe.setDate(LocalDateTime.of(2000, 2, 2, 0, 0));
		recipe.setPatient(patient);
		recipe.setMandator(mandator);
		coreModelService.save(recipe);
		
		Optional<IRecipe> loaded = coreModelService.load(recipe.getId(), IRecipe.class);
		assertTrue(loaded.isPresent());
		assertFalse(recipe == loaded.get());
		assertEquals(recipe, loaded.get());
		assertEquals(recipe.getPatient(), loaded.get().getPatient());
		assertEquals(recipe.getMandator(), loaded.get().getMandator());
		assertEquals(recipe.getDate(), loaded.get().getDate());
		assertEquals(LocalDateTime.of(2000, 2, 2, 0, 0), loaded.get().getDate());
		
		coreModelService.remove(recipe);
	}
}
