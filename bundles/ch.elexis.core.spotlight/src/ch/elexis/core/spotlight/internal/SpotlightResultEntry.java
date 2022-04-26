package ch.elexis.core.spotlight.internal;

import java.util.Optional;

import ch.elexis.core.spotlight.ISpotlightResultEntry;

public class SpotlightResultEntry implements ISpotlightResultEntry {

	final Category category;
	final String label;
	final String loaderString;
	Object object;

	public SpotlightResultEntry(Category category, String label, String loaderString) {
		this(category, label, loaderString, null);
	}

	public SpotlightResultEntry(Category category, String label, String loaderString, Object object) {
		this.category = category;
		this.label = label;
		this.loaderString = loaderString;
		this.object = object;
	}

	@Override
	public Category getCategory() {
		return category;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getLoaderString() {
		return loaderString;
	}

	@Override
	public Optional<Object> getObject() {
		return Optional.ofNullable(object);
	}

	@Override
	public void setObject(Object object) {
		this.object = object;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((category == null) ? 0 : category.hashCode());
		result = prime * result + ((loaderString == null) ? 0 : loaderString.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SpotlightResultEntry other = (SpotlightResultEntry) obj;
		if (category != other.category)
			return false;
		if (loaderString == null) {
			if (other.loaderString != null)
				return false;
		} else if (!loaderString.equals(other.loaderString))
			return false;
		return true;
	}

}
