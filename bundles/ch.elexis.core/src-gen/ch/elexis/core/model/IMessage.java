/**
 * Copyright (c) 2019 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 */
package ch.elexis.core.model;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Map;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>IMessage</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link ch.elexis.core.model.IMessage#getSender <em>Sender</em>}</li>
 *   <li>{@link ch.elexis.core.model.IMessage#getReceiver <em>Receiver</em>}</li>
 *   <li>{@link ch.elexis.core.model.IMessage#isSenderAcceptsAnswer <em>Sender Accepts Answer</em>}</li>
 *   <li>{@link ch.elexis.core.model.IMessage#getCreateDateTime <em>Create Date Time</em>}</li>
 *   <li>{@link ch.elexis.core.model.IMessage#getMessageText <em>Message Text</em>}</li>
 *   <li>{@link ch.elexis.core.model.IMessage#getMessageCodes <em>Message Codes</em>}</li>
 *   <li>{@link ch.elexis.core.model.IMessage#getMessagePriority <em>Message Priority</em>}</li>
 * </ul>
 *
 * @see ch.elexis.core.model.ModelPackage#getIMessage()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface IMessage extends Identifiable, Deleteable {
	/**
	 * Returns the value of the '<em><b>Sender</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sender</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The sender of the message. Possibly a userid that may be resolved to an IUser or another identifier (e.g. a station or system name).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Sender</em>' attribute.
	 * @see #setSender(String)
	 * @see ch.elexis.core.model.ModelPackage#getIMessage_Sender()
	 * @model required="true"
	 * @generated
	 */
	String getSender();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IMessage#getSender <em>Sender</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sender</em>' attribute.
	 * @see #getSender()
	 * @generated
	 */
	void setSender(String value);

	/**
	 * Returns the value of the '<em><b>Receiver</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Receiver</em>' reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The list of receivers. If empty, send message to everybody.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Receiver</em>' attribute list.
	 * @see ch.elexis.core.model.ModelPackage#getIMessage_Receiver()
	 * @model
	 * @generated
	 */
	List<String> getReceiver();

	/**
	 * Returns the value of the '<em><b>Sender Accepts Answer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * wether the sender of this message will accept or handle an answer, i.e. unidirectional communication.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Sender Accepts Answer</em>' attribute.
	 * @see #setSenderAcceptsAnswer(boolean)
	 * @see ch.elexis.core.model.ModelPackage#getIMessage_SenderAcceptsAnswer()
	 * @model
	 * @generated
	 */
	boolean isSenderAcceptsAnswer();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IMessage#isSenderAcceptsAnswer <em>Sender Accepts Answer</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sender Accepts Answer</em>' attribute.
	 * @see #isSenderAcceptsAnswer()
	 * @generated
	 */
	void setSenderAcceptsAnswer(boolean value);

	/**
	 * Returns the value of the '<em><b>Create Date Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Create Date Time</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Create Date Time</em>' attribute.
	 * @see #setCreateDateTime(LocalDateTime)
	 * @see ch.elexis.core.model.ModelPackage#getIMessage_CreateDateTime()
	 * @model dataType="ch.elexis.core.types.LocalDateTime"
	 * @generated
	 */
	LocalDateTime getCreateDateTime();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IMessage#getCreateDateTime <em>Create Date Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Create Date Time</em>' attribute.
	 * @see #getCreateDateTime()
	 * @generated
	 */
	void setCreateDateTime(LocalDateTime value);

	/**
	 * Returns the value of the '<em><b>Message Text</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * the message text to be read by the receiving user(s)
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Message Text</em>' attribute.
	 * @see #setMessageText(String)
	 * @see ch.elexis.core.model.ModelPackage#getIMessage_MessageText()
	 * @model
	 * @generated
	 */
	String getMessageText();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IMessage#getMessageText <em>Message Text</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Message Text</em>' attribute.
	 * @see #getMessageText()
	 * @generated
	 */
	void setMessageText(String value);

	/**
	 * Returns the value of the '<em><b>Message Codes</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * a message code to allow for extended representation and/or action configuration (e.g. show a specific image, perform a specific task)
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Message Codes</em>' attribute.
	 * @see #setMessageCodes(Map)
	 * @see ch.elexis.core.model.ModelPackage#getIMessage_MessageCodes()
	 * @model transient="true"
	 * @generated
	 */
	Map<String, String> getMessageCodes();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IMessage#getMessageCodes <em>Message Codes</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Message Codes</em>' attribute.
	 * @see #getMessageCodes()
	 * @generated
	 */
	void setMessageCodes(Map<String, String> value);

	/**
	 * Returns the value of the '<em><b>Message Priority</b></em>' attribute.
	 * The default value is <code>"0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Message Priority</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Message Priority</em>' attribute.
	 * @see #setMessagePriority(int)
	 * @see ch.elexis.core.model.ModelPackage#getIMessage_MessagePriority()
	 * @model default="0"
	 * @generated
	 */
	int getMessagePriority();

	/**
	 * Sets the value of the '{@link ch.elexis.core.model.IMessage#getMessagePriority <em>Message Priority</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Message Priority</em>' attribute.
	 * @see #getMessagePriority()
	 * @generated
	 */
	void setMessagePriority(int value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void setSender(IUser user);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void addMessageCode(String key, String value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void addReceiver(String receiver);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void addReceiver(IUser receiver);

} // IMessage
