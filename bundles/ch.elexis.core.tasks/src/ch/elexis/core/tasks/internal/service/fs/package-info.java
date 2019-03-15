/**
 * Implementations for FS change watchers.
 * 
 * WatchServiceHolder is the java internal way of watching a directory. It seems however not to
 * detect changes on mounted shares (e.g. smb shares)
 * 
 * @see https://stackoverflow.com/questions/26577788/monitor-remote-shared-folder-windows-smb-using-watchservice
 *
 *      it also behaves really slow on OS X
 *
 */
package ch.elexis.core.tasks.internal.service.fs;