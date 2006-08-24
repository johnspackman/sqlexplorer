/*
 * Copyright (C) 2006 Davy Vanherbergen
 * dvanherbergen@users.sourceforge.net
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
package net.sourceforge.sqlexplorer.sessiontree.model.utility;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;


public class DictionaryLoader extends Job {

    private SessionTreeNode _sessionNode;
    
    private static final String ID = "net.sourceforge.sqlexplorer";
    
    /**
     * Hidden constructor.
     */
    private DictionaryLoader() {
        super(null);
    }
    
    /**
     * Default constructor,
     */
    public DictionaryLoader(SessionTreeNode sessionNode) {
        super(Messages.getString("Progress.Dictionary.Title"));
        _sessionNode = sessionNode;
    }
    
    /**
     * Load dictionary in background process.
     * 
     */
    protected IStatus run(IProgressMonitor monitor) {
        
        Dictionary dictionary = _sessionNode.getDictionary();
        
        
        // check if we can persisted dictionary 
        monitor.setTaskName(Messages.getString("Progress.Dictionary.Scanning"));

        
        try {
        
            boolean isLoaded = dictionary.restore(_sessionNode.getRoot(), monitor);
    
            if (!isLoaded) {           

                // load full dictionary
                dictionary.load(_sessionNode.getRoot(), monitor);
                monitor.done();
            }
            
        } catch (InterruptedException ie) {
            return new Status(IStatus.CANCEL, ID, IStatus.CANCEL, Messages.getString("Progress.Dictionary.Cancelled"), null);
            
        } catch (Exception e) {            
            return new Status(IStatus.ERROR, ID, IStatus.CANCEL, Messages.getString("Progress.Dictionary.Error"), e);
            
        } finally {
            monitor.done();
        }
        
        // everything ended ok..
        return new Status(IStatus.OK, ID, IStatus.OK, "tested ok ", null);
    }

}
