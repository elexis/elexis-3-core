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
 */
package ch.elexis.data;