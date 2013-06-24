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

import java.util.List;
import java.util.Map;

import ch.elexis.core.exceptions.PersistenceException;


/**
 * An IPersistentObject is an abstract representation of an Object with a number of features:
 * <ul>
 * <li>it persists itself</li>
 * <li>it has an unlimited number of randomly named properties, that can be accessed with
 * set(String,String) and get(String) methods</li>
 * <li>it has a globally unique identifier</li>
 * </ul>
 * 
 * @author gerry
 * 
 */
public interface IPersistentObject extends ISelectable {
	/** predefined field name for the GUID */
	public static final String FLD_ID = "id";
	/** predefined property to handle a field that is a compressed HashMap */
	public static final String FLD_EXTINFO = "ExtInfo";
	/** predefined property to hande a field that marks the Object as deleted */
	public static final String FLD_DELETED = "deleted";
	/**
	 * predefined property that holds an automatically updated field containing the last update of
	 * this object as long value (milliseconds as in Date())
	 */
	public static final String FLD_LASTUPDATE = "lastupdate";
	/**
	 * predefined property that holds the date of creation of this object in the form YYYYMMDD
	 */
	public static final String FLD_DATE = "Datum";
	
	/**
	 * return a human readable identifier (not necessarily unique) for this Object
	 */
	abstract public String getLabel();
	
	/**
	 * Tell whether this Object is valid (measured by its own implementation dependent means)
	 * 
	 * @return true if this Object is valid (which is not the same as "correct")
	 */
	public abstract boolean isValid();
	
	/**
	 * Return an identifier for this object that is guaranteed to be globally unique.
	 * 
	 * @note this is a shortcut for get(FLD_ID)
	 * 
	 * @return the ID.
	 */
	public abstract String getId();
	
	/**
	 * Serialize this object into a String. The String must onbly be valid within the same database
	 * i.e. it is not guaranteed, that the creation of an object with this String on a different
	 * installation will yield the same object (ar a valid object at all)
	 */
	public abstract String storeToString();
	
	/** An object with this ID does not exist */
	public static final int STATE_INEXISTENT = 0;
	/** This id is not valid */
	public static final int STATE_INVALID_ID = 1;
	/** An object with this ID exists but is marked deleted */
	public static final int DELETED = 2;
	/** This is an existing object */
	public static final int STATE_EXISTING = 3;
	
	/**
	 * Check the state of an object with this ID Note: This method accesses the database and
	 * therefore is much more costly than the simple instantaniation of a PersistentObject
	 * 
	 * @return a value between INEXISTENT and EXISTING
	 */
	
	public abstract int state();
	
	/**
	 * Check whether an object exists (i.e. lives in the database and is NOT marked as deleted)
	 * 
	 * @return false if it does not exist at all, OR if it is marked as deleted
	 */
	
	public abstract boolean exists();
	
	/**
	 * Check wether the object exists in the database. This is the case for all objects in the
	 * database for which state() returns neither INVALID_ID nor INEXISTENT. Note: objects marked as
	 * deleted will also return true!
	 * 
	 * @return true, if the object is available in the database, false otherwise. The object might
	 *         be marked as deleted. Therfore, exists() and isValid() could return false while
	 *         isAvailable() returns true.
	 */
	public abstract boolean isAvailable();
	
	/**
	 * Return an domain_id of a XID for a specified domain
	 * 
	 * @param domain
	 * @return an identifier that might be null
	 */
	public abstract String getXid(final String domain);
	
	/**
	 * return the "best" Xid for a given object. This is the Xid with the highest quality. If no Xid
	 * is given for this object, a newly created Xid of local quality will be returned
	 */
	public IXid getXid();
	
	/**
	 * retrieve all Xids of this object
	 * 
	 * @return a List that might be empty but is never null
	 */
	public abstract List<IXid> getXids();
	
	/**
	 * Assign a Xid to this object.
	 * 
	 * @param domain
	 *            the domain whose ID will be assigned
	 * @param domain_id
	 *            the id out of the given domain fot this object
	 * @param updateIfExists
	 *            if true update values if Xid with same domain and domain_id exists. Otherwise the
	 *            method will fail if a collision occurs.
	 * @return true on success, false on failure
	 */
	public abstract boolean addXid(final String domain, final String domain_id,
		final boolean updateIfExists);
	
	/**
	 * Find the "highest" or the only Sticker of this object
	 * 
	 * @return a Sticker or Null of no Sticker was assigned to this object
	 */
	public ISticker getSticker();
	
	/**
	 * Return all Stickers attributed to this object
	 * 
	 * @return A possibly empty list of Stickers but never null
	 */
	public abstract List<ISticker> getStickers();
	
	/**
	 * Remove a Stickerfrom this object
	 * 
	 * @param st
	 *            the Sticker to remove
	 */
	public abstract void removeSticker(ISticker st);
	
	/**
	 * Add a Sticker to this object
	 * 
	 * @param et
	 *            the Sticker to add
	 */
	public abstract void addSticker(ISticker et);
	
	/**
	 * check wether this object is marked as deleted
	 * 
	 * @return true if this object is present in the database and is marked as deleted
	 * @deprecated use state()
	 */
	public abstract boolean isDeleted();
	
	/**
	 * check wether this object may dragged and dropped .
	 * 
	 * @Note if the object returns true, it MUST implement a valid storeToString so that it can be
	 *       recreated from this String
	 * @return true if d&d is ok for this object
	 */
	public abstract boolean isDragOK();
	
