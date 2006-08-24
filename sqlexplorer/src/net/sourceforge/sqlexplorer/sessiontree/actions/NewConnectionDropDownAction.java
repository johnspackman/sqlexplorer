/*
 * Created on Apr 11, 2004
 * 
 */
package net.sourceforge.sqlexplorer.sessiontree.actions;

import net.sourceforge.sqlexplorer.AliasModel;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.util.ImageUtil;
import net.sourceforge.squirrel_sql.fw.sql.ISQLAlias;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * @author Aadi
 * 
 */
public class NewConnectionDropDownAction extends Action implements IMenuCreator, IViewActionDelegate {
    private Menu menu;


    public NewConnectionDropDownAction() {
        setText(Messages.getString("ConnectionsView.Actions.NewConnection"));
        setToolTipText(Messages.getString("ConnectionsView.Actions.NewConnectionToolTip"));
        setImageDescriptor(ImageUtil.getDescriptor("Images.NewConnectionIcon"));
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
        
    }

    public void run(IAction action) {
        // noop
    }

    public void selectionChanged(IAction action, ISelection selection) {
        // noop
    }

}