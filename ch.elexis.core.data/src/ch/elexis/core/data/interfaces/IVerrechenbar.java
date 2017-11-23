/*******************************************************************************
 * Copyright (c) 2005-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.core.data.interfaces;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.jdt.NonNull;
import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Mandant;
import ch.elexis.data.Verrechnet;
import ch.rgw.tools.IFilter;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;
import ch.rgw.tools.TimeTool;

/**
 * Das Leistungskonzept ist "pluggable" definiert. Dies, damit neue Abrechnungssysteme jederzeit
 * leicht integriert werden können. Ein Leistungssystem muss nur das Interface Verrechenbar
 * implementieren, um von Elexis ohne weitere Modifikationen genutzt werden zu können.
 * 
 * @author gerry
 * 
 */
public interface IVerrechenbar extends ICodeElement {
	/**
	 * Definition von Informationen zu der Leistung welche für die MWSt relevant sind.
	 * <p>
	 * Schweizer MWSt (at.medevit.medelexis.vat_ch):
	 * <li>VAT_DEFAULT ... Standard MWST Satz laut Einstellungsseite</li>
	 * <li>VAT_NONE ... Keine MWST</li>
	 * <li>VAT_CH_ISMEDICAMENT ... Artikel ist als Medikament anerkannt</li>
	 * <li>VAT_CH_NOTMEDICAMENT ... Artikel ist nicht als Medikament anerkannt</li>
	 * <li>VAT_CH_ISTREATMENT ... Leistung ist als Heilbehandlung anerkannt</li>
	 * <li>VAT_CH_NOTTREATMENT ... Leistung ist nicht als Heilbehandlung anerkannt</li>
	 * </p>
	 */
	public enum VatInfo {
		VAT_DEFAULT, VAT_NONE, VAT_CH_ISMEDICAMENT, VAT_CH_NOTMEDICAMENT, VAT_CH_ISTREATMENT,
			VAT_CH_NOTTREATMENT;
		
		/**
		 * Get a String representation of a set of {@link VatInfo} elements for persisting the
		 * information.
		 * 
		 * @param set
		 * @return
		 */
		public static String encodeAsString(EnumSet<VatInfo> set){
			StringBuilder sb = new StringBuilder();
			
			for (VatInfo info : set) {
				if (sb.length() == 0)
					sb.append(info.name());
				else
					sb.append("," + info.name());
			}
			return sb.toString();
		}
		
		/**
		 * Get an EnumSet of {@link VatInfo} from a String representation produced with
		 * {@link VatInfo#encodeAsString(EnumSet)}.
		 * 
		 * @param code
		 * @return
		 */
		public static EnumSet<VatInfo> decodeFromString(String code){
			String[] names = code.split(",");
			EnumSet<VatInfo> ret = EnumSet.noneOf(VatInfo.class);
			
			for (int i = 0; i < names.length; i++) {
				ret.add(VatInfo.valueOf(names[i]));
			}
			return ret;
		}
	};
	
	public static IOptifier optifier = new DefaultOptifier();
	public static Comparator<IVerrechenbar> comparator = new IVerrechenbar.DefaultComparator();
	public static IFilter ifilter = new IVerrechenbar.DefaultFilter();
	
	public IOptifier getOptifier();
	
	/** Einen Comparator zum Sortieren von Leistungen dieses Typs liefern */
	public Comparator<IVerrechenbar> getComparator();
	
	/** Einen Filter liefern, um Elemente dieses Typs nach Mandant zu filtern */
	public IFilter getFilter(Mandant m);
	
	/**
	 * Get the (T)ax (P)oint value of the {@link IVerrechenbar}. Parameters are provided as context
	 * to determine the correct value.
	 * 
	 * @param date
	 * @param fall
	 * @return
	 */
	public int getTP(@NonNull TimeTool date, @Nullable IFall fall);
	
	/**
	 * Get the (T)ax (P)oint value of the {@link IVerrechenbar}. Parameters are provided as context
	 * to determine the correct value. This method was introduced because a context with
	 * {@link Konsultation} was needed, as context with {@link IFall} was not specific enough. </br>
	 * </br>
	 * If parameter kons is null, value of {@link IVerrechenbar#getTP(TimeTool, IFall)} is returned.
	 * 
	 * @param date
	 * @param kons
	 * @return
	 */
	public default int getTP(@NonNull TimeTool date, @Nullable Konsultation kons){
		if (kons != null) {
			return getTP(date, kons.getFall());
		}
		return getTP(date, (IFall) null);
	}
	
	public double getFactor(TimeTool date, IFall fall);
	
	/**
	 * Eigene Kosten für diese Leistung
	 * 
	 * @param dat
	 *            Datum, für das die Kosten geliefert werden sollen
	 */
	public Money getKosten(TimeTool dat);
	
	/** Zeitanrechnung für diese Leistung (in Minuten) */
	public int getMinutes();
	
	public String getXidDomain();
	
	/** Die MWSt Informationen zu dieser Leistung */
	public VatInfo getVatInfo();
	
	// public AbstractDataLoaderJob getDataloader();
	// public String[] getDisplayedFields();
	
	public static class DefaultComparator implements Comparator<IVerrechenbar> {
		public int compare(final IVerrechenbar v1, final IVerrechenbar v2){
			int i = v1.getCodeSystemName().compareTo(v2.getCodeSystemName());
			if (i == 0) {
				i = v1.getCode().compareTo(v2.getCode());
			}
			return i;
		}
		
	}
	
	public static class DefaultFilter implements IFilter {
		public boolean select(final Object toTest){
			return true;
		}
		
	}
	
	public static class DefaultOptifier implements IOptifier {
		
		private Logger log;
		private Verrechnet newVerrechnet;
		
		public Result<Object> optify(final Konsultation kons){
			return new Result<Object>(kons);
		}
		
		public Result<IVerrechenbar> add(final IVerrechenbar code, final Konsultation kons){
			List<Verrechnet> old = kons.getLeistungen();
			Verrechnet foundVerrechnet = null;
			for (Verrechnet verrechnet : old) {
				IVerrechenbar vrElement = verrechnet.getVerrechenbar();
				if (vrElement == null) {
					// #2454 This should not happen, may however if we have to consider
					// elements where the responsible plugin is not available
					if (log == null)
						log = LoggerFactory.getLogger(DefaultOptifier.class);
					log.error("IVerrechenbar is not resolvable in " + verrechnet.getId() + " is "
						+ verrechnet.get(Verrechnet.CLASS) + " available?");
					continue;
				}
				
				if (vrElement.getId().equals(code.getId())) {
					if (verrechnet.getText().equals(code.getText())) {
						foundVerrechnet = verrechnet;
						break;
					}
				}
			}
			
			if (foundVerrechnet != null) {
				foundVerrechnet.changeAnzahl(foundVerrechnet.getZahl() + 1);
			} else {
				newVerrechnet = new Verrechnet(code, kons, 1);
				old.add(newVerrechnet);
			}
			return new Result<IVerrechenbar>(code);
		}
		
		public Result<Verrechnet> remove(final Verrechnet v, final Konsultation kons){
			List<Verrechnet> old = kons.getLeistungen();
			old.remove(v);
			v.delete();
			return new Result<Verrechnet>(null);
		}

		@Override
		public Verrechnet getCreatedVerrechnet(){
			return newVerrechnet;
		}
	}
}
