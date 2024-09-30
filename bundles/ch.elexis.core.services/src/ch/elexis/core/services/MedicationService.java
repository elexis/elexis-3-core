package ch.elexis.core.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IArticleDefaultSignature;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.IRecipe;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.builder.IRecipeBuilder;
import ch.elexis.core.model.prescription.Constants;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.model.prescription.Methods;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;

@Component
public class MedicationService implements IMedicationService {

	@Override
	public float getDailyDosageAsFloat(IPrescription prescription) {
		float total = 0f;
		List<Float> res = getDosageAsFloats(prescription);
		for (int j = 0; j < res.size(); j++) {
			total += res.get(j);
		}
		return total;
	}

	@Override
	public List<Float> getDosageAsFloats(IPrescription prescription) {
		ArrayList<Float> list = new ArrayList<>();
		ArrayList<Float> sub_list = new ArrayList<>();
		float num = 0;
		String dosis = prescription.getDosageInstruction();
		if (dosis != null) {
			// Match stuff like '1/2', '7/8', '~1,2'
			// System.out.println(dosis.matches(special_num_at_start));
			if (dosis.matches(special_num_at_start)) {
				list.add(getNum(dosis.replace("~", StringUtils.EMPTY)));
			} else if (dosis.matches("[0-9½¼]+([xX][0-9]+(/[0-9]+)?|)")) { //$NON-NLS-1$
				String[] dose = dosis.split("[xX]"); //$NON-NLS-1$
				float count = getNum(dose[0]);
				if (dose.length > 1)
					num = getNum(dose[1]) * count;
				else
					num = getNum(dose[0]);
				list.add(num);
			} else {
				sub_list = getDosageAsFloats(dosis, "-");
				if (StringUtils.countMatches(dosis, "-") > 1 && !sub_list.isEmpty()) {
					return sub_list;
				}
				sub_list = getDosageAsFloats(dosis, "/");
				if (StringUtils.countMatches(dosis, "/") > 1 && !sub_list.isEmpty()) {
					return sub_list;
				}
				if (dosis.indexOf('-') != -1 || dosis.indexOf('/') != -1) {
					String[] dos = dosis.split("[- /]"); //$NON-NLS-1$
					if (dos.length > 2) {
						for (String d : dos) {
							boolean hasDigit = d.matches("^[~/.]*[½¼0-9].*");
							if (d.indexOf(' ') != -1)
								list.add(getNum(d.substring(0, d.indexOf(' '))));
							else if (d.length() > 0 && hasDigit)
								list.add(getNum(d));
							if (list.size() >= 4)
								return list;
						}
					} else if (dos.length > 1) {
						list.add(getNum(dos[1]));
					} else {
						// nothing to add
					}
				}
			}
		}
		return list;
	}

	private ArrayList<Float> getDosageAsFloats(String dosis, String trennzeichen) {
		ArrayList<Float> list = new ArrayList<>();
		if (dosis.indexOf('-') != -1 || dosis.indexOf('/') != -1) {
			String[] dos = dosis.split(trennzeichen);
			if (dos.length > 2) {
				for (String d : dos) {
					boolean hasDigit = d.matches("^[~/.]*[½¼0-9].*");
					if (d.indexOf(' ') != -1)
						list.add(getNum(d.substring(0, d.indexOf(' '))));
					else if (d.length() > 0 && hasDigit)
						list.add(getNum(d));
					else if (d.length() == 0)
						list.add(0.0f);
					if (list.size() >= 4)
						return list;
				}
			} else if (dos.length > 1) {
				list.add(getNum(dos[1]));
			} else {
				// nothing to add
			}
		}
		return list;
	}

	private static final String special_num_at_start = "^(~|)[0-9]/[0-9][ a-zA-Z]*$";

	private float getNum(String num) {
		try {
			String n = num.trim();
			if (n.matches(special_num_at_start)) {
				float value = getNum(n.substring(0, 1)) / getNum(n.substring(2));
				return value;
			} else if (n.equalsIgnoreCase("½")) {
				return 0.5F;
			} else if (n.equalsIgnoreCase("¼")) {
				return 0.25F;
			} else if (n.equalsIgnoreCase("1½")) {
				return 1.5F;

			} else if (n.indexOf('/') != -1) {
				if (n.length() == 1) {
					return 0.0f;
				}
				String[] bruch = n.split(StringConstants.SLASH);
				if (bruch.length < 2) {
					return 0.0f;
				}
				float zaehler = Float.parseFloat(bruch[0]);
				float nenner = Float.parseFloat(bruch[1]);
				if (nenner == 0.0f) {
					return 0.0f;
				}
				return zaehler / nenner;
			}
			// matching for values like 2x1, 2,5x1 or 20x1
			else if (n.toLowerCase().matches("^[0-9][,.]*[0-9]*x[0-9][,.]*[0-9]*$")) {
				n = n.replace("\\s", StringUtils.EMPTY);
				String[] nums = n.toLowerCase().split("x");
				float num1 = Float.parseFloat(nums[0].replace(",", "."));
				float num2 = Float.parseFloat(nums[1].replace(",", "."));
				return num1 * num2;
			}
			// matching numbers with comma i.e. 1,5 and parses it to 1.5 for float value
			else if (n.matches("^[0-9,]")) {
				n = n.replace(",", ".");
				return Float.parseFloat(n);
			}
			// any other digit-letter combination. replaces comma with dot and removes all
			// non-digit chars. i.e. 1,5 p. Day becomes 1.5
			else {
				n = n.replace(",", ".");
				n = n.replaceAll("[^\\d.]", StringUtils.EMPTY);
				if (n.endsWith(".")) {
					n = n.substring(0, n.length() - 1);
				}
				if (n.isEmpty()) {
					return 0.0f;
				}
				return Float.parseFloat(n);
			}
		} catch (NumberFormatException e) {
			LoggerFactory.getLogger(getClass()).warn("Error getting number for [" + num + "]");
			return 0.0F;
		}
	}

