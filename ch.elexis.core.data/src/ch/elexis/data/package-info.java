/**
 * <b>This package does not follow the default naming structure due to the following reason:</b><br><br>
 * 
 * Elexis uses the class names as a foreign key to resolve cross object relations within its database. The renaming
 * led to several problems occuring in this resolving process, and were discussed in great detail in the elexis
 * developer mailing list. For further details, please see the respective <a href="http://sourceforge.net/p/elexis/mailman/message/31669360/">thread</a>.
 * <br>
 * <br>
 * <b>In order to realize compatibility with previous Elexis releases (2.1.x) classes extending {@link ch.elexis.data.PersistentObject} have to keep their original package naming.</b>
 * <hr>
 * 
 * <p>Dieses Package enth&auml;lt diejenigen Klassen, die direkten Kontakt mit der Datenbank haben.</p>
 * <p>Es ist wesentlich, dass von anderen Teilen des Programms aus nur &uuml;ber die hier definierten Klassen mit der Datenbank kommuniziert wird. Nur so kann die Konsistenz der Daten gew&auml;hrleistet werden und kann sichergestellt werden, dass
 *		bei einem Wechsel der Datenbank-Engine keine un&uuml;berschaubaren Programm&auml;nderungen
 *		notwendig werden.</p>
 *		<h2>Prinzip</h2>
 *		<p>Basisklasse ist {@link ch.elexis.data.PersistentObject}. Diese ist nicht zur direkten Verwendung gedacht.
 *		Alle anderen Klassen sind von PersistentObject abgeleitet. Jede Klasse steht f&uuml;r ein bestimmtes
 *		Datenobjekt und entscheidet selbst, welche Tabellen und Felder zur Speicherung ihrer einzelnen
 *		Daten verwendet wird.</p>
 *		<p>Jede Datenklasse hat einen Konstruktor mit Parametern, um ein neues, bisher noch nicht in der
 *		Datenbank befindliches Objekt dieser Klasse zu erzeugen, und eine statische Funktion load(String id),
 *		mit der ein Objekt aus der Datenbank rekonstruiert werden kann.</p>
 *		<p>Die Identifikation eines Objekts geschieht &uuml;ber die automatisch erzeugte eindeutige ID, ein String
 *		mit garantiert maximal 25 Zeichen, der garantiert innerhalb des Netzwerks nur ein einziges Mal vorkommt.	
 *		Die ID eines DatenObjekts kann mit getID() erfragt werden.</p>
 *		<h2>Abfragen</h2>
 *		<p>Abfragen erfolgen &uuml;ber die Klasse Query. Eine Query-Abfrage liefert eine Liste mit allen zur Bedingung
 *		passenden Objekten zur&uuml;ck. F&uuml;r eine Abfrage m&uuml;ssen folgende Schritte vorgenommen werden:</p>
 *		<ol>
 * 			<li>Erstellung eines Query-Objekts: <code>Query qbe=new Query(Person.class)</code>. Parameter der Funktion
 *
 *			    ist die Klasse, deren Objekte gesucht werden sollen.</li>
 *			<li>Eingeben der Bedingung: <code>qbe.add("Feld","Op","Wert")</code>. Wobei "Feld" der Name des Felds ist,
 *			"Op" der Suchoperator (z.B. = oder LIKE) und "Wert" die Filterbedingung. Zu beachten: Bei "Like" sollte die
 *			Filterbedingung mit % eingeschlossen sein, um auch unvollst&auml;ndige W&ouml;rter zu finden.</li>
 *			<li>(Eventuell) Eingeben einer Verkn&uuml;pfung zu einer weiteren Bedingung: <code>qbe.and()</code> oder
 *				 <code>qbe.or()</code></li>
 *			<li>(Eventuell) Eingeben einer weiteren Bedingung mit qbe.add() wie oben beschrieben. Diese Schritte
 *				k&ouml;nnen beliebig oft wiederholt werden.</li>
 *			<li>Ausf&uuml;hren der Abfrage: <code>List&lt;PersistentObject&gt; list=qbe.execute()</code></li>
 *		</ol>
 */
package ch.elexis.data;