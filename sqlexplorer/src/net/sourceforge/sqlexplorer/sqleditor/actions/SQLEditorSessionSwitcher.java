package net.sourceforge.sqlexplorer.sqleditor.actions;

import java.sql.SQLException;
import java.util.HashMap;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.Alias;
import net.sourceforge.sqlexplorer.dbproduct.ConnectionListener;
import net.sourceforge.sqlexplorer.dbproduct.User;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


public class SQLEditorSessionSwitcher extends ControlContribution implements ConnectionListener {

    
    private SQLEditor _editor;
    
    private Combo _sessionCombo;
    private HashMap<Integer, User> sessionIndexes = new HashMap<Integer, User>();
    
    /**
     * @param editor SQLEditor to which this session switcher belongs
     */
    public SQLEditorSessionSwitcher(SQLEditor editor) {
        super("net.sourceforge.sqlexplorer.sessionswitcher");
        _editor = editor;
    }
    
    protected Control createControl(Composite parent) {
    	SQLExplorerPlugin.getDefault().getAliasManager().addListener(this);
    	
        _sessionCombo = new Combo(parent, SWT.READ_ONLY);
        _sessionCombo.setToolTipText(Messages.getString("SQLEditor.Actions.ChooseSession.ToolTip"));
        
        _sessionCombo.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent event) {

                // change session for this editor
                User user = null;
                int selIndex = _sessionCombo.getSelectionIndex();
                if (selIndex != 0)
                	user = sessionIndexes.get(selIndex - 1);
                if (user != null) {
                	if (_editor.getSession() == null || _editor.getSession().getUser() != user)
                		try {
                			_editor.setSession(user.createSession());
                		}catch(SQLException e) {
                			SQLExplorerPlugin.error(e.getMessage(), e);
                		}
                } else
                	_editor.setSession(null);
                _editor.getEditorToolBar().refresh(true);
            }
        });
        setSessionOptions();

        return _sessionCombo;
    }
    
    @Override
	public void dispose() {
    	SQLExplorerPlugin.getDefault().getAliasManager().removeListener(this);
		super.dispose();
	}

	private void setSessionOptions() {
    	if (_sessionCombo.isDisposed())
    		return;
        _sessionCombo.removeAll();
        _sessionCombo.add("");
        
        int index = 0;
        User currentUser = null;
        if (_editor.getSession() != null)
        	currentUser = _editor.getSession().getUser();
    	for (Alias alias : SQLExplorerPlugin.getDefault().getAliasManager().getAliases())
    		for (User user : alias.getUsers()) {
    			_sessionCombo.add(user.getDescription());
    			sessionIndexes.put(new Integer(index++), user);
    			if (currentUser == user)
    				_sessionCombo.select(_sessionCombo.getItemCount() - 1);
        }
    }
    
	public void modelChanged() {
        _editor.getSite().getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
            	setSessionOptions();
            }
        });
	}
}
