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
package ch.elexis.core.model;

import java.util.Date;
import java.util.List;

import ch.elexis.core.types.DocumentStatus;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IDocument</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IDocument#getPatientId <em>Patient Id</em>}</li>
 *   <li>{@link ch.elexis.core.model.IDocument#getAuthorId <em>Author Id</em>}</li>
 *   <li>{@link ch.elexis.core.model.IDocument#getTitle <em>Title</em>}</li>
 *   <li>{@link ch.elexis.core.model.IDocument#getDescription <em>Description</em>}</li>
 *   <li>{@link ch.elexis.core.model.IDocument#getStatus <em>Status</em>}</li>
 *   <li>{@link ch.elexis.core.model.IDocument#getCreated <em>Created</em>}</li>
 *   <li>{@link ch.elexis.core.model.IDocument#getLastchanged <em>Lastchanged</em>}</li>
 *   <li>{@link ch.elexis.core.model.IDocument#getMimeType <em>Mime Type</em>}</li>
 *   <li>{@link ch.elexis.core.model.IDocument#getCategory <em>Category</em>}</li>
 *   <li>{@link ch.elexis.core.model.IDocument#getHistory <em>History</em>}</li>
 *   <li>{@link ch.elexis.core.model.IDocument#getStoreId <em>Store Id</em>}</li>
 *   <li>{@link ch.elexis.core.model.IDocument#getExtension <em>Extension</em>}</li>
 *   <li>{@link ch.elexis.core.model.IDocument#getKeywords <em>Keywords</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIDocument()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IDocument extends Identifiable, Deleteable {
	/**
	 * Returns the value of the '<em><b>Patient Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Patient Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Patient Id</em>' attribute.
	 * @see #setPatientId(String)
	 * @see ch.elexis.core.model.ModelPackage#getIDocument_PatientId()
	 * @model
	 * @generated
	 */
	String getPatientId();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IDocument#getPatientId <em>Patient Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Patient Id</em>' attribute.
	 * @see #getPatientId()
	 * @generated
	 */
	void setPatientId(String value);

	/**
	 * Returns the value of the '<em><b>Author Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Author Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Author Id</em>' attribute.
	 * @see #setAuthorId(String)
	 * @see ch.elexis.core.model.ModelPackage#getIDocument_AuthorId()
	 * @model
	 * @generated
	 */
	String getAuthorId();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IDocument#getAuthorId <em>Author Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Author Id</em>' attribute.
	 * @see #getAuthorId()
	 * @generated
	 */
	void setAuthorId(String value);

	/**
	 * Returns the value of the '<em><b>Title</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Title</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Title</em>' attribute.
	 * @see #setTitle(String)
	 * @see ch.elexis.core.model.ModelPackage#getIDocument_Title()
	 * @model
	 * @generated
	 */
	String getTitle();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IDocument#getTitle <em>Title</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Title</em>' attribute.
	 * @see #getTitle()
	 * @generated
	 */
	void setTitle(String value);

	/**
	 * Returns the value of the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Description</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @see ch.elexis.core.model.ModelPackage#getIDocument_Description()
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IDocument#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Status</b></em>' attribute.
	 * The literals are from the enumeration {@link ch.elexis.core.types.DocumentStatus}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Status</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Status</em>' attribute.
	 * @see ch.elexis.core.types.DocumentStatus
	 * @see #setStatus(DocumentStatus)
	 * @see ch.elexis.core.model.ModelPackage#getIDocument_Status()
	 * @model
	 * @generated
	 */
	DocumentStatus getStatus();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IDocument#getStatus <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Status</em>' attribute.
	 * @see ch.elexis.core.types.DocumentStatus
	 * @see #getStatus()
	 * @generated
	 */
	void setStatus(DocumentStatus value);

	/**
	 * Returns the value of the '<em><b>Created</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Created</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Created</em>' attribute.
	 * @see #setCreated(Date)
	 * @see ch.elexis.core.model.ModelPackage#getIDocument_Created()
	 * @model
	 * @generated
	 */
	Date getCreated();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IDocument#getCreated <em>Created</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Created</em>' attribute.
	 * @see #getCreated()
	 * @generated
	 */
	void setCreated(Date value);

	/**
	 * Returns the value of the '<em><b>Lastchanged</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Lastchanged</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Lastchanged</em>' attribute.
	 * @see #setLastchanged(Date)
	 * @see ch.elexis.core.model.ModelPackage#getIDocument_Lastchanged()
	 * @model
	 * @generated
	 */
	Date getLastchanged();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IDocument#getLastchanged <em>Lastchanged</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Lastchanged</em>' attribute.
	 * @see #getLastchanged()
	 * @generated
	 */
	void setLastchanged(Date value);

	/**
	 * Returns the value of the '<em><b>Mime Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Mime Type</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Mime Type</em>' attribute.
	 * @see #setMimeType(String)
	 * @see ch.elexis.core.model.ModelPackage#getIDocument_MimeType()
	 * @model
	 * @generated
	 */
	String getMimeType();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IDocument#getMimeType <em>Mime Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Mime Type</em>' attribute.
	 * @see #getMimeType()
	 * @generated
	 */
	void setMimeType(String value);

	/**
	 * Returns the value of the '<em><b>Category</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Category</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Category</em>' reference.
	 * @see #setCategory(ICategory)
	 * @see ch.elexis.core.model.ModelPackage#getIDocument_Category()
	 * @model required="true"
	 * @generated
	 */
	ICategory getCategory();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IDocument#getCategory <em>Category</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Category</em>' reference.
	 * @see #getCategory()
	 * @generated
	 */
	void setCategory(ICategory value);

	/**
	 * Returns the value of the '<em><b>History</b></em>' reference list.
	 * The list contents are of type {@link ch.elexis.core.model.IHistory}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>History</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>History</em>' reference list.
	 * @see ch.elexis.core.model.ModelPackage#getIDocument_History()
	 * @model
	 * @generated
	 */
	List<IHistory> getHistory();

	/**
	 * Returns the value of the '<em><b>Store Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Store Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Store Id</em>' attribute.
	 * @see #setStoreId(String)
	 * @see ch.elexis.core.model.ModelPackage#getIDocument_StoreId()
	 * @model
	 * @generated
	 */
	String getStoreId();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IDocument#getStoreId <em>Store Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Store Id</em>' attribute.
	 * @see #getStoreId()
	 * @generated
	 */
	void setStoreId(String value);

	/**
	 * Returns the value of the '<em><b>Extension</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Extension</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Extension</em>' attribute.
	 * @see #setExtension(String)
	 * @see ch.elexis.core.model.ModelPackage#getIDocument_Extension()
	 * @model
	 * @generated
	 */
	String getExtension();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IDocument#getExtension <em>Extension</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Extension</em>' attribute.
	 * @see #getExtension()
	 * @generated
	 */
	void setExtension(String value);

	/**
	 * Returns the value of the '<em><b>Keywords</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Keywords</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Keywords</em>' attribute.
	 * @see #setKeywords(String)
	 * @see ch.elexis.core.model.ModelPackage#getIDocument_Keywords()
	 * @model
	 * @generated
	 */
	String getKeywords();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IDocument#getKeywords <em>Keywords</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Keywords</em>' attribute.
	 * @see #getKeywords()
	 * @generated
	 */
	void setKeywords(String value);

} // IDocument
