package ch.elexis.core.importer.div.importers;

import java.util.List;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.ILabOrder;
import ch.elexis.core.model.ILabResult;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.types.LabItemTyp;
import ch.elexis.hl7.model.TextData;
import ch.rgw.tools.TimeTool;

public interface ILabImportUtil {

	ILabItem getLabItem(String code, IContact labor);

	ILabItem createLabItem(String code, String name, IContact labor, String object, String object2,
		String unit, LabItemTyp typ, String testGroupName, String nextTestGroupSequence);

	ILabItem getDocumentLabItem(String liShort, String liName, IContact labor);

	void createCommentsLabResult(TextData hl7TextData, IPatient pat, IContact labor, int number, TimeTool commentDate);

	String importLabResults(List<TransientLabResult> results, ImportHandler importHandler);

	void createDocumentManagerEntry(String title, String lab, byte[] data, String mimeType, TimeTool date, IPatient pat);

	ILabResult createLabResult(IPatient patient, TimeTool date, ILabItem labItem, String result,
		String comment, String refVal, IContact origin, String subId, ILabOrder labOrder,
		String labOrderorderId, String labOrdermandantId,
		TimeTool labOrdertime, String labOrdergroupName);
	
	void updateLabResult(ILabResult iLabResult, TransientLabResult transientLabResult);
	
}
