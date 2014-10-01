/*******************************************************************************
 * Copyright (c) 2005-2014, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *******************************************************************************/

package ch.elexis.admin;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.data.Anwender;
import ch.elexis.data.NamedBlob;
import ch.rgw.io.InMemorySettings;
import ch.rgw.io.Settings;
import ch.rgw.tools.Log;
import ch.rgw.tools.StringTool;

/**
 * Diese Klasse realisiert das Zugriffskontroll- und Rechteverwaltungskonzept von Elexis.
 * <ul>
 * <li>Es gibt Gruppen und Anwender.</li>
 * <li>Jeder Anwender gehört zu mindestens einer Gruppe.</li>
 * <li>Es existiert von Anfang an eine Gruppe "Alle" und ein Anwender "Jeder"</li>
 * <li>Jedes Recht kann einer oder mehreren Gruppen und/oder einer onder mehreren Anwendern gewährt
 * werden.</li>
 * <li>Ein Anwender erhält alle Rechte, die ihm entweder individuell gewährt wurden, oder die einer
 * der Gruppen gewährt wurden, zu denen er gehört.</li>
 * </ul>
 * Eine Ressource, die ein Zugriffsrecht realisieren will, muss für dieses Recht ein ACE erstellen,
 * Zugriffsrechte können hierarchisch aufgebaut sein. Beispielsweise kann ein Recht foo/bar/baz
 * definiert sein. Wenn keine Regel für baz existiert, dann wird nach einer Regel für bar gesucht
 * und diese Angewandt. Wenn auch die nicht gefunden wird, wird nach einer Regel für foo gesucht.
 * Wenn auch dies fehlschlägt, wird das Recht in jedem Fall verweigert. Das Zugriffsrecht kann dann
 * mit grant(gruppe,recht) oder grant(Anwender,recht) gewährt resp. mit revoke(gruppe,Name) oder
 * revoke(Anwender,name) entzogen werden. Um herauszufinden, ob ein Anwender bw. einer seiner
 * Gruppen das Recht hat, auf eine ressource zuzugreifen, muss man request(anwedner,recht) fragen.
 * Eine Abkürzung ist request(recht). Dies fragt, ob der aktuell eingeloggte Anwender das
 * betreffende Recht hat.
 * 
 * @author Gerry
 * @see ACE
 * 
 */
public class AccessControl {
	public static final String KEY_GROUPS = "Groups";
	
	public static final String DB_UID = "dbUID";
	
	// Set this to true to make any user admin (e.g. to reset admin pwd
	private final static boolean FORCE_ADMIN = false;
	
	public static final String ALL_GROUP = StringConstants.ROLE_ALL;
	public static final String USER_GROUP = StringConstants.ROLE_USERS;
	public static final String ADMIN_GROUP = StringConstants.ROLE_ADMIN;
	public static final String GROUP_FOR_PREFERENCEPAGE = "ch.elexis.preferences.acl"; //$NON-NLS-1$
	
	private static final String BLOBNAME = "AccessControl"; //$NON-NLS-1$
	private static final String ACLNAME = "AccessControlACL"; //$NON-NLS-1$
	private static Hashtable<String, ACE> rights;
	private static Hashtable<String, List<String>> usergroups;
	private static Hashtable<String, ACE> acls;
	private static final Log log = Log.get("AccessControl"); //$NON-NLS-1$
	
	// TODO: Cleanup alte Gruppen/Anwender
	/**
	 * Die Zugriffsrechte aus den globalen Settings laden.
	 */
	@SuppressWarnings("unchecked")
	public void load(){
		NamedBlob rset = NamedBlob.load(BLOBNAME);
		if (rset == null) {
			log.log("Warnung: ACEs nicht gefunden, erstelle neu ", Log.ERRORS); //$NON-NLS-1$
			NamedBlob.createTable();
			rset = NamedBlob.load(BLOBNAME);
		}
		NamedBlob aclset = NamedBlob.load(ACLNAME);
		rights = rset.getHashtable();
		acls = aclset.getHashtable();
		if (rights.isEmpty() || acls.isEmpty()) {
			reset();
		}
		usergroups = new Hashtable<String, List<String>>();
		log.log("loaded AccessControl", Log.INFOS); //$NON-NLS-1$
		for (String k1 : rights.keySet()) {
			log.log(k1, Log.DEBUGMSG);
		}
		log.log("loaded ACLs", Log.INFOS); //$NON-NLS-1$
		for (String k1 : acls.keySet()) {
			log.log(k1, Log.DEBUGMSG);
		}
	}
	
	/**
	 * Zugriffsrechte zurücksichern. Alle Rechte, die seit dem letzten flush geändert wurden, sind
	 * nur temporär bis zum nächsten flush()!
	 */
	public void flush(){
		NamedBlob.load(BLOBNAME).put(rights);
		NamedBlob.load(ACLNAME).put(acls);
	}
	
