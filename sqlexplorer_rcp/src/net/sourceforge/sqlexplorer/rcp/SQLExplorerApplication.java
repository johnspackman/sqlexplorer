package net.sourceforge.sqlexplorer.rcp;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;


/**
 * Main entry point used to run SQL Explorer as standalone client.
 * 
 * @author Davy Vanherbergen
 */
public class SQLExplorerApplication implements IApplication {


	/*
	  * (non-Javadoc)
	  * 
	  * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	  */
	
	public Object start(IApplicationContext context) throws Exception 
	{
		Display display = PlatformUI.createDisplay();
		try 
		{
			int returnCode = PlatformUI.createAndRunWorkbench(display, new SQLExplorerWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART) 
			{
				return IApplication.EXIT_RESTART;
			}
			return IApplication.EXIT_OK;
		} 
		finally 
		{
			display.dispose();
		}
	}

	 /*
	  * (non-Javadoc)
	  * 
	  * @see org.eclipse.equinox.app.IApplication#stop()
	  */
	public void stop() 
	{
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
		{
			return;
		}
		
		final Display display = workbench.getDisplay();
		if(display != null)
		{
			display.syncExec(new Runnable() 
			{
				public void run() 
				{
					if (!display.isDisposed())
						workbench.close();
				}
			});
		}
	}	

}