	@Override
	public Optional<IArticleDefaultSignature> getDefaultSignature(IArticle article) {
		IQuery<IArticleDefaultSignature> query = CoreModelServiceHolder.get().getQuery(IArticleDefaultSignature.class);
		query.and("article", COMPARATOR.LIKE, "%" + StoreToStringServiceHolder.getStoreToString(article));
		Optional<IArticleDefaultSignature> ret = query.executeSingleResult();
		if (!ret.isPresent()) {
			ret = getDefaultSignature(article.getAtcCode());
		}
		return ret;
	}

	@Override
	public Optional<IArticleDefaultSignature> getDefaultSignature(String atcCode) {
		if (StringUtils.isNotBlank(atcCode)) {
			IQuery<IArticleDefaultSignature> query = CoreModelServiceHolder.get()
					.getQuery(IArticleDefaultSignature.class);
			query.and("atccode", COMPARATOR.LIKE, atcCode);
			return query.executeSingleResult();
		}
		return Optional.empty();
	}

	@Override
	public IArticleDefaultSignature getTransientDefaultSignature(IArticle article) {
		IArticleDefaultSignature ret = CoreModelServiceHolder.get().create(IArticleDefaultSignature.class);
		ret.setArticle(article);
		return ret;
	}

	@Override
	public IPrescription createPrescriptionCopy(IPrescription prescription) {
		IPrescription ret = CoreModelServiceHolder.get().create(IPrescription.class);
		ret.setArticle(prescription.getArticle());
		ret.setPatient(prescription.getPatient());
		ret.setDosageInstruction(prescription.getDosageInstruction());
		ret.setDisposalComment(prescription.getDisposalComment());
		ret.setEntryType(prescription.getEntryType());
		ret.setRemark(prescription.getRemark());
		String extInfoValue = (String) prescription
				.getExtInfo(ch.elexis.core.model.prescription.Constants.FLD_EXT_VERRECHNET_ID);
		if (extInfoValue != null && !extInfoValue.isEmpty()) {
			ret.setExtInfo(ch.elexis.core.model.prescription.Constants.FLD_EXT_VERRECHNET_ID, extInfoValue);
		}
		ret.setDateFrom(LocalDateTime.now());
		ret.setPrescriptor(ContextServiceHolder.get().getActiveUserContact().orElse(null));
		return ret;
	}

	@Override
	public void stopPrescription(IPrescription prescription, LocalDateTime stopDateTime, String stopReason) {
		if (stopDateTime == null) {
			stopDateTime = LocalDateTime.now();
		}
		prescription.setDateTo(stopDateTime);
		if (ContextServiceHolder.get().getActiveUserContact().isPresent()) {
			prescription.setExtInfo(Constants.FLD_EXT_STOPPED_BY,
					ContextServiceHolder.get().getActiveUserContact().get().getId());
		}
		if (stopReason != null) {
			prescription.setStopReason(stopReason);
		}
	}

	@Override
	public IRecipe createRecipe(IPatient patient, List<IPrescription> prescRecipes) {
		LocalDateTime now = LocalDateTime.now();
		List<Identifiable> entries = new ArrayList<>();
		IRecipe ret = new IRecipeBuilder(CoreModelServiceHolder.get(), patient,
				ContextServiceHolder.get().getActiveMandator().orElse(null)).build();
		for (int i = 0; i < prescRecipes.size(); i++) {
			IPrescription iPrescription = prescRecipes.get(i);
			IPrescription copy = createPrescriptionCopy(iPrescription);
			copy.setEntryType(EntryType.RECIPE);
			copy.setDateTo(now);
			copy.setRecipe(ret);
			copy.setExtInfo(Constants.FLD_EXT_RECIPE_ORDER, Integer.toString(i));
			entries.add(copy);
		}
		CoreModelServiceHolder.get().save(ret);
		CoreModelServiceHolder.get().save(entries);
		return ret;
	}

	private int getNextRecipeOrder(IRecipe recipe) {
		List<IPrescription> prescriptions = recipe.getPrescriptions();
		if (prescriptions != null && !prescriptions.isEmpty()) {
			return prescriptions.stream().mapToInt(p -> getRecipeOrder(p)).max().orElse(0) + 1;
		}
		return 0;
	}

	private int getRecipeOrder(IPrescription prescription) {
		String orderString = (String) prescription.getExtInfo(Constants.FLD_EXT_RECIPE_ORDER);
		if (orderString != null && !orderString.isEmpty()) {
			try {
				return Integer.valueOf(orderString);
			} catch (NumberFormatException ne) {
				// ignore ...
			}
		}
		return 0;
	}

	@Override
	public String[] getSignatureAsStringArray(String signature) {
		return Methods.getSignatureAsStringArray(signature);
	}
}
