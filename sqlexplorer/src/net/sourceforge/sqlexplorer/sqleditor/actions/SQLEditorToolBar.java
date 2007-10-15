package net.sourceforge.sqlexplorer.sqleditor.actions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.util.ImageUtil;
import net.sourceforge.sqlexplorer.util.TextUtil;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;

/**
 * SQLEditorToolBar controls the toolbar displayed in the editor.
 * @modified John Spackman
 */
public class SQLEditorToolBar {
	
    private SQLEditorCatalogSwitcher _catalogSwitcher;

    private ToolBarManager _catalogToolBarMgr;

//    private AbstractEditorAction _clearTextAction;

//    private AbstractEditorAction _commitAction;

    private CoolBar _coolBar;

    private CoolBarManager _coolBarMgr;

    private ToolBarManager _defaultToolBarMgr;

    private SQLEditor _editor;

//    private AbstractEditorAction _execSQLAction;

    private ToolBarManager _extensionToolBarMgr;

//    private AbstractEditorAction _openFileAction;

//    private AbstractEditorAction _rollbackAction;

//    private AbstractEditorAction _saveAsAction;

    // Drop down to switch sessions
    private SQLEditorSessionSwitcher _sessionSwitcher;
    
    // Whether to limit rows and by how much
    private SQLLimitRowsControl _limitRows;
    
    private ToolBarManager _sessionToolBarMgr;
    
    private LinkedList<AbstractEditorAction> actions = new LinkedList<AbstractEditorAction>();


    /**
     * Create a new toolbar on the given composite.
     * 
     * @param parent composite to draw toolbar on.
     * @param editor parent editor for this toolbar.
     */
    public SQLEditorToolBar(Composite parent, SQLEditor editor) {

        _editor = editor;

        // create coolbar

        _coolBar = new CoolBar(parent, SWT.FLAT);
        _coolBarMgr = new CoolBarManager(_coolBar);

        GridData gid = new GridData();
        gid.horizontalAlignment = GridData.FILL;
        _coolBar.setLayoutData(gid);

        // initialize default actions

        _defaultToolBarMgr = new ToolBarManager(SWT.FLAT);

        actions.add(new ExecSQLAction(_editor));
        actions.add(new CommitAction(_editor));
        actions.add(new RollbackAction(_editor));
        actions.add(new OpenFileAction(_editor));
        actions.add(new SaveFileAsAction(_editor));
        actions.add(new ClearTextAction(_editor));
        actions.add(new OptionsDropDownAction(_editor, parent));
        
        addDefaultActions(_defaultToolBarMgr);

        // initialize extension actions

        _extensionToolBarMgr = new ToolBarManager(SWT.FLAT);
        createExtensionActions(_extensionToolBarMgr);

        // initialize session actions

        _sessionToolBarMgr = new ToolBarManager(SWT.FLAT);

        _sessionSwitcher = new SQLEditorSessionSwitcher(editor);
        _sessionToolBarMgr.add(_sessionSwitcher);
        
        _limitRows = new SQLLimitRowsControl(editor);
        _sessionToolBarMgr.add(_limitRows);

        // initialize catalog actions

        _catalogToolBarMgr = new ToolBarManager(SWT.FLAT);
        if (_editor.getSession() != null && _editor.getSession().supportsCatalogs()) {
            _catalogSwitcher = new SQLEditorCatalogSwitcher(editor);
            _catalogToolBarMgr.add(_catalogSwitcher);
        }

        // add all toolbars to parent coolbar

        _coolBarMgr.add(new ToolBarContributionItem(_defaultToolBarMgr));
        _coolBarMgr.add(new ToolBarContributionItem(_extensionToolBarMgr));
        _coolBarMgr.add(new ToolBarContributionItem(_sessionToolBarMgr));
        _coolBarMgr.add(new ToolBarContributionItem(_catalogToolBarMgr));

        _coolBarMgr.update(true);

    }

    public void addResizeListener(ControlListener listener) {
        _coolBar.addControlListener(listener);
    }


    private void createExtensionActions(ToolBarManager mgr) {

        mgr.removeAll();

        IAction[] toolActions = getEditorActions();
        if (toolActions != null) {
            for (int i = 0; i < toolActions.length; i++) {
                mgr.add(toolActions[i]);
            }
        }

    }

