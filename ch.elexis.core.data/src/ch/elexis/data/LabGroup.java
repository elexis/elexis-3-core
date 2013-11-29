/*******************************************************************************
 * Copyright (c) 2005-2008, D. Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    D. Lutz - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.data;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.exceptions.PersistenceException;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;
import ch.rgw.tools.StringTool;

/**
 * A LabGroup is a named collection of LabItem elements.
 * 
 * @author Daniel Lutz <danlutz@watz.ch>
 * 
 */
public class LabGroup extends PersistentObject implements Comparable<LabGroup> {
	private static final String TABLENAME = "LABGROUPS";
	private static final String GROUP_ITEM_TABLENAME = "LABGROUP_ITEM_JOINT";
	private JdbcLink j = getConnection();
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	static {
		addMapping(TABLENAME, "Name", "Items=JOINT:GroupID:ItemID:" + GROUP_ITEM_TABLENAME);
	}
	
	/**
	 * Der parameterlose Konstruktor wird nur von der Factory gebraucht und sollte nie public sein.
	 */
	protected LabGroup(){
		// empty
	}
	
	protected LabGroup(String id){
		super(id);
	}
	
	/**
	 * Creates an new LabGroup with an initial list of entries. Set items to <code>null</code> toe
	 * create an empty LabGroup
	 * 
	 * @param name
	 *            the name of the LabGroup
	 * @param items
	 *            the initiall list of items (may be null)
	 */
	public LabGroup(String name, List<LabItem> items){
		create(null);
		setName(name);
		addItems(items);
	}
	
	public boolean delete(){
		removeAll();
		return super.delete();
	}
	
	/**
	 * Get the name of this LabGroup;
	 * 
	 * @return the name of the LabGroup
	 */
	public String getName(){
		return checkNull(get("Name"));
	}
	
	/**
	 * Set the name of this LabGroup;
	 * 
	 * @param name
	 *            the name of the LabGroup
	 */
	public void setName(String name){
		set("Name", name);
	}
	
	public static LabGroup load(String id){
		return new LabGroup(id);
	}
	
	public String getLabel(){
		return getName();
	}
	
	/**
	 * Compare the names of two groups
	 */
	public int compareTo(LabGroup arg0){
		LabGroup other = (LabGroup) arg0;
		return getName().compareTo(other.getName());
	}
	
	/**
	 * Returns the list of items of this group
	 * 
	 * @return a list of items
	 */
	public List<LabItem> getItems(){
		ArrayList<LabItem> items = new ArrayList<LabItem>();
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ItemID FROM " + GROUP_ITEM_TABLENAME).append(
			" WHERE GroupID = " + getWrappedId());
		
		Stm stm = j.getStatement();
		ResultSet rs = stm.query(sql.toString());
		try {
			while (rs.next()) {
				String id = rs.getString(1);
				LabItem item = LabItem.load(id);
				if (item.state() == EXISTS) {
					items.add(item);
				}
			}
			rs.close();
		} catch (Exception ex) {
			ElexisStatus status =
				new ElexisStatus(ElexisStatus.ERROR, CoreHub.PLUGIN_ID, ElexisStatus.CODE_NONE,
					"Persistence error: " + ex.getMessage(), ex, ElexisStatus.LOG_ERRORS);
			throw new PersistenceException(status);
		} finally {
			j.releaseStatement(stm);
		}
		return items;
	}
	
	/**
	 * Adds a new item to this group
	 * 
	 * @param item
	 *            the new item to be added
	 */
	public void addItem(LabItem item){
		if (item != null && (item.state() == EXISTS)) {
			// add item if it doesn't yet exists
			String exists =
				j.queryString("SELECT ItemID FROM " + GROUP_ITEM_TABLENAME + " WHERE GroupID = "
					+ getWrappedId() + " AND ItemID = " + item.getWrappedId());
			if (StringTool.isNothing(exists)) {
				StringBuffer sql = new StringBuffer();
				sql.append("INSERT INTO " + GROUP_ITEM_TABLENAME + " (GroupID, ItemID) VALUES (")
					.append(getWrappedId()).append(",").append(item.getWrappedId()).append(")");
				j.exec(sql.toString());
			}
		}
	}
	
	/**
	 * Add a list of items to add to this group. Existing items are preserved.
	 * 
	 * @param items
	 *            the items to be added
	 */
	public void addItems(List<LabItem> items){
		if (items != null) {
			for (LabItem item : items) {
				addItem(item);
			}
		}
	}
	
	/**
	 * Add a new set of items to this group. Existing items are removed.
	 * 
	 * @param items
	 *            the new items to be added
	 */
	public void setItems(List<LabItem> items){
		removeAll();
		addItems(items);
	}
	
	/**
	 * Remove an item from this group
	 * 
	 * @param item
	 *            the item to be removed
	 */
	public void removeItem(LabItem item){
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM " + GROUP_ITEM_TABLENAME + " WHERE GroupID = ")
			.append(getWrappedId()).append(" AND ").append("ItemID = ").append(item.getWrappedId());
		j.exec(sql.toString());
	}
	
	/**
	 * Removes all items of this group
	 */
	public void removeAll(){
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM " + GROUP_ITEM_TABLENAME + " WHERE GroupID = ").append(
			getWrappedId());
		j.exec(sql.toString());
	}
}
