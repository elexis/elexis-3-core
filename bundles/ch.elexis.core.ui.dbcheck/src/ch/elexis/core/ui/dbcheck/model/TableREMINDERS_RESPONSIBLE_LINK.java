package ch.elexis.core.ui.dbcheck.model;

public class TableREMINDERS_RESPONSIBLE_LINK extends TableDescriptor {
	
	@Override
	protected String[] returnFieldsIn186(){
		return new String[] {
			"ID", "lastupdate", "deleted", "ReminderID", "ResponsibleID"
		};
	}
	
	@Override
	protected String[] returnFieldTypesIn186(){
		return new String[] {
			"varchar(25)", "bigint(20)", "char(1)", "varchar(25)", "varchar(25)"
		};
	}
	
	@Override
	protected String[] invalidStatesin186(){
		return null;
	}
	
	@Override
	protected String[] cleaningSQLforPostgreSQLin186(){
		return new String[] {
			"Deleting redundant entries",
			"DELETE FROM reminders_responsible_link WHERE id IN (SELECT rrl.id FROM reminders_responsible_link rrl, (SELECT min(id) AS id, reminderid, responsibleid FROM reminders_responsible_link GROUP BY reminderid, responsibleid HAVING count(*) > 1) AS del WHERE rrl.reminderid = del.reminderid AND rrl.id != del.id)"
		};
	}
	
	@Override
	protected String[] cleaningSQLforMySQLin186(){
		return null;
	}
	
	@Override
	protected String[] referentialIntegrityCheckSQLin186(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected String[] returnFieldsIn188(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected String[] returnFieldTypesIn188(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected String[] returnFieldsIn189(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected String[] returnFieldTypesIn189(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected String[] returnFieldsIn1810(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected String[] returnFieldTypesIn1810(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected String[] returnFieldsIn1811(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected String[] returnFieldTypesIn1811(){
		// TODO Auto-generated method stub
		return null;
	}
	
}
