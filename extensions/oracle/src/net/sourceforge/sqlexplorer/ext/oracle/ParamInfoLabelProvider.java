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
package net.sourceforge.sqlexplorer.ext.oracle;


import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;


public class ParamInfoLabelProvider
	extends LabelProvider
	implements ITableLabelProvider {


	/**
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(Object, int)
	 */
	public final String getColumnText(Object element, int columnIndex) {
		ParamObj obj=((ParamObj)element);
		if(columnIndex==0)
			return obj.argumentName!=null?obj.argumentName:"<return value>";
		else if(columnIndex==1)
			return obj.dataType!=null?obj.dataType:"";
		else if(columnIndex==2)
			return obj.dataLength!=-1?(""+obj.dataLength):"";
		else if(columnIndex==3)
			return obj.dataPrecision!=-1?(""+obj.dataPrecision):"";
		else if(columnIndex==4)
			return ""+obj.inOut;
			
		return "";
	}
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

}