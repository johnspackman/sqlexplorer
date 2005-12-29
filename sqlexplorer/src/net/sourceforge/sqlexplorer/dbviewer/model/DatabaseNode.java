package net.sourceforge.sqlexplorer.dbviewer.model;
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

import java.util.ArrayList;
import java.util.Iterator;

import net.sourceforge.sqlexplorer.dbviewer.DetailManager;

import org.eclipse.swt.widgets.Composite;

public class DatabaseNode implements IDbModel{
	
	private String _metaFilterExpression;
	
	String txt;
	
	
	public Composite getComposite(DetailManager detailManager){return null;};
	ArrayList children = new ArrayList(10);
	
	
	public void add(IDbModel e){
		children.add(e);
	}
	
	public DatabaseNode(String txt){
		this.txt=txt;
	}
	
	/**
	 * Returns all or a subset of schemas/databases depending on whether
	 * a comma separated list of regular expression filters has been set. 
	 */
	public Object[] getChildren(){
			
		if (_metaFilterExpression == null || _metaFilterExpression.length() == 0) {
			return children.toArray();
		}
		
		String[] filterExpressions = _metaFilterExpression.split(",");
		
		if (filterExpressions != null && filterExpressions.length > 0) {
			
			ArrayList restrictedChildren = new ArrayList();
		
			Iterator it = children.iterator();
			while (it.hasNext()) {
				
				Object schema = it.next();
				if (schema != null) {
					
					String name = schema.toString();					
					for (int j = 0; j < filterExpressions.length; j++) {
						
						String regex = filterExpressions[j].trim();
						regex = regex.replace("?", ".");
						regex = regex.replace("*", ".*");
						if (regex.length() == 0 || name.matches(regex)) {
							// we have a match, include node..
							restrictedChildren.add(schema);
							break;
						}						
					}						
				}
				
			}

			
			if (restrictedChildren.size() > 0) {
				return restrictedChildren.toArray();
			} 
		}
		
		return children.toArray();
	}
	
	
	
	public Object getParent(){return null;};

	public String toString(){return txt;}

	public void setMetaFilterExpression(String expression) {
		_metaFilterExpression = expression;
	}
		
}
