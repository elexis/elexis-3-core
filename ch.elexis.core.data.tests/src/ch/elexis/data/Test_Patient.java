package ch.elexis.data;

import java.util.List;

import org.junit.Test;

import ch.rgw.tools.JdbcLink;

public class Test_Patient extends AbstractPersistentObjectTest {
	
	private static Patient male = null;
	private static Patient female = null;
	private static JdbcLink link = null;

	private void setUpTestDb(String flavor){
		link = initDB(flavor);
		male = new Patient("Mustermann", "Max", "1.1.2000", "m");
		female = new Patient("Musterfrau", "Erika", "31.1.2000", "w");
	}
	
	@Test
	public void iterateOverAllDbTypes(){
		final String familyNameWithApostrophe = "D'Andrea";
		String[] dbFlavors = new String[] {
			"postgresql", "mysql", "h2",
		};
		
		for (final String flavor : dbFlavors) {
			System.out.println("Running test with DB " + flavor);
			/*
			 * Display.getDefault().asyncExec(new Runnable() { public void run(){
			 * setUpTestDb(flavor); male.set(Patient.FLD_NAME, familyNameWithApostrophe); assert
			 * (Patient.NAME == familyNameWithApostrophe); } });
			 */
			if (link != null) {
				// PersistentObject.deleteAllTables();
				link.disconnect();
			}
			setUpTestDb(flavor);
			male.set(Patient.FLD_NAME, familyNameWithApostrophe);
			assert (Patient.NAME == familyNameWithApostrophe);
			Query<Patient> qbe = new Query<Patient>(Patient.class);
			qbe.add(Patient.FLD_NAME, Query.LIKE, familyNameWithApostrophe);
			List<Patient> res = qbe.execute();
			System.out.println("Search via " + flavor + " returned " + res.size() + " patients");
			assert (res.size() >= 1);
		}
	}
	
	@Test
	public void testSetComplicateName(){
		final String familyNameWithApostrophe = "D'Andrea";
		male.set(Patient.FLD_NAME, familyNameWithApostrophe);
		System.out.println("male.getName() is " + male.getName());
		assert (male.getName() == familyNameWithApostrophe);
		// fail("Not yet implemented"); // TODO
	}
	/*
	 * @Test public void testGetConstraint(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testSetConstraint(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testIsValid(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testIsDragOK(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testDelete(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetLabelBoolean(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetDiagnosen(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetPersAnamnese(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetSystemAnamnese(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testPatient(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testPatientStringStringStringString(){ // fail("Not yet implemented"); //
	 * TODO }
	 * 
	 * @Test public void testPatientStringStringTimeToolString(){ // fail("Not yet implemented"); //
	 * TODO }
	 * 
	 * @Test public void testGetFaelle(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetFixmedikation(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetMedikation(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetLetzteKons(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testCreateFallUndKons(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testNeuerFall(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetPatCode(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetKontostand(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetBalance(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetAccountExcess(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testLoadString(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testLoadByPatientID(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testDeleteBoolean(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetAuftragsnummer(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetAlter(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetRechnungen(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetAllergies(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testSetAllergies(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetPersonalAnamnese(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testSetPersonalAnamnese(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetComment(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testSetComment(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetFamilyAnamnese(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testSetFamilyAnamnese(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testSetDiagnosen(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetRisk(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testSetRisk(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testSetStammarzt(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetStammarzt(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testHashCode(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testConnectSettings(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testConnectStringStringStringStringBoolean(){ //
	 * fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testConnectJdbcLink(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetConnection(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testAddMapping(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testSetTrace(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testLock(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testUnlock(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetId(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetWrappedId(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testPersistentObject(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testPersistentObjectString(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testStoreToString(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testState(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testExists(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testIsAvailable(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetXidString(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetXid(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetXids(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testAddXid(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetSticker(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetStickers(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testRemoveSticker(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testAddSticker(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testIsDeleted(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testMap(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetFieldType(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetString(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetBinary(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetVersionedResource(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetMap(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetInt(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetListStringBoolean(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetListStringStringArray(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testSetStringString(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testSetMap(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testSetVersionedResource(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testSetBinary(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testSetInt(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testAddToList(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testRemoveFromListString(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testRemoveFromListStringString(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testCreate(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testDeleteList(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testUndelete(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testSetStringArrayStringArray(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetStringArrayStringArray(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testIsMatchingIPersistentObjectIntStringArray(){ //
	 * fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testIsMatchingStringArrayIntStringArray(){ // fail("Not yet implemented");
	 * // TODO }
	 * 
	 * @Test public void testIsMatchingMapOfStringStringIntBoolean(){ //
	 * fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testDisconnect(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testEqualsObject(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testCheckNull(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testCheckZero(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testCheckZeroDouble(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetLastUpdate(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testClearCache(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testResetCache(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetCacheTime(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testSetDefaultCacheLifetime(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetDefaultCacheLifetime(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testCreateOrModifyTable(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testExecuteSQLScript(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testExecuteScript(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testRemoveTable(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testFlatten(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testFold(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetExportFields(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetExportUIDValue(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testGetExportUIDVersion(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testExportData(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testTableExists(){
	 * 
	 * // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testTs(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testAddChangeListener(){ // fail("Not yet implemented"); // TODO }
	 * 
	 * @Test public void testRemoveChangeListener(){ // fail("Not yet implemented"); // TODO }
	 */
}
