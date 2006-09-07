package net.sourceforge.sqlexplorer.db2.tabs;

import net.sourceforge.sqlexplorer.dbdetail.tab.AbstractSQLSourceTab;

public class FunctionSourceTab extends AbstractSQLSourceTab {

    public String getSQL() {   
        return "select text from syscat.routines where routineschema = ? and routinename = ? and routinetype='F'";
    }
    
    public Object[] getSQLParameters() {
        return new Object[] {getNode().getSchemaOrCatalogName(), getNode().getName()};
    }
}
