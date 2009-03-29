package net.sourceforge.sqlexplorer.derby.tabs;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.sourceforge.sqlexplorer.dataset.DataSet;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractDataSetTab;
import net.sourceforge.sqlexplorer.dbproduct.SQLConnection;
import net.sourceforge.sqlexplorer.dbproduct.Session;

public class TriggerTab extends AbstractDataSetTab {

	private static String SQL_TRIGGER_INFO = "SELECT " +
			"T.TRIGGERNAME, T.CREATIONTIMESTAMP, T.EVENT, T.FIRINGTIME, T.TYPE, T.STATE, T.TRIGGERDEFINITION, " +
			"S.SCHEMANAME, " +
			"B.TABLENAME " +
		"FROM SYS.SYSTRIGGERS T, SYS.SYSSCHEMAS S, SYS.SYSTABLES B " +
		"WHERE T.TRIGGERNAME=? AND S.SCHEMANAME=? AND S.SCHEMAID=T.SCHEMAID AND B.TABLEID=T.TABLEID";
	//  AND B.TABLENAME=?
	
	
	private static int DATA_COUNT = 9;
	private static String[] INFO_LABELS = {
		"Name",
		"Date Created",
		"Event",
		"Firing Time",
		"Type",
		"State",
		"SQL Action",
		"Schema",
		"Table"
	};
	
	private static String COLUMN_LABELS[] = {"Property", "Value"};
	

	public TriggerTab() {
	}


	public String getLabelText() {
		return "Trigger Info";
	}

	public String getStatusMessage() {
		return "Trigger '" + getNode().getQualifiedName() + "' info";
	}

	public DataSet getDataSet() throws Exception {
		
		DataSet dataSet = null;
		
        Session session = getNode().getSession();
        if (session == null)
            return null;

        SQLConnection connection = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		
		try {
			connection = session.grabConnection();
			pst = connection.prepareStatement(SQL_TRIGGER_INFO);
			pst.setString(1, getNode().getName());
			pst.setString(2, getNode().getSchemaOrCatalogName());
			
			rs = pst.executeQuery();
			if(rs.next()) {
				String[][] data = new String[DATA_COUNT][2];
				
				for (int i = 0; i < DATA_COUNT; i++) {
					data[i][0] = INFO_LABELS[i];
					data[i][1] = rs.getString(i+1);
				}
				
				dataSet = new DataSet(COLUMN_LABELS, data);
			}
			
		} finally {
			if (rs != null)	{
				try {
					rs.close();
				} catch (SQLException e) {
					// Ignore here
				}
			}
			
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					// Ignore here
				}
			}
			if(connection != null)
			{
				session.releaseConnection(connection);
			}
		}
		
		return dataSet;
	}

}
