package ch.elexis.core.ui.dbcheck.model;

public class TableLABGROUPS extends TableDescriptor {

	@Override
	protected String[] returnFieldsIn186() {
		return new String[] { "ID", "lastupdate", "deleted", "name" };
	}

	@Override
	protected String[] returnFieldTypesIn186() {
		return new String[] { "varchar(25)", "bigint(20)", "char(1)", "varchar(30)" };
	}

	@Override
	protected String[] invalidStatesin186() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String[] cleaningSQLforPostgreSQLin186() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String[] cleaningSQLforMySQLin186() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String[] referentialIntegrityCheckSQLin186() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String[] returnFieldsIn188() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String[] returnFieldTypesIn188() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String[] returnFieldsIn189() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String[] returnFieldTypesIn189() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String[] returnFieldsIn1810() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String[] returnFieldTypesIn1810() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String[] returnFieldsIn1811() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String[] returnFieldTypesIn1811() {
		// TODO Auto-generated method stub
		return null;
	}

}
