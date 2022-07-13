/**
 * Copyright (c) 2013 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 */
package ch.elexis.core.types;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>Document Status</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see ch.elexis.core.types.TypesPackage#getDocumentStatus()
 * @model
 * @generated
 */
public enum DocumentStatus implements Enumerator {
	/**
	 * The '<em><b>NEW</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Newly added, no further actions taken until now.
	 * <!-- end-model-doc -->
	 * @see #NEW_VALUE
	 * @generated
	 * @ordered
	 */
	NEW(0, "NEW", "NEW"),

	/**
	 * The '<em><b>PREPROCESSED</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The document was automatically preprocessed, which may or may not happen. For PDF e.g. preprocessing includes performing OCR on scanned pdfs.
	 * <!-- end-model-doc -->
	 * @see #PREPROCESSED_VALUE
	 * @generated
	 * @ordered
	 */
	PREPROCESSED(1, "PREPROCESSED", "PREPROCESSED"), /**
	 * The '<em><b>INDEXED</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The document was indexed, that is it is available for extended search (used e.g. by SOLR)
	 * <!-- end-model-doc -->
	 * @see #INDEXED_VALUE
	 * @generated
	 * @ordered
	 */
	INDEXED(2, "INDEXED", "INDEXED"), /**
	 * The '<em><b>SENT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The document was sent to an external contact.
	 * <!-- end-model-doc -->
	 * @see #SENT_VALUE
	 * @generated
	 * @ordered
	 */
	SENT(4, "SENT", "SENT"), /**
	 * The '<em><b>NOT FOUND OR NO CONTENT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The document could not be loaded or its content length is 0
	 * <!-- end-model-doc -->
	 * @see #NOT_FOUND_OR_NO_CONTENT_VALUE
	 * @generated
	 * @ordered
	 */
	NOT_FOUND_OR_NO_CONTENT(8, "NOT_FOUND_OR_NO_CONTENT", "NOT_FOUND_OR_NO_CONTENT");

	/**
	 * The '<em><b>NEW</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>NEW</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Newly added, no further actions taken until now.
	 * <!-- end-model-doc -->
	 * @see #NEW
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int NEW_VALUE = 0;

	/**
	 * The '<em><b>PREPROCESSED</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The document was automatically preprocessed, which may or may not happen. For PDF e.g. preprocessing includes performing OCR on scanned pdfs.
	 * <!-- end-model-doc -->
	 * @see #PREPROCESSED
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int PREPROCESSED_VALUE = 1;

	/**
	 * The '<em><b>INDEXED</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The document was indexed, that is it is available for extended search (used e.g. by SOLR)
	 * <!-- end-model-doc -->
	 * @see #INDEXED
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int INDEXED_VALUE = 2;

	/**
	 * The '<em><b>SENT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>SENT</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The document was sent to an external contact.
	 * <!-- end-model-doc -->
	 * @see #SENT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int SENT_VALUE = 4;

	/**
	 * The '<em><b>NOT FOUND OR NO CONTENT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The document could not be loaded or its content length is 0
	 * <!-- end-model-doc -->
	 * @see #NOT_FOUND_OR_NO_CONTENT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int NOT_FOUND_OR_NO_CONTENT_VALUE = 8;

	/**
	 * An array of all the '<em><b>Document Status</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final DocumentStatus[] VALUES_ARRAY =
		new DocumentStatus[] {
			NEW,
			PREPROCESSED,
			INDEXED,
			SENT,
			NOT_FOUND_OR_NO_CONTENT,
		};

	/**
	 * A public read-only list of all the '<em><b>Document Status</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<DocumentStatus> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Document Status</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static DocumentStatus get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			DocumentStatus result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Document Status</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static DocumentStatus getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			DocumentStatus result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Document Status</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static DocumentStatus get(int value) {
		switch (value) {
			case NEW_VALUE: return NEW;
			case PREPROCESSED_VALUE: return PREPROCESSED;
			case INDEXED_VALUE: return INDEXED;
			case SENT_VALUE: return SENT;
			case NOT_FOUND_OR_NO_CONTENT_VALUE: return NOT_FOUND_OR_NO_CONTENT;
		}
		return null;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final int value;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final String name;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final String literal;

	/**
	 * Only this class can construct instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private DocumentStatus(int value, String name, String literal) {
		this.value = value;
		this.name = name;
		this.literal = literal;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getValue() {
	  return value;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getName() {
	  return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getLiteral() {
	  return literal;
	}

	/**
	 * Returns the literal value of the enumerator, which is its string representation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		return literal;
	}
	
} //DocumentStatus
