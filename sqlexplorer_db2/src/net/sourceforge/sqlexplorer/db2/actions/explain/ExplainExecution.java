package net.sourceforge.sqlexplorer.db2.actions.explain;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.parsers.Query;
import net.sourceforge.sqlexplorer.parsers.QueryParser;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.sqlpanel.AbstractSQLExecution;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

public class ExplainExecution extends AbstractSQLExecution {

    static class TreeLabelProvider extends LabelProvider implements ITableLabelProvider {

        public Image getColumnImage(Object element, int columnIndex) {

            return null;
        }


        public String getColumnText(Object element, int columnIndex) {

            ExplainNode en = (ExplainNode) element;
            if (columnIndex == 0)
                return en.toString();
            if (columnIndex == 1) {
                int cost = en.getCost();
                if (cost != -1)
                    return "" + cost;
                else
                    return "";
            }

            else if (columnIndex == 2) {
                int card = en.getCardinality();
                if (card != -1)
                    return "" + card;
                else
                    return "";
            }
            return "";
        }
    }

    private PreparedStatement _prepStmt;

    private Statement _stmt;


    public ExplainExecution(SQLEditor editor, QueryParser queryParser) {
    	super(editor, queryParser);
        
        // set initial message
        setProgressMessage(Messages.getString("SQLResultsView.ConnectionWait"));
    }


