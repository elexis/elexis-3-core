package ch.elexis.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.types.LabItemTyp;

public class LabMapping extends PersistentObject {

	private static Logger logger = LoggerFactory.getLogger(LabMapping.class);

	public static final String TABLENAME = "at_medevit_elexis_labmap"; //$NON-NLS-1$

	public static final String VERSIONID = "VERSION"; //$NON-NLS-1$

	public static final String FLD_ORIGINID = "originid"; //$NON-NLS-1$
	public static final String FLD_ITEMNAME = "itemname"; //$NON-NLS-1$
	public static final String FLD_LABITEMID = "labitemid"; //$NON-NLS-1$
	public static final String FLD_CHARGE = "charge"; //$NON-NLS-1$

	private static StringBuilder notImported = new StringBuilder();

	private static int importerLabItemsCreated = 0;

	static {
		addMapping(TABLENAME, FLD_ITEMNAME, FLD_ORIGINID, FLD_LABITEMID, FLD_CHARGE);

	}

	public LabMapping() {
		// TODO Auto-generated constructor stub
	}

	public LabMapping(String id) {
		super(id);
	}

	public static LabMapping load(final String id) {
		return new LabMapping(id);
	}

	public LabMapping(String contactId, String itemName, String labItemId, boolean charge) {
		LabMapping existing = getByContactAndItemName(contactId, itemName);
		if (existing != null) {
			throw new IllegalArgumentException(
					String.format("Mapping for origin id [%s] - [%s] already exists can not create multiple instances.", //$NON-NLS-1$
							contactId, itemName));
		}

		create(null);
		set(FLD_ORIGINID, contactId);
		set(FLD_ITEMNAME, itemName);
		set(FLD_LABITEMID, labItemId);
		setCharge(charge);
	}

