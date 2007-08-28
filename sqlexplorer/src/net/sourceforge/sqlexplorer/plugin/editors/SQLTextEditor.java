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

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeModel;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeModelChangedListener;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.sqlexplorer.sessiontree.model.utility.Dictionary;
import net.sourceforge.sqlexplorer.sqleditor.SQLTextViewer;
import net.sourceforge.sqlexplorer.sqleditor.actions.ExecSQLAction;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/**
 * TextEditor specialisation; encapsulates functionality specific to editing
 * SQL.
 * 
 * Virtually all of this code came from SQLEditor, which used to be derived
 * directly from TextEditor; SQLEditor now combines the text editor (here,
 * SQLTextEditor) and the result and messages panes in a single editor, hence
 * this was separated out for clarity
 * 
 * Note that MouseClickListener was also moved to a top-level, package-private
 * class for readability
 * 
 * @modified John Spackman
 * 
 */
public class SQLTextEditor extends TextEditor {

	private class SQLEditorSessionListener implements SessionTreeModelChangedListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeModelChangedListener#modelChanged()
		 */
		public void modelChanged(SessionTreeNode nd) {

			SessionTreeNode[] sessionNodes = SQLExplorerPlugin.getDefault().stm
					.getRoot().getSessionTreeNodes();

			boolean sessionFound = false;
			for (int i = 0; i < sessionNodes.length; i++) {
				if (editor.getSessionTreeNode() == sessionNodes[i]) {
					sessionFound = true;
					break;
				}
			}

			// do full refresh of toolbar
			if (!sessionFound) {
				SQLTextEditor.this.editor.setSessionTreeNode(null);
				SQLTextEditor.this.editor.getEditorToolBar().refresh(true);

			// only update the combo selection
			} else {
				SQLTextEditor.this.editor.getEditorToolBar().refresh(false);
			}
		}
	}

	private SQLEditor editor;

	private SQLEditorSessionListener listener;

	private MouseClickListener mcl;

	private IPartListener partListener;

	/* package */SQLTextViewer sqlTextViewer;

	private SessionTreeModel stm = SQLExplorerPlugin.getDefault().stm;

	private boolean _enableContentAssist = SQLExplorerPlugin.getDefault()
			.getPluginPreferences().getBoolean(IConstants.SQL_ASSIST);

	private IPreferenceStore store;

	public SQLTextEditor(SQLEditor editor) {
		super();
		this.editor = editor;
		mcl = new MouseClickListener(editor);
		store = SQLExplorerPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(SQLExplorerPlugin.getDefault().getPreferenceStore());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#createActions()
	 */
	protected void createActions() {

		super.createActions();

		if (!_enableContentAssist) {
			return;
		}

		Action action = new Action("Auto-Completion") {

			public void run() {
				sqlTextViewer.showAssistance();
			}
		};

		// This action definition is associated with the accelerator Ctrl+Space
		action
				.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssistProposal", action);

	}

	public void createPartControl(Composite parent) {

		super.createPartControl(parent);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(
				getSourceViewer().getTextWidget(),
				SQLExplorerPlugin.PLUGIN_ID + ".SQLEditor");

		Object adapter = getAdapter(org.eclipse.swt.widgets.Control.class);
		if (adapter instanceof StyledText) {
			StyledText text = (StyledText) adapter;
			text.setWordWrap(SQLExplorerPlugin.getDefault()
					.getPluginPreferences().getBoolean(IConstants.WORD_WRAP));
		}
	}

	protected ISourceViewer createSourceViewer(final Composite parent,
			IVerticalRuler ruler, int style) {

		parent.setLayout(new FillLayout());
		final Composite myParent = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = layout.horizontalSpacing = layout.verticalSpacing = 0;
		myParent.setLayout(layout);

		listener = new SQLEditorSessionListener();
		stm.addListener(listener);

		// create divider line

		Composite div1 = new Composite(myParent, SWT.NONE);
		GridData lgid = new GridData();
		lgid.grabExcessHorizontalSpace = true;
		lgid.horizontalAlignment = GridData.FILL;
		lgid.heightHint = 1;
		lgid.verticalIndent = 1;
		div1.setLayoutData(lgid);
		div1.setBackground(editor.getSite().getShell().getDisplay()
				.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));

		// create text viewer

		GridData gid = new GridData();
		gid.grabExcessHorizontalSpace = gid.grabExcessVerticalSpace = true;
		gid.horizontalAlignment = gid.verticalAlignment = GridData.FILL;

		Dictionary dictionary = null;
		if (editor.getSessionTreeNode() != null && _enableContentAssist) {
			dictionary = editor.getSessionTreeNode().getDictionary();
		}
		sqlTextViewer = new SQLTextViewer(myParent, style, store, dictionary,
				ruler);
		sqlTextViewer.getControl().setLayoutData(gid);

		// create bottom divider line

		Composite div2 = new Composite(myParent, SWT.NONE);
		lgid = new GridData();
		lgid.grabExcessHorizontalSpace = true;
		lgid.horizontalAlignment = GridData.FILL;
		lgid.heightHint = 1;
		lgid.verticalIndent = 0;
		div2.setLayoutData(lgid);
		div2.setBackground(editor.getSite().getShell().getDisplay()
				.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));

		final SQLEditor thisEditor = editor;
		sqlTextViewer.getTextWidget().addVerifyKeyListener(
				new VerifyKeyListener() {

					private ExecSQLAction _execSQLAction = new ExecSQLAction();

					public void verifyKey(VerifyEvent event) {

						if (event.stateMask == SWT.CTRL && event.keyCode == 13) {
							event.doit = false;
							_execSQLAction.setEditor(thisEditor);
							_execSQLAction.run();
						}
					}
				});

		sqlTextViewer.getTextWidget().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {

				SQLTextEditor.this.editor.getEditorSite().getPage().activate(
						SQLTextEditor.this.editor.getEditorSite().getPart());
			}
		});

		myParent.layout();

		IDocument dc = new Document();
		sqlTextViewer.setDocument(dc);

		mcl.install(sqlTextViewer);

		return sqlTextViewer;
	}

	public void setNewDictionary(final Dictionary dictionary) {
		if (editor.getSite() != null && editor.getSite().getShell() != null && editor.getSite().getShell().getDisplay() != null)
			editor.getSite().getShell().getDisplay().asyncExec(new Runnable() {
	
				public void run() {
	
					if (sqlTextViewer != null) {
						sqlTextViewer.setNewDictionary(dictionary);
						if (editor.getSessionTreeNode() != null) {
							sqlTextViewer.refresh();
						}
					}
	
				}
			});
	}

	public void onChangeSession() {

		if (editor.getSessionTreeNode() != null && _enableContentAssist) {
			setNewDictionary(editor.getSessionTreeNode().getDictionary());
		} else {
			setNewDictionary(null);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		if (partListener != null)
			editor.getEditorSite().getPage().removePartListener(partListener);
		stm.removeListener(listener);
		mcl.uninstall();
		super.dispose();
	}

	ISourceViewer getViewer() {
		return getSourceViewer();
	}
}
