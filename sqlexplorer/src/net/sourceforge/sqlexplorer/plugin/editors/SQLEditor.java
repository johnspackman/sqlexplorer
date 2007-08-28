/*
 * Copyright (C) 2007 SQL Explorer Development Team
 * http://sourceforge.net/projects/eclipsesql
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sourceforge.sqlexplorer.plugin.editors;

import java.io.BufferedReader;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.views.ConnectionsView;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.sqlexplorer.sessiontree.model.utility.Dictionary;
import net.sourceforge.sqlexplorer.sqleditor.actions.SQLEditorToolBar;
import net.sourceforge.sqlexplorer.sqlpanel.AbstractSQLExecution;
import net.sourceforge.sqlexplorer.util.PartAdapter2;
import net.sourceforge.sqlexplorer.util.TextUtil;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

/**
 * SQLEditor is the top-level Editor component which is registered with Eclipse
 * as the Editor for each new SQL connection.
 * 
 * NOTE RE PREVIOUS VERSIONS: The previous version of this class was derived
 * from TextEditor and "only" provided facilities for editing and executing SQL
 * text; the results were displayed in a separate view, where the results from
 * different editors would intermingle. Also, any messages output from the
 * server (ie which was not a result set) were lost.
 * 
 * This version includes a splitter pane where the top half is the text editor
 * and the bottom half is a tabbed control containing one tab per result set and
 * a further tab for messages. The code in SQLEditor was becoming very large and
 * so the code for the text editor part has been split off into SQLTextEditor;
 * it is largely unchanged except for the refactoring.
 * 
 * Another change is that the editor no longer provides a status bar; because each
 * query tab provides a status bar with a message specific to the resultset, it
 * was redundant to provide the same info (well, "most recent event" info) in a
 * status bar just 2 pixels below.  However, the setMessage method is still
 * supported and now updates the main Eclipse status bar instead.  This means that
 * the "limit rows" feature moves to the Editor toolbar.
 * 
 * Behavoural Changes
 * The editors now prompt to save when they're closed; this is part of the slight
 * paradigm shift of SQLExplorer - instead of the SQL window being a scratch pad
 * for executing ad-hoc SQL, it's becoming part of a development environment
 * for stored procedures and 
 * 
 * @modified John Spackman
 * 
 */
public class SQLEditor extends EditorPart {
	
	// Max length of the SQL to display in the results table
	private static final int MAX_SQL_DISPLAY_LENGTH = 50;
	
	/*
	 * Encapsulates everything to do with a message in the message results tab
	 */
	public static class Message {
		
		// Whether this is a success or failure message
		private boolean success;
		
		// Line number the message relates to
		private int lineNo;
		
		// Column (within the line identified by lineNo) that the message relates to
		private int charNo;
		
		// The SQL
		private String sql;
		
		// The message
		private String message;

		/**
		 * Constructor.
		 * @param success true if the message is about success
		 * @param lineNo first line number in sql that the message relates to
		 * @param charNo first character within the line identified by lineNo that the message relates to
		 * @param sql the SQL or command that was executed
		 * @param message the message
		 */
		public Message(boolean success, int lineNo, int charNo, CharSequence sql, String message) {
			super();
			this.success = success;
			this.lineNo = lineNo;
			this.charNo = charNo;
			if (sql != null)
				setSql(sql);
			this.message = message;
		}
		
		/**
		 * Constructor.
		 * @param success true if the message is about success
		 * @param lineNo first line number in sql that the message relates to
		 * @param charNo first character within the line identified by lineNo that the message relates to
		 * @param sql the SQL or command that was executed
		 * @param message the message
		 */
		public Message(boolean success, int lineNo, int charNo, String message) {
			this(success, lineNo, charNo, null, message);
		}
		
		/**
		 * Constructor.
		 * @param success true if the message is about success
		 * @param message the message
		 */
		public Message(boolean success, String sql, String message) {
			this(success, 0, 0, sql, message);
		}
		
		/**
		 * Constructor.
		 * @param success true if the message is about success
		 * @param message the message
		 */
		public Message(boolean success, String message) {
			this(success, 0, 0, message);
		}
		
		/**
		 * Returns an array of strings to be placed in the table's TableItem
		 * @return
		 */
		/*package*/ String[] getTableText() {
			String successText;
			if (success)
				successText = Messages.getString("SQLEditor.Results.Messages.Success");
			else
				successText = Messages.getString("SQLEditor.Results.Messages.Failure");
			
			String location = "";
			if (lineNo > 0) {
				location = "line " + lineNo;
				if (charNo > 0)
					location += ", col " + charNo;
			}
			
			String[] result = new String[] {
				successText,
				location,
				(sql == null) ? "" : sql,
				TextUtil.getWrappedText(message)
			};
			return result; 
		}

