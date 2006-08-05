package net.sourceforge.sqlexplorer.history.actions;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.SqlexplorerImages;
import net.sourceforge.sqlexplorer.history.SQLHistoryElement;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.TableItem;

public class RemoveFromHistoryAction extends AbstractHistoryContextAction {

    private ImageDescriptor _imageRemove = ImageDescriptor.createFromURL(SqlexplorerImages.getRemoveIcon());


    public ImageDescriptor getImageDescriptor() {

        return _imageRemove;
    }


    public String getText() {

        return Messages.getString("SQLHistoryView.RemoveFromHistory");
    }


    public void run() {

        try {
            TableItem[] selections = _table.getSelection();
            if (selections != null && selections.length != 0) {
                for (int i = 0; i < selections.length; i++) {
                    _history.remove((SQLHistoryElement) selections[i].getData());
                }
            }
            _table.deselectAll();

        } catch (Throwable e) {
            SQLExplorerPlugin.error("Error removing item from clipboard", e);
        }
    }
}
