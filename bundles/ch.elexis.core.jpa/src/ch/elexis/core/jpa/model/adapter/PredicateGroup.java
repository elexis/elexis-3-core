package ch.elexis.core.jpa.model.adapter;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;

/**
 * Helper class to build groups of predicates that can be joined.
 *
 * @author thomas
 *
 */
public class PredicateGroup {

	private Predicate currentPredicate;
	private CriteriaBuilder criteriaBuilder;

	public PredicateGroup(CriteriaBuilder criteriaBuilder) {
		this(criteriaBuilder, null);
	}

	public PredicateGroup(CriteriaBuilder criteriaBuilder, Predicate predicate) {
		this.criteriaBuilder = criteriaBuilder;
		this.currentPredicate = predicate;
	}

	public void and(Predicate p) {
		if (currentPredicate == null) {
			currentPredicate = p;
		} else {
			currentPredicate = criteriaBuilder.and(currentPredicate, p);
		}
	}

	public void or(Predicate p) {
		if (currentPredicate == null) {
			currentPredicate = p;
		} else {
			currentPredicate = criteriaBuilder.or(currentPredicate, p);
		}
	}

	public Predicate getPredicate() {
		return currentPredicate;
	}
}
