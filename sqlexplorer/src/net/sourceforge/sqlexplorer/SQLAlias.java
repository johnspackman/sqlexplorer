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
package net.sourceforge.sqlexplorer;

import java.beans.PropertyChangeListener;
import java.io.Serializable;

import net.sourceforge.squirrel_sql.fw.id.IIdentifier;
import net.sourceforge.squirrel_sql.fw.persist.ValidationException;
import net.sourceforge.squirrel_sql.fw.sql.ISQLAlias;
import net.sourceforge.squirrel_sql.fw.sql.SQLDriverProperty;
import net.sourceforge.squirrel_sql.fw.sql.SQLDriverPropertyCollection;
import net.sourceforge.squirrel_sql.fw.util.PropertyChangeReporter;
import net.sourceforge.squirrel_sql.fw.util.StringManager;
import net.sourceforge.squirrel_sql.fw.util.StringManagerFactory;

/**
 * This class is a copy of the original one and adds the possibility to store a
 * string expression for restricting metadata downloads.
 * 
 * @author Davy Vanherbergen
 * 
 */
public class SQLAlias implements Cloneable, Serializable, ISQLAlias, Comparable {

    private static interface IStrings {

        public static final String ERR_BLANK_DRIVER = SQLAlias.s_stringMgr.getString("SQLAlias.error.blankdriver");

        public static final String ERR_BLANK_NAME = SQLAlias.s_stringMgr.getString("SQLAlias.error.blankname");

        public static final String ERR_BLANK_URL = SQLAlias.s_stringMgr.getString("SQLAlias.error.blankurl");

    }

    private boolean _autoLogon;

    private boolean _connectAtStartup;

    private IIdentifier _driverId;

    private SQLDriverPropertyCollection _driverProps;

    private String _folderFilterExpression = "";

    private IIdentifier _id;

    private String _name;

    private String _nameFilterExpression = "";

    private String _password;

    private transient PropertyChangeReporter _propChgReporter;

    private String _schemaFilterExpression = "";

    private String _url;

    private boolean _useDriverProperties;

    private String _userName;

    private static final StringManager s_stringMgr;

    public static final long serialVersionUID = 1;


    public SQLAlias() {

        _useDriverProperties = false;
        _driverProps = new SQLDriverPropertyCollection();
    }


    public SQLAlias(IIdentifier id) {

        _useDriverProperties = false;
        _driverProps = new SQLDriverPropertyCollection();
        _id = id;
        _name = "";
        _driverId = null;
        _url = "";
        _userName = "";
        _password = "";
    }

    static {
        s_stringMgr = StringManagerFactory.getStringManager(net.sourceforge.squirrel_sql.fw.sql.SQLAlias.class);
    }


    public void addPropertyChangeListener(PropertyChangeListener listener) {

        getPropertyChangeReporter().addPropertyChangeListener(listener);
    }


    public synchronized void assignFrom(ISQLAlias rhs) throws ValidationException {

        setName(rhs.getName());
        setDriverIdentifier(rhs.getDriverIdentifier());
        setUrl(rhs.getUrl());
        setUserName(rhs.getUserName());
        setPassword(rhs.getPassword());
        setAutoLogon(rhs.isAutoLogon());
        setUseDriverProperties(rhs.getUseDriverProperties());
        setDriverProperties(rhs.getDriverProperties());
    }


    public Object clone() throws CloneNotSupportedException {

        SQLAlias alias;
        alias = (SQLAlias) super.clone();
        alias._propChgReporter = null;
        alias.setDriverProperties(getDriverProperties());
        return alias;
    }


    public int compareTo(Object rhs) {

        return _name.compareTo(((ISQLAlias) rhs).getName());
    }


    public boolean equals(Object rhs) {

        boolean rc = false;
        if (rhs != null && rhs.getClass().equals(getClass()))
            rc = ((ISQLAlias) rhs).getIdentifier().equals(getIdentifier());
        return rc;
    }


    public IIdentifier getDriverIdentifier() {

        return _driverId;
    }


    public synchronized SQLDriverPropertyCollection getDriverProperties() {

        int count = _driverProps.size();
        SQLDriverProperty newar[] = new SQLDriverProperty[count];
        for (int i = 0; i < count; i++)
            newar[i] = (SQLDriverProperty) _driverProps.getDriverProperty(i).clone();

        SQLDriverPropertyCollection coll = new SQLDriverPropertyCollection();
        coll.setDriverProperties(newar);
        return coll;
    }


    public String getFolderFilterExpression() {

        return _folderFilterExpression;
    }


    public IIdentifier getIdentifier() {

        return _id;
    }


    public String getName() {

        return _name;
    }


    public String getNameFilterExpression() {

        return _nameFilterExpression;
    }


    public String getPassword() {

        return _password;
    }


    private synchronized PropertyChangeReporter getPropertyChangeReporter() {

        if (_propChgReporter == null)
            _propChgReporter = new PropertyChangeReporter(this);
        return _propChgReporter;
    }


