package net.sourceforge.sqlexplorer.rcp;

import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.ide.application.IDEWorkbenchAdvisor;


/**
 * WorkbenchAdvisor.  Controls the workbench layout.
 * IDEWorkbenchAdvisor must be used to initialize IDE features properly like projects
 * used in this application.
 * 
 * TODO check if this is required with 3.4 or above too
 * 
 * @author Davy Vanherbergen
 *
 */
public class SQLExplorerWorkbenchAdvisor extends IDEWorkbenchAdvisor {
   
	
    /**
     * Get unique id for our sql explorer perspective.
     * This should match a perspective defined in the plugin.xml
     * 
     * @see org.eclipse.ui.application.WorkbenchAdvisor#getInitialWindowPerspectiveId()
     */
    public String getInitialWindowPerspectiveId() {
        return "net.sourceforge.sqlexplorer.plugin.perspectives.SQLExplorerPluginPerspective";
    }

    
    /**
     * Instantiate our own window advisor.
     * 
     * @see org.eclipse.ui.application.WorkbenchAdvisor#createWorkbenchWindowAdvisor(org.eclipse.ui.application.IWorkbenchWindowConfigurer)
     */
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {

        return new SQLExplorerWorkbenchWindowAdvisor(configurer);
    }
        
    
}