		/**
		 * Whether the message is a success or failure message
		 * @return the success
		 */
		public boolean isSuccess() {
			return success;
		}

		/**
		 * The first line within getSql() to which the message relates; zero
		 * if not available
		 * @return the lineNo
		 */
		public int getLineNo() {
			return lineNo;
		}

		/**
		 * The first character offset within the line identified by getLineNo()
		 * to which the message relates.  Zero if not available
		 * @return the charNo
		 */
		public int getCharNo() {
			return charNo;
		}

		/**
		 * Returns the message text
		 * @return the message
		 */
		public String getMessage() {
			return message;
		}

		/**
		 * @return the sql
		 */
		public String getSql() {
			return sql;
		}

		/**
		 * @param sql the sql to set
		 */
		public void setSql(CharSequence sql) {
			this.sql = TextUtil.compressWhitespace(sql, MAX_SQL_DISPLAY_LENGTH);;
		}

		/**
		 * @param lineNo the lineNo to set
		 */
		public void setLineNo(int lineNo) {
			this.lineNo = lineNo;
		}

		/**
		 * @param charNo the charNo to set
		 */
		public void setCharNo(int charNo) {
			this.charNo = charNo;
		}
		
		public String toString() {
			return "[" + lineNo + "," + charNo + "] " + message;
		}
	}

	// The colour of borders and the fade-to-white colour of the selected tabs
	public static final Color BORDER_COLOR = new Color(null, 153, 186, 243);

	// The color of the sash
	private static final Color SASH_COLOR = BORDER_COLOR;

	// Typical file extensions
	public static final String[] SUPPORTED_FILETYPES = new String[] { "*.txt",
			"*.sql", "*.*" };

	// The Session node from the Connections view
	private SessionTreeNode sessionTreeNode;

	// Toolbar
	private SQLEditorToolBar toolBar;

	// The actual text editor for the top half of our composite editor
	private SQLTextEditor textEditor;

	// TabFolder for results and messages; note that we use CTabFolder and not TabFolder
	//	because CTabFolder adds support for an "X" close button on each tab
	private CTabFolder tabFolder;

	// The messages tab
	private CTabItem messagesTab;

	// The Table in the Messages tab
	private Table messagesTable;
	
	// Our own dirty flag - used for new files
	private boolean isDirty;
	
