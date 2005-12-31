package net.sourceforge.sqlexplorer.sqlpanel;
/*
 * Copyright (C) 2002-2004 Andrea Mazzolini
 * andreamazzolini@users.sourceforge.net
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

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.MultiLineString;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.plugin.views.SqlResultsView;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.squirrel_sql.fw.sql.QueryTokenizer;
import net.sourceforge.squirrel_sql.fw.sql.ResultSetReader;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;


public class SqlExecProgress implements IRunnableWithProgress {

	/**
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(IProgressMonitor)
	 */
	
	private String _sql;
	
	SQLEditor txtComp;
	
	int maxRows;
	private SessionTreeNode sessionTreeNode;
	
	boolean sqlError;
	Throwable exception;
	public SqlExecProgress(String sqlString, SQLEditor txtComp,int maxRows, SessionTreeNode sessionTreeNode){
		_sql=sqlString;
		this.txtComp=txtComp;
		this.maxRows=maxRows;
		this.sessionTreeNode=sessionTreeNode;
		
	}
	
	public void run(final IProgressMonitor monitor)
		throws InvocationTargetException, 
			InterruptedException
		{
		
			// TODO make ';'  and Comment '#' configurable.
 		final long startTime=System.currentTimeMillis();
		QueryTokenizer qt = new QueryTokenizer(_sql,";", "#"); //$NON-NLS-1$
		List queryStrings = new ArrayList();
		while (qt.hasQuery())
		{
			final String querySql = qt.nextQuery();
			// ignore commented lines.
			if (!querySql.startsWith("--")) //$NON-NLS-1$
			{
				queryStrings.add(querySql);
			}
		}

		
		final ArrayList rsLis=new ArrayList();
		SqlTableModel sqlTbModel=null;
		while (!queryStrings.isEmpty())
		{

			String querySql = (String)queryStrings.remove(0);
			if (querySql != null)
			{
				sqlTbModel=processQuery(querySql,monitor);	
				if(sqlTbModel!=null)
				{
					rsLis.add(sqlTbModel);
				}
				SQLExplorerPlugin.getDefault().addSQLtoHistory(new MultiLineString(querySql));
			}
		}
				
		txtComp.getSite().getShell().getDisplay().asyncExec(new Runnable(){
			public void run(){
				try{
					SqlResultsView resultsView=(SqlResultsView) txtComp.getSite().getPage().showView("net.sourceforge.sqlexplorer.plugin.views.SqlResultsView");
					resultsView.setData(((SqlTableModel[])rsLis.toArray(new SqlTableModel[rsLis.size()])));//mo,new SQLTableSorter(count,metaData));
					long endTime=System.currentTimeMillis();
					String message=Messages.getString("Time__1")+" "+(int)(endTime-startTime)+Messages.getString("_ms");  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					txtComp.setMessage(message);
				}catch(Throwable e){
					SQLExplorerPlugin.error("Error displaying data",e);
					txtComp.setMessage(e.getMessage());
				}
			}
		});
	}
	private class LocalThread extends Thread{
		public void run(){
			try{
				while(true){
					
					if(end)
						break;
					if(monitor.isCanceled()){
						try{
							stmt.cancel();
						}
						finally{
							break;
						}
					}
					Thread.sleep(100);
				}
			}catch(Throwable e){
			}
			
		}
		public LocalThread(final IProgressMonitor monitor,final Statement stmt){
			this.monitor=monitor;
			this.stmt=stmt;
		}
		IProgressMonitor monitor; Statement stmt;
		boolean end=false;
		public void endMonitor(){;
			end=true;
		}
	}
	
	private SqlTableModel processQuery(String sql,final IProgressMonitor monitor){
		final long startTime=System.currentTimeMillis();
		
		LocalThread lt=null;
 		try{
			final Statement stmt=sessionTreeNode.getConnection().createStatement();
			lt=new LocalThread(monitor,stmt);
			lt.start();
 			
			boolean b = stmt.execute(sql);
			
			if(b)
			{
				final ResultSet rs = stmt.getResultSet();
				if(rs!=null){
				
					final ResultSetMetaData metaData=rs.getMetaData();
					final int count=metaData.getColumnCount();
					
					
					
					final String[]ss=new String[count];
					for(int i=0;i<count;i++){
						ss[i]=metaData.getColumnName(i+1);
					}
					final SQLTableSorter sorter=new SQLTableSorter(count,metaData);
					ResultSetReader reader=new ResultSetReader(rs);
					SqlTableModel md= new SqlTableModel(reader,metaData,maxRows,sessionTreeNode.getConnection(),ss,sorter, new MultiLineString(sql));
					lt.endMonitor();
					return md;
				}
			}
			else
			{
				lt.endMonitor();
			
				txtComp.getSite().getShell().getDisplay().asyncExec(new Runnable(){
					public void run(){
						try{
							long endTime=System.currentTimeMillis();
							String message=Messages.getString("Time__1")+" "+(int)(endTime-startTime)+Messages.getString("_ms");  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							txtComp.setMessage(message + Messages.getString("SqlExecProgress._updated_rowcount__5") + stmt.getUpdateCount()); //$NON-NLS-1$
							
						}catch(Throwable e){
							SQLExplorerPlugin.error("Error displaying data ",e); //$NON-NLS-1$
							txtComp.setMessage(e.getMessage());
						}
					};
				});
			}
			
 		}catch(final Throwable e){
 			if(!monitor.isCanceled()){
				SQLExplorerPlugin.error("Error processing query ",e); //$NON-NLS-1$
				exception=e;
				sqlError=true;
 			}
			/*txtComp.getSite().getShell().getDisplay().asyncExec(new Runnable(){
				public void run(){
			        MessageDialog.openError(txtComp.getSite().getShell(),Messages.getString("Error..._2"),e.getMessage());
				}
			});*/
			return null;
		}
		finally{
			lt.endMonitor();
		}
		return null;
			
	}
 							
	/**
	 * @return
	 */
	public Throwable getException() {
		return exception;
	}

	/**
	 * @return
	 */
	public boolean isSqlError() {
		return sqlError;
	}

}

