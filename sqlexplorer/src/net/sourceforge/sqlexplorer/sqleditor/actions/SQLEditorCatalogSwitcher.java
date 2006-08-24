package net.sourceforge.sqlexplorer.sqleditor.actions;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


public class SQLEditorCatalogSwitcher extends ControlContribution {

    
    private SQLEditor _editor;
    
    private Combo _catalogCombo;

    
    /**
     * @param editor SQLEditor to which this catalog switcher belongs
     */
    public SQLEditorCatalogSwitcher(SQLEditor editor) {
        
        super("net.sourceforge.sqlexplorer.catalogswitcher");
        
        _editor = editor;
        
    }
    
    protected Control createControl(Composite parent) {

        _catalogCombo = new Combo(parent, SWT.READ_ONLY);
        _catalogCombo.setToolTipText(Messages.getString("SQLEditor.Actions.ChooseCatalog.ToolTip"));
        _catalogCombo.setSize(200, _catalogCombo.getSize().y);
        
        _catalogCombo.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent arg0) {
                
                int selIndex = _catalogCombo.getSelectionIndex();
                String newCat = _catalogCombo.getItem(selIndex);
                if (_editor.getSessionTreeNode() != null) {
                    try {
                        _editor.getSessionTreeNode().setCatalog(newCat);
                    } catch (Exception e1) {
                        SQLExplorerPlugin.error("Error changing catalog", e1);
                    }
                }
            }
        });
        
        _catalogCombo.add("");
        
        
        if (_editor.getSessionTreeNode() != null && _editor.getSessionTreeNode().supportsCatalogs()) {
                       
            String catalogs[] = _editor.getSessionTreeNode().getRoot().getChildNames();
            String currentCatalog = _editor.getSessionTreeNode().getCatalog();
            
            for (int i = 0; i < catalogs.length; i++) {
                _catalogCombo.add(catalogs[i]);
                if (currentCatalog.equals(catalogs[i])) {
                    _catalogCombo.select(_catalogCombo.getItemCount() - 1);
                }
            }
            
        } 
        
        return _catalogCombo;
    }

    
}