	/**
	 * Zugriffsrecht für den aktuell angemeldeten Anwender erfragen.
	 * 
	 * @param right
	 *            Das erfragte Recht
	 * @return true, wenn der Anwender (oder eine der Gruppen, zu denen der Anwender gehört) das
	 *         Recht hat.
	 */
	public boolean request(ACE right){
		return (request(CoreHub.actUser, right));
	}
	
	/**
	 * query the access right of a given user (that is {@link Anwender})
	 * 
	 * @param user
	 *            the user {@link Anwender}
	 * @param right
	 *            the right to query for, if <code>null</code> always <code>true</code> is returned
	 * @return <code>true</code> if the user (or one of the groups this user belongs to) has the
	 *         resp. right. Also <code>true</code> if the user belongs to group "Admin". Always
	 *         <code>false</code> if no user is logged in
	 */
	@SuppressWarnings("unchecked")
	public boolean request(Anwender user, ACE rightACE){
		if (FORCE_ADMIN) {
			return true;
		} else {
			if (rightACE == null) {
				return true;
			}
			String right = rightACE.getCanonicalName();
			if (rights == null) {
				return false;
			}
			// Wenn alle dieses Recht haben-> ok
			if (rights.get(Messages.AccessControl_GroupAll + right) != null) { //$NON-NLS-1$
				return true;
			}
			// Wenn gar kein user angegeben ist -> verweigern
			if (user == null) {
				return false;
			}
			// Wenn das Recht für jeden User für sich besteht
			if (rights.get("Self" + right) != null) { //$NON-NLS-1$
				if (CoreHub.actUser.getId().equals(user.getId())) {
					return true;
				}
			}
			// Wenn das Recht für den genannten User individuell besteht
			if (rights.get(user.getId() + right) != null) {
				return true;
			}
			
			// Wenn das Recht für eine Gruppe, zu der der User gehört, besteht
			List<String> list = usergroups.get(user.getId() + "#groups#"); //$NON-NLS-1$
			// we cache the groups during runtime. If not yet in cache, load
			// group
			// membership from user data
			if (list == null) {
				// Anwender act=CoreHub.actUser;
				list = new ArrayList<String>();
				Map h = user.getMap("ExtInfo"); //$NON-NLS-1$
				if (h != null) {
					String grp = (String) h.get(KEY_GROUPS);
					if (grp != null) {
						String[] grps = grp.split(","); //$NON-NLS-1$
						for (String g : grps) {
							list.add(g);
						}
						usergroups.put(user.getId() + "#groups#", list); //$NON-NLS-1$
					}
				}
			}
			// The list is never null here, but might be empty
			for (String g : list) {
				if (ADMIN_GROUP.equals(g)) {
					// If the user is member of the admin groups, he has any
					// right
					return true;
				}
				if (rights.get(g + right) != null) {
					return true;
				}
			}
			// Falls das gewünschte Recht nicht geregelt ist, eine
			// Hierarchiestufe
			// höher suchen
			ACE parent = rightACE.getParent();
			if (parent != null) {
				return request(user, parent);
			}
			return false;
		}
	}
	
	/**
	 * Zugriffsrecht(e) erteilen
	 * 
	 * @param user
	 *            Anwender, der diese Rechte erhalten soll
	 * @param elements
	 *            ein oder mehrere Rechte
	 */
	public void grant(Anwender user, ACE... elements){
		for (ACE right : elements) {
			rights.put(user.getId() + right.getCanonicalName(), right);
			acls.put(right.getCanonicalName(), right);
		}
	}
	
	/**
	 * Zugriffsrechte entziehen
	 * 
	 * @param user
	 *            Anwender, dem diese Rechte entzogen werden sollen
	 * @param elements
	 *            ein oder mehrere Rechte
	 */
	public void revoke(Anwender user, ACE... elements){
		for (ACE right : elements) {
			rights.remove(user.getId() + right.getCanonicalName());
		}
	}
	
	/**
	 * Zugriffsrechte erteilen
	 * 
	 * @param group
	 *            Gruppe, der diese Rechte erteilt werden sollen
	 * @param elements
	 *            ein oder mehrere Rechte
	 */
	public void grant(String group, ACE... elements){
		for (ACE right : elements) {
			rights.put(group + right.getCanonicalName(), right);
			acls.put(right.getCanonicalName(), right);
		}
	}
	
	/**
	 * Zugriffsrechte entziehem
	 * 
	 * @param group
	 *            Gruppe
	 * @param elements
	 *            ein oder mehrere Rechte
	 */
	public void revoke(String group, ACE... elements){
		for (ACE right : elements) {
			rights.remove(group + right.getCanonicalName());
		}
	}
	
	/**
	 * Zugriffsrecht für "self" erteilen
	 * 
	 */
	public void grantForSelf(ACE... elements){
		for (ACE r : elements) {
			rights.put("Self" + r.getCanonicalName(), r); //$NON-NLS-1$
			acls.put(r.getCanonicalName(), r);
		}
	}
	
