/*
 * Copyright (C) 2006 SQL Explorer Development Team
 * http://sourceforge.net/projects/eclipsesql
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sourceforge.sqlexplorer.oracle.actions.explain;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.parsers.Query;
import net.sourceforge.sqlexplorer.parsers.QueryParser;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.sqlpanel.AbstractSQLExecution;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.Menu;
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

            	CTabItem resultsTab = allocateResultsTab(query);

                try {
	                Composite composite = resultsTab.getParent();
	
	                GridLayout gLayout = new GridLayout();
	                gLayout.numColumns = 2;
	                gLayout.marginLeft = 0;
	                gLayout.horizontalSpacing = 0;
	                gLayout.verticalSpacing = 0;
	                gLayout.marginWidth = 0;
	                gLayout.marginHeight = 0;
	                composite.setLayout(gLayout);

                    composite.setData("parenttab", resultsTab.getParent());

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
                                            AbstractSQLExecution sqlExec = (AbstractSQLExecution) ((TabItem) o).getData();
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
                    
                    // add context menu to table & cursor
                    final ExplainPlanActionGroup actionGroup = new ExplainPlanActionGroup(tv, node.getChildren()[0]);
                    MenuManager menuManager = new MenuManager("ExplainPlanContextMenu");
                    menuManager.setRemoveAllWhenShown(true);
                    Menu contextMenu = menuManager.createContextMenu(table);        
                    
                    tv.getControl().setMenu(contextMenu);
                    
                    menuManager.addMenuListener(new IMenuListener() {

                        public void menuAboutToShow(IMenuManager manager) {
                            actionGroup.fillContextMenu(manager);
                        }
                    });                    

                    composite.layout();
                    composite.redraw();

                } catch (Exception e) {

                    // add message
                	if (resultsTab != null) {
		                Composite composite = resultsTab.getParent();
	                    String message = e.getMessage();
	                    Label errorLabel = new Label(composite, SWT.FILL);
	                    errorLabel.setText(message);
	                    errorLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
                	}

                    SQLExplorerPlugin.error("Error creating explain tab", e);
                }

            };
        });
    }


    protected void doExecution(IProgressMonitor monitor) throws Exception {
        int numErrors = 0;
        SQLException lastSQLException = null;
        Query query = null;

        try {
        	try {
        		
            	for (Iterator<Query> iter = getQueryParser().iterator(); iter.hasNext(); ) {
            		query = iter.next();
        			if (monitor.isCanceled())
        				break;
	
		            _stmt = _connection.createStatement();
		            String id_ = Integer.toHexString(new Random().nextInt()).toUpperCase();
		            _stmt.execute("delete plan_table where statement_id='" + id_ + "'");
		            _stmt.close();
		            _stmt = null;
		
		            if (monitor.isCanceled()) {
		                return;
		            }
		
		            _stmt = _connection.createStatement();
		            _stmt.execute("EXPLAIN PLAN SET statement_id = '" + id_ + "' FOR " + query.getQuerySql());
		            _stmt.close();
		            _stmt = null;
		
		            if (monitor.isCanceled()) {
		                return;
		            }
		
		            _prepStmt = _connection.prepareStatement("select "
		                    + "level, object_type,operation,options,object_owner,object_name,optimizer,cardinality ,cost,id,parent_id,level "
		                    + " from " + " plan_table " + " start with id = 0 and statement_id=? "
		                    + " connect by prior id=parent_id and statement_id=?");
		            _prepStmt.setString(1, id_);
		            _prepStmt.setString(2, id_);
		            ResultSet rs = _prepStmt.executeQuery();
		
		            if (monitor.isCanceled()) {
		                return;
		            }
		
		            Map<Integer, ExplainNode> mp = new HashMap<Integer, ExplainNode>();
		            while (rs.next()) {
		                String object_type = rs.getString("object_type");
		                String operation = rs.getString("operation");
		                String options = rs.getString("options");
		                String object_owner = rs.getString("object_owner");
		                String object_name = rs.getString("object_name");
		                String optimizer = rs.getString("optimizer");
		                int cardinality = rs.getInt("cardinality");
		                if (rs.wasNull()) {
		                    cardinality = -1;
		                }
		
		                int cost = rs.getInt("cost");
		                if (rs.wasNull())
		                    cost = -1;
		                int parentID = rs.getInt("parent_id");
		                int id = rs.getInt("id");
		                int level = rs.getInt("level");
		                ExplainNode nd = null;
		                if (id == 0) {
		                    ExplainNode dummy = new ExplainNode(null);
		                    mp.put(new Integer(-1), dummy);
		                    dummy.setId(-1);
		                    nd = new ExplainNode(dummy);
		                    dummy.add(nd);
		                    nd.setId(0);
		                    mp.put(new Integer(0), nd);
		                } else {
		                    ExplainNode nd_parent = (ExplainNode) mp.get(new Integer(parentID));
		
		                    nd = new ExplainNode(nd_parent);
		                    nd_parent.add(nd);
		                    mp.put(new Integer(id), nd);
		                }
		                nd.setCardinality(cardinality);
		                nd.setCost(cost);
		                nd.setObject_name(object_name);
		                nd.setObject_owner(object_owner);
		                nd.setObject_type(object_type);
		                nd.setOperation(operation);
		                nd.setOptimizer(optimizer);
		                nd.setOptions(options);
		                nd.setId(id);
		                nd.setLevel(level);
		            }
		            rs.close();
		            _prepStmt.close();
		            _prepStmt = null;
		            ExplainNode nd_parent = (ExplainNode) mp.get(new Integer(-1));
		
		            if (monitor.isCanceled()) {
		                return;
		            }
		
		            displayResults(nd_parent, query);
	            	debugLogQuery(query, null);
        		}
            	query = null;
        	} catch(SQLException e) {
            	debugLogQuery(query, e);
            	boolean stopOnError = SQLExplorerPlugin.getDefault().getPreferenceStore().getBoolean(IConstants.STOP_ON_ERROR);
                logException(e, query, stopOnError);
                closeStatements();
            	if (stopOnError)
            		throw e;
            	numErrors++;
            	lastSQLException = e;
        	}
        } catch (Exception e) {
        	closeStatements();
            throw e;
        }

        if (numErrors == 1)
        	throw lastSQLException;
        else if (numErrors > 1)
			MessageDialog.openError(getEditor().getSite().getShell(), "SQL Error", "One or more of your SQL statements failed - check the Messages log for details");
    }

    private void closeStatements() {
        if (_stmt != null) {
            try {
                _stmt.close();
                _stmt = null;
            } catch (Exception e) {
                SQLExplorerPlugin.error("Error closing statement.", e);
            }
        }

        if (_prepStmt != null) {
            try {
                _prepStmt.close();
                _prepStmt = null;
            } catch (Exception e) {
                SQLExplorerPlugin.error("Error closing statement.", e);
            }
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
