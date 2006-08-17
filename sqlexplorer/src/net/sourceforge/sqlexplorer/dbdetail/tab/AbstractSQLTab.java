package net.sourceforge.sqlexplorer.dbdetail.tab;

import net.sourceforge.sqlexplorer.dataset.DataSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public abstract class AbstractSQLTab extends AbstractDataSetTab {
   
    protected static final Log _logger = LogFactory.getLog(AbstractSQLTab.class);

    public final DataSet getDataSet() throws Exception {
        return new DataSet(null, getSQL(), null, getNode().getSession().getInteractiveConnection());
    }

    public abstract String getLabelText();
    
    public abstract String getSQL();
    
}
