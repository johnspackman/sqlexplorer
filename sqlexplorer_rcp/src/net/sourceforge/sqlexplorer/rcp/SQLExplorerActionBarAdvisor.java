package net.sourceforge.sqlexplorer.rcp;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;


/**
 * ActionBarAdvisor. Creates default menu options like File > exit, etc.
 * 
 * @author Davy Vanherbergen
 */
public class SQLExplorerActionBarAdvisor extends ActionBarAdvisor {
	
	private IWorkbenchAction _exitAction;
	private IWorkbenchAction _saveAction;
	private IWorkbenchAction _saveAsAction;

    private IWorkbenchAction _preferencesAction;
    
    private IContributionItem _viewList;
    
    
    /**
     * Default constructor.
     */
    public SQLExplorerActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

    
	/**
     * Creates the actions and registers them.
     * Registering is needed to ensure that key bindings work.
     * 
	 * @see org.eclipse.ui.application.ActionBarAdvisor#makeActions(org.eclipse.ui.IWorkbenchWindow)
	 */
	protected void makeActions(final IWorkbenchWindow window) {

		register(ActionFactory.QUIT.create(window));

		register(ActionFactory.SAVE.create(window));               
		register(ActionFactory.SAVE_AS.create(window));               
		register(ActionFactory.SAVE_ALL.create(window));               

        _viewList = ContributionItemFactory.VIEWS_SHORTLIST.create(window);
        
        register(ActionFactory.PREFERENCES.create(window));
        
      
	}


	/**
     * Populate the menubar with actions.
     * 
	 * @see org.eclipse.ui.application.ActionBarAdvisor#fillMenuBar(org.eclipse.jface.action.IMenuManager)
	 */
	protected void fillMenuBar(IMenuManager menuBar) {

        // create menus
        MenuManager fileMenu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);
        MenuManager editMenu = new MenuManager("&Edit", IWorkbenchActionConstants.M_EDIT);
        MenuManager navigateMenu = new MenuManager("&Navigate", IWorkbenchActionConstants.M_NAVIGATE);
        MenuManager viewMenu = new MenuManager("&View", "sqlexplorer.View");        
        MenuManager helpMenu = new MenuManager("&Help", "help");
               
        // create file menu
        menuBar.add(fileMenu);
        fileMenu.add(getAction(ActionFactory.PREFERENCES.getId()));
        fileMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        fileMenu.add(new Separator(IWorkbenchActionConstants.FILE_START));
        fileMenu.add(getAction(ActionFactory.SAVE.getId()));
        fileMenu.add(getAction(ActionFactory.SAVE_AS.getId()));
        fileMenu.add(getAction(ActionFactory.SAVE_ALL.getId()));
        fileMenu.add(new Separator(IWorkbenchActionConstants.FILE_END));
        fileMenu.add(getAction(ActionFactory.QUIT.getId()));        
        fileMenu.add(new Separator());
        
        // create edit menu
        menuBar.add(editMenu);
        editMenu.setVisible(false);
                
        // navigate menu is used by text editor        
        menuBar.add(navigateMenu);
        navigateMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        navigateMenu.setVisible(false);      
        
        // create view menu
        menuBar.add(viewMenu);       
        viewMenu.add(_viewList);        
        viewMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        
        // create help menu
		menuBar.add(helpMenu);

	}

}
