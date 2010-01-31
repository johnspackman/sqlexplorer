package net.sourceforge.sqlexplorer.sybase.actions;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.actions.AbstractDBTreeContextAction;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditorInput;
import net.sourceforge.sqlexplorer.sybase.nodes.ProcedureNode;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;


public class ShowProcedureSource extends AbstractDBTreeContextAction {
	
	public String getText() {
		return Messages.getString("sybase.dbstructure.actions.ShowProcedureSource");
	}
	
	public boolean isAvailable() {
        if (_selectedNodes.length != 0) {
            return true;
        }
        return false;
    }

	public void run() {
         try {
        	 
        	 ProcedureNode procNode;
    		 StringBuffer script = new StringBuffer("");
    		 
             String queryDelimiter = SQLExplorerPlugin.getStringPref(IConstants.SQL_QRY_DELIMITER);
             String altQueryDelimiter = SQLExplorerPlugin.getStringPref(IConstants.SQL_ALT_QRY_DELIMITER);

    		 // If there is an alternative delimeter set, I prefer the alt, 
    		 // because 'go' is the more usual delimeter in the sybase world.
             if (altQueryDelimiter != null && !altQueryDelimiter.equals("")) {
            	 queryDelimiter = altQueryDelimiter;
             }

             SQLConnection con = _selectedNodes[0].getSession().grabConnection();

             Statement stmt = con.createStatement();
             
             try {
                 for (int i = 0; i < _selectedNodes.length; i++) {

                     if (_selectedNodes[i].getType().equalsIgnoreCase("procedure")) {
                    	 procNode = (ProcedureNode) _selectedNodes[i];
                    	 generateProcedureDDL(con, script, stmt, procNode, queryDelimiter);
                    	 
                     }
                     
                     //if (_selectedNodes[i].getType().equalsIgnoreCase("view")) {
                    //	 procNode = (View) _selectedNodes[i];
                    //	 generateProcedureDDL(con, script, stmt, procNode, queryDelimiter);
                    // }
                 }
             } finally {
                 try {
                     stmt.close();
                 } catch (Exception e) {
                     SQLExplorerPlugin.error("Error closing statement.", e);
                 }
                 
                 if (con != null)
                	 _selectedNodes[0].getSession().releaseConnection((net.sourceforge.sqlexplorer.dbproduct.SQLConnection) con);        	
                 
             }

             if (script.length() == 0) {
                 return;
             }
             
             String inputTitle = "SQL Editor (" + SQLExplorerPlugin.getDefault().getEditorSerialNo() + ").sql";
             SQLEditorInput input = new SQLEditorInput(inputTitle);
             //input.setSessionNode(_selectedNodes[0].getSession());
             
             IWorkbenchPage page = SQLExplorerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();

             SQLEditor editorPart = (SQLEditor) page.openEditor((IEditorInput) input,
                     "net.sourceforge.sqlexplorer.plugin.editors.SQLEditor");
             editorPart.setText(script.toString());
             
         } catch (Throwable e) {
             SQLExplorerPlugin.error("Error creating export script", e);
         } 
    }

	private void generateProcedureDDL(SQLConnection con, StringBuffer script, Statement stmt, ProcedureNode procNode, String queryDelimeter) throws SQLException {

		ResultSet rs = null;		
			
		//String owner = procNode.getSession().getMetaData().getUserName();
		String owner = procNode.getUName();
		String spName = procNode.getName();
		String spUniqueName = procNode.getUniqueIdentifier();
		String dbName = procNode.getParent().getParent().toString(); 
		String scriptCommandDelim = "\n" + queryDelimeter + "\n\n";
		int objId = procNode.getID();
		
		
		script.append("USE " + dbName);
		script.append(scriptCommandDelim);
		
		script.append("SETUSER 'dbo'");
		script.append(scriptCommandDelim);
		
		String dropStatement = "IF object_id('" + spUniqueName
			+ "') IS NOT NULL DROP PROCEDURE " 
			+ owner + "." + spName;
		
		script.append(dropStatement);
		script.append(scriptCommandDelim);

		script.append("SETUSER '" + owner + "'");
		script.append(scriptCommandDelim);

		
		String sql = "Select text from " + dbName 
			+ "..syscomments NOHOLDLOCK where id = " + objId;
		
		rs = stmt.executeQuery(sql);
		 
		while (rs.next()) {
		    script.append(rs.getString(1));
		}
		script.append(scriptCommandDelim);
		
		StringBuffer grantStatements = getGrantStatements(spUniqueName, dbName, stmt, scriptCommandDelim);
		script.append(grantStatements);
		
		//System.out.println(procNode.getSession().getMetaData().getDatabaseProductVersion());
		
		String procxmodeStatement = getProcxmodeStatement(spName, spUniqueName, con);
		
		if (!procxmodeStatement.equals("")) {
			script.append(procxmodeStatement);
			script.append(scriptCommandDelim);
		}
		
		script.append("SETUSER");
		script.append(scriptCommandDelim);
		
		rs.close();
	}
	
