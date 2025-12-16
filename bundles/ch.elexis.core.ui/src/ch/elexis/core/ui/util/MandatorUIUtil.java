package ch.elexis.core.ui.util;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;

public final class MandatorUIUtil {

	private MandatorUIUtil() {
		// utility
	}

	public static String buildMandatorLabel(IMandator mandator) {
		if (mandator == null) {
			return StringUtils.EMPTY;
		}
		String name = mandator.getDescription1();
		String vorname = mandator.getDescription2();
		String kuerzel = mandator.getDescription3();
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isNotBlank(name)) {
			sb.append(name);
		}
		if (StringUtils.isNotBlank(vorname)) {
			if (sb.length() > 0) {
				sb.append(StringUtils.SPACE);
			}
			sb.append(vorname);
		}
		if (StringUtils.isNotBlank(kuerzel)) {
			if (sb.length() > 0) {
				sb.append(StringUtils.SPACE);
			}
			sb.append('(').append(kuerzel).append(')');
		}
		if (sb.length() == 0 && mandator instanceof Identifiable) {
			return ((Identifiable) mandator).getLabel();
		}
		return sb.toString();
	}

	public static String getMandatorLabel(Object element) {
		if (element instanceof IMandator mandator) {
			return buildMandatorLabel(mandator);
		}
		return element != null ? element.toString() : StringUtils.EMPTY;
	}

	public static Comparator<Object> createMandatorComparator(Set<String> lockedMandatorIds) {
		return (o1, o2) -> {
			boolean locked1 = isLockedMandator(o1, lockedMandatorIds);
			boolean locked2 = isLockedMandator(o2, lockedMandatorIds);
			if (locked1 != locked2) {
				return locked1 ? 1 : -1;
			}
			String l1 = getMandatorLabel(o1);
			String l2 = getMandatorLabel(o2);
			return StringUtils.compareIgnoreCase(l1, l2);
		};
	}

	private static boolean isLockedMandator(Object o, Set<String> lockedMandatorIds) {
		if (!(o instanceof IMandator) || lockedMandatorIds == null) {
			return false;
		}
		IMandator m = (IMandator) o;
		return lockedMandatorIds.contains(m.getId());
	}

	public static ColumnLabelProvider createMandatorLabelProvider(Set<String> lockedMandatorIds) {
		return new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				return MandatorUIUtil.getMandatorLabel(element);
			}

			@Override
			public Color getForeground(Object element) {
				if (isLockedMandator(element, lockedMandatorIds)) {
					return Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
				}
				return super.getForeground(element);
			}

			@Override
			public String getToolTipText(Object element) {
				if (isLockedMandator(element, lockedMandatorIds)) {
					return Messages.MandatorFilter_lockedMandatorTooltip;

				}
				return null;
			}
		};
	}

	public static final class MandatorSelectionData {
		private final List<IMandator> mandators;
		private final Set<String> lockedMandatorIds;

		public MandatorSelectionData(List<IMandator> mandators, Set<String> lockedMandatorIds) {
			this.mandators = mandators;
			this.lockedMandatorIds = lockedMandatorIds;
		}

		public List<IMandator> getMandators() {
			return mandators;
		}

		public Set<String> getLockedMandatorIds() {
			return lockedMandatorIds;
		}
	}

	public static MandatorSelectionData loadMandatorsAndLockedIds(IModelService modelService) {
		IQuery<IMandator> mandatorQuery = modelService.getQuery(IMandator.class);
		List<IMandator> mandators = mandatorQuery.execute();
		IQuery<IUser> userQuery = modelService.getQuery(IUser.class);
		userQuery.and(ModelPackage.Literals.IUSER__ASSIGNED_CONTACT, COMPARATOR.NOT_EQUALS, null);
		userQuery.and(ModelPackage.Literals.IUSER__ACTIVE, COMPARATOR.EQUALS, Boolean.FALSE);
		List<IUser> inactiveUsers = userQuery.execute();
		Set<String> lockedMandatorIds = inactiveUsers.stream().map(IUser::getAssignedContact)
				.filter(contact -> contact != null).map(contact -> contact.getId()).collect(Collectors.toSet());
		return new MandatorSelectionData(mandators, lockedMandatorIds);
	}
}