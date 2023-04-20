package ch.elexis.core.webdav;

@FunctionalInterface
public interface WebdavFileNameFilter {

	public boolean accept(String webdavFileName);

}
