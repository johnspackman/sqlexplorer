package net.sourceforge.sqlexplorer.ext.oracle.actions;

import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sourceforge.sqlexplorer.dbviewer.model.IDbModel;
import net.sourceforge.sqlexplorer.dbviewer.model.SchemaNode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditorInput;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;

/**
 * @author Mazzolini
 *
 */
public class ExtractDDL extends Action {
	SessionTreeNode sessionNode;
	IDbModel nd;
	public ExtractDDL(SessionTreeNode sessionNode,IDbModel nd) {
		this.sessionNode=sessionNode;
		this.nd=nd;
	}
	public String getText(){
		return "Extract DDL"; //$NON-NLS-1$
	}
	public void run(){
		final String sql="select DBMS_METADATA.GET_DDL(?,?,?) FROM dual";
		try{
			SQLConnection conn=sessionNode.getConnection();
			PreparedStatement stmt = conn.prepareStatement(sql);
			IDbModel parent=(IDbModel) nd.getParent();
			String object_type=parent.toString();
			if(object_type.equalsIgnoreCase("PACKAGE BODY"))
				object_type="PACKAGE_BODY";
			stmt.setString(1, object_type);
			stmt.setString(2, nd.toString());
			String owner=((SchemaNode)(parent).getParent()).toString();
			stmt.setString(3,owner);
			StringBuffer result = new StringBuffer(1000);
			ResultSet rs=stmt.executeQuery();
			String txt=null;
			if(rs.next())
			{
				Clob clob=rs.getClob(1);
				txt = clob.getSubString(1, (int)clob.length());
			}
	
			rs.close();
			stmt.close();
			SQLEditorInput input = new SQLEditorInput("EXTRACT DDL ("+SQLExplorerPlugin.getDefault().getNextElement()+").sql");
			input.setSessionNode(sessionNode);
			IWorkbenchPage page=SQLExplorerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
			try{
				SQLEditor editorPart= (SQLEditor) page.openEditor(input,"net.sf.jfacedbc.plugin.editors.SQLEditor");
				editorPart.setText(txt);
			}catch(Throwable e){
				SQLExplorerPlugin.error("Error creating sql editor for extract DDL",e);
			}
		}catch(Exception e){
			SQLExplorerPlugin.error("Error extracting ddl",e);
		}
	}
}