    private void displayResults(final ExplainNode node, final Query query) {

        getEditor().getSite().getShell().getDisplay().asyncExec(new Runnable() {

            public void run() {

            	CTabItem tabItem = allocateResultsTab(query);
            	if (tabItem == null)
            		return;

            	Composite composite = null;
                try {
                    composite = new Composite(tabItem.getParent().getParent(), SWT.NONE);
                    tabItem.setControl(composite);

                    GridLayout gLayout = new GridLayout();
                    gLayout.numColumns = 2;
                    gLayout.marginLeft = 0;
                    gLayout.horizontalSpacing = 0;
                    gLayout.verticalSpacing = 0;
                    gLayout.marginWidth = 0;
                    gLayout.marginHeight = 0;
                    composite.setLayout(gLayout);

                    Composite pp = new Composite(composite, SWT.NULL);
                    pp.setLayout(new FillLayout());
                    pp.setLayoutData(new GridData(GridData.FILL_BOTH));
                    TreeViewer tv = new TreeViewer(pp, SWT.BORDER | SWT.FULL_SELECTION);
                    Tree table = tv.getTree();
                    table.setLinesVisible(true);
                    table.setHeaderVisible(true);
                    TreeColumn tc = new TreeColumn(table, SWT.NULL);
                    tc.setText("");
                    tc = new TreeColumn(table, SWT.NULL);
                    tc.setText("Cost");
                    tc = new TreeColumn(table, SWT.NULL);
                    tc.setText("Cardinality");
                    TableLayout tableLayout = new TableLayout();
                    tableLayout.addColumnData(new ColumnWeightData(6, 150, true));
                    tableLayout.addColumnData(new ColumnWeightData(1, 50, true));
                    tableLayout.addColumnData(new ColumnWeightData(1, 50, true));
                    table.setLayout(tableLayout);

                    tv.setContentProvider(new ITreeContentProvider() {

                        public void dispose() {

                        }


                        public Object[] getChildren(Object parentElement) {

                            return ((ExplainNode) parentElement).getChildren();
                        }


                        public Object[] getElements(Object inputElement) {

                            ExplainNode nd = ((ExplainNode) inputElement);

                            return nd.getChildren();
                        }


                        public Object getParent(Object element) {

                            return ((ExplainNode) element).getParent();
                        }


                        public boolean hasChildren(Object element) {

                            if (((ExplainNode) element).getChildren().length > 0)
                                return true;
                            return false;
                        }


                        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

                        }
                    });
                    tv.setLabelProvider(new TreeLabelProvider() {
                    });
                    tv.setInput(node);
                    tv.refresh();
                    tv.expandAll();

                    // make columns full size
                    for (int i = 0; i < table.getColumnCount(); i++) {
                        table.getColumn(i).pack();
                    }
                    
                    /*
                    final Composite parent = composite;                    
                    table.addKeyListener(new KeyAdapter() {
                        
                        public void keyReleased(KeyEvent e) {

                            switch (e.keyCode) {

                                case SWT.F5:
                                    
                                    // refresh SQL Results
                                    try {
                                        Object o = parent.getData("parenttab");
                                        if (o != null) {
                                            AbstractSQLExecution sqlExec = (AbstractSQLExecution) ((TabItem)o).getData();
                                            if (sqlExec != null) {
                                                sqlExec.startExecution();
                                            }
                                        }
                                    } catch (Exception e1) {
                                        SQLExplorerPlugin.error("Error refreshing", e1);
                                    }

                                    break;

                            }

                        }

                    });*/
                    
                    composite.layout();
                    composite.redraw();
                    
                } catch (Exception e) {

                    // add message
                    String message = e.getMessage();
                    Label errorLabel = new Label(composite, SWT.FILL);
                    errorLabel.setText(message);
                    errorLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

                    SQLExplorerPlugin.error("Error creating explain tab", e);
                }
            };
        });
    }


    protected void doExecution(IProgressMonitor monitor) throws Exception {

        try {

            setProgressMessage(Messages.getString("SQLResultsView.Executing"));

            Query query = null;
        	for (Iterator<Query> iter = getQueryParser().iterator(); iter.hasNext(); ) {
        		query = iter.next();
    			if (monitor.isCanceled())
    				break;

	            int id_ = new Random().nextInt(1000);
	
	            _prepStmt = _connection.prepareStatement(
	                    "delete from SYSTOOLS.explain_statement where queryno = ? ");
	            _prepStmt.setInt(1, id_);
	            _prepStmt.executeUpdate();
	            _prepStmt.close();
	            _prepStmt = null;
	
	            if (monitor.isCanceled()) {
	                return;
	            }
	            
	            _stmt = _connection.createStatement();
	            _stmt.execute("EXPLAIN PLAN SET queryno = " + id_ + " FOR " + query.getQuerySql());
	            _stmt.close();
	            _stmt = null;
	
	            if (monitor.isCanceled()) {
	                return;
	            }
	            
	            _prepStmt = _connection.prepareStatement(
	                    "SELECT O.Operator_ID as id, S2.Target_ID as parent_id, O.Operator_Type, "
	                            + "S.OBJECT_SCHEMA, EOB.OBJECT_TYPE, S.Object_Name, O.CPU_COST,  "
	                            + "CAST(O.Total_Cost AS INTEGER) Cost FROM SYSTOOLS.EXPLAIN_OPERATOR O "
	                            + "LEFT OUTER JOIN SYSTOOLS.EXPLAIN_STREAM S2 ON O.Operator_ID=S2.Source_ID "
	                            + "LEFT OUTER JOIN SYSTOOLS.EXPLAIN_STREAM S  ON O.Operator_ID = S.Target_ID "
	                            + "AND O.Explain_Time = S.Explain_Time AND S.Object_Name IS NOT NULL "
	                            + "LEFT OUTER JOIN SYSTOOLS.explain_object EOB ON O.Explain_Time = EOB.Explain_Time     "
	                            + "AND S.OBJECT_NAME = EOB.OBJECT_NAME "
	                            + "where o.explain_time =  (select max(explain_time) from SYSTOOLS.explain_statement where queryno = ?) "
	                            + "ORDER BY O.Operator_ID ASC, S2.TARGET_ID ASC ");
	            _prepStmt.setInt(1, id_);
	            ResultSet rs = _prepStmt.executeQuery();
	
	            if (monitor.isCanceled()) {
	                return;
	            }
	            
	            HashMap<Integer,ExplainNode> mp = new HashMap<Integer, ExplainNode>();
	            ExplainNode baseNode = new ExplainNode(null);
	            mp.put(new Integer(-1), baseNode);
	            int lastId = -1;
	
	            while (rs.next()) {
	                String object_type = rs.getString("object_type");
	                String operation = rs.getString("operator_type");
	                String options = null;
	                String object_owner = rs.getString("object_schema");
	                String object_name = rs.getString("object_name");
	                String optimizer = null;
	                int cardinality = rs.getInt("cpu_cost");
	                if (rs.wasNull()) {
	                    cardinality = -1;
	                }
	
	                int cost = rs.getInt("cost");
	                if (rs.wasNull())
	                    cost = -1;
	                int parentID = rs.getInt("parent_id");
	                if (rs.wasNull()) {
	                    parentID = -1;
	                }
	                int id = rs.getInt("id");
	
	                if (id != lastId) {
	
	                    lastId = id;
	                    ExplainNode nd_parent = (ExplainNode) mp.get(new Integer(parentID));
	                    ExplainNode nd = new ExplainNode(nd_parent);
	                    mp.put(new Integer(id), nd);
	
	                    nd_parent.add(nd);
	                    nd.setId(id);
	                    nd.setCardinality(cardinality);
	                    nd.setCost(cost);
	                    nd.setObject_name(object_name);
	                    nd.setObject_owner(object_owner);
	                    nd.setObject_type(object_type);
	                    nd.setOperation(operation);
	                    nd.setOptimizer(optimizer);
	                    nd.setOptions(options);
	                }
	            }
	            rs.close();
	            _prepStmt.close();
	            _prepStmt = null;
	
	            if (monitor.isCanceled()) {
	                return;
	            }
	            
	            displayResults(baseNode, query);
        	}

        } catch (Exception e) {

            if (_stmt != null) {

                try {
                    _stmt.close();
                    _stmt = null;
                } catch (Exception e1) {
                    SQLExplorerPlugin.error("Error closing statement.", e);
                }
            }

            if (_prepStmt != null) {
                try {
                    _prepStmt.close();
                    _prepStmt = null;
                } catch (Exception e1) {
                    SQLExplorerPlugin.error("Error closing statement.", e);
                }
            }
            throw e;
        }

    }


    protected void doStop() throws Exception {

        Exception t = null;

        if (_stmt != null) {

            try {
                _stmt.cancel();
            } catch (Exception e) {
                t = e;
                SQLExplorerPlugin.error("Error cancelling statement.", e);
            }
            try {
                _stmt.close();
                _stmt = null;
            } catch (Exception e) {
                SQLExplorerPlugin.error("Error closing statement.", e);
            }
        }

        if (_prepStmt != null) {

            try {
                _prepStmt.cancel();
            } catch (Exception e) {
                t = e;
                SQLExplorerPlugin.error("Error cancelling statement.", e);
            }
            try {
                _prepStmt.close();
                _prepStmt = null;
            } catch (Exception e) {
                SQLExplorerPlugin.error("Error closing statement.", e);
            }
        }

        if (t != null) {
            throw t;
        }
    }
}
