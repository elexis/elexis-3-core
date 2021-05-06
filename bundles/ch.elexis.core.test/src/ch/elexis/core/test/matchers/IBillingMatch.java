package ch.elexis.core.test.matchers;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IEncounter;
import ch.rgw.tools.Money;

public class IBillingMatch {
	public final String code;
	public final double count;
	public final int scale1;
	public final int scale2;
	public final boolean deleted;
	public Integer vk_tp;
	public Money vk_preis;
	
	public IBillingMatch(String code, double count){
		this(code, count, false);
	}
	
	public IBillingMatch(String code, double count, boolean deleted){
		this(code, count, 100, 100, deleted);
	}
	
	public IBillingMatch(String code, double count, int scale1, int scale2, boolean deleted){
		this(code, count, scale1, scale2, null, null, deleted);
	}
	
	/**
	 * 
	 * @param code
	 * @param count
	 * @param scale1
	 * @param scale2
	 * @param vk_tp
	 *            if <code>null</code> do not match the price, else price matching will be performed
	 * @param vk_preis
	 *            if <code>null</code> do not match the price, else price matching will be performed
	 * @param deleted
	 */
	public IBillingMatch(String code, double count, int scale1, int scale2, Integer vk_tp,
		Money vk_preis, boolean deleted){
		super();
		this.code = code;
		this.scale1 = scale1;
		this.scale2 = scale2;
		this.count = count;
		this.vk_tp = vk_tp;
		this.vk_preis = vk_preis;
		this.deleted = deleted;
	}
	
	public String getCode(){
		return code;
	}
	
	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + (int) (count * 100);
		result = prime * result + (deleted ? 1231 : 1237);
		result = prime * result + scale1;
		result = prime * result + scale2;
		result = prime * result + ((vk_preis == null) ? 0 : vk_preis.hashCode());
		result = prime * result + ((vk_tp == null) ? 0 : vk_tp.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IBillingMatch other = (IBillingMatch) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (count != other.count)
			return false;
		if (deleted != other.deleted)
			return false;
		if (scale1 != other.scale1)
			return false;
		if (scale2 != other.scale2)
			return false;
		if (vk_preis == null) {
			if (other.vk_preis != null)
				return false;
		} else if (!vk_preis.equals(other.vk_preis))
			return false;
		if (vk_tp == null) {
			if (other.vk_tp != null)
				return false;
		} else if (!vk_tp.equals(other.vk_tp))
			return false;
		return true;
	}
	
	@Override
	public String toString(){
		return "IBillingMatch [code=" + code + ", count=" + count + ", scale1=" + scale1
			+ ", scale2=" + scale2 + ", deleted=" + deleted + ", vk_tp=" + vk_tp + ", vk_preis="
			+ vk_preis + "]\n";
	}
	
	public static void assertMatch(IEncounter encounter, List<IBillingMatch> matches){
		List<IBilled> billed = encounter.getBilled();
		
		List<IBillingMatch> existingVerrechnet = billed.stream()
			.map(v -> new IBillingMatch(v.getBillable().getId(), v.getAmount(), v.getPrimaryScale(),
				v.getSecondaryScale(), v.getPoints(), v.getPrice(), v.isDeleted()))
			.collect(Collectors.toList());
		
		Set<String> dontMatchPrice = new HashSet<>();
		for (IBillingMatch vm : matches) {
			if (vm.vk_tp == null || vm.vk_preis == null) {
				dontMatchPrice.add(vm.code);
			}
		}
		
		if (dontMatchPrice.size() > 0) {
			for (IBillingMatch vm : existingVerrechnet) {
				if (dontMatchPrice.contains(vm.code)) {
					vm.vk_preis = null;
					vm.vk_tp = null;
				}
			}
		}
		
		Collection<IBillingMatch> disjunction =
			CollectionUtils.disjunction(existingVerrechnet, matches);
		if (disjunction.size() > 0) {
			throw new AssertionError(disjunction);
		}
	}
	
}
