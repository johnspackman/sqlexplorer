package net.sourceforge.sqlexplorer.sqleditor.actions;

import java.util.HashMap;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.connections.SessionEstablishedListener;
import net.sourceforge.sqlexplorer.dbproduct.Alias;
import net.sourceforge.sqlexplorer.dbproduct.ConnectionJob;
import net.sourceforge.sqlexplorer.dbproduct.ConnectionListener;
import net.sourceforge.sqlexplorer.dbproduct.Session;
import net.sourceforge.sqlexplorer.dbproduct.User;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.editors.SQLEditor;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


public class SQLEditorSessionSwitcher extends ControlContribution implements ConnectionListener, SessionEstablishedListener {

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
                
                // Nothing to do?
                if (user != null && _editor.getSession() != null && _editor.getSession().getUser() == user)
                	return;
                
                // Disconnect from the current session while we try to connect
            	_editor.setSession(null);
    			_sessionCombo.deselectAll();
                _editor.getEditorToolBar().refresh();
    			
    			if (user == null)
    				return;

    			// Start the connection job
    			ConnectionJob.createSession(user.getAlias(), user, SQLEditorSessionSwitcher.this, false);
            }
        });
        setSessionOptions();
        if (_sessionCombo.getItemCount() == 1)
        	MessageDialog.openInformation(parent.getShell(), Messages.getString("SQLEditor.Actions.ChooseSession.NoConnections.Title"), Messages.getString("SQLEditor.Actions.ChooseSession.NoConnections.Message"));

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

	public void cannotEstablishSession(User user) {
    	if (_sessionCombo.isDisposed())
    		return;
        _editor.getSite().getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
				_sessionCombo.deselectAll();
		        _editor.getEditorToolBar().refresh();
            }
        });
	}

	public void sessionEstablished(final Session session) {
    	if (_sessionCombo.isDisposed())
    		return;
        _editor.getSite().getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
				_editor.setSession(session);
				setSessionOptions();
		        _editor.getEditorToolBar().refresh();
            }
        });
	}
}
