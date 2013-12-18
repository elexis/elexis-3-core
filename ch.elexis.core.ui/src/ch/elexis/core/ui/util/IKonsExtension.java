/*******************************************************************************
 * Copyright (c) 2005-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.util;

import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.StyleRange;

import ch.elexis.core.ui.text.IRichTextDisplay;

/**
 * Erweiterung für Konsultationseinträge. Wird vom Extensionpoint KonsExtensions benötigt Eine
 * KonsExtension kann Textstellen umformatieren, kann Hyperlinks einfügen und kann Einträge für das
 * Popup-Menu der Konsultationsanzeige definieren. Die IKonsExtension wird zunächst beim Analysieren
 * der KonsExtension mit connect() initialisiert. Dann wird sie beim Rendern des Texts im
 * KonsDetailView für jedes von ihr deklarierte xref-tag einmal aufgerufen (doLayout). Sie kann da
 * "true" zurückgeben um anzuzeigen, dass sie auf Mausklicks reagieren will, oder false, wenn es nur
 * um Layout ohne Klickaktivität geht. Falls sie auf doLayout "true" zurückgegeben hat, wird sie
 * immer dann via doXref aufgerufe, wenn der Benutzer den von ihr gesetzten Link anklickt.
 * Schliesslich wird die IKonsExtension immer dann aufgerufen, wenn der Anwender das Kontext- menu
 * des Textfelds anzeigen will (rechte Maustaste). Wenn getAction eine IAction zurückliefert, dann
 * wird diese ins Kontextmenu eingebunden. Wenn getAction null zurückliefert, erfolgt keine
 * Veränderung des Kontxtmenüs. Referenzimplementation: ch.elexis.privatnotizen
 * 
 * @author gerry
 * 
 * @previously_deprecated was deprecated in preparation of a new version - but now no more. If the
 *                        new version should come along again later -> "use IRangeRenderer and
 *                        SSDRange"
 * 
 */
public interface IKonsExtension extends IExecutableExtension {
	
	/**
	 * diese KonsExtension mit einem EnhancedTextField verknüpfen
	 * 
	 * @param tf
	 *            das TextField, an das diese Extension gebunden wird
	 * @return einen Namen, der diese Extension eindeutig identifiziert
	 */
	public String connect(final IRichTextDisplay tf);
	
	/**
	 * Einen Querverweis für die Darstellung layouten
	 * 
	 * @param styleRange
	 *            eine StyleRange zum beliebig bearbeiten. Kann NULL sein.
	 * @param provider
	 *            den Provider-String, den diese IKonsExtension dem Extension-Point angegeben hat
	 * @param id
	 *            die ID, die die IKonsExtension dieser Textstelle zugewiesen hat
	 * @return true wenn der Text auch als Hyperlink funktionieren soll. styleRange.length muss dann
	 *         >0 sein.
	 */
	public boolean doLayout(StyleRange styleRange, final String provider, final String id);
	
	/**
	 * Aktion für einen Querverweis auslösen (wurde angeklickt)
	 * 
	 * @param refProvider
	 *            Provider-String
	 * @param refID
	 *            ID für die angeklickte Textstelle
	 * @return false wenn bei der Aktion ein Fehler auftrat
	 */
	public boolean doXRef(String refProvider, String refID);
	
	/**
	 * Transportable Repräsentation des eingebetteten Inhalts liefern
	 * 
	 * @param refProvider
	 *            Provider-String
	 * @param refID
	 *            ID für das betreffende Object
	 * @return ein MimePart mit dem Objekt, oder null, wenn die Extension keine Tranpsortform hat
	 */
	// public MimePart doRender(String refProvider, String refID);
	
	// public boolean doImport(MimePart object, int pos, String title);
	/**
	 * Actions für diese Extension holen. z.B. für Kontextmenu
	 */
	public IAction[] getActions();
	
	/**
	 * Ein Object wurde eingefügt, z.B. mit drag&drop
	 * 
	 * @param o
	 *            eingefügtes Object
	 */
	public void insert(Object o, int pos);
	
	/**
	 * Anwender hat eine XRef gelöscht -> ggf. damit verbundene Daten müssen jetzt entfernt werden
	 */
	public void removeXRef(String refProvider, String refID);
}
