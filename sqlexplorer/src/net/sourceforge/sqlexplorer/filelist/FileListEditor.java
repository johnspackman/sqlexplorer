package net.sourceforge.sqlexplorer.filelist;

import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.util.ImageUtil;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.EditorPart;

public class FileListEditor extends EditorPart {

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		// Create a wrapper for our stuff
		final Composite myParent = new Composite(parent, SWT.NONE);
		FormLayout layout = new FormLayout();
		myParent.setLayout(layout);
		FormData data;

		// Create the toolbar and attach it to the top of the composite
		ToolBar toolBar = new ToolBar(myParent, SWT.NONE);
		data = new FormData();
		data.top = new FormAttachment(0, 0);
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(100, 0);
		toolBar.setLayoutData(data);
		
		ToolBarManager mgr = new ToolBarManager(toolBar);
		mgr.add(new Action(Messages.getString("FileListEditor.Actions.Execute"), ImageUtil.getDescriptor("Images.ExecSQLIcon")) {
			@Override
			public void run() {
			}

			@Override
			public String getToolTipText() {
				return Messages.getString("FileListEditor.Actions.Execute.ToolTip");
			}
		});
		
		// Attach the editor to the toolbar and the top of the sash
		TextEditor editor = new TextEditor();
		Composite editorParent = new Composite(myParent, SWT.NONE);
		editorParent.setLayout(new FillLayout());
		editor.createPartControl(editorParent);
		data = new FormData();
		data.top = new FormAttachment(toolBar, 0);
		data.bottom = new FormAttachment(100, 100);
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(100, 0);
		editorParent.setLayoutData(data);
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFocus() {
	}

}
