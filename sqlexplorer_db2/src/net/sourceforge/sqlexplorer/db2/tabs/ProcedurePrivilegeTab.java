package net.sourceforge.sqlexplorer.db2.tabs;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dataset.DataSet;
import net.sourceforge.sqlexplorer.dataset.DataSetRow;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractDataSetTab;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProcedurePrivilegeTab extends AbstractDataSetTab {

	private static final Log _logger = LogFactory.getLog(ProcedurePrivilegeTab.class);
	
    public String getLabelText() {
    	return Messages.getString("DatabaseDetailView.Tab.Priviliges");
    }

    public DataSet getDataSet() throws Exception {       
        
    	String schemaName = getNode().getSchemaOrCatalogName();
        String nodeName = getNode().toString();
        
        _logger.debug("nodeName is: " + nodeName);
        _logger.debug("getNode().getType() is: " + getNode().getType());
        
        String specificName = this.getSpecificName();
        _logger.debug(specificName);
        
        String sql = "select distinct ra.GRANTEE,ra.SCHEMA,'" + nodeName + "' as NAME ,ra.SPECIFICNAME,ra.GRANTOR,ra.GRANTEETYPE,ra.EXECUTEAUTH,ra.GRANT_TIME from syscat.routineauth ra, syscat.routines r where ra.schema = '" + schemaName + "' and ra.specificname = '" + specificName + "'";
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
    	DataSet ds = new DataSet(null, sql, null, getNode().getSession());
    	DataSetRow dsw[] = ds.getRows();
    	_logger.debug("DataSetRow size during query specific name" + dsw.length);
    	String specificName = ds.getColumn(0).getDisplayValue(dsw[0].getCellValue(0));
    	return specificName;
    }
}
