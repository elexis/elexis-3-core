package ch.elexis.core.services;

import static ch.elexis.core.services.AllServiceTests.getPatient;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.ILabMapping;
import ch.elexis.core.model.ILabOrder;
import ch.elexis.core.model.ILabResult;
import ch.elexis.core.model.ILaboratory;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.model.builder.ILabItemBuilder;
import ch.elexis.core.model.builder.ILabResultBuilder;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.types.LabItemTyp;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.rgw.tools.Result;

public class ILabServiceTest extends AbstractServiceTest {
	
	private static String FORMULA = "Math.round((04_500-04_501-(04_520/2.2))*100)/100.0";
	
	private ILabService labService = OsgiServiceUtil.getService(ILabService.class).get();
	
	static ILaboratory laboratory;
	
	static ILabItem _04_500;
	static ILabItem _04_501;
	static ILabItem _04_520;
	static ILabItem _04_510_LDL_CALCULATED;
	
	@BeforeClass
	public static void before(){
		
		laboratory =
			new IContactBuilder.LaboratoryBuilder(CoreModelServiceHolder.get(), "myLab")
				.buildAndSave();
		laboratory.addXid(XidConstants.XID_KONTAKT_LAB_SENDING_FACILITY, "ABXMicrosEmi", true);
		
		_04_500 = new ILabItemBuilder(coreModelService, "TCHO-P", "TCHO-P", "3.88-5.66",
			"3.88-5.66", "mmol/l", LabItemTyp.NUMERIC, "04 Cholesterin", 500)
				.origin(laboratory, "TCHO-P", true).buildAndSave();
		_04_501 = new ILabItemBuilder(coreModelService, "HDLC-P", "HDLC-P", "0.93-1.55",
			"1.16-1.78", "mmol/l", LabItemTyp.NUMERIC, "04 Cholesterin", 501).buildAndSave();
		_04_520 = new ILabItemBuilder(coreModelService, "TG-P", "TG-P", "0.56-1.68", "0.56-1.68",
			"mmol/l", LabItemTyp.NUMERIC, "04 Cholesterin", 520).buildAndSave();
		_04_510_LDL_CALCULATED = new ILabItemBuilder(coreModelService, "LDL", "LDL (calculated",
			"<4.0", "<4.0", "mmol/l", LabItemTyp.FORMULA, "04 Cholesterin", 510).build();
		_04_510_LDL_CALCULATED.setFormula(FORMULA);
		coreModelService.save(_04_510_LDL_CALCULATED);
		
		assertEquals("04_500", _04_500.getVariableName());
		assertEquals("04_501", _04_501.getVariableName());
		assertEquals("04_520", _04_520.getVariableName());
		assertEquals("04_510", _04_510_LDL_CALCULATED.getVariableName());
	}
	
	@Test
	public void evaluate(){
		new ILabResultBuilder(coreModelService, _04_500, getPatient()).result("5.33")
			.buildLabOrder("1").buildAndSave();
		new ILabResultBuilder(coreModelService, _04_501, getPatient()).result("1.82")
			.buildLabOrder("1").buildAndSave();
		new ILabResultBuilder(coreModelService, _04_520, getPatient()).result("1.85")
			.buildLabOrder("1").buildAndSave();
		
		ILabResult ldlHldResult =
			new ILabResultBuilder(coreModelService, _04_510_LDL_CALCULATED, getPatient())
				.buildLabOrder("1").buildAndSave();
		
		IQuery<ILabOrder> query = coreModelService.getQuery(ILabOrder.class);
		query.and(ModelPackage.Literals.ILAB_ORDER__RESULT, COMPARATOR.EQUALS, ldlHldResult);
		assertTrue(query.executeSingleResult().isPresent());
		
		Result<String> result = labService.evaluate(ldlHldResult);
		assertTrue(result.isOK());
		assertNotNull(result.get());
		assertEquals("2.67", result.get());
	}
	
	@Test
	public void resolveLabMapping() {
		assertFalse(labService.getLabMappingByContactAndItem(laboratory, _04_501).isPresent());
		
		Optional<ILabMapping> mapping = labService.getLabMappingByContactAndItem(laboratory, _04_500);
		assertTrue(mapping.isPresent());
		assertEquals("TCHO-P", mapping.get().getItemName());

	}
	
}
