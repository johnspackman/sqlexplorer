/*
 * Copyright (C) 2001 Colin Bell
 * colbell@users.sourceforge.net
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.util.URLUtil;
import net.sourceforge.squirrel_sql.fw.id.IIdentifier;
import net.sourceforge.squirrel_sql.fw.persist.ValidationException;
import net.sourceforge.squirrel_sql.fw.sql.ISQLAlias;
import net.sourceforge.squirrel_sql.fw.sql.ISQLDriver;
import net.sourceforge.squirrel_sql.fw.sql.SQLDriver;
import net.sourceforge.sqlexplorer.SQLDriverManager;
import net.sourceforge.squirrel_sql.fw.util.DuplicateObjectException;
import net.sourceforge.squirrel_sql.fw.util.IObjectCacheChangeListener;
import net.sourceforge.squirrel_sql.fw.xml.XMLException;
import net.sourceforge.squirrel_sql.fw.xml.XMLObjectCache;

/**
 * XML cache of JDBC drivers and aliases.
 * 
 * @author <A HREF="mailto:colbell@users.sourceforge.net">Colin Bell</A>
 */
public class DataCache {

    private final static Class SQL_ALIAS_IMPL = SQLAlias.class;

    private final static Class OLD_SQL_ALIAS_IMPL = net.sourceforge.squirrel_sql.fw.sql.SQLAlias.class;

    private final static Class SQL_DRIVER_IMPL = SQLDriver.class;

    private SQLDriverManager _driverMgr;

    /** Cache that contains data. */
    private XMLObjectCache _cache = new XMLObjectCache();


    /**
     * Ctor. Loads drivers and aliases from the XML document.
     * 
     * @param app Application API.
     * 
     * @throws IllegalArgumentException Thrown if <TT>null</TT> <TT>IApplication</TT>
     *             passed.
     * 
     * @throws IllegalStateException Thrown if no <TT>SQLDriverManager</TT> or
     *             <TT>Logger</TT> exists in IApplication.
     */
    public DataCache(SQLDriverManager dm) throws IllegalArgumentException {
        super();
        _driverMgr = dm;

        loadDrivers();
        loadAliases();

        migrateAliases();
    }


    /**
     * Save cached objects. JDBC drivers are saved to <CODE>ApplicationFiles.getUserDriversFileName()</CODE>
     * and aliases are saved to <CODE>ApplicationFiles.getUserAliasesFileName()</CODE>.
     */
    public void save() {
        try {
            _cache.saveAllForClass(ApplicationFiles.USER_DRIVER_FILE_NAME, SQL_DRIVER_IMPL);
        } catch (IOException ex) {
            SQLExplorerPlugin.error("Error occured saving drivers", ex);
        } catch (XMLException ex) {
            SQLExplorerPlugin.error("Error occured saving drivers", ex);

        }
        try {
            _cache.saveAllForClass(ApplicationFiles.USER_ALIAS_FILE_NAME, SQL_ALIAS_IMPL);
        } catch (Exception ex) {
            SQLExplorerPlugin.error("Error occured saving aliases", ex);
        }
    }


    /**
     * Return the <TT>ISQLDriver</TT> for the passed identifier.
     */
    public ISQLDriver getDriver(IIdentifier id) {
        return (ISQLDriver) _cache.get(SQL_DRIVER_IMPL, id);
    }


    public void addDriver(ISQLDriver sqlDriver) throws ClassNotFoundException, IllegalAccessException, InstantiationException,
            DuplicateObjectException, MalformedURLException {
        _driverMgr.registerSQLDriver(sqlDriver);
        _cache.add(sqlDriver);
    }


    public void removeDriver(ISQLDriver sqlDriver) {
        _cache.remove(SQL_DRIVER_IMPL, sqlDriver.getIdentifier());
        try {
            _driverMgr.unregisterSQLDriver(sqlDriver);
        } catch (Exception ex) {
            SQLExplorerPlugin.error("Error occured removing driver", ex);
        }
    }


    public Iterator drivers() {
        return _cache.getAllForClass(SQL_DRIVER_IMPL);
    }


    public void addDriversListener(IObjectCacheChangeListener lis) {
        _cache.addChangesListener(lis, SQL_DRIVER_IMPL);
    }


    public void removeDriversListener(IObjectCacheChangeListener lis) {
        _cache.removeChangesListener(lis, SQL_DRIVER_IMPL);
    }


    public ISQLAlias getAlias(IIdentifier id) {
        return (ISQLAlias) _cache.get(SQL_ALIAS_IMPL, id);
    }


    public Iterator aliases() {
        return _cache.getAllForClass(SQL_ALIAS_IMPL);
    }


    public void addAlias(ISQLAlias alias) throws DuplicateObjectException {
        _cache.add(alias);
    }


    public void removeAlias(ISQLAlias alias) {
        _cache.remove(SQL_ALIAS_IMPL, alias.getIdentifier());
    }


    public Iterator getAliasesForDriver(ISQLDriver driver) {
        ArrayList data = new ArrayList();
        for (Iterator it = aliases(); it.hasNext();) {
            ISQLAlias alias = (ISQLAlias) it.next();
            if (driver.equals(getDriver(alias.getDriverIdentifier()))) {
                data.add(alias);
            }
        }
        return data.iterator();
    }


    public void addAliasesListener(IObjectCacheChangeListener lis) {
        _cache.addChangesListener(lis, SQL_ALIAS_IMPL);
    }


    public void removeAliasesListener(IObjectCacheChangeListener lis) {
        _cache.removeChangesListener(lis, SQL_ALIAS_IMPL);
    }


    private void loadDrivers() {
        try {
            _cache.load(ApplicationFiles.USER_DRIVER_FILE_NAME);
            if (!drivers().hasNext()) {
                loadDefaultDrivers();
            } else {
                fixupDrivers();
            }
        } catch (FileNotFoundException ex) {
            loadDefaultDrivers();// first time user has run pgm.
        } catch (Exception ex) {
            loadDefaultDrivers();
        }

        registerDrivers();
    }


    public ISQLAlias createAlias(IIdentifier id) {
        return new SQLAlias(id);
    }


    public ISQLDriver createDriver(IIdentifier id) {
        return new SQLDriver(id);
    }


    private void fixupDrivers() {
        for (Iterator it = drivers(); it.hasNext();) {
            ISQLDriver driver = (ISQLDriver) it.next();
            String[] fileNames = driver.getJarFileNames();
            if (fileNames == null || fileNames.length == 0) {
                String fileNameArray[] = driver.getJarFileNames();
                if (fileNameArray != null && fileNameArray.length > 0) {
                    driver.setJarFileNames(fileNameArray);
                    try {
                        driver.setJarFileName(null);
                    } catch (ValidationException ignore) {
                    }
                }
            }
        }
    }


    private void loadDefaultDrivers() {
        final URL url = URLUtil.getResourceURL("default_drivers.xml");
        try {
            InputStreamReader isr = new InputStreamReader(url.openStream());
            try {
                _cache.load(isr);
            } finally {
                isr.close();
            }
        } catch (Exception ex) {
            SQLExplorerPlugin.error("Error loading default driver file", ex);
        }

    }


    /**
     * Restore any missing drivers.
     */
    public void restoreDefaultDrivers() {

        XMLObjectCache tmpCache = new XMLObjectCache();
        URL url = URLUtil.getResourceURL("default_drivers.xml");

        try {
            InputStreamReader isr = new InputStreamReader(url.openStream());
            try {
                tmpCache.load(isr);
            } finally {
                isr.close();
            }
        } catch (Exception ex) {
            SQLExplorerPlugin.error("Error loading default driver file", ex);
        }

        Iterator it = tmpCache.getAllForClass(SQL_DRIVER_IMPL);

        while (it.hasNext()) {
            ISQLDriver driver = (ISQLDriver) it.next();
            if (driver != null) {
                try {
                    _cache.add(driver);
                    _driverMgr.registerSQLDriver(driver);
                } catch (Exception e) {
                    SQLExplorerPlugin.error("Error restoring default driver: " + driver.getName(), e);
                }
            }
        }
    }


    private void registerDrivers() {
        SQLDriverManager driverMgr = _driverMgr;
        for (Iterator it = drivers(); it.hasNext();) {
            ISQLDriver sqlDriver = (ISQLDriver) it.next();
            try {
                driverMgr.registerSQLDriver(sqlDriver);
            } catch (Throwable th) {
            }
        }
    }


    private void loadAliases() {
        try {
            _cache.load(ApplicationFiles.USER_ALIAS_FILE_NAME);
        } catch (FileNotFoundException ignore) { // first time user has run
                                                    // pgm.
        } catch (XMLException ex) {
            SQLExplorerPlugin.error("Error loading aliases file ", ex);
        } catch (DuplicateObjectException ex) {
            SQLExplorerPlugin.error("Error loading aliases file ", ex);
        }
    }


    /**
     * Convert old style aliases to the new explorer Aliases.
     */
    private void migrateAliases() {

        try {

            Iterator it = _cache.getAllForClass(OLD_SQL_ALIAS_IMPL);
            while (it.hasNext()) {
                ISQLAlias oldAlias = (ISQLAlias) it.next();
                if (oldAlias != null) {
                    SQLAlias newAlias = new SQLAlias(oldAlias.getIdentifier());
                    newAlias.assignFrom(oldAlias);
                    _cache.add(newAlias);
                }
            }
            _cache.saveAllForClass(ApplicationFiles.USER_ALIAS_FILE_NAME, SQL_ALIAS_IMPL);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            SQLExplorerPlugin.error("Error migrating aliases.", e);
        }

    }
}
