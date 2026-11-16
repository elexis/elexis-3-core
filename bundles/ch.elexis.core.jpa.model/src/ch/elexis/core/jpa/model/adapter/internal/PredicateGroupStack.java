package ch.elexis.core.jpa.model.adapter.internal;

import java.util.Iterator;
import java.util.Stack;

import ch.elexis.core.jpa.model.adapter.PredicateGroup;
import jakarta.persistence.criteria.CriteriaBuilder;

/**
 * Manage PredicateGroup objects using a {@link Stack}.
 *
 * @author thomas
 *
 */
public class PredicateGroupStack {

	private CriteriaBuilder criteriaBuilder;
	private Stack<PredicateGroup> predicateGroups;

	public PredicateGroupStack(CriteriaBuilder criteriaBuilder) {
		this.criteriaBuilder = criteriaBuilder;
		this.predicateGroups = new Stack<>();
	}

	/**
	 * Get the current (top) {@link PredicateGroup} from the stack, or create a new
	 * {@link PredicateGroup} and put it on top of the stack.
	 *
	 * @return
	 */
	public PredicateGroup getCurrentPredicateGroup() {
		PredicateGroup ret = null;
		if (predicateGroups.isEmpty()) {
			ret = createPredicateGroup();
		} else {
			return predicateGroups.peek();
		}
		return ret;
	}

	/**
	 * Create a new {@link PredicateGroup} on the stack.
	 *
	 * @return
	 */
	public PredicateGroup createPredicateGroup() {
		PredicateGroup ret = new PredicateGroup(criteriaBuilder);
		predicateGroups.push(ret);
		return ret;
	}

	/**
	 * Join the 2 last created {@link PredicateGroup}s with and, and return the
	 * resulting {@link PredicateGroup}. It is also the new top of the stack.
	 *
	 * @return
	 */
	public PredicateGroup andPredicateGroups() {
		removeInvalidGroups();
		if (predicateGroups.size() > 1) {
			PredicateGroup top = predicateGroups.pop();
			PredicateGroup join = predicateGroups.pop();
			return predicateGroups.push(
					new PredicateGroup(criteriaBuilder, criteriaBuilder.and(join.getPredicate(), top.getPredicate())));
		} else {
			throw new IllegalStateException("At least 2 groups required for and operation"); //$NON-NLS-1$
		}
	}

	/**
	 * Remove any {@link PredicateGroup} without a predicate.
	 *
	 */
	private void removeInvalidGroups() {
		for (Iterator<PredicateGroup> iterator = predicateGroups.iterator(); iterator.hasNext();) {
			PredicateGroup predicateGroup = (PredicateGroup) iterator.next();
			if (predicateGroup.getPredicate() == null) {
				iterator.remove();
			}
		}
	}

	/**
	 * Join the 2 last created {@link PredicateGroup}s with or, and return the
	 * resulting {@link PredicateGroup}. It is also the new top of the stack.
	 *
	 * @return
	 */
	public PredicateGroup orPredicateGroups() {
		removeInvalidGroups();
		if (predicateGroups.size() > 1) {
			PredicateGroup top = predicateGroups.pop();
			PredicateGroup join = predicateGroups.pop();
			return predicateGroups.push(
					new PredicateGroup(criteriaBuilder, criteriaBuilder.or(join.getPredicate(), top.getPredicate())));
		} else {
			throw new IllegalStateException("At least 2 groups required for or operation"); //$NON-NLS-1$
		}
	}

	/**
	 * Get the current size of the {@link PredicateGroup} stack.
	 *
	 * @return
	 */
	public int getPredicateGroupsSize() {
		return predicateGroups.size();
	}
}
