/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.sqlexplorer.gef.model;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import org.eclipse.draw2d.geometry.Point;


public class LocationPropertySource
	implements IPropertySource{

public static String ID_XPOS = "xPos"; //$NON-NLS-1$
public static String ID_YPOS = "yPos"; //$NON-NLS-1$
protected static IPropertyDescriptor[] descriptors;

static{
	descriptors = new IPropertyDescriptor[] {
		new TextPropertyDescriptor(ID_XPOS,"X"),
		new TextPropertyDescriptor(ID_YPOS,"Y")
	};
}

protected Point point = null;

public LocationPropertySource(Point point){
	this.point = new Point(point);
}

public Object getEditableValue(){
	return this;
}

public IPropertyDescriptor[] getPropertyDescriptors(){
	return descriptors;
}

public Object getPropertyValue(Object propName){
	if(ID_XPOS.equals(propName)){
		return new String(new Integer(point.x).toString());
	}
	if(ID_YPOS.equals(propName)){
		return new String(new Integer(point.y).toString());
	}
	return null;
}

public Point getValue(){
	return new Point(point);
}

public boolean isPropertySet(Object propName){
	if(ID_XPOS.equals(propName) || ID_YPOS.equals(propName))return true;
	return false;
}

public void resetPropertyValue(Object propName){}

public void setPropertyValue(Object propName, Object value){
	if(ID_XPOS.equals(propName)){
		Integer newInt = new Integer((String)value);
		point.x = newInt.intValue();
	}
	if(ID_YPOS.equals(propName)){
		Integer newInt = new Integer((String)value);
		point.y = newInt.intValue();
	}
}

public String toString(){
	return new String("["+point.x+","+point.y+"]");//$NON-NLS-3$//$NON-NLS-2$//$NON-NLS-1$
}

}
