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
package net.sourceforge.sqlexplorer.ext.oracle.dialogs.actions;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

//import org.eclipse.swt.SWT;
//import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import net.sourceforge.squirrel_sql.fw.sql.SQLConnection;

/**
 * @author mazzolini
 */
public class GetErrors {
	SQLConnection conn;
	String owner;
	String objectType;
	String objectName;
	final static String sql1= " SELECT sequence, line, position, text "+
						" FROM sys.all_errors "+
						" WHERE owner =? "+
						" AND   name  =? "+
						" AND   type  =? "+
						" ORDER BY sequence";
	public GetErrors(SQLConnection conn, String owner, String objectType, String objectName){
		this.conn=conn;
		this.owner=owner;
		this.objectName=objectName;
		this.objectType=objectType;
	}
	public String getError(StyledText st){
		String result="";
		try{
			PreparedStatement stmt =conn.prepareStatement(sql1);
	
			stmt.setString(1, owner);
			stmt.setString(2, objectName);
			stmt.setString(3, objectType);
			ResultSet rs = stmt.executeQuery();
			String sep = System.getProperty("line.separator"); //$NON-NLS-1$
			Display disp=st.getDisplay();
			final Color blue = new Color(disp, 0, 0, 255);
			final Color red = new Color(disp, 255, 0, 0);
			st.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					blue.dispose();
					red.dispose();
				}
			});
			//int start=0;
			String allText="";				
			while(rs.next()){
							

				String sequence=rs.getString(1);
				String line=rs.getString(2);
				String pos=rs.getString(3);
				String text=rs.getString(4);
				
				allText+=sequence+") "+objectType+" "+owner+"."+objectName+" - (Line: "+line+ " Column: "+pos+ ")"+sep+text+sep;
				//StyleRange styleRange = new StyleRange();
				//styleRange.start = 0;
				//styleRange.length = sequence.length()+1;
				//styleRange.fontStyle = SWT.BOLD;
				//styleRange.foreground = red;
				//st.setStyleRange(styleRange);
			}
			st.setText(allText);
			rs.close();
			stmt.close();
		}catch(Throwable e){
		}
		return result;
	}
}
