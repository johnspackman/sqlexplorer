package net.sourceforge.sqlexplorer.db2.actions.explain;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Random;

import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.plugin.views.SqlResultsView;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.sqlexplorer.sqlpanel.SQLExecution;

import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class ExplainExecution extends SQLExecution {

    public ExplainExecution(SQLEditor editor, SqlResultsView resultsView, String sqlString, int maxRows,
            SessionTreeNode sessionTreeNode) {

        super(editor, resultsView, sqlString, maxRows, sessionTreeNode);
    }


    public void startExecution() {

        try {
            
            Statement st = _session.getConnection().createStatement();
            int id_ = new Random().nextInt(1000);

            PreparedStatement dps = _session.getConnection().prepareStatement(
                    "delete from SYSTOOLS.explain_statement where queryno = ? ");
            dps.setInt(1, id_);
            dps.executeUpdate();
            dps.close();
            

            st = _session.getConnection().createStatement();
            st.execute("EXPLAIN PLAN SET queryno = " + id_ + " FOR " + _sqlStatement);
            st.close();
            
            PreparedStatement ps = _session.getConnection().prepareStatement(
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
            ps.setInt(1, id_);            
            ResultSet rs = ps.executeQuery();
            
            HashMap mp = new HashMap();
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
            ps.close();

            displayResults(baseNode);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void stop() {

    }


    protected void displayResults(final ExplainNode node) {

        _resultsView.getSite().getShell().getDisplay().asyncExec(new Runnable() {

            public void run() {

                clearCanvas();

                GridLayout gLayout = new GridLayout();
                gLayout.numColumns = 2;
                gLayout.marginLeft = 0;
                gLayout.horizontalSpacing = 0;
                gLayout.verticalSpacing = 0;
                gLayout.marginWidth = 0;
                gLayout.marginHeight = 0;
                _composite.setLayout(gLayout);

                try {
                    _composite.setData("parenttab", _parentTab);

                    Composite pp = new Composite(_composite, SWT.NULL);
                    pp.setLayout(new FillLayout());
                    pp.setLayoutData(new GridData(GridData.FILL_BOTH));
                    TableTreeViewer tv = new TableTreeViewer(pp, SWT.BORDER | SWT.FULL_SELECTION);
                    Table table = tv.getTableTree().getTable();
                    table.setLinesVisible(true);
                    table.setHeaderVisible(true);
                    TableColumn tc = new TableColumn(table, SWT.NULL);
                    tc.setText("");
                    tc = new TableColumn(table, SWT.NULL);
                    tc.setText("Cost");
                    tc = new TableColumn(table, SWT.NULL);
                    tc.setText("Cardinality");
                    TableLayout tableLayout = new TableLayout();
                    tableLayout.addColumnData(new ColumnWeightData(6, 150, true));
                    tableLayout.addColumnData(new ColumnWeightData(1, 50, true));
                    tableLayout.addColumnData(new ColumnWeightData(1, 50, true));
                    table.setLayout(tableLayout);

                    tv.setContentProvider(new ITreeContentProvider() {

                        public Object[] getChildren(Object parentElement) {

                            return ((ExplainNode) parentElement).getChildren();
                        }


                        public Object getParent(Object element) {

                            return ((ExplainNode) element).getParent();
                        }


                        public boolean hasChildren(Object element) {

                            if (((ExplainNode) element).getChildren().length > 0)
                                return true;
                            return false;
                        }


                        public Object[] getElements(Object inputElement) {

                            ExplainNode nd = ((ExplainNode) inputElement);

                            return nd.getChildren();
                        }


                        public void dispose() {

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
                    for(int i = 0; i < table.getColumnCount(); i++) {
                        table.getColumn(i).pack();      
                    }
                    
                } catch (Exception e) {

                    // add message
                    String message = e.getMessage();
                    Label errorLabel = new Label(_composite, SWT.FILL);
                    errorLabel.setText(message);
                    errorLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

                    SQLExplorerPlugin.error("Error creating explain tab", e);
                }

                _composite.layout();
                _composite.redraw();

            };
        });
    }

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
}