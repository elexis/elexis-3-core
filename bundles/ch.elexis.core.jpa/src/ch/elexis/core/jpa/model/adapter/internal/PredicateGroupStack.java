package ch.elexis.core.jpa.model.adapter.internal;

import java.util.Stack;

import javax.persistence.criteria.CriteriaBuilder;

import ch.elexis.core.jpa.model.adapter.PredicateGroup;

/**
 * Manage PredicateGroup objects using a {@link Stack}.
 * 
 * @author thomas
 *
 */
public class PredicateGroupStack {
	
	private CriteriaBuilder criteriaBuilder;
	private Stack<PredicateGroup> predicateGroups;
	
	public PredicateGroupStack(CriteriaBuilder criteriaBuilder){
		this.criteriaBuilder = criteriaBuilder;
		this.predicateGroups = new Stack<>();
	}
	
	/**
	 * Get the current (top) {@link PredicateGroup} from the stack, or create a new
	 * {@link PredicateGroup} and put it on top of the stack.
	 * 
	 * @return
	 */
	public PredicateGroup getCurrentPredicateGroup(){
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
	public PredicateGroup createPredicateGroup(){
		PredicateGroup ret = new PredicateGroup(criteriaBuilder);
		predicateGroups.push(ret);
		return ret;
	}
	
	/**
	 * Join the 2 last created {@link PredicateGroup}s with and, and return the resulting
	 * {@link PredicateGroup}. It is also the new top of the stack.
	 * 
	 * @return
	 */
	public PredicateGroup andPredicateGroups(){
		if (predicateGroups.size() > 1) {
			PredicateGroup top = predicateGroups.pop();
			PredicateGroup join = predicateGroups.pop();
			return predicateGroups.push(new PredicateGroup(criteriaBuilder,
				criteriaBuilder.and(join.getPredicate(), top.getPredicate())));
		} else {
			throw new IllegalStateException("At least 2 groups required for and operation");
		}
	}
	
	/**
	 * Join the 2 last created {@link PredicateGroup}s with or, and return the resulting
	 * {@link PredicateGroup}. It is also the new top of the stack.
	 * 
	 * @return
	 */
	public PredicateGroup orPredicateGroups(){
		if (predicateGroups.size() > 1) {
			PredicateGroup top = predicateGroups.pop();
			PredicateGroup join = predicateGroups.pop();
			return predicateGroups.push(new PredicateGroup(criteriaBuilder,
				criteriaBuilder.or(join.getPredicate(), top.getPredicate())));
		} else {
			throw new IllegalStateException("At least 2 groups required for or operation");
		}
	}
	
	/**
	 * Get the current size of the {@link PredicateGroup} stack.
	 * 
	 * @return
	 */
	public int getPredicateGroupsSize(){
		return predicateGroups.size();
	}
}
