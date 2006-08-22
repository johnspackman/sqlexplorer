/**
 * 
 */
package net.sourceforge.sqlexplorer.dbdetail.tab;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sqleditor.SQLTextViewer;

import org.eclipse.jface.text.Document;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;


/**
 * @author k709335
 *
 */
public abstract class AbstractSourceTab extends AbstractTab {

    private String _source = null;
    
    private SQLTextViewer _viewer = null;
    
    public final void fillDetailComposite(Composite composite) {

        if (_source == null) {
            _source = getSource();
        }
        
        _viewer = new SQLTextViewer(composite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.FULL_SELECTION, 
                SQLExplorerPlugin.getDefault().getPreferenceStore(), null);
        _viewer.setDocument(new Document(_source));
        _viewer.refresh();
        _viewer.getTextWidget().setWordWrap(true);
        _viewer.setEditable(false);
        
    }

    public String getLabelText() {
        return Messages.getString("DatabaseDetailView.Tab.Source");
    }

    public abstract String getSource();
    
    
    public final void refresh() {
       _source = null;
    }
    
    public String getStatusMessage() {
        return Messages.getString("DatabaseDetailView.Tab.SourceFor") + " " + getNode().getQualifiedName();
    }

}
