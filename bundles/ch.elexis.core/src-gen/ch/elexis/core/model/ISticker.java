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

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>ISticker</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * A sticker is a piece of information that may be attached to an Identifiable. It can either exist for itself, or as an attachedTo (bound to an identifiable) instance.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.ISticker#getBackground <em>Background</em>}</li>
 *   <li>{@link ch.elexis.core.model.ISticker#getForeground <em>Foreground</em>}</li>
 *   <li>{@link ch.elexis.core.model.ISticker#isVisible <em>Visible</em>}</li>
 *   <li>{@link ch.elexis.core.model.ISticker#getName <em>Name</em>}</li>
 *   <li>{@link ch.elexis.core.model.ISticker#getImportance <em>Importance</em>}</li>
 *   <li>{@link ch.elexis.core.model.ISticker#getImage <em>Image</em>}</li>
 *   <li>{@link ch.elexis.core.model.ISticker#getAttachedTo <em>Attached To</em>}</li>
 *   <li>{@link ch.elexis.core.model.ISticker#getAttachedToData <em>Attached To Data</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getISticker()
 * @model interface="true" abstract="true" superTypes="ch.elexis.core.types.Comparable&lt;ch.elexis.core.model.ISticker&gt; ch.elexis.core.model.Deleteable ch.elexis.core.model.Identifiable ch.elexis.core.model.WithAssignableId"
 * @generated
 */
public interface ISticker extends Comparable<ISticker>, Deleteable, Identifiable, WithAssignableId {
	/**
	 * Returns the value of the '<em><b>Background</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Background</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Background</em>' attribute.
	 * @see #setBackground(String)
	 * @see ch.elexis.core.model.ModelPackage#getISticker_Background()
	 * @model
	 * @generated
	 */
	String getBackground();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ISticker#getBackground <em>Background</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Background</em>' attribute.
	 * @see #getBackground()
	 * @generated
	 */
	void setBackground(String value);

	/**
	 * Returns the value of the '<em><b>Foreground</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Foreground</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Foreground</em>' attribute.
	 * @see #setForeground(String)
	 * @see ch.elexis.core.model.ModelPackage#getISticker_Foreground()
	 * @model
	 * @generated
	 */
	String getForeground();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ISticker#getForeground <em>Foreground</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Foreground</em>' attribute.
	 * @see #getForeground()
	 * @generated
	 */
	void setForeground(String value);

	/**
	 * Returns the value of the '<em><b>Visible</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Visible</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Visible</em>' attribute.
	 * @see #setVisible(boolean)
	 * @see ch.elexis.core.model.ModelPackage#getISticker_Visible()
	 * @model
	 * @generated
	 */
	boolean isVisible();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ISticker#isVisible <em>Visible</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Visible</em>' attribute.
	 * @see #isVisible()
	 * @generated
	 */
	void setVisible(boolean value);

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see ch.elexis.core.model.ModelPackage#getISticker_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ISticker#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Importance</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Importance</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Importance</em>' attribute.
	 * @see #setImportance(int)
	 * @see ch.elexis.core.model.ModelPackage#getISticker_Importance()
	 * @model
	 * @generated
	 */
	int getImportance();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ISticker#getImportance <em>Importance</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Importance</em>' attribute.
	 * @see #getImportance()
	 * @generated
	 */
	void setImportance(int value);

	/**
	 * Returns the value of the '<em><b>Image</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Image</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Image</em>' reference.
	 * @see #setImage(IImage)
	 * @see ch.elexis.core.model.ModelPackage#getISticker_Image()
	 * @model
	 * @generated
	 */
	IImage getImage();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ISticker#getImage <em>Image</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Image</em>' reference.
	 * @see #getImage()
	 * @generated
	 */
	void setImage(IImage value);

	/**
	 * Returns the value of the '<em><b>Attached To</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * If not null this is an attached instance of a sticker. 
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Attached To</em>' reference.
	 * @see #setAttachedTo(Identifiable)
	 * @see ch.elexis.core.model.ModelPackage#getISticker_AttachedTo()
	 * @model transient="true"
	 * @generated
	 */
	Identifiable getAttachedTo();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ISticker#getAttachedTo <em>Attached To</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Attached To</em>' reference.
	 * @see #getAttachedTo()
	 * @generated
	 */
	void setAttachedTo(Identifiable value);

	/**
	 * Returns the value of the '<em><b>Attached To Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * optionally attached data on an attached sticker instance
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Attached To Data</em>' attribute.
	 * @see #setAttachedToData(String)
	 * @see ch.elexis.core.model.ModelPackage#getISticker_AttachedToData()
	 * @model transient="true"
	 * @generated
	 */
	String getAttachedToData();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.ISticker#getAttachedToData <em>Attached To Data</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Attached To Data</em>' attribute.
	 * @see #getAttachedToData()
	 * @generated
	 */
	void setAttachedToData(String value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	String getLabel();

} // ISticker