	public void revokeFromSelf(ACE... strings){
		for (ACE e : strings) {
			rights.remove("Self" + e.getCanonicalName()); //$NON-NLS-1$
		}
	}
	
	/**
	 * Einen Anwender einer Gruppe zufügen
	 * 
	 * @param group
	 *            Die Gruppe, der der Anwender angeschlossen werden soll
	 * @param user
	 *            der Anwender
	 */
	public void addToGroup(String group, Anwender user){
		String g = remove(group, user);
		g = g + "," + group; //$NON-NLS-1$
		user.setInfoElement(KEY_GROUPS, g);
	}
	
	/**
	 * Einen Anwender aus einer Gruppe entfernen
	 * 
	 * @param group
	 *            Gruppe, aus der der Anwender austreten soll
	 * @param user
	 *            der Anwender
	 */
	public void removeFromGroup(String group, Anwender user){
		String g = remove(group, user);
		user.setInfoElement(KEY_GROUPS, g);
	}
	
	private String remove(String group, Anwender user){
		String g = (String) user.getInfoElement(KEY_GROUPS);
		if (g != null) {
			g = g.replaceAll(user.getId(), ""); //$NON-NLS-1$
			g = g.replaceAll("\\s*,*$", ""); //$NON-NLS-1$ //$NON-NLS-2$
			return g;
		}
		return ""; //$NON-NLS-1$
	}
	
	/**
	 * Alle Gruppen holen
	 * 
	 * @return eine Liste aller definierten Gruppen
	 */
	public List<String> getGroups(){
		ArrayList<String> ret = new ArrayList<String>();
		String grp = CoreHub.globalCfg.get(Preferences.ACC_GROUPS, ADMIN_GROUP);
		for (String s : grp.split(",")) { //$NON-NLS-1$
			ret.add(s);
		}
		return ret;
	}
	
	/**
	 * Eine Liste aller Gruppen holen, die in bestimmtes Recht haben
	 * 
	 * @param right
	 *            das zu erfragende Recht
	 * @return Alle Gruppen, deren Mitglieder dieses Recht haben
	 */
	public List<String> groupsForGrant(ACE rightACE){
		ArrayList<String> ret = new ArrayList<String>();
		String right = rightACE.getCanonicalName();
		Pattern p = Pattern.compile("([a-zA-Z0-9]+)" + right); //$NON-NLS-1$
		
		Enumeration<String> e = rights.keys();
		while (e.hasMoreElements()) {
			String k = e.nextElement();
			Matcher m = p.matcher(k);
			if (m.matches()) {
				String grp = m.group(1);
				Anwender an = Anwender.load(grp);
				if (an == null) {
					ret.add(grp);
				}
			}
		}
		return ret;
	}
	
	/**
	 * Alle Anwender efragen, die ein bestimmtes Recht haben
	 * 
	 * @param right
	 *            das zu erfragende Recht
	 * @return eine Liste aller Anwender, die das gesuchte Recht direkt (nicht über
	 *         Gruppenmitgliedschaft) haben.
	 */
	public List<Anwender> usersForGrant(ACE rightACE){
		ArrayList<Anwender> ret = new ArrayList<Anwender>();
		String right = rightACE.getCanonicalName();
		Pattern p = Pattern.compile("([a-zA-Z0-9]+)" + right); //$NON-NLS-1$
		
		Enumeration<String> e = rights.keys();
		while (e.hasMoreElements()) {
			String k = e.nextElement();
			Matcher m = p.matcher(k);
			if (m.matches()) {
				String grp = m.group(1);
				Anwender an = Anwender.load(grp);
				if ((an != null) && an.exists()) {
					ret.add(an);
				}
			}
		}
		return ret;
	}
	
	public void deleteGrant(ACE grantACE){
		String grant = grantACE.getCanonicalName();
		
		Pattern p = Pattern.compile("([a-zA-Z0-9]+)" + grant); //$NON-NLS-1$
		
		Enumeration<String> e = rights.keys();
		while (e.hasMoreElements()) {
			String k = e.nextElement();
			Matcher m = p.matcher(k);
			if (m.matches()) {
				rights.remove(k);
			}
		}
		acls.remove(grantACE);
	}
	
	public Settings asSettings(){
		return new InMemorySettings(rights);
	}
	
	/** Alles auf Standard zurücksetzen und dbUID generieren */
	public void reset(){
		rights.clear();
		grant(ALL_GROUP, AccessControlDefaults.getAlle());
		grant(USER_GROUP, AccessControlDefaults.getAnwender());
		acls.put(DB_UID, new ACE(ACE.ACE_ROOT, DB_UID, StringTool.unique("db%id")));
		flush();
	}
	
	public String getDBUID(boolean bCreate){
		ACE dbuid = acls.get(DB_UID);
		if (bCreate && dbuid == null) {
			dbuid = new ACE(ACE.ACE_ROOT, DB_UID, StringTool.unique("db%id"));
			rights.put(DB_UID, dbuid);
			flush();
		}
		return dbuid.getLocalizedName();
	}
	
}