    public String getSchemaFilterExpression() {

        return _schemaFilterExpression;
    }


    private String getString(String data) {

        return data == null ? "" : data.trim();
    }


    public String getUrl() {

        return _url;
    }


    public boolean getUseDriverProperties() {

        return _useDriverProperties;
    }


    public String getUserName() {

        return _userName;
    }


    public synchronized int hashCode() {

        return getIdentifier().hashCode();
    }


    public boolean isAutoLogon() {

        return _autoLogon;
    }


    public boolean isConnectAtStartup() {

        return _connectAtStartup;
    }


    public boolean isFiltered() {

        return ((_nameFilterExpression != null && _nameFilterExpression.trim().length() != 0)
                || (_schemaFilterExpression != null && _schemaFilterExpression.trim().length() != 0)
                || (_folderFilterExpression != null && _folderFilterExpression.trim().length() != 0));

    }


    public synchronized boolean isValid() {

        return _name.length() > 0 && _driverId != null && _url.length() > 0;
    }


    public void removePropertyChangeListener(PropertyChangeListener listener) {

        getPropertyChangeReporter().removePropertyChangeListener(listener);
    }


    public void setAutoLogon(boolean value) {

        if (_autoLogon != value) {
            _autoLogon = value;
            getPropertyChangeReporter().firePropertyChange("autoLogon", !_autoLogon, _autoLogon);
        }
    }


    public void setConnectAtStartup(boolean value) {

        if (_connectAtStartup != value) {
            _connectAtStartup = value;
            getPropertyChangeReporter().firePropertyChange("connectAtStartup", !_connectAtStartup, _connectAtStartup);
        }
    }


    public void setDriverIdentifier(IIdentifier data) throws ValidationException {

        if (data == null)
            throw new ValidationException(IStrings.ERR_BLANK_DRIVER);
        if (_driverId != data) {
            IIdentifier oldValue = _driverId;
            _driverId = data;
            getPropertyChangeReporter().firePropertyChange("driverIdentifier", oldValue, _driverId);
        }
    }


    public synchronized void setDriverProperties(SQLDriverPropertyCollection value) {

        _driverProps.clear();
        if (value != null)
            synchronized (value) {
                int count = value.size();
                SQLDriverProperty newar[] = new SQLDriverProperty[count];
                for (int i = 0; i < count; i++)
                    newar[i] = (SQLDriverProperty) value.getDriverProperty(i).clone();

                _driverProps.setDriverProperties(newar);
            }
    }


    public void setFolderFilterExpression(String expression) {

        String data = getString(expression);
        if (_folderFilterExpression != data) {
            String oldValue = _folderFilterExpression;
            _folderFilterExpression = data;
            getPropertyChangeReporter().firePropertyChange("folderFilterExpression", oldValue, _folderFilterExpression);
        }
    }


    public void setIdentifier(IIdentifier id) {

        _id = id;
    }


    public void setName(String name) throws ValidationException {

        String data = getString(name);
        if (data.length() == 0)
            throw new ValidationException(IStrings.ERR_BLANK_NAME);
        if (_name != data) {
            String oldValue = _name;
            _name = data;
            getPropertyChangeReporter().firePropertyChange("name", oldValue, _name);
        }
    }


    public void setNameFilterExpression(String expression) {

        String data = getString(expression);
        if (_nameFilterExpression != data) {
            String oldValue = _nameFilterExpression;
            _nameFilterExpression = data;
            getPropertyChangeReporter().firePropertyChange("nameFilterExpression", oldValue, _nameFilterExpression);
        }
    }


    public void setPassword(String password) {

        String data = getString(password);
        if (_password != data) {
            String oldValue = _password;
            _password = data;
            getPropertyChangeReporter().firePropertyChange("password", oldValue, _password);
        }
    }


    public void setSchemaFilterExpression(String expression) {

        String data = getString(expression);
        if (_schemaFilterExpression != data) {
            String oldValue = _schemaFilterExpression;
            _schemaFilterExpression = data;
            getPropertyChangeReporter().firePropertyChange("schemaFilterExpression", oldValue, _schemaFilterExpression);
        }
    }


    public void setUrl(String url) throws ValidationException {

        String data = getString(url);
        if (data.length() == 0)
            throw new ValidationException(IStrings.ERR_BLANK_URL);
        if (_url != data) {
            String oldValue = _url;
            _url = data;
            getPropertyChangeReporter().firePropertyChange("url", oldValue, _url);
        }
    }


    public void setUseDriverProperties(boolean value) {

        if (_useDriverProperties != value) {
            boolean oldValue = _useDriverProperties;
            _useDriverProperties = value;
            getPropertyChangeReporter().firePropertyChange("useDriverProperties", oldValue, _useDriverProperties);
        }
    }


    public void setUserName(String userName) {

        String data = getString(userName);
        if (_userName != data) {
            String oldValue = _userName;
            _userName = data;
            getPropertyChangeReporter().firePropertyChange("userName", oldValue, _userName);
        }
    }


    public String toString() {

        return getName();
    }

}