	public void setCharge(boolean charge) {
		set(FLD_CHARGE, charge ? "1" : "0"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public boolean isCharge() {
		String chargeStr = checkNull(get(FLD_CHARGE));
		if (chargeStr.isEmpty()) {
			return false;
		} else {
			return chargeStr.equals("1"); //$NON-NLS-1$
		}
	}

	public LabItem getLabItem() {
		String labItemId = checkNull(get(FLD_LABITEMID));
		if (labItemId.isEmpty()) {
			return null;
		} else {
			LabItem ret = LabItem.load(labItemId);
			if (ret.exists()) {
				return ret;
			} else {
				logger.error(String.format("LabItem [%s] does not exist.", get(FLD_LABITEMID))); //$NON-NLS-1$
				return ret;
			}
		}
	}

	public Kontakt getOrigin() {
		String originId = checkNull(get(FLD_ORIGINID));
		if (originId.isEmpty()) {
			return null;
		} else {
			Kontakt ret = Kontakt.load(originId);
			if (ret.exists()) {
				return ret;
			} else {
				logger.error(String.format("Kontakt [%s] does not exist.", get(FLD_ORIGINID))); //$NON-NLS-1$
				return ret;
			}
		}
	}

	public String getItemName() {
		return checkNull(get(FLD_ITEMNAME));
	}

	public void setItemName(String itemName) {
		set(FLD_ITEMNAME, itemName);
	}

	@Override
	public String getLabel() {
		LabItem item = LabItem.load(get(FLD_LABITEMID));
		if (item.exists()) {
			return String.format("%s - %s -> %s", get(FLD_ORIGINID), get(FLD_ITEMNAME), //$NON-NLS-1$
					item.getLabel());
		} else {
			return String.format("%s - %s -> item [%s] does not exist.", get(FLD_ORIGINID), //$NON-NLS-1$
					get(FLD_ITEMNAME), get(FLD_LABITEMID));
		}
	}

	@Override
	protected String getTableName() {
		return TABLENAME;
	}

	/**
	 * Test if the mapping is valid. Checking {@link LabItem}, item name and origin.
	 *
	 * @return
	 */
	public boolean isMappingValid() {
		LabItem item = getLabItem();
		if (item == null || !item.exists()) {
			return false;
		}
		String itemName = getItemName();
		if (itemName == null || itemName.isEmpty()) {
			return false;
		}
		Kontakt origin = getOrigin();
		if (origin == null || !origin.exists()) {
			return false;
		}
		return true;
	}

	public static LabMapping getByContactAndItemName(String contactId, String itemName) {
		Query<LabMapping> qbe = new Query<>(LabMapping.class);
		qbe.add("ID", Query.NOT_EQUAL, VERSIONID); //$NON-NLS-1$
		qbe.add(FLD_ORIGINID, Query.EQUALS, contactId);
		qbe.add(FLD_ITEMNAME, Query.EQUALS, itemName);
		List<LabMapping> res = qbe.execute();
		if (res.isEmpty()) {
			return null;
		} else {
			if (res.size() > 1) {
				throw new IllegalArgumentException(
						String.format("Found more then 1 mapping for origin id [%s] - [%s]", contactId, itemName)); //$NON-NLS-1$
			}
			return res.get(0);
		}
	}

	public static LabMapping getByContactAndItemId(String contactId, String itemId) {
		Query<LabMapping> qbe = new Query<>(LabMapping.class);
		qbe.add("ID", Query.NOT_EQUAL, VERSIONID); //$NON-NLS-1$
		qbe.add(FLD_ORIGINID, Query.EQUALS, contactId);
		qbe.add(FLD_LABITEMID, Query.EQUALS, itemId);
		List<LabMapping> res = qbe.execute();
		if (res.isEmpty()) {
			return null;
		} else {
			if (res.size() > 1) {
				logger.warn(String.format("Found [%s] mappings for origin id [%s] and item id [%s]", res.size(),
						contactId, itemId));
				logger.info(String.format("Using mapping with item name [%s]", res.get(0).getItemName()));
			}
			return res.get(0);
		}
	}

	public static List<LabMapping> getByLabItemId(String labItemId) {
		Query<LabMapping> qbe = new Query<>(LabMapping.class);
		qbe.add("ID", Query.NOT_EQUAL, VERSIONID); //$NON-NLS-1$
		qbe.add(FLD_LABITEMID, Query.EQUALS, labItemId);
		return qbe.execute();
	}

	/**
	 * Create or update the mapping table. The CSV format is as follows <br\>
	 *
	 * <pre>
	 * CONTACT_NAME;CONTACT_ITEMNAME;LOINCCODE;LABITEM_NAME;LABITEM_SHORTNAME;LABITEM_REFM;
	 * LABITEM_REFF;LABITEM_UNIT;LABITEM_TYP;LABITEM_GROUP;LABITEM_BILLINGCODE
	 * </pre>
	 *
	 * Possible values for LABITEM_TYP -> numeric,absolute,text <br\>
	 *
	 * @param csv
	 * @throws IOException
	 */
	public static void importMappingFromCsv(InputStream csv) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(csv));

		notImported.setLength(0);
		importerLabItemsCreated = 0;

		String line;
		while ((line = reader.readLine()) != null) {
			String[] parts = line.split(";", -1); //$NON-NLS-1$
			if (parts.length < 10) {
				String reason = String.format(Messages.LabMapping_reasonLineNotValid, line, parts.length);
				logger.warn(reason);
				notImported.append(line);
				notImported.append(" -> "); //$NON-NLS-1$
				notImported.append(reason);
				notImported.append(StringUtils.LF);
				continue;
			} else if (parts[0].equalsIgnoreCase("CONTACT_NAME")) { //$NON-NLS-1$
				// skip description line
				continue;
			}

			Labor labor = null;
			List<Labor> origins = lookupLabor(parts[0]);
			if (origins.isEmpty()) {
				logger.warn(String.format("Could not find labor with name [%s]", parts[0])); //$NON-NLS-1$
				labor = createLabor(parts[0]);
			} else if (origins.size() > 1) {
				String reason = String.format(Messages.LabMapping_reasonMoreContacts, parts[0]);
				logger.warn(reason);
				notImported.append(line);
				notImported.append(" -> "); //$NON-NLS-1$
				notImported.append(reason);
				notImported.append(StringUtils.LF);
				continue;
			} else {
				labor = origins.get(0);
			}

			String laborItemName = parts[1];
			String labItemLoinc = parts[2];
			String labItemName = parts[3];
			String labItemShort = parts[4];
			String labItemRefM = parts[5];
			String labItemRefF = parts[6];
			String labItemUnit = parts[7];
			String labItemTyp = parts[8];
			String labItemGroup = parts[9];
			String labItemBillingCode = StringUtils.EMPTY;
			if (parts.length > 10) {
				labItemBillingCode = parts[10];
			}

			if (laborItemName == null || laborItemName.isEmpty() || labItemName == null || labItemName.isEmpty()
					|| labItemShort == null || labItemShort.isEmpty()) {
				String reason = Messages.LabMapping_reasonDefinitionNotValid;
				logger.warn(reason);
				notImported.append(line);
				notImported.append(" -> "); //$NON-NLS-1$
				notImported.append(reason);
				notImported.append(StringUtils.LF);
				continue;
			}

			LabItem labItem = null;
			List<LabItem> items = lookupLabItem(labItemLoinc, labor.getId(), labItemShort, labItemRefM, labItemRefF,
					labItemUnit);
			if (items.isEmpty()) {
				logger.warn(String.format("Could not find labor item with loinc [%s] and shortname [%s]", labItemLoinc, //$NON-NLS-1$
						labItemShort));
				labItem = createLabItem(labor.getId(), labItemName, labItemShort, labItemRefM, labItemRefF, labItemUnit,
						labItemTyp, labItemGroup, labItemLoinc, labItemBillingCode);
			} else if (origins.size() > 1) {
				String reason = String.format(Messages.LabMapping_reasonMoreLabItems, labItemLoinc, labItemShort);
				logger.warn(reason);
				notImported.append(line);
				notImported.append(" -> "); //$NON-NLS-1$
				notImported.append(reason);
				notImported.append(StringUtils.LF);
				continue;
			} else {
				labItem = items.get(0);
			}

			LabMapping existing = LabMapping.getByContactAndItemName(labor.getId(), laborItemName);
			if (existing != null) {
				logger.info(String.format("Merging mapping [%s] - [%s] -> [%s] - [%s]", //$NON-NLS-1$
						labor.getKuerzel(), laborItemName, labItemLoinc, labItemShort));
				mergeMapping(labor, laborItemName, labItem);
			} else {
				logger.info(String.format("Creating mapping [%s] - [%s] -> [%s] - [%s]", //$NON-NLS-1$
						labor.getKuerzel(), laborItemName, labItemLoinc, labItemShort));
				createMapping(labor, parts[1], labItem);
			}
		}

		if (notImported.length() > 0) {
			throw new IOException(notImported.toString());
		}
	}

