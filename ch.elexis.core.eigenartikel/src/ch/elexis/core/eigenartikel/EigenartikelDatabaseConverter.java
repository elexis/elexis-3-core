package ch.elexis.core.eigenartikel;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;

import static ch.elexis.core.eigenartikel.Eigenartikel.*;

public class EigenartikelDatabaseConverter {
	
	private static Logger log = LoggerFactory.getLogger(EigenartikelDatabaseConverter.class);
	
	/**
	 * Convert the Eigenartikel contained in the ARTIKEL table to the 3.2 format
	 */
	public static void performConversionTo32Format(IProgressMonitor pm){
		List<Eigenartikel> qre = new Query<Eigenartikel>(Eigenartikel.class).execute();
		String taskName = "Converting " + qre.size() + " Eigenartikel to 3.2 format.";
		log.info(taskName);
		pm.beginTask(taskName, qre.size());
		for (Eigenartikel ea : qre) {
			String[] values = ea.get(false, FLD_EAN, FLD_SUB_ID, FLD_LIEFERANT_ID, MAXBESTAND,
				MINBESTAND, ISTBESTAND, FLD_EK_PREIS, FLD_VK_PREIS);
			long count = Arrays.asList(values).stream().filter(Objects::nonNull)
				.filter(p -> p.length() > 0).count();
			if (count > 0) {
				// convert condition already met, no HashMap resolve required
				EigenartikelDatabaseConverter.convertTo32PackageProduct(ea);
				continue;
			}
			
			String ext = ea.getExt(VERPACKUNGSEINHEIT);
			if (ext != null && ext.length() > 0) {
				EigenartikelDatabaseConverter.convertTo32PackageProduct(ea);
			}
			log.debug("Converted " + ea.getLabel());
			pm.worked(1);
		}
		log.info("Done");
		pm.done();
	}
	
	/**
	 * Convert a single Eigenartikel to the 3.2 package product format
	 * 
	 * @param ea
	 */
	public static void convertTo32PackageProduct(Eigenartikel ea){
		String parent = ea.get(FLD_EXTID);
		if (parent != null && parent.length() > 0) {
			if (new Query<Eigenartikel>(Eigenartikel.class, PersistentObject.FLD_ID, parent)
				.size() > 0) {
				log.info("Skipping " + ea.getId() + " as a parent is already referenced.");
				return;
			}
		}
		
		Eigenartikel eaProduct = new Eigenartikel(ea.getName(), ea.getInternalName());
		eaProduct.set(new String[] {
			FLD_ATC_CODE, FLD_CODECLASS
		}, ea.getATC_code(), "U");
		
		ea.set(new String[] {
			FLD_EXTID, FLD_CODECLASS
		}, eaProduct.getId(), "U");
	}
}
