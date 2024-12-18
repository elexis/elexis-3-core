package ch.elexis.core.jpa.entities.gson;

import java.io.IOException;

import jakarta.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import ch.elexis.core.jpa.entities.AbstractEntityWithId;
import ch.elexis.core.jpa.entities.entitymanager.ElexisEntityManagerServiceHolder;

/**
 * JSON De-/Serialize an {@link AbstractEntityWithId} with its ID only. Used by
 * Gson. Applied as annotation to a field in a JPA entity.
 */
public class AbstractEntityWithIdJsonAdapter extends TypeAdapter<AbstractEntityWithId> {

	private static final String ENTITY_TYPE = "entityType";
	private static final String ID = "id";

	@Override
	public void write(JsonWriter out, AbstractEntityWithId value) throws IOException {
		if (value != null) {
			out.beginObject();
			out.name(ENTITY_TYPE).value(value.getClass().getName());
			out.name(ID).value(value.getId());
			out.endObject();
		}
	}

	@Override
	public AbstractEntityWithId read(JsonReader in) throws IOException {
		String entityType = null;
		String id = null;

		in.beginObject();
		while (in.hasNext()) {
			switch (in.nextName()) {
			case ENTITY_TYPE:
				entityType = in.nextString();
				break;
			case ID:
				id = in.nextString();
				break;
			default:
				break;
			}
		}
		in.endObject();

		return tryToLoad(entityType, id);
	}

	/**
	 * Tries to load the requested entity.
	 *
	 * @param entityType
	 * @param id
	 * @return
	 * @throws IOException if id can not be loaded or class for entityType can not
	 *                     be found
	 */
	private AbstractEntityWithId tryToLoad(String entityType, String id) throws IOException {
		if (StringUtils.isNotBlank(entityType) && StringUtils.isNotBlank(id)) {

			try {
				Class<?> clazz = getClass().getClassLoader().loadClass(entityType);

				EntityManager em = (EntityManager) ElexisEntityManagerServiceHolder.getEntityManager()
						.getEntityManager(true);
				try {
					AbstractEntityWithId entity = (AbstractEntityWithId) em.find(clazz, id);
					if (entity == null) {
						throw new IOException("entityType [" + entityType + "] with id [" + id + "] not found");
					}
					return entity;
				} finally {
					ElexisEntityManagerServiceHolder.getEntityManager().closeEntityManager(em);
				}
			} catch (ClassNotFoundException e) {
				throw new IOException(e);
			}
		}

		return null;
	}

}
