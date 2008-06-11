package net.sourceforge.sqlexplorer.db2.tabs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dataset.DataSet;
import net.sourceforge.sqlexplorer.dataset.DataSetRow;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractDataSetTab;

public class FunctionPrivilegeTab extends AbstractDataSetTab {
	private static final Log _logger = LogFactory.getLog(ProcedurePrivilegeTab.class);
	
    public String getLabelText() {
    	return Messages.getString("DatabaseDetailView.Tab.Priviliges");
    }

    public DataSet getDataSet() throws Exception {       
        
    	String schemaName = getNode().getSchemaOrCatalogName();
        String nodeName = getNode().toString();
        _logger.debug("nodeName is: " + nodeName);
        String specificName = this.getSpecificName();
        _logger.debug("specific name is: " + specificName);

        String sql = "select distinct GRANTEE,SCHEMA,'" + nodeName + "' as NAME ,SPECIFICNAME,GRANTOR,GRANTEETYPE,EXECUTEAUTH,GRANT_TIME from syscat.routineauth where schema = '" + schemaName + "' and specificname = '" + specificName + "'";
        _logger.debug("sql to get privilege for procedure is: " + sql);
        return new DataSet(null, sql, null, getNode().getSession());
    }

    public String getStatusMessage() {
        return Messages.getString("DatabaseDetailView.Tab.Priviliges.status") + " " + getNode().getQualifiedName();
    }
    public String getSpecificName() throws Exception {
    	String schemaName = getNode().getSchemaOrCatalogName();
        String nodeName = getNode().toString();
    	String sql = "select specificname from syscat.routines where routineschema = '" + schemaName + "' and routinename = '" + nodeName + "'";
    	_logger.debug("sql to get specific name: " + sql);
    	DataSet ds = new DataSet(null, sql, null, getNode().getSession());
    	DataSetRow dsw[] = ds.getRows();
    	_logger.debug("DataSetRow size during query specific name" + dsw.length);
    	String specificName = ds.getColumn(0).getDisplayValue(dsw[0].getCellValue(0));
    	return specificName;
    }
}
