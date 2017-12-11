package ch.elexis.core.interfaces;

import ch.elexis.core.exceptions.ElexisException;

public interface ITransferable<T> {
	/**
	 * returns a transferable dto object
	 * 
	 * @return
	 */
	public T getDTO();
	
	/**
	 * persists the transferable dto object
	 * 
	 * @param dto
	 * @throws ElexisException
	 */
	public void persistDTO(T dto) throws ElexisException;
}