	// True if the editor does not have a filename yet
	private boolean isUntitled;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorPart#init(org.eclipse.ui.IEditorSite,
	 *      org.eclipse.ui.IEditorInput)
	 */
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		
		// Configure the editor
		setSite(site);
		setInput(input);

		// Create the text editor 
		textEditor = new SQLTextEditor(this);
		textEditor.init(site, input);
		
		
		// Make sure we get notification that our editor is closing because
		//	we may need to stop running queries
		getSite().getPage().addPartListener(new PartAdapter2() {

			/* (non-JavaDoc)
			 * @see net.sourceforge.sqlexplorer.util.PartAdapter2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
			 */
			public void partClosed(IWorkbenchPartReference partRef) {
				if (partRef.getPart(false) == SQLEditor.this) {
					onCloseEditor();
				}
			}
			
		});

		// If we havn't got a view, then try for the current session in the ConnectionsView
		if (sessionTreeNode == null) {
			IWorkbenchPage page = SQLExplorerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
			if (page != null) {
		        ConnectionsView view = (ConnectionsView)page.findView(ConnectionsView.class.getName());
		        if (view != null)
		        	setSessionTreeNode(view.getDefaultSession());
			}
		}
	}

    /* (non-JavaDoc)
	 * @see org.eclipse.ui.part.EditorPart#setInput(org.eclipse.ui.IEditorInput)
	 */
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		
		// Handle our own form of input
		if (input instanceof SQLEditorInput) {
			SQLEditorInput sqlInput = (SQLEditorInput) input;
			if (input != null) {
				setSessionTreeNode(sqlInput.getSessionNode());
				isDirty = true;
				isUntitled = true;
			}
//		} else if (input instanceof IPathEditorInput) {
//			IPathEditorInput partInput = (IPathEditorInput)input;
//		} else if (input instanceof IURIEditorInput) {
//			
//		} else if (input instanceof IStorageEditorInput) {
//			
		}

		setPartName(input.getName());
	}

	/**
     * Loads a file into the editor
     * @param file file to load
     */
    public void loadFile(File file) throws IOException {
        BufferedReader reader = null;

        StringBuffer all = new StringBuffer((int)file.length());
        String str = null;
        //String delimiter = _editor.getSqlTextViewer().getTextWidget().getLineDelimiter();
        
        /*
         * Note: I have changed the delimiter to a hardcoded \n because this a) allows the
         * interface to SQLEditor to be cleaner (see SQLEditor for refactoring description)
         * and I can find several other places where text will be passed to the same text 
         * editor and \n is hard coded.  If there is an issue with how the view encodes
         * line delimiters, it is likely to be a global problem and we should handle it in 
         * SQLEditor.setText() instead.
         * 
         */
        reader = new BufferedReader(new FileReader(file));
        try {
	        while ((str = reader.readLine()) != null) {
	            all.append(str);
	            all.append('\n');
	        }
	
	        setText(all.toString());
	        isDirty = false;
        } finally {
        	reader.close();
        }
    }
    
	/*
	 * (non-JavaDoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		try {
			parent.setLayout(new FillLayout());

			// Create a wrapper for our stuff
			final Composite myParent = new Composite(parent, SWT.NONE);
			FormLayout layout = new FormLayout();
			myParent.setLayout(layout);
			FormData data;

			// Create sash and attach it to 75% of the way down
			final Sash sash = createSash(myParent);
			data = new FormData();
			data.top = new FormAttachment(75, 0);
			data.left = new FormAttachment(0, 0);
			data.right = new FormAttachment(100, 0);
			sash.setLayoutData(data);

			// Create status bar and attach it to the bottom
			/*Composite statusBar = createStatusBar(myParent);
			data = new FormData();
			data.left = new FormAttachment(0, 0);
			data.right = new FormAttachment(100, 0);
			data.bottom = new FormAttachment(100, 0);
			statusBar.setLayoutData(data);*/

			// Create the toolbar and attach it to the top of the composite
			createToolbar(myParent);
			data = new FormData();
			data.top = new FormAttachment(0, 0);
			data.left = new FormAttachment(0, 0);
			data.right = new FormAttachment(100, 0);
			toolBar.getToolbarControl().setLayoutData(data);
			
			// Attach the editor to the toolbar and the top of the sash
			final Composite editor = createEditor(myParent);
			data = new FormData();
			data.top = new FormAttachment(toolBar.getToolbarControl(), 0);
			data.bottom = new FormAttachment(sash, 0);
			data.left = new FormAttachment(0, 0);
			data.right = new FormAttachment(100, 0);
			editor.setLayoutData(data);

			// Attach the tabs to the bottom of the sash and the bottom of the composite
			CTabFolder tabFolder = createResultTabs(myParent);
			data = new FormData();
			data.top = new FormAttachment(sash, 0);
			data.left = new FormAttachment(0, 0);
			data.right = new FormAttachment(100, 0);
			data.bottom = new FormAttachment(100, 0);
			tabFolder.setLayoutData(data);

		} catch (Exception e) {
			SQLExplorerPlugin.error("Couldn't create text editor", e);
			MessageDialog.openError(getSite().getShell(), Messages
					.getString("SQLEditor.Init.CreateTextEditor"), e.getClass()
					.getCanonicalName()
					+ ": " + e.getMessage());
		}
	}
	
	/**
	 * Creates the toolbar
	 * @param parent
	 */
	private void createToolbar(final Composite parent) {
		toolBar = new SQLEditorToolBar(parent, this);
		
		toolBar.addResizeListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
			}

			public void controlResized(ControlEvent e) {
				parent.getParent().layout(true);
				parent.layout(true);
			}
		});
	}

	/**
	 * Creates the sash (the draggable splitter) between the editor and the
	 * results tab
	 * 
	 * @param parent
	 * @return
	 */
	private Sash createSash(Composite parent) {
		// Create the sash and put it in the middle
		final Sash sash = new Sash(parent, SWT.HORIZONTAL);
		sash.setBackground(SASH_COLOR);

		sash.addSelectionListener(new SelectionListener() {

			/*
			 * (non-JavaDoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			/*
			 * (non-JavaDoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				FormData data = (FormData)sash.getLayoutData();
				Rectangle rect = sash.getParent().getBounds();
				data.top = new FormAttachment(e.y, rect.height, 0);
				sash.getParent().layout();
				sash.getParent().getParent().layout();
			}
		});

		return sash;
	}

	/**
	 * Creates the text editor in the top half
	 * 
	 * @param parent
	 * @return
	 */
	private Composite createEditor(Composite parent) {
		// Attach the editor to the top of the composite and the top of the sash
		final Composite editorParent = new Composite(parent, SWT.NONE);
		editorParent.setLayout(new FillLayout());
		textEditor.createPartControl(editorParent);
		textEditor.addPropertyListener(new IPropertyListener() {
			public void propertyChanged(Object source, int propertyId) {
				SQLEditor.this.firePropertyChange(propertyId);
			}
		});

		return editorParent;
	}

	/**
	 * Creates the results tabs in the bottom half
	 * 
	 * @param parent
	 * @return
	 */
	private CTabFolder createResultTabs(Composite parent) {
		tabFolder = new CTabFolder(parent, SWT.TOP | SWT.CLOSE);
		tabFolder.setBorderVisible(true);
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// Set up a gradient background for the selected tab
		Display display = getSite().getShell().getDisplay();
	    tabFolder.setSelectionBackground(
	    		new Color[] {
				        display.getSystemColor(SWT.COLOR_WHITE),
		                new Color(null, 211, 225, 250),
		                new Color(null, 175, 201, 246),
		                BORDER_COLOR
	    		},
	    		new int[] {25, 50, 75},
	    		true
	    	);

	    
		messagesTab = new CTabItem(tabFolder, SWT.NONE);
		messagesTab.setText(Messages
				.getString("SQLEditor.Results.Messages.Caption"));

		messagesTable = new Table(tabFolder, SWT.SINGLE | SWT.BORDER
				| SWT.FULL_SELECTION);
		messagesTab.setControl(messagesTable);
		messagesTable.setLinesVisible(true);
		messagesTable.setHeaderVisible(true);
		messagesTable.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				Message message = (Message)((TableItem)e.item).getData();
				setCursorPosition(message.getLineNo(), message.getCharNo());
			}
		});

		TableColumn col = new TableColumn(messagesTable, SWT.NONE);
		col.setText(Messages.getString("SQLEditor.Results.Messages.Status"));
		col.pack();

		col = new TableColumn(messagesTable, SWT.NONE);
		col.setText(Messages.getString("SQLEditor.Results.Messages.Location"));
		col.pack();

		col = new TableColumn(messagesTable, SWT.NONE);
		col.setText(Messages.getString("SQLEditor.Results.Messages.SQL"));
		col.pack();

		col = new TableColumn(messagesTable, SWT.NONE);
		col.setText(Messages.getString("SQLEditor.Results.Messages.Text"));
		col.pack();

		tabFolder.setSelection(messagesTab);
		
	    // Add a listener to get the close button on each tab
	    tabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {

			/* (non-JavaDoc)
			 * @see org.eclipse.swt.custom.CTabFolder2Adapter#close(org.eclipse.swt.custom.CTabFolderEvent)
			 */
			public void close(CTabFolderEvent event) {
				super.close(event);
				CTabItem tabItem = (CTabItem)event.data;
				event.doit = onCloseTab(tabItem);
			}
	    	
	    });
	    
		return tabFolder;
	}
	
	/*
	 * (non-JavaDoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
		if (isUntitled) {
			if (!doSaveAsInternal())
				monitor.setCanceled(true);
		} else {
			textEditor.doSave(monitor);
			setIsDirty(false);
		}
	}

	/**
	 * Save editor content to file.
	 * 
	 * @see org.eclipse.ui.ISaveablePart#doSaveAs()
	 */
	public void doSaveAs() {
		doSaveAsInternal();
	}

	/**
	 * Implementation for save-as; returns true if successfull, false if not (i.e.
	 * the user cancelled the dialog)
	 * @return true if saved, false if cancelled
	 */
	private boolean doSaveAsInternal() {
		FileDialog dialog = new FileDialog(getSite().getShell(), SWT.SAVE);
		dialog.setText(Messages.getString("SQLEditor.SaveAsDialog.Title"));
		dialog.setFilterExtensions(SUPPORTED_FILETYPES);
		dialog.setFilterNames(SUPPORTED_FILETYPES);
		dialog.setFileName("sql_editor.txt");

		String path = dialog.open();
		if (path == null)
			return false;

		try {
			File file = new File(path);
			if (file.exists()) {
				file.delete();
			}

			file.createNewFile();

			String content = textEditor.sqlTextViewer.getDocument().get();

			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(content, 0, content.length());
			writer.close();

		} catch (Exception e) {

			SQLExplorerPlugin.error("Couldn't save sql history.", e);
			MessageDialog.openError(getSite().getShell(), Messages
					.getString("SQLEditor.SaveAsDialog.Error"), e.getMessage());
			return false;
		}

		setIsDirty(false);
		return true;
	}

	/**
	 * Adds a message to the message window
	 * @param message
	 */
	public void addMessage(Message message) {
		TableItem tableRow = new TableItem(messagesTable, SWT.NONE);
		tableRow.setText(message.getTableText());
		tableRow.setData(message);
		
		TableColumn[] cols = messagesTable.getColumns();
		for (int i = 0; i < cols.length; i++)
			cols[i].pack();
	}
	
	/**
	 * Allocates a new tab for an AbstractSQLExecution; the tab and it's contents
	 * are completely undefined
	 * @param sqlExec
	 * @return
	 */
	public ResultsTab createResultsTab(AbstractSQLExecution sqlExec) {

		// Create the new tab, make it second to last (IE keep the messages tab
		//	always at the end) and set the new tab's title to the 1-based index
		CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE, tabFolder.getItems().length - 1);
		tabItem.setText(Integer.toString(tabFolder.getItems().length - 1));
		tabFolder.setSelection(tabItem);
		
		// Make sure we can track the execution
		tabItem.setData(sqlExec);

		// Create a composite to add all controls to
		Composite composite = new Composite(tabFolder, SWT.NONE);
		tabItem.setControl(composite);
		
		// Make sure we're visible
        getSite().getPage().bringToTop(this);
        
		return new ResultsTab(this, tabItem, composite);
	}
	
	/**
	 * Called internally when the user tries to close a tab
	 * @param tabItem
	 * @return true if the tab should be closed
	 */
	private boolean onCloseTab(CTabItem tabItem) {
		// Cannot close the messages tab
		if (tabItem == messagesTab)
			return false;

		// The SQL query runs in a background thread and if the tab item gets disposed of 
		//	BEFORE the query has finished we need to notify the thread that there is no UI
		//	left and that the thread should terminate as soon as possible.
		synchronized(this) {
			// Get the SQL execution
	        final AbstractSQLExecution sqlExecution = getSqlExecution(tabItem);
        
			// Stop the statement - but allow it to happen asynchronously
	        if (sqlExecution != null) {
		        tabItem.setData(null);
		        
		        BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
		            public void run() {
	                	sqlExecution.stop();
		            }
		        });
	        }
		}
		
		return true;
	}
	
	/**
	 * Called internally when the user tries to close the editor
	 */
	private void onCloseEditor() {
		clearResults();
	}

	/**
	 * Closes all result tabs and signals all associated AbstractSQLExecutions
	 * to stop (if they're still running)
	 */
	public void clearResults() {
		synchronized(this) {
			CTabItem[] tabItems = tabFolder.getItems();
			for (int i = 0; i < tabItems.length; i++)
				if (tabItems[i] != messagesTab) 
					closeTab(tabItems[i]);
			messagesTable.removeAll();
		}
	}
	
	/**
	 * Removes a tab from the results tabs, stopping any pending executions etc
	 * @param tab
	 */
	private void closeTab(CTabItem tab) {
        tab.dispose();
	}
	
	/**
	 * Converts a TabItem into the AbstractSQLExecution for that tab
	 * @param tab
	 * @return
	 */
	public AbstractSQLExecution getSqlExecution(CTabItem tab) {
		return (AbstractSQLExecution)tab.getData();
	}
	
	/**
	 * Converts an AbstractSQLExecution into its TabItem
	 * @param sqlExec
	 * @return
	 */
	public CTabItem getResultsTab(AbstractSQLExecution sqlExec) {
		CTabItem[] items = tabFolder.getItems();
		for (int i = 0; i < items.length; i++)
			if (items[i].getData() == sqlExec)
				return items[i];
		return null;
	}
	
	/*
	 * (non-JavaDoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#isDirty()
	 */
	public boolean isDirty() {
    	boolean saveOnClose = SQLExplorerPlugin.getDefault().getPreferenceStore().getBoolean(IConstants.REQUIRE_SAVE_ON_CLOSE_EDITOR);
		return saveOnClose && (isDirty || textEditor.isDirty());
	}
	
	protected void setIsDirty(boolean isDirty) {
		if (this.isDirty != isDirty) {
			this.isDirty = isDirty;
			firePropertyChange(PROP_DIRTY);
		}
		if (!isDirty)
			isUntitled = false;
	}

	/*
	 * (non-JavaDoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
		textEditor.setFocus();
	}

	/*
	 * (non-JavaDoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return textEditor.isSaveAsAllowed();
	}

	/**
	 * Sets the message displayed on the Eclipse main status bar
	 * @param s
	 */
	public void setMessage(String s) {
		IStatusLineManager manager = getEditorSite().getActionBars().getStatusLineManager();
		manager.setMessage(s);
//		statusMgr.setMessage(s);
	}

	/**
	 * Sets the dictionary used by the text editor
	 * 
	 * @param dictionary
	 */
	public void setNewDictionary(final Dictionary dictionary) {
		textEditor.setNewDictionary(dictionary);
	}

	/**
	 * Sets the session to use for executing queries
	 * 
	 * @param pSessionTreeNode
	 */
	public void setSessionTreeNode(SessionTreeNode sessionTreeNode) {
		this.sessionTreeNode = sessionTreeNode;
		if (textEditor != null)
			textEditor.onChangeSession();
	}

	/**
	 * @return the sessionTreeNode
	 */
	public SessionTreeNode getSessionTreeNode() {
		return sessionTreeNode;
	}

	/**
	 * Sets the text of the editor
	 * 
	 * @param txt
	 */
	public void setText(String txt) {
		IDocument dc = new Document(txt);
		textEditor.sqlTextViewer.setDocument(dc);
		textEditor.sqlTextViewer.refresh();
	}

	/**
	 * Returns the text to be executed; this is the entire text if there is no
	 * selection, else just the selected text
	 * 
	 * @return
	 */
	public String getSQLToBeExecuted() {

		String sql = textEditor.sqlTextViewer.getTextWidget()
				.getSelectionText();
		if (sql == null || sql.trim().length() == 0)
			sql = textEditor.sqlTextViewer.getTextWidget().getText();

		// Normalise this to have standard \n in strings.  \r confuses Oracle and
		//	isn't normally needed internally anyway
    	StringBuffer sb = new StringBuffer(sql);
    	for (int i = 0; i < sb.length(); i++) {
    		if (sb.charAt(i) == '\r') {
    			sb.deleteCharAt(i);
    			i--;
    		}
    	}
    	sql = sb.toString();

		return sql != null ? sql : "";
	}

	/**
	 * Clears the text of the editor
	 */
	public void clearText() {
		textEditor.sqlTextViewer.clearText();
	}

	/**
	 * Returns the toolbar
	 * 
	 * @return
	 */
	public SQLEditorToolBar getEditorToolBar() {
		return toolBar;
	}

	/**
	 * Returns whether to limit the results and if so by how much.
	 * 
	 * @return the maximum number of rows to retrieve, 0 for unlimited, or null
	 *         if it cannot be interpretted
	 */
	public Integer getLimitResults() {
		return toolBar.getLimitResults();
	}
	
	/**
	 * Updates the cursor position info in the status bar
	 */
	public void updateCursorPosition() {
		Object adapter = textEditor.getAdapter(org.eclipse.swt.widgets.Control.class);
		if (adapter instanceof StyledText) {
			StyledText text = (StyledText) adapter;
			int offset = text.getCaretOffset();
			int lineNo = text.getLineAtOffset(offset);
			int lineOffset = text.getOffsetAtLine(lineNo);
			int charNo = offset - lineOffset;
			
			IStatusLineManager manager = getEditorSite().getActionBars().getStatusLineManager();
			IContributionItem items[] = manager.getItems();
			for (IContributionItem item : items) {
				if (item instanceof CursorPositionContrib) {
					CursorPositionContrib contrib = (CursorPositionContrib) item;
					contrib.setPosition(lineNo + 1, charNo + 1);
					break;
				}
			}
		}
	}
	
	/**
	 * Moves the text cursor to a given line and column 
	 * @param lineNo
	 * @param charNo
	 */
	public void setCursorPosition(int lineNo, int charNo) {
		if (lineNo < 1)
			return;
		if (charNo < 1)
			charNo = 1;
		Object adapter = textEditor.getAdapter(org.eclipse.swt.widgets.Control.class);
		if (adapter instanceof StyledText) {
			StyledText text = (StyledText) adapter;
			int lineOffset = text.getOffsetAtLine(lineNo - 1);
			text.setCaretOffset(lineOffset + charNo - 1);
			updateCursorPosition();
			text.setFocus();
			text.showSelection();
		}
	}
}