	private StringBuffer getGrantStatements(String fullSpName, String dbName, Statement stmt, String delim) throws SQLException {

		List<String> grantees = new ArrayList<String>();
		
		String sql = "SELECT user_name(s.uid)"
			+ " FROM " + dbName + "..sysprotects s NOHOLDLOCK"
			+ " WHERE s.id = object_id('" + fullSpName + "')";
	
		ResultSet rs = stmt.executeQuery(sql);
	
		while (rs.next()) {
			grantees.add(rs.getString(1));
		}
	
		rs.close();
	
		StringBuffer grantStatements = new StringBuffer();
		
		String statement = "";
		String grantee = "";
		
		for (int i = 0; i < grantees.size(); i++) {
			grantee = (String) grantees.get(i);
			statement = "Grant Execute on " + fullSpName + " to " + grantee;
			grantStatements.append(statement);
			grantStatements.append(delim);
		}
	
		return grantStatements;
	}
	
	private String getProcxmodeStatement(String spName, String fullSpName, SQLConnection con) throws SQLException {
		
		String sql = 
			" create table #execmode (intval integer, charval varchar(30))"
			+ "	insert into #execmode values(0,  'unchained')"
			+ " insert into #execmode values(16, 'chained')"
			+ " insert into #execmode values(32, 'anymode')"
			+ " insert into #execmode values(256,'[not] dynamic [ownership chain]')"
			+ " SELECT t.charval as mode"
			+ " FROM   sysobjects o NOHOLDLOCK, #execmode t"
			+ " where	((o.type = 'P') or (o.type = 'XP')) and"
			+ "     	(((o.sysstat2 & t.intval ) != 0) or"
			+ " 		 ((t.intval = 0) and (o.sysstat2 & 48) = 0))"
			+ " AND o.name = '" + spName + "'"
			+ "	drop table #execmode"; 
		
		SybExecute execute = new SybExecute(con, sql);
		ResultSet rs = execute.getNextResultSet();
		String mode = "";
		
		while (rs != null) {
			while (rs.next()) {
				mode = rs.getString(1);
			}
			rs.close();
			rs = execute.getNextResultSet();
		}
		execute.close();
		
		String procxmodeStatement = "";
		
		if (!mode.equals("")) {
			procxmodeStatement = 
			"sp_procxmode '" + fullSpName + "', " + mode;
		}
		
		return procxmodeStatement;
	}

	
	private class SybExecute {
		
		private PreparedStatement stmt = null;
		private boolean firstResultReceived = false;
		
		public SybExecute(SQLConnection con, String sql) throws SQLException {
			stmt = con.prepareStatement(sql);
			stmt.execute();
		}
		
	    /**
	     * Honors multiple result sets and asks for update counts 
	     * as described in the jdbc documentation. 
	     * @throws SQLException
	     */
	    public ResultSet getNextResultSet() throws SQLException {
	        
	    	if (!firstResultReceived) {
				ResultSet rs = stmt.getResultSet();
				firstResultReceived = true;
			    if (rs == null) {
			    	rs = getNextResultSet();
			    }
			    return rs;
	    	}
	    	
	    	boolean isResultSet = stmt.getMoreResults();
	    	
	    	while (!isResultSet) {
	    		if (stmt.getUpdateCount() == -1) {
	    			return null;
	    		}
	    		isResultSet = stmt.getMoreResults();
	    	}
	    	
	    	ResultSet result = stmt.getResultSet(); 
	    	return result;
	    }
	    
	    public void close() throws SQLException {
	    	stmt.close();
	    }
		
	}
	
	
}
