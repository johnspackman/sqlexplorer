package net.sourceforge.sqlexplorer.rcp;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.NewWizardMenu;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;


/**
 * ActionBarAdvisor. Creates default menu options like File > exit, etc.
 * 
 * @author Davy Vanherbergen
 */
public class SQLExplorerActionBarAdvisor extends ActionBarAdvisor 
{
	    
    private IContributionItem _viewList;
	private NewWizardMenu newWizardMenu;
	private IWorkbenchWindow window;
    
    
    /**
     * Default constructor.
     */
    public SQLExplorerActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
		window = configurer.getWindowConfigurer().getWindow();		
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

		register(ActionFactory.HELP_CONTENTS.create(window));               
		
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
        MenuManager helpMenu = new MenuManager("&Help", IWorkbenchActionConstants.M_HELP);
               
        // create file menu
        menuBar.add(fileMenu);
        fileMenu.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));
        
        // create the New submenu, using the same id for it as the New action
        String newText = "New";
        String newId = ActionFactory.NEW.getId();
        MenuManager newMenu = new MenuManager(newText, newId); 
        newMenu.add(new Separator(newId));
        this.newWizardMenu = new NewWizardMenu(getWindow());
        newMenu.add(this.newWizardMenu);
        newMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));            
        fileMenu.add(newMenu);
    
        fileMenu.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
        fileMenu.add(new Separator());
        fileMenu.add(getAction(ActionFactory.SAVE.getId()));
        fileMenu.add(getAction(ActionFactory.SAVE_AS.getId()));
        fileMenu.add(getAction(ActionFactory.SAVE_ALL.getId()));
        fileMenu.add(new Separator());
        fileMenu.add(getAction(ActionFactory.PREFERENCES.getId()));
        fileMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        fileMenu.add(getAction(ActionFactory.QUIT.getId()));        
        fileMenu.add(new GroupMarker(IWorkbenchActionConstants.FILE_END));
        
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
		helpMenu.add(new Separator(IWorkbenchActionConstants.HELP_START));
		helpMenu.add(getAction(ActionFactory.HELP_CONTENTS.getId()));
		helpMenu.add(new Separator(IWorkbenchActionConstants.HELP_END));
		helpMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

		// create invisible window menu
        MenuManager winMenu = new MenuManager("&Window", IWorkbenchActionConstants.M_WINDOW);
		menuBar.add(winMenu);
	}


	private IWorkbenchWindow getWindow() {
		return window;
	}

}