	private static LabItem createLabItem(String laborId, String labItemName, String labItemShort, String labItemRefM,
			String labItemRefF, String labItemUnit, String labItemTyp, String labItemGroup, String labItemLoinc,
			String labItemBillingCode) {
		logger.warn(String.format("Creating new labor item with name [%s] and shortname [%s] loinc [%s]", labItemName, //$NON-NLS-1$
				labItemShort, labItemLoinc));
		LabItem item = new LabItem(labItemShort, labItemName, Labor.load(laborId), labItemRefM, labItemRefF,
				labItemUnit, getLabItemTyp(labItemTyp), labItemGroup, Integer.toString(importerLabItemsCreated++));

		if (labItemLoinc != null && !labItemLoinc.isEmpty()) {
			item.setLoincCode(labItemLoinc);
		}
		if (labItemBillingCode != null && !labItemBillingCode.isEmpty()) {
			item.setBillingCode(labItemBillingCode);
		}
		return item;
	}

	private static LabItemTyp getLabItemTyp(String labItemTyp) {
		if (labItemTyp.equalsIgnoreCase("numeric")) { //$NON-NLS-1$
			return LabItemTyp.NUMERIC;
		} else if (labItemTyp.equalsIgnoreCase("absolute")) { //$NON-NLS-1$
			return LabItemTyp.ABSOLUTE;
		} else if (labItemTyp.equalsIgnoreCase("text")) { //$NON-NLS-1$
			return LabItemTyp.TEXT;
		}
		return LabItemTyp.NUMERIC;
	}

	private static Labor createLabor(String labName) {
		logger.warn("Creating new labor with name [" + labName + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		return new Labor(labName, "Labor " + labName); //$NON-NLS-1$
	}

	private static List<Labor> lookupLabor(String labName) {
		Query<Labor> qbe = new Query<>(Labor.class);
		qbe.add(Kontakt.FLD_NAME1, Query.LIKE, "%" + labName + "%"); //$NON-NLS-1$ //$NON-NLS-2$
		List<Labor> list = qbe.execute();
		return list;
	}

	private static List<LabItem> lookupLabItem(String loinc, String laborId, String shortDesc, String refM, String refW,
			String unit) {
		// lookup using loinc first ...
		List<LabItem> labItems = null;
		if (loinc != null && !loinc.isEmpty()) {
			Query<LabItem> qli = new Query<>(LabItem.class);
			qli.add(LabItem.LOINCCODE, "=", loinc.trim()); //$NON-NLS-1$
			labItems = qli.execute();
			if (labItems.size() == 1) {
				return labItems;
			}
		}
		return lookupLabItem(laborId, shortDesc, refM, refW, unit);
	}

	private static List<LabItem> lookupLabItem(String laborId, String shortDesc, String refM, String refW,
			String unit) {
		// dont use laborId of LabItem ... it is in the mappings now
		List<LabItem> items = LabItem.getLabItems(null, shortDesc, refM, refW, unit);
		if (!items.isEmpty()) {
			ArrayList<LabItem> ret = new ArrayList<>();
			for (LabItem labItem : items) {
				List<LabMapping> mappings = getByLabItemId(labItem.getId());
				for (LabMapping labMapping : mappings) {
					if (labMapping.get(LabMapping.FLD_ORIGINID).equals(laborId)) {
						ret.add(labItem);
					}
				}
			}
			return ret;
		}
		return items;
	}

	private static void createMapping(Kontakt origin, String itemName, LabItem item) {
		new LabMapping(origin.getId(), itemName, item.getId(), false);
	}

	private static void mergeMapping(Kontakt origin, String itemName, LabItem item) {
		LabMapping mapping = getByContactAndItemName(origin.getId(), itemName);
		mapping.set(LabMapping.FLD_LABITEMID, item.getId());
	}
}
