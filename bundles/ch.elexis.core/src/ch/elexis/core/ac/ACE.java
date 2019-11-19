package ch.elexis.core.ac;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import ch.rgw.tools.StringTool;
public class ACE {
	
	public static final String ACE_ROOT_LITERAL = "root";//$NON-NLS-1$	
	public static final ACE ACE_ROOT = new ACE(null, ACE_ROOT_LITERAL, Messages.ACE_root);
	public static final ACE ACE_IMPLICIT = new ACE(ACE.ACE_ROOT, "implicit", Messages.ACE_implicit); //$NON-NLS-1$
	
	private final ACE parent;
	private String name;
	private String localizedName;
	private List<ACE> children = new ArrayList<ACE>();
	
	/**
	 * Create a new ACE. This is the recommended constructor for most cases.
	 * 
	 * @param parent
	 *            the parent ACE. If this is a top-level ACE, use {@link #ACE_ROOT} as parent.
	 * @param name
	 *            the internal, immutable name of this ACE. Should be unique. Therefore, it is
	 *            recommended to prefix the name with the plugin ID
	 * @param localizedName
	 *            the name that will be presented to the user. This should be a translatable String
	 */
	public ACE(ACE parent, String name, String localizedName){
		this.parent = parent;
		this.name = name;
		this.localizedName = localizedName;
		this.children = new ArrayList<>();
		if (parent != null)
			parent.addChild(this);
	}
	
	/**
	 * create a new ACE without localized name. The localized name will be the same as the internal
	 * name. So this constructor should <b>not</b> be used for ACE's that will be shown to the user.
	 * 
	 * @param parent
	 *            the parent ACE. If this is a top-evel ACE, use ACE_ROOT as parent.
	 * @param name
	 *            the internal, immutable name of this ACE. Should be unique. Therefore, it is
	 *            recommended to prefix the name with the plugin ID.
	 */
	public ACE(ACE parent, String name){
		this(parent, name, name);
	}
	
	private void addChild(ACE ace){
		children.add(ace);
	}
	
	public String getName(){
		return name;
	}
	
	public ACE getParent(){
		return parent;
	}
	
	public List<ACE> getChildren(){
		return new ArrayList<ACE>(children);
	}
	
	public String getLocalizedName(){
		return localizedName;
	}
	
	public void setLocalizedName(String value){
		localizedName = value;
	}
	
	public List<ACE> getChildren(boolean deep){
		if (deep) {
			return getChildrenRecursive();
		} else {
			return getChildren();
		}
	}
	
	/**
	 * recursively fetch all children, adding self
	 * 
	 * @return
	 */
	private List<ACE> getChildrenRecursive(){
		List<ACE> ret = new ArrayList<ACE>();
		ret.add(this);
		for (ACE ace : children) {
			ret.addAll(ace.getChildrenRecursive());
		}
		return ret;
	}
	
	public List<ACE> getParentChainIncludingSelf(){
		List<ACE> aces = new ArrayList<ACE>();
		aces.add(this);
		if (this.equals(ACE_ROOT))
			return aces;
		ACE parent = getParent();
		while (parent != ACE_ROOT) {
			aces.add(parent);
			parent = parent.getParent();
		}
		return aces;
	}
	
	public String getCanonicalName(){
		StringBuilder sp = new StringBuilder();
		sp.append(getName());
		ACE parent = getParent();
		while ((parent != null) && (!parent.equals(ACE.ACE_ROOT))) {
			sp.insert(0, parent.getName() + StringTool.slash); //$NON-NLS-1$
			parent = parent.getParent();
		}
		return sp.toString();
	}
	
	public String getUniqueHash(){
		if (ACE_ROOT.equals(this))
			return ACE_ROOT_LITERAL;
		int valCan = Math.abs(getCanonicalName().hashCode());
		int valNam = Math.abs(getName().hashCode());
		BigInteger valI = new BigInteger(valCan + StringTool.leer + valNam);
		return valI.toString(16);
	}
}
