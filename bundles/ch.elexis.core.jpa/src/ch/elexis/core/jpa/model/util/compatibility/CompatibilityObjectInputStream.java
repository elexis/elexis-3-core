package ch.elexis.core.jpa.model.util.compatibility;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class CompatibilityObjectInputStream extends ObjectInputStream {
	
	private CompatibilityClassResolver resolver = new CompatibilityClassResolver();
	
	private boolean compatibility = false;
	
	public CompatibilityObjectInputStream(InputStream in) throws IOException{
		super(in);
	}

	protected java.lang.Class<?> resolveClass(java.io.ObjectStreamClass desc)
		throws IOException, ClassNotFoundException{
		if (resolver != null) {
			Class<?> resolved = resolver.resolveClass(desc);
			if (resolved != null) {
				compatibility = true;
			}
			return (resolved != null) ? resolved : super.resolveClass(desc);
		} else {
			return super.resolveClass(desc);
		}
	}
	
	public boolean usedCompatibility(){
		return compatibility;
	};
}
