package net.sourceforge.sqlexplorer;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
      

/**
 * This class is a copy of the original one that adds the possibility
 * to store a string expression for restricting metadata downloads.
 * 
 * @author Davy Vanherbergen
 *
 */
public class SQLAliasBeanInfo extends SimpleBeanInfo
{
 

 public SQLAliasBeanInfo()
     throws IntrospectionException
 {
     if(s_desc == null)
     {
         s_desc = new PropertyDescriptor[11];
         s_desc[0] = new PropertyDescriptor("identifier", CLAZZ, "getIdentifier", "setIdentifier");
         s_desc[1] = new PropertyDescriptor("name", CLAZZ, "getName", "setName");
         s_desc[2] = new PropertyDescriptor("url", CLAZZ, "getUrl", "setUrl");
         s_desc[3] = new PropertyDescriptor("userName", CLAZZ, "getUserName", "setUserName");
         s_desc[4] = new PropertyDescriptor("driverIdentifier", CLAZZ, "getDriverIdentifier", "setDriverIdentifier");
         s_desc[5] = new PropertyDescriptor("useDriverProperties", CLAZZ, "getUseDriverProperties", "setUseDriverProperties");
         s_desc[6] = new PropertyDescriptor("driverProperties", CLAZZ, "getDriverProperties", "setDriverProperties");
         s_desc[7] = new PropertyDescriptor("password", CLAZZ, "getPassword", "setPassword");
         s_desc[8] = new PropertyDescriptor("autoLogon", CLAZZ, "isAutoLogon", "setAutoLogon");
         s_desc[9] = new PropertyDescriptor("connectAtStartup", CLAZZ, "isConnectAtStartup", "setConnectAtStartup");
         s_desc[10] = new PropertyDescriptor("metaFilterExpression", CLAZZ, "getMetaFilterExpression", "setMetaFilterExpression");
     }
 }

 public PropertyDescriptor[] getPropertyDescriptors()
 {
     return s_desc;
 }


 private static PropertyDescriptor s_desc[];
 private static Class CLAZZ;

 static 
 {
     CLAZZ = net.sourceforge.sqlexplorer.SQLAlias.class;
 }
}