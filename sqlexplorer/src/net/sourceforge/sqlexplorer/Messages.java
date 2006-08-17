package net.sourceforge.sqlexplorer;

/*
 * Copyright (C) 2002-2004 Andrea Mazzolini
 * andreamazzolini@users.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 * This class manages the string bundle
 */
public class Messages {

    private static final String BUNDLE_NAME = ".text";

    private static ResourceBundle[] resources = null;
    
    private Messages() {
    }


    public static String getString(String key) {
        
        if (resources == null) {
            
            // initialize resources
            
            Bundle mainPlugin = SQLExplorerPlugin.getDefault().getBundle();
            Bundle[] fragments = Platform.getFragments(mainPlugin);
            
            if (fragments == null) {
                fragments = new Bundle[0];
            }
            
            resources = new ResourceBundle[fragments.length + 1];
            
            resources[0] = ResourceBundle.getBundle(mainPlugin.getSymbolicName() + BUNDLE_NAME);
            
            for (int i = 0; i < fragments.length; i++) {            
                try {
                    resources[i + 1] = ResourceBundle.getBundle(fragments[i].getSymbolicName() + BUNDLE_NAME);
                } catch (Exception e) {
                    SQLExplorerPlugin.error("No text.properties found for: " + fragments[i].getBundleId(), e);
                }
            }
        }
        
        for (int i = 0; i < resources.length; i++) {
            
            try {
                return resources[i].getString(key);
            } catch (MissingResourceException e) {
                // noop
            }            
        }
        
        return '!' + key + '!';
    }
    
    
}