    private void addDefaultActions(ToolBarManager mgr) {
        mgr.removeAll();
        
        for (AbstractEditorAction action : actions) {
        	action.setEnabled(!action.isDisabled());
        	mgr.add(action);
        }
    }
    
    
    /**
     * Loop through all extensions and add the appropriate actions.
     * 
     * Actions are selected by database product name
     * 
     * @param nodes currently selected nodes
     * @return array of actions
     */
    private IAction[] getEditorActions() {

        Session tree = _editor.getSession();
        if (tree == null) {
            return null;
        }
        String databaseProductName = tree.getRoot().getDatabaseProductName().toLowerCase().trim();
        List actions = new ArrayList();

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint("net.sourceforge.sqlexplorer", "editorAction");
        IExtension[] extensions = point.getExtensions();

        for (int i = 0; i < extensions.length; i++) {

            IExtension e = extensions[i];

            IConfigurationElement[] ces = e.getConfigurationElements();

            for (int j = 0; j < ces.length; j++) {
                try {

                    boolean isValidProduct = false;

                    String[] validProducts = ces[j].getAttribute("database-product-name").split(",");
                    String imagePath = ces[j].getAttribute("icon");
                    String id = ces[j].getAttribute("id");
                    
                    // check if action is valid for current database product
                    for (int k = 0; k < validProducts.length; k++) {

                        String product = validProducts[k].toLowerCase().trim();

                        if (product.length() == 0) {
                            continue;
                        }

                        if (product.equals("*")) {
                            isValidProduct = true;
                            break;
                        }

                        String regex = TextUtil.replaceChar(product, '*', ".*");
                        if (databaseProductName.matches(regex)) {
                            isValidProduct = true;
                            break;
                        }

                    }

                    if (!isValidProduct) {
                        continue;
                    }

                    AbstractEditorAction action = (AbstractEditorAction) ces[j].createExecutableExtension("class");
                    action.setEditor(_editor);
                    
                    String fragmentId = id.substring(0, id.indexOf('.', 28));
                    if (imagePath != null && imagePath.trim().length() != 0) {
                        action.setImageDescriptor(ImageUtil.getFragmentDescriptor(fragmentId, imagePath));
                    }
                    
                    actions.add(action);

                } catch (Throwable ex) {
                    SQLExplorerPlugin.error("Could not create editor action", ex);
                }
            }
        }

        return (IAction[]) actions.toArray(new IAction[] {});
    }


    /**
     * Refresh actions availability on the toolbar.
     */
    public void refresh(final boolean sessionChanged) {

    	if (_editor.getSite() != null && _editor.getSite().getShell() != null && _editor.getSite().getShell().getDisplay() != null)
	        _editor.getSite().getShell().getDisplay().asyncExec(new Runnable() {
	
	            public void run() {
	
	            	if (_coolBar.isDisposed())
	            		return;
	            	
	                if (sessionChanged) {
	
	                    // reset actions
	                    addDefaultActions(_defaultToolBarMgr);
	                    _defaultToolBarMgr.update(true);
	                    
	                    // rebuild extension toolbar
	                    createExtensionActions(_extensionToolBarMgr);
	                    _extensionToolBarMgr.update(true);
	                }
	
	                // update session toolbar
	                _sessionToolBarMgr.update(true);
	
	                // update catalog toolbar
	                _catalogToolBarMgr.removeAll();
	                if (_editor.getSession() != null && _editor.getSession().supportsCatalogs()) {
	                    _catalogSwitcher = new SQLEditorCatalogSwitcher(_editor);
	                    _catalogToolBarMgr.add(_catalogSwitcher);
	                }
	
	                _coolBarMgr.update(true);
	                _coolBar.update();
	            }
	        });
    }


	/**
	 * Returns the control
	 * @return the _coolBar
	 */
	public CoolBar getToolbarControl() {
		return _coolBar;
	}

	/**
	 * Returns whether to limit the results and if so by how much.
	 * 
	 * @return the maximum number of rows to retrieve, 0 for unlimited, or null
	 *         if it cannot be interpretted
	 */
	public Integer getLimitResults() {
		return _limitRows.getLimitResults();
	}
}
