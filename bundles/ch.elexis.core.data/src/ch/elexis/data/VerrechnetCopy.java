package ch.elexis.data;

import java.util.ArrayList;
import java.util.List;

import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;

public class VerrechnetCopy extends Verrechnet {

	public static final String RECHNUNGID = "RechnungId"; //$NON-NLS-1$
	public static final String BEHANDLUNGID = "BehandlungId"; //$NON-NLS-1$

	private static final String TABLENAME = "VERRECHNETCOPY"; //$NON-NLS-1$

	static {
		addMapping(TABLENAME, RECHNUNGID, BEHANDLUNGID, LEISTG_TXT, LEISTG_CODE, CLASS, COUNT, COST_BUYING,
				SCALE_TP_SELLING, SCALE_SELLING, PRICE_SELLING, SCALE, SCALE2, "ExtInfo=Detail", USERID);
	}

	public VerrechnetCopy() {
	}

	public VerrechnetCopy(String id) {
		super(id);
	}

	public static VerrechnetCopy load(final String id) {
		return new VerrechnetCopy(id);
	}

	@Override
	protected String getTableName() {
		return TABLENAME;
	}

	/**
	 * {@link VerrechnetCopy} instances are created on successful creation of a
	 * {@link Rechnung}. <b>Using this class differently is not allowed!</b>
	 *
	 * @param verrechnet
	 * @param bill
	 */
	public VerrechnetCopy(Verrechnet verrechnet, Rechnung bill) {
		create(null);
		set(new String[] { RECHNUNGID, BEHANDLUNGID, LEISTG_TXT, LEISTG_CODE, CLASS, COUNT, COST_BUYING,
				SCALE_TP_SELLING, SCALE_SELLING, PRICE_SELLING, SCALE, SCALE2, USERID },
				new String[] { bill.getId(), verrechnet.getKons().getId(), verrechnet.get(LEISTG_TXT),
						verrechnet.get(LEISTG_CODE), verrechnet.get(CLASS), verrechnet.get(COUNT),
						verrechnet.get(COST_BUYING), verrechnet.get(SCALE_TP_SELLING), verrechnet.get(SCALE_SELLING),
						verrechnet.get(PRICE_SELLING), verrechnet.get(SCALE), verrechnet.get(SCALE2),
						verrechnet.get(USERID) });
		// copy vat scale for reporting
		setDetail(Verrechnet.VATSCALE, verrechnet.getDetail(Verrechnet.VATSCALE));
	}

	/**
	 * Get a list of all {@link Verrechnet} for a bill. Also returns
	 * {@link Verrechnet} for canceled bills.
	 *
	 * @param bill
	 * @return
	 */
	public static List<Verrechnet> getVerrechnetByBill(Rechnung bill) {
		ArrayList<Verrechnet> ret = new ArrayList<Verrechnet>();
		Query<VerrechnetCopy> vcQuery = new Query<VerrechnetCopy>(VerrechnetCopy.class);
		vcQuery.add(VerrechnetCopy.RECHNUNGID, Query.EQUALS, bill.getId());
		List<VerrechnetCopy> res = vcQuery.execute();
		ret.addAll(res);
		return ret;
	}

	/**
	 * Get a list of all {@link VerrechnetCopy} for a {@link Konsultation}. This
	 * information can be used to determine all {@link Rechnung} for a
	 * {@link Konsultation}.
	 *
	 * @param consultation
	 * @return
	 */
	public static List<VerrechnetCopy> getVerrechnetCopyByConsultation(Konsultation consultation) {
		Query<VerrechnetCopy> vcQuery = new Query<VerrechnetCopy>(VerrechnetCopy.class);
		vcQuery.add(VerrechnetCopy.BEHANDLUNGID, Query.EQUALS, consultation.getId());
		return vcQuery.execute();
	}

	@Override
	public Money getBruttoPreis() {
		int tp = checkZero(get(SCALE_TP_SELLING));
		TimeTool date = new TimeTool(getLastUpdate());
		Konsultation k = Konsultation.load(get(BEHANDLUNGID));
		Fall fall = k.getFall();
		IVerrechenbar v = getVerrechenbar();
		double tpw = 1.0;
		if (v != null) { // Unknown tax system
			tpw = v.getFactor(date, fall);
		}
		return new Money((int) Math.round(tpw * tp));
	}

	@Override
	public Konsultation getKons() {
		return Konsultation.load(get(BEHANDLUNGID));
	}
}
