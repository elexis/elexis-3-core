package ch.elexis.core.services.internal.text;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.IRecipe;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.IContext;
import ch.elexis.core.services.IDocumentService.IDirectTemplateReplacement;
import ch.elexis.core.text.ITextPlugin;
import ch.elexis.core.time.TimeUtil;
import ch.rgw.tools.StringTool;

public class RecipeDocumentTemplateReplacement implements IDirectTemplateReplacement {

	private boolean extended;
	private boolean takinglist;

	private String template;

	public RecipeDocumentTemplateReplacement(String template, boolean takinglist, boolean extended) {
		this.extended = extended;
		this.takinglist = takinglist;
		this.template = template;
	}

	@Override
	public boolean replace(ITextPlugin textPlugin, IContext context) {
		IRecipe recipe = (IRecipe) getIdentifiable(context).orElse(null);
		if (recipe != null) {
			List<IPrescription> lines = recipe.getPrescriptions();
			lines = lines.stream().filter(p -> p.getArticle() != null).collect(Collectors.toList());

			String[][] fields = new String[lines.size()][];
			if (extended) {
				int[] wt = new int[] { 5, 45, 10, 10, 15, 15 };
				fields = createExtendedTakingListFields(lines);
				textPlugin.insertTable(template, 0, fields, wt);
			} else {
				int[] wt = new int[] { 10, 70, 20 };
				if (takinglist) {
					fields = createTakingListFields(lines);
				} else {
					fields = createRezeptListFields(lines);
				}
				textPlugin.insertTable(template, 0, fields, wt);
			}
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public Optional<? extends Identifiable> getIdentifiable(IContext context) {
		Optional<IRecipe> ret = context.getTyped(IRecipe.class);
		if (ret.isEmpty()) {
			ret = (Optional<IRecipe>) context.getNamed(template.substring(1, template.length() - 1));
		}
		return ret;
	}

	private String[][] createRezeptListFields(List<IPrescription> lines) {
		String[][] fields = new String[lines.size()][];

		for (int i = 0; i < fields.length; i++) {
			IPrescription p = lines.get(i);
			fields[i] = new String[3];
			fields[i][0] = StringUtils.EMPTY;
			String bem = p.getRemark();
			if (StringTool.isNothing(bem)) {
				fields[i][1] = p.getArticle().getLabel();
			} else {
				fields[i][1] = p.getArticle().getLabel() + "\t\r" + bem; //$NON-NLS-1$
			}
			fields[i][2] = p.getDosageInstruction();

		}
		return fields;
	}

	private String[][] createTakingListFields(List<IPrescription> lines) {
		String[][] fields = new String[lines.size()][];

		for (int i = 0; i < fields.length; i++) {
			IPrescription p = lines.get(i);
			fields[i] = new String[3];
			fields[i][0] = StringUtils.EMPTY;
			String bem = p.getRemark();
			String patInfo = p.getDisposalComment();
			if (StringTool.isNothing(bem)) {
				fields[i][1] = p.getArticle().getLabel();
			} else {
				if (patInfo == null || patInfo.isEmpty()) {
					fields[i][1] = p.getArticle().getLabel() + "\t\r" + bem; //$NON-NLS-1$
				} else {
					fields[i][1] = p.getArticle().getLabel() + "\t\r" + bem + StringUtils.CR + patInfo; //$NON-NLS-1$
				}
			}
			fields[i][2] = p.getDosageInstruction();
		}
		return fields;
	}

	private String[][] createExtendedTakingListFields(List<IPrescription> lines) {
		String[][] fields = new String[lines.size() + 1][];

		fields[0] = new String[6];
		fields[0][0] = StringUtils.EMPTY;
		fields[0][1] = "Medikament";
		fields[0][2] = "Einnahme";
		fields[0][3] = "Von bis und mit";
		fields[0][4] = "Anwendungsinstruktion";
		fields[0][5] = "Anwendungsgrund";

		for (int i = 1; i < fields.length; i++) {
			IPrescription p = lines.get(i - 1);
			fields[i] = new String[6];
			if (p.getEntryType() != null && p.getEntryType() != EntryType.RECIPE
					&& p.getEntryType() != EntryType.UNKNOWN) {
				fields[i][0] = p.getEntryType().name().substring(0, 1);
			} else {
				fields[i][0] = StringUtils.EMPTY;
			}
			fields[i][1] = StringUtils.defaultString(p.getArticle().getLabel());
			fields[i][2] = StringUtils.defaultString(p.getDosageInstruction());
			fields[i][3] = StringUtils.defaultString(TimeUtil.DATE_GER.format(p.getDateFrom()));
			fields[i][4] = StringUtils.defaultString(p.getRemark());
			fields[i][5] = StringUtils.defaultString(p.getDisposalComment());
		}
		return fields;
	}
}
