package ch.elexis.core.ui.dbcheck.model;

public class TableUSERCONFIG extends TableDescriptor {

	@Override
	protected String[] returnFieldsIn186() {
		return new String[] { "lastupdate", "UserID", "Param", "Value" };
	}

	@Override
	protected String[] returnFieldTypesIn186() {
		return new String[] { "bigint(20)", "varchar(25)", "varchar(80)", "longtext" };
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
