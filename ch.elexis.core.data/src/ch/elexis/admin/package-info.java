/**
 * <b>This package does not follow the default naming structure due to the following reason:</b><br><br>
 * 
 * Elexis uses the class names as a foreign key to resolve cross object relations within its database. The renaming
 * led to several problems occuring in this resolving process, and were discussed in great detail in the elexis
 * developer mailing list. For further details, please see the respective <a href="http://sourceforge.net/p/elexis/mailman/message/31669360/">thread</a>.
 * <br>
 * <br>
 * <b>In order to realize compatibility with previous Elexis releases (2.1.x) classes that are referenced within existing databases have to keep their original naming structure.</b>
* <hr>
* <h1>Klassen, die Steuerungs- und Kontrollaufgaben &uuml;bernehmen</h1>
* <p>Diese Package ist eine Ebene zwischen den Datenobjekten der untersten und der
* Benutzeroberfl&auml;che der obersten Ebene. Hier erfolgt Zugriffskontrolle und
* Eingabeverifikation
 */
package ch.elexis.admin;