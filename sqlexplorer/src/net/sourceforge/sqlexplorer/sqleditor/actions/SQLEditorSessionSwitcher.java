package net.sourceforge.sqlexplorer.sqleditor.actions;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


public class SQLEditorSessionSwitcher extends ControlContribution {

    
    private SQLEditor _editor;
    
    private Combo _sessionCombo;
    
    /**
     * @param editor SQLEditor to which this session switcher belongs
     */
    public SQLEditorSessionSwitcher(SQLEditor editor) {
        
        super("net.sourceforge.sqlexplorer.sessionswitcher");
        
        _editor = editor;
        
    }
    
    protected Control createControl(Composite parent) {

        _sessionCombo = new Combo(parent, SWT.READ_ONLY);
        _sessionCombo.setToolTipText(Messages.getString("SQLEditor.Actions.ChooseSession.ToolTip"));
        
        _sessionCombo.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {

                // change session for this editor
                SessionTreeNode sessionTreeNode = null;
                int selIndex = _sessionCombo.getSelectionIndex();
                if (selIndex != 0) {                                        
                    sessionTreeNode = SQLExplorerPlugin.getDefault().stm.getRoot().getSessionTreeNodes()[selIndex - 1];
                }
                _editor.setSessionTreeNode(sessionTreeNode);
                _editor.getEditorToolBar().refresh(true);
            }
        });
        
        setSessionOptions();

        
        return _sessionCombo;
    }

    
    private void setSessionOptions() {
               
        SessionTreeNode[] sessionNodes = SQLExplorerPlugin.getDefault().stm.getRoot().getSessionTreeNodes();

        _sessionCombo.removeAll();
        _sessionCombo.add("");
        
        for (int i = 0; i < sessionNodes.length; i++) {
            _sessionCombo.add(sessionNodes[i].toString());
            if (_editor.getSessionTreeNode() == sessionNodes[i]) {
                _sessionCombo.select(_sessionCombo.getItemCount() - 1);
            }
        }
                
    }
    
    
    public void refresh() {
        
        setSessionOptions();
    }
}
