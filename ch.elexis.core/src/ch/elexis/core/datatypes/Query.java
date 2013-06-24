/*******************************************************************************
 * Copyright (c) 2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.datatypes;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A Query is a generic way to retrieve IPersistentObjects. Unlike the Query in Elexis through 2.x,
 * this is not just a wrapper around an SQL expression. Instead, a Query now is a List of
 * Query.Terms, each connected to the previous term with a Connector (AND, OR, AND NOT, OR NOT), and
 * each consisting of a field, a comparison operator and a match expression.
 * 
 * A Query is executed by sending it to a PersistentObjectManager as argument to executeQuery();
 * 
 * @author gerry
 * 
 */
public class Query {
	public enum OP {
		AND, OR, AND_NOT, OR_NOT, EQUAL, NOT_EQUAL, GREATER, LESS, GREATER_OR_EQUAL, LESS_OR_EQUAL,
			LIKE
	}
	
	public static class Term {
		/**
		 * create a term
		 * 
		 * @param connector
		 *            Operator to apply to connect to previous term (AND, AND_NOT, OR, OR_NOT)
		 * @param field
		 *            field to query
		 * @param operator
		 *            Operator to match
		 * @param value
		 *            value to query for
		 */
		public Term(OP connector, String field, OP operator, String value){
			this.connector = connector;
			this.operator = operator;
			this.field = field;
			this.value = value;
		}
		
		public OP getConnector(){
			return connector;
		}
		
		public OP getOperator(){
			return operator;
		}
		
		public String getField(){
			return field;
		}
		
		public String getValue(){
			return value;
		}
		
		OP connector;
		OP operator;
		String field;
		String value;
	}
	
	public Query(Class<?> queryType){
		this.queryType = queryType;
	}
	
	private Class<?> queryType;
	
	public Class<?> getQueryType(){
		return queryType;
	}
	
	private List<Term> terms = new LinkedList<Term>();
	
	public void addTerm(Term term){
		terms.add(term);
	}
	
	public List<Term> getTerms(){
		return Collections.unmodifiableList(terms);
	}
	
	public void clear(){
		terms.clear();
	}
	
}
