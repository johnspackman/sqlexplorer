/*
 * Created on Apr 11, 2004
 * 
 * TODO To change the template for this generated file go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
package net.sourceforge.sqlexplorer.sessiontree.actions;

import net.sourceforge.sqlexplorer.AliasModel;
import net.sourceforge.sqlexplorer.SqlexplorerImages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.squirrel_sql.fw.sql.ISQLAlias;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * @author Aadi
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class NewConnectionDropDownAction extends Action implements IMenuCreator, IViewActionDelegate {
    private Menu menu;

    private IViewPart view;

    public NewConnectionDropDownAction() {
        setText("Open New Connection");
        setToolTipText("Open New Connection");
        setImageDescriptor(ImageDescriptor.createFromURL(SqlexplorerImages.getCreateDriverIcon()));
        setMenuCreator(this);
    }

    public void dispose() {
        if (menu != null) {
            menu.dispose();
        }
    }

    public Menu getMenu(Control parent) {
        if (menu != null) {
            menu.dispose();
            menu = null;
        }

        AliasModel aliasModel = SQLExplorerPlugin.getDefault().getAliasModel();
        Object[] aliases = aliasModel.getElements();
        if (aliases != null) {
            menu = new Menu(parent);
            for (int i = 0; i < aliases.length; i++) {
                NewConnection action = new NewConnection((ISQLAlias) aliases[i]);
                addActionToMenu(menu, action);
            }
        }
        return menu;
    }

    public Menu getMenu(Menu parent) {
        return null;
    }

    protected void addActionToMenu(Menu parent, Action action) {
        ActionContributionItem item = new ActionContributionItem(action);
        item.fill(parent, -1);
    }

    public void init(IViewPart view) {
        this.view = view;
    }

    public void run(IAction action) {
        System.out.println("This rocks");
    }

    public void selectionChanged(IAction action, ISelection selection) {
        // TODO Auto-generated method stub
    }

}