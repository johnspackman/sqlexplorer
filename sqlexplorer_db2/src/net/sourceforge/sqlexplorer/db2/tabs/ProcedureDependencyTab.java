package net.sourceforge.sqlexplorer.db2.tabs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dataset.DataSet;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractDataSetTab;

public class ProcedureDependencyTab extends AbstractDataSetTab {

	private static final Log _logger = LogFactory.getLog(ProcedureDependencyTab.class);
	
    public String getLabelText() {
    	return Messages.getString("db2.Tab.dependency");
    }

    public DataSet getDataSet() throws Exception {       
        
    	String schemaName = getNode().getSchemaOrCatalogName();
        String nodeName = getNode().toString();
        
        _logger.debug("nodeName is: " + nodeName);
        _logger.debug("getNode().getType() is: " + getNode().getType());
        
        String sql = "select r.ROUTINESCHEMA as SCHEMA,r.ROUTINENAME as NAME,(CASE p.BTYPE WHEN 'T' THEN 'Table' WHEN 'V' THEN 'View' WHEN 'I' THEN 'Index' WHEN 'A' THEN 'Alias' WHEN 'B' THEN 'Trigger' WHEN 'F' THEN 'Function instance' WHEN 'M' THEN 'Function mapping' WHEN 'N' THEN 'Nickname' WHEN 'O' THEN 'Privilege dependency on all subtables or subviews in a table or view hierarchy' WHEN 'P' THEN 'Page size' WHEN 'R' THEN 'Structured type' WHEN 'S' THEN 'Materialized query table' WHEN 'U' THEN 'Typed table' WHEN 'W' THEN 'Typed view' ELSE p.BTYPE END) as Dependency_TYPE,p.BSCHEMA AS Dependency_Schema,p.BNAME AS Dependency_Name,RTRIM(p.BSCHEMA)||'.'||p.BNAME AS Qualified_Name from SYSCAT.routines r inner join syscat.routinedep rd on( r.routineSCHEMA = rd.routineschema and r.routinename = rd.routinename) inner join SYSCAT.PACKAGEDEP p on (rd.BSCHEMA = p.PKGSCHEMA and rd.bname=p.PKGNAME ) where r.ROUTINESCHEMA='" + schemaName + "' and r.ROUTINENAME='" + nodeName + "' and r.ROUTINETYPE='P'";
        _logger.debug("sql to get dependency for procedure is: " + sql);
        return new DataSet(null, sql, null, getNode().getSession());
    }

    public String getStatusMessage() {
        return Messages.getString("db2.Tab.dependency.status") + " " + getNode().getQualifiedName();
    }
}
