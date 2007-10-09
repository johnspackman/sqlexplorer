package net.sourceforge.sqlexplorer.db2.tabs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dataset.DataSet;
import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractDataSetTab;

public class TableDependencyTab extends AbstractDataSetTab {

	private static final Log _logger = LogFactory.getLog(ProcedureDependencyTab.class);
	
    public String getLabelText() {
    	return Messages.getString("db2.Tab.dependency");
    }

    public DataSet getDataSet() throws Exception {       
        
    	String schemaName = getNode().getSchemaOrCatalogName();
        String nodeName = getNode().toString();
        _logger.debug("nodeName is: " + nodeName);
        _logger.debug("getNode().getType() is: " + getNode().getType());
        
        StringBuffer sql = new StringBuffer();
        
        sql.append("WITH OBJ as (\n");
        sql.append("select TABSCHEMA as SCHEMA, TABNAME as NAME from SYSCAT.TABLES\n");
        sql.append("where TABSCHEMA = '");
        sql.append(schemaName);
        sql.append("' and TABNAME = '");
        sql.append(nodeName);
        sql.append("'),PROC as (\n");
        sql.append("select PROCSCHEMA, PROCNAME, rd.bname as PKGNAME\n");
        sql.append("from SYSCAT.PROCEDURES p\n");
        sql.append("join syscat.routinedep rd\n");
        sql.append("on( p.PROCSCHEMA = rd.routineschema and p.specificname = rd.routinename)\n");
        sql.append("),RPL (\n");
        sql.append("ROOTTYPE, ROOTSCHEMA, ROOTNAME, LEVEL, SCHEMA, NAME, DTYPE,\n");
        sql.append("DEFINER, BTYPE, BSCHEMA, BNAME, TABAUTH\n");
        sql.append(") AS (\n");
        sql.append("SELECT BTYPE AS ROOTTYPE, BSCHEMA AS ROOTSCHEMA, BNAME AS ROOTNAME,\n");
        sql.append("1 as LEVEL, VIEWSCHEMA as SCHEMA, VIEWNAME as NAME, DTYPE, \n");
        sql.append("DEFINER, BTYPE, BSCHEMA,BNAME, TABAUTH\n");
        sql.append("FROM SYSCAT.VIEWDEP ROOT\n");
        sql.append("UNION ALL\n");
        sql.append("SELECT ROOTTYPE, ROOTSCHEMA, ROOTNAME, PARENT.LEVEL+1, \n");
        sql.append("CHILD.VIEWSCHEMA as SCHEMA,CHILD.VIEWNAME as NAME, CHILD.DTYPE, \n");
        sql.append("CHILD.DEFINER, CHILD.BTYPE,CHILD.BSCHEMA, CHILD.BNAME, CHILD.TABAUTH\n");
        sql.append("FROM RPL PARENT, SYSCAT.VIEWDEP CHILD\n");
        sql.append("WHERE PARENT.SCHEMA = CHILD.BSCHEMA\n");
        sql.append("AND PARENT.NAME = CHILD.BNAME AND PARENT.LEVEL < 50\n");
        sql.append("UNION ALL\n");
        sql.append("SELECT BTYPE AS ROOTTYPE, BSCHEMA AS ROOTSCHEMA, BNAME AS ROOTNAME,\n");
        sql.append("1 as LEVEL, FUNCSCHEMA as SCHEMA, FUNCNAME as NAME, 'F' as DTYPE, \n");
        sql.append("'?' as DEFINER, BTYPE, BSCHEMA,BNAME, TABAUTH\n");
        sql.append("FROM SYSCAT.FUNCDEP ROOT\n");
        sql.append("UNION ALL\n");
        sql.append("SELECT ROOTTYPE, ROOTSCHEMA, ROOTNAME, PARENT.LEVEL+1, \n");
        sql.append("CHILD.FUNCSCHEMA as SCHEMA,CHILD.FUNCNAME as NAME, 'F' as DTYPE, \n");
        sql.append("'?' as DEFINER, CHILD.BTYPE,CHILD.BSCHEMA, CHILD.BNAME, CHILD.TABAUTH\n");
        sql.append("FROM RPL PARENT, SYSCAT.FUNCDEP CHILD\n");
        sql.append("WHERE PARENT.SCHEMA = CHILD.BSCHEMA\n");
        sql.append("AND PARENT.NAME = CHILD.BNAME AND PARENT.LEVEL < 50\n");
        sql.append("UNION ALL\n");
        sql.append("SELECT BTYPE AS ROOTTYPE, BSCHEMA AS ROOTSCHEMA, BNAME AS ROOTNAME,\n");
        sql.append("1 as LEVEL, PROC.PROCSCHEMA as SCHEMA, PROC.PROCNAME as NAME, 'P' as DTYPE, \n");
        sql.append("'?' as DEFINER, BTYPE, BSCHEMA,BNAME, TABAUTH \n");
        sql.append("FROM SYSCAT.PACKAGEDEP ROOT, PROC PROC\n");
        sql.append("where PROC.PROCSCHEMA = ROOT.PKGSCHEMA\n");
        sql.append("and PROC.PKGNAME = ROOT.PKGNAME\n");
        sql.append("UNION ALL\n");
        sql.append("SELECT ROOTTYPE, ROOTSCHEMA, ROOTNAME, PARENT.LEVEL+1, PROC.PROCSCHEMA as SCHEMA,\n");
        sql.append("PROC.PROCNAME as NAME, 'P' as DTYPE, '?' as DEFINER, CHILD.BTYPE,\n");
        sql.append("CHILD.BSCHEMA, CHILD.BNAME, CHILD.TABAUTH\n");
        sql.append("FROM RPL PARENT, SYSCAT.PACKAGEDEP CHILD, PROC PROC\n");
        sql.append("WHERE PARENT.SCHEMA = CHILD.BSCHEMA\n");
        sql.append("AND PARENT.NAME = CHILD.BNAME\n");
        sql.append("and PROC.PROCSCHEMA = CHILD.PKGSCHEMA\n");
        sql.append("AND PROC.PKGNAME = CHILD.PKGNAME\n");
        sql.append("AND PARENT.LEVEL < 50 )\n");
        //                          1         2           3         4      5
        sql.append("SELECT distinct ROOTTYPE, ROOTSCHEMA, ROOTNAME, LEVEL, RPL.SCHEMA,\n");
        sql.append("CASE WHEN DTYPE = 'F' THEN \n");
        sql.append("(SELECT ROUTINENAME FROM SYSCAT.ROUTINES \n");
        sql.append("WHERE ROUTINESCHEMA = RPL.SCHEMA AND SPECIFICNAME = RPL.NAME )\n");
        //                               6
        sql.append("ELSE RPL.NAME END AS NAME,\n");
        //          7      8        9      10
        sql.append("DTYPE, DEFINER, BTYPE, BSCHEMA,\n");
        sql.append("CASE WHEN BTYPE = 'F' THEN (\n");
        sql.append("SELECT ROUTINENAME FROM SYSCAT.ROUTINES \n");
        sql.append("WHERE ROUTINESCHEMA = BSCHEMA AND SPECIFICNAME = BNAME )\n");
        //                            11    12
        sql.append("ELSE BNAME END AS BNAME,TABAUTH FROM RPL, OBJ\n");
        sql.append("where ROOTSCHEMA = OBJ.SCHEMA and ROOTNAME = OBJ.NAME\n");
        sql.append("ORDER BY ROOTTYPE, ROOTSCHEMA, ROOTNAME, LEVEL");

        _logger.debug("sql to get dependency for procedure is: " + sql.toString());
        return new DataSet(null, sql.toString(), null, getNode().getSession());
    }

    public String getStatusMessage() {
        return Messages.getString("db2.Tab.dependency.status") + " " + getNode().getQualifiedName();
    }
}
