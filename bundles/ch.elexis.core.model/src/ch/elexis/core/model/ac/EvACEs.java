package ch.elexis.core.model.ac;

import ch.elexis.core.ac.EvACE;
import ch.elexis.core.ac.EvaluatableACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.ILabResult;

public class EvACEs {
	
	public static final EvaluatableACE ACCOUNTING_GLOBAL = EvACE.of(IInvoice.class, Right.READ).and(Right.VIEW);
	public static final EvaluatableACE CASE_DEFINE_SPECIALS = EvACE.of(ICoverage.class, Right.UPDATE).and(Right.EXECUTE);
	public static final EvaluatableACE ACCOUNTING_STATS = EvACE.of(IEncounter.class, Right.READ).and(Right.VIEW);
	public static final EvaluatableACE KONS_REASSIGN = EvACE.of(IEncounter.class, Right.UPDATE).and(Right.READ);
	public static final EvaluatableACE LSTG_VERRECHNEN = EvACE.of(IBilled.class, Right.CREATE).and(Right.UPDATE);
	public static final EvaluatableACE LAB_SEEN = EvACE.of(ILabResult.class, Right.EXECUTE).and(Right.READ).and(Right.VIEW);
}
