package ch.elexis.data;


public class Role extends PersistentObject {
	
	public static final String TABLENAME = "ROLE";
	
	public static final String FLD_PARENT = "PARENT";
	
	static {
		addMapping(TABLENAME, FLD_PARENT, FLD_EXTINFO);
	}
	
	protected Role(){}
	
	protected Role(final String id){
		super(id);
	}
	
	public static Role load(final String id){
		Role ret = new Role(id);
		if (ret.exists()) {
			return ret;
		}
		return null;
	}
	
	@Override
	public String getLabel(){
		String label = Messages.getString("Role_"+getId());
		if(!label.startsWith("!")) {
			return label;
		}
		return getId();
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	public static Role getRoot() {
		return new Role("root");
	}

	public Object[] getChildren(){
		Query<Role> qbe = new Query<Role>(Role.class);
		qbe.add(FLD_PARENT, Query.LIKE, getId());
		return qbe.execute().toArray();
	}
	
	@Override
	public String toString(){
		return getLabel();
	}

	public Role getParent(){
		return new Role(get(FLD_PARENT));
	}
}
