package net.sourceforge.sqlexplorer.db2.tabs;

import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSQLSourceTab;

public class ProcedureSourceTab extends AbstractSQLSourceTab {

    public String getSQL() {   
        return "select text from syscat.routines where routineschema = ? and routinename = ? and routinetype='P'";
    }
    
    public Object[] getSQLParameters() {
        return new Object[] {getNode().getSchemaOrCatalogName(), getNode().getName()};
    }
}