	/**
	 * get a named property
	 * 
	 * @param field
	 *            name of the Property
	 * @return the value of the property or null if no such property exists
	 */
	public abstract String get(final String field);
	
	/**
	 * Read a property that contains a Map
	 * 
	 * @param field
	 *            Name of the map
	 * @return a map that might be empty but is never null
	 */
	public abstract Map<?, ?> getMap(final String field);
	
	/**
	 * read a property that is an Integer
	 * 
	 * @param field
	 *            name of the Property
	 * @return The value of the property. If the property does not exist or can not be expressed as
	 *         integer: return 0
	 */
	public abstract int getInt(final String field);
	
	/**
	 * Store a property
	 * 
	 * @param field
	 *            name of the Property to write
	 * @param value
	 *            value of the property. Any preexistent value will be overwritten.
	 * @return true on success
	 */
	public abstract boolean set(final String field, String value);
	
	/**
	 * store a property that is a map.
	 * 
	 * @param field
	 *            Name of the map
	 * @param map
	 *            the map to store. The map will be serialized in an implementation dependent way.
	 * @throws PersistenceException
	 *             on storage failure
	 */
	public abstract void setMap(final String field, final Map<Object, Object> map)
		throws PersistenceException;
	
	/**
	 * Set a value of type int.
	 * 
	 * @param field
	 *            a table field of numeric type
	 * @param value
	 *            the value to be set
	 * @return true on success, false else
	 */
	public abstract boolean setInt(final String field, final int value);
	
	/**
	 * Mehrere Felder auf einmal setzen (Effizienter als einzelnes set)
	 * 
	 * @param fields
	 *            die Feldnamen
	 * @param values
	 *            die Werte
	 * @return false bei Fehler
	 */
	public abstract boolean set(final String[] fields, final String[] values);
	
	/**
	 * Mehrere Felder auf einmal auslesen
	 * 
	 * @param fields
	 *            die Felder
	 * @param values
	 *            String Array für die gelesenen Werte
	 * @return true ok, values wurden gesetzt
	 */
	public abstract boolean get(final String[] fields, final String[] values);
	
	/** Strings must match exactly (but ignore case) */
	public static final int MATCH_EXACT = 0;
	/** String must start with test (ignoring case) */
	public static final int MATCH_START = 1;
	/** String must match as regular expression */
	public static final int MATCH_REGEXP = 2;
	/** String must contain test (ignoring case) */
	public static final int MATCH_CONTAINS = 3;
	/**
	 * Try to find match method.
	 * <ul>
	 * <li>If test starts with % or * use MATCH_CONTAINS</li>
	 * <li>If test is enclosed in / use MATCH_REGEXP</li>
	 * </ul>
	 * 
	 */
	public static final int MATCH_AUTO = 4;
	
	/**
	 * Testet ob zwei Objekte bezüglich definierbarer Felder übereinstimmend sind
	 * 
	 * 
	 * @param other
	 *            anderes Objekt
	 * @param mode
	 *            gleich, LIKE oder Regexp
	 * @param fields
	 *            die interessierenden Felder
	 * @return true wenn this und other vom selben typ sind und alle interessierenden Felder genäss
	 *         mode übereinstimmen.
	 */
	public boolean isMatching(final IPersistentObject other, final int mode, final String... fields);
	
	/**
	 * testet, ob die angegebenen Felder den angegebenen Werten entsprechen.
	 * 
	 * @param fields
	 *            die zu testenden Felde
	 * @param mode
	 *            Testmodus (MATCH_EXACT, MATCH_LIKE oder MATCH_REGEXP)
	 * @param others
	 *            die Vergleichswerte
	 * @return true bei übereinsteimmung
	 */
	public boolean isMatching(final String[] fields, final int mode, final String... others);
	
	/**
	 * Testet ob dieses Objekt den angegebenen Feldern entspricht.
	 * 
	 * @param fields
	 *            HashMap mit name,wert paaren für die Felder
	 * @param mode
	 *            Testmodus (MATCH_EXACT, MATCH_BEGIN, MATCH_REGEXP, MATCH_CONTAIN oder MATCH_AUTO)
	 * @param bSkipInexisting
	 *            don't return false if a fieldname is not found but skip this field instead
	 * @return true wenn dieses Objekt die entsprechenden Felder hat
	 */
	public boolean isMatching(final Map<String, String> fields, final int mode,
		final boolean bSkipInexisting);
	
	/**
	 * return the time of the last update of this object
	 * 
	 * @return the time (as given in System.currentTimeMillis()) of the last write operation on this
	 *         object or 0 if there was no valid lastupdate time
	 */
	public abstract long getLastUpdate();
	
	/**
	 * Add a listener to this object that will be informed, if a given property gets changed. This
	 * is more efficient than attaching an ElexisEventListener. If more Properties of the same
	 * object should be observed, the ElexisEventListener might be more appropriate
	 * 
	 * @param listener
	 *            The Listener to attach
	 * @param propertyToObserve
	 *            the name of the property to observe
	 */
	public void addChangeListener(IChangeListener listener, String propertyToObserve);
	
	/**
	 * Remove a property change listener. If no such Listener was registered, nothing happens. If
	 * the same listener was attached several times for different properties, only the one with the
	 * given property will be removed.
	 * 
	 * @param listener
	 *            the listener to remove
	 * @param propertyObserved
	 *            the property that was observed by this listener.
	 */
	public void removeChangeListener(IChangeListener listener, String propertyObserved);
	
}