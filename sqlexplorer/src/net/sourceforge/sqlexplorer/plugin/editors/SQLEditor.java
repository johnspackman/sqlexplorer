/*
 * Copyright (C) 2002-2004 Andrea Mazzolini
 * andreamazzolini@users.sourceforge.net
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbstructure.nodes.INode;
import net.sourceforge.sqlexplorer.dbstructure.nodes.TableNode;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.plugin.views.DatabaseStructureView;
import net.sourceforge.sqlexplorer.plugin.views.SqlexplorerViewConstants;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeModel;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeModelChangedListener;
import net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeNode;
import net.sourceforge.sqlexplorer.sessiontree.model.utility.Dictionary;
import net.sourceforge.sqlexplorer.sqleditor.SQLTextViewer;
import net.sourceforge.sqlexplorer.sqleditor.actions.ClearTextAction;
import net.sourceforge.sqlexplorer.sqleditor.actions.ExecSQLAction;
import net.sourceforge.sqlexplorer.sqleditor.actions.OpenFileAction;
import net.sourceforge.sqlexplorer.sqleditor.actions.SaveFileAsAction;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

public class SQLEditor extends TextEditor {

    class MouseClickListener implements KeyListener, MouseListener, MouseMoveListener, FocusListener, PaintListener,
            IPropertyChangeListener, IDocumentListener, ITextInputListener {

        INode activeTableNode;

        private boolean fActive;

        /** The currently active style range. */
        private IRegion fActiveRegion;

        /** The link color. */
        private Color fColor;

        /** The hand cursor. */
        private Cursor fCursor;

        /** The key modifier mask. */
        private int fKeyModifierMask = SWT.CTRL;

        /** The currently active style range as position. */
        private Position fRememberedPosition;

        private ISourceViewer sourceViewer;


        private void activateCursor(ISourceViewer viewer) {
            StyledText text = viewer.getTextWidget();
            if (text == null || text.isDisposed())
                return;
            Display display = text.getDisplay();
            if (fCursor == null)
                fCursor = new Cursor(display, SWT.CURSOR_HAND);
            text.setCursor(fCursor);
        }


        public void deactivate() {
            deactivate(false);
        }


        public void deactivate(boolean redrawAll) {
            if (!fActive)
                return;

            repairRepresentation(redrawAll);
            fActive = false;
        }


        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
         */
        public void documentAboutToBeChanged(DocumentEvent event) {
            if (fActive && fActiveRegion != null) {
                fRememberedPosition = new Position(fActiveRegion.getOffset(), fActiveRegion.getLength());
                try {
                    event.getDocument().addPosition(fRememberedPosition);
                } catch (BadLocationException x) {
                    fRememberedPosition = null;
                }
            }
        }


        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
         */
        public void documentChanged(DocumentEvent event) {
            if (fRememberedPosition != null && !fRememberedPosition.isDeleted()) {
                event.getDocument().removePosition(fRememberedPosition);
                fActiveRegion = new Region(fRememberedPosition.getOffset(), fRememberedPosition.getLength());
            }
            fRememberedPosition = null;

            if (sourceViewer != null) {
                StyledText widget = sourceViewer.getTextWidget();
                if (widget != null && !widget.isDisposed()) {
                    widget.getDisplay().asyncExec(new Runnable() {

                        public void run() {
                            deactivate();
                        }
                    });
                }
            }

        }


        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
         */
        public void focusGained(FocusEvent e) {
        }


        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
         */
        public void focusLost(FocusEvent e) {
            deactivate();

        }


        private int getCurrentTextOffset(ISourceViewer viewer) {

            try {
                StyledText text = viewer.getTextWidget();
                if (text == null || text.isDisposed())
                    return -1;

                Display display = text.getDisplay();
                Point absolutePosition = display.getCursorLocation();
                Point relativePosition = text.toControl(absolutePosition);

                int widgetOffset = text.getOffsetAtLocation(relativePosition);
                if (viewer instanceof ITextViewerExtension5) {
                    ITextViewerExtension5 extension = (ITextViewerExtension5) viewer;
                    return extension.widgetOffset2ModelOffset(widgetOffset);
                } else {
                    return widgetOffset + viewer.getVisibleRegion().getOffset();
                }

            } catch (IllegalArgumentException e) {
                return -1;
            }
        }


        private IRegion getCurrentTextRegion(ISourceViewer viewer) {
            if (viewer == null)
                return null;
            Dictionary dictionary = ((SQLTextViewer) viewer).dictionary;
            if (dictionary == null)
                return null;
            int offset = getCurrentTextOffset(viewer);
            if (offset == -1)
                return null;

            try {

                IRegion reg = selectWord(viewer.getDocument(), offset);
                if (reg == null)
                    return null;
                String selection = viewer.getDocument().get(reg.getOffset(), reg.getLength());
                if (selection == null)
                    return null;
                Object obj = dictionary.getByTableName(selection.toLowerCase());

                if (obj == null)
                    return null;
                else {
                    if (!(obj instanceof ArrayList))
                        return null;
                    ArrayList ls = (ArrayList) obj;
                    if (ls.isEmpty())
                        return null;
                    Object node = ((ArrayList) obj).get(0);
                    if (node instanceof TableNode)
                        activeTableNode = (INode) node;
                    else
                        return null;
                }
                return reg;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }


        private Point getMaximumLocation(StyledText text, int offset, int length) {
            Point maxLocation = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);

            for (int i = 0; i <= length; i++) {
                Point location = text.getLocationAtOffset(offset + i);

                if (location.x > maxLocation.x)
                    maxLocation.x = location.x;
                if (location.y > maxLocation.y)
                    maxLocation.y = location.y;
            }

            return maxLocation;
        }


        private Point getMinimumLocation(StyledText text, int offset, int length) {
            Point minLocation = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);

            for (int i = 0; i <= length; i++) {
                Point location = text.getLocationAtOffset(offset + i);

                if (location.x < minLocation.x)
                    minLocation.x = location.x;
                if (location.y < minLocation.y)
                    minLocation.y = location.y;
            }

            return minLocation;
        }


        private void highlightRegion(ISourceViewer viewer, IRegion region) {

            if (region.equals(fActiveRegion))
                return;

            repairRepresentation();

            StyledText text = viewer.getTextWidget();
            if (text == null || text.isDisposed())
                return;

            // highlight region
            int offset = 0;
            int length = 0;

            if (viewer instanceof ITextViewerExtension5) {
                ITextViewerExtension5 extension = (ITextViewerExtension5) viewer;
                IRegion widgetRange = extension.modelRange2WidgetRange(region);
                if (widgetRange == null)
                    return;

                offset = widgetRange.getOffset();
                length = widgetRange.getLength();

            } else {
                offset = region.getOffset() - viewer.getVisibleRegion().getOffset();
                length = region.getLength();
            }

            StyleRange oldStyleRange = text.getStyleRangeAtOffset(offset);
            Color foregroundColor = fColor;
            Color backgroundColor = oldStyleRange == null ? text.getBackground() : oldStyleRange.background;
            StyleRange styleRange = new StyleRange(offset, length, foregroundColor, backgroundColor);
            text.setStyleRange(styleRange);

            // underline
            text.redrawRange(offset, length, true);

            fActiveRegion = region;
        }


        private boolean includes(IRegion region, IRegion position) {
            return position.getOffset() >= region.getOffset()
                    && position.getOffset() + position.getLength() <= region.getOffset() + region.getLength();
        }


        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.text.ITextInputListener#inputDocumentAboutToBeChanged(org.eclipse.jface.text.IDocument,
         *      org.eclipse.jface.text.IDocument)
         */
        public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
            if (oldInput == null)
                return;
            deactivate();
            oldInput.removeDocumentListener(this);
        }


        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.text.ITextInputListener#inputDocumentChanged(org.eclipse.jface.text.IDocument,
         *      org.eclipse.jface.text.IDocument)
         */
        public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
            if (newInput == null)
                return;
            newInput.addDocumentListener(this);
        }


        public void install(ISourceViewer sourceViewer) {

            this.sourceViewer = sourceViewer;
            if (sourceViewer == null)
                return;

            StyledText text = sourceViewer.getTextWidget();
            if (text == null || text.isDisposed())
                return;

            updateColor(sourceViewer);

            sourceViewer.addTextInputListener(this);

            IDocument document = sourceViewer.getDocument();
            if (document != null)
                document.addDocumentListener(this);

            text.addKeyListener(this);
            text.addMouseListener(this);
            text.addMouseMoveListener(this);
            text.addFocusListener(this);
            text.addPaintListener(this);
        }


        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
         */
        public void keyPressed(KeyEvent event) {
            if (fActive) {
                deactivate();
                return;
            }

            if (event.keyCode != fKeyModifierMask) {
                deactivate();
                return;
            }

            fActive = true;

        }


        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
         */
        public void keyReleased(KeyEvent e) {
            if (!fActive)
                return;

            deactivate();

        }


        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
         */
        public void mouseDoubleClick(MouseEvent e) {
        }


        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
         */
        public void mouseDown(MouseEvent event) {
            if (!fActive)
                return;

            if (event.stateMask != fKeyModifierMask) {
                deactivate();
                return;
            }

            if (event.button != 1) {
                deactivate();
                return;
            }
        }


        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
         */
        public void mouseMove(MouseEvent event) {
            if (event.widget instanceof Control && !((Control) event.widget).isFocusControl()) {
                deactivate();
                return;
            }

            if (!fActive) {
                if (event.stateMask != fKeyModifierMask)
                    return;
                // modifier was already pressed
                fActive = true;
            }

            if (sourceViewer == null) {
                deactivate();
                return;
            }

            StyledText text = sourceViewer.getTextWidget();
            if (text == null || text.isDisposed()) {
                deactivate();
                return;
            }

            if ((event.stateMask & SWT.BUTTON1) != 0 && text.getSelectionCount() != 0) {
                deactivate();
                return;
            }

            IRegion region = getCurrentTextRegion(sourceViewer);
            if (region == null || region.getLength() == 0) {
                repairRepresentation();
                return;
            }

            highlightRegion(sourceViewer, region);
            activateCursor(sourceViewer);
        }


        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
         */
        public void mouseUp(MouseEvent e) {
            if (!fActive)
                return;

            if (e.button != 1) {
                deactivate();
                return;
            }

            boolean wasActive = fCursor != null;

            deactivate();

            if (wasActive) {

                BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {

                    public void run() {
                        try {
                            DatabaseStructureView structureView = (DatabaseStructureView) SQLEditor.this.getEditorSite().getWorkbenchWindow().getActivePage().findView(
                                    SqlexplorerViewConstants.SQLEXPLORER_DBSTRUCTURE);
                            if (structureView != null) {
                                SQLEditor.this.getEditorSite().getWorkbenchWindow().getActivePage().bringToTop(structureView);
                                // TODO figure out what this is for...
                               // structureView.selectNode(sessionTreeNode, activeTableNode);
                            }

                        } catch (Exception e1) {
                            SQLExplorerPlugin.error("Error selecting table", e1);
                        }
                    }
                });

            }

        }


        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
         */
        public void paintControl(PaintEvent event) {
            if (fActiveRegion == null)
                return;

            if (sourceViewer == null)
                return;

            StyledText text = sourceViewer.getTextWidget();
            if (text == null || text.isDisposed())
                return;

            int offset = 0;
            int length = 0;

            if (sourceViewer instanceof ITextViewerExtension5) {

                ITextViewerExtension5 extension = (ITextViewerExtension5) sourceViewer;
                IRegion widgetRange = extension.modelRange2WidgetRange(new Region(offset, length));
                if (widgetRange == null)
                    return;

                offset = widgetRange.getOffset();
                length = widgetRange.getLength();

            } else {

                IRegion region = sourceViewer.getVisibleRegion();
                if (!includes(region, fActiveRegion))
                    return;

                offset = fActiveRegion.getOffset() - region.getOffset();
                length = fActiveRegion.getLength();
            }

            // support for bidi
            Point minLocation = getMinimumLocation(text, offset, length);
            Point maxLocation = getMaximumLocation(text, offset, length);

            int x1 = minLocation.x;
            int x2 = minLocation.x + maxLocation.x - minLocation.x - 1;
            int y = minLocation.y + text.getLineHeight() - 1;

            GC gc = event.gc;
            if (fColor != null && !fColor.isDisposed())
                gc.setForeground(fColor);
            gc.drawLine(x1, y, x2, y);

        }


        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
         */
        public void propertyChange(PropertyChangeEvent event) {
            // noop
        }


        private void repairRepresentation() {
            repairRepresentation(false);
        }


        private void repairRepresentation(boolean redrawAll) {

            if (fActiveRegion == null)
                return;

            if (sourceViewer != null) {
                resetCursor(sourceViewer);

                int offset = fActiveRegion.getOffset();
                int length = fActiveRegion.getLength();

                // remove style
                if (!redrawAll && sourceViewer instanceof ITextViewerExtension2)
                    ((ITextViewerExtension2) sourceViewer).invalidateTextPresentation(offset, length);
                else
                    sourceViewer.invalidateTextPresentation();

                // remove underline
                if (sourceViewer instanceof ITextViewerExtension5) {
                    ITextViewerExtension5 extension = (ITextViewerExtension5) sourceViewer;
                    offset = extension.modelOffset2WidgetOffset(offset);
                } else {
                    offset -= sourceViewer.getVisibleRegion().getOffset();
                }

                StyledText text = sourceViewer.getTextWidget();
                try {
                    text.redrawRange(offset, length, true);
                } catch (IllegalArgumentException x) {
                    x.printStackTrace();
                    // JavaPlugin.log(x);
                }
            }

            fActiveRegion = null;
        }


        private void resetCursor(ISourceViewer viewer) {
            StyledText text = viewer.getTextWidget();
            if (text != null && !text.isDisposed())
                text.setCursor(null);

            if (fCursor != null) {
                fCursor.dispose();
                fCursor = null;
            }
        }


        private IRegion selectWord(IDocument document, int anchor) {

            try {
                int offset = anchor;
                char c;

                while (offset >= 0) {
                    c = document.getChar(offset);
                    if (!Character.isJavaIdentifierPart(c))
                        break;
                    --offset;
                }

                int start = offset;

                offset = anchor;
                int length = document.getLength();

                while (offset < length) {
                    c = document.getChar(offset);
                    if (!Character.isJavaIdentifierPart(c))
                        break;
                    ++offset;
                }

                int end = offset;

                if (start == end)
                    return new Region(start, 0);
                else
                    return new Region(start + 1, end - start - 1);

            } catch (BadLocationException x) {
                return null;
            }
        }


        public void uninstall() {

            if (fColor != null) {
                fColor.dispose();
                fColor = null;
            }

            if (fCursor != null) {
                fCursor.dispose();
                fCursor = null;
            }

            if (sourceViewer == null)
                return;

            sourceViewer.removeTextInputListener(this);

            IDocument document = sourceViewer.getDocument();
            if (document != null)
                document.removeDocumentListener(this);

            StyledText text = sourceViewer.getTextWidget();
            if (text == null || text.isDisposed())
                return;

            text.removeKeyListener(this);
            text.removeMouseListener(this);
            text.removeMouseMoveListener(this);
            text.removeFocusListener(this);
            text.removePaintListener(this);
        }


        private void updateColor(ISourceViewer viewer) {
            if (fColor != null)
                fColor.dispose();

            StyledText text = viewer.getTextWidget();
            if (text == null || text.isDisposed())
                return;

            Display display = text.getDisplay();
            fColor = new Color(display, new RGB(0, 0, 255));
        }

    }

    public static final String[] SUPPORTED_FILETYPES = new String[] {"*.txt", "*.sql", "*.*"};

    private ClearTextAction _clearTextAction;

    private ExecSQLAction _execSQLAction;

    private OpenFileAction _openFileAction;

    private SaveFileAsAction _saveAsAction;

    Combo catalogCombo;

    Combo combo;

    SQLEditorSessionListener listener;

    private MouseClickListener mcl = new MouseClickListener();

    private IPartListener partListener;

    SessionTreeNode sessionTreeNode;

    public SQLTextViewer sqlTextViewer;

    StatusLineManager statusMgr;

    SessionTreeModel stm = SQLExplorerPlugin.getDefault().stm;

    IPreferenceStore store;


    public SQLEditor() {

        store = SQLExplorerPlugin.getDefault().getPreferenceStore();
        setPreferenceStore(store);
    }


    public void buildCombo(final SessionTreeModel stm) {
        this.getSite().getShell().getDisplay().asyncExec(new Runnable() {

            public void run() {
                try {
                    combo.removeAll();
                    catalogCombo.removeAll();
                    final SessionTreeNode[] sessionNodes = stm.getRoot().getSessionTreeNodes();
                    combo.add("");
                    boolean found = false;
                    for (int i = 0; i < sessionNodes.length; i++) {
                        combo.add(sessionNodes[i].toString());
                        if (sessionTreeNode == sessionNodes[i]) {
                            combo.select(combo.getItemCount() - 1);
                            found = true;
                        }

                    }
                    if (!found) {
                        sessionTreeNode = null;
                        setNewDictionary(null);
                        catalogCombo.setVisible(false);
                    }
                    if (found) {
                        if (sessionTreeNode.supportsCatalogs()) {
                            catalogCombo.setVisible(true);
                            String catalogs[] = sessionTreeNode.getCatalogs();
                            String currentCatalog = sessionTreeNode.getCatalog();
                            for (int i = 0; i < catalogs.length; i++) {
                                catalogCombo.add(catalogs[i]);
                                if (currentCatalog.equals(catalogs[i])) {
                                    catalogCombo.select(catalogCombo.getItemCount() - 1);
                                }
                            }
                        } else {
                            catalogCombo.setVisible(false);
                        }
                    }
                    _execSQLAction.setEnabled(found);

                } catch (Throwable e) {
                }
            }
        });
    }


    /**
     * 
     */
    public void clearText() {
        sqlTextViewer.clearText();

    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.texteditor.AbstractTextEditor#createActions()
     */
    protected void createActions() {

        super.createActions();
        Action action = new Action("Auto-Completion") {

            public void run() {
                sqlTextViewer.showAssistance();
            }
        };

        // This action definition is associated with the accelerator Ctrl+Space
        action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
        setAction("ContentAssistProposal", action);
        _execSQLAction.setActionDefinitionId("net.sourceforge.sqlexplorer.sqlrun");
        setAction("SQL Run", _execSQLAction);

    }


    protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int style) {

        parent.setLayout(new FillLayout());
        Composite myParent = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout();
        layout.marginHeight = layout.marginWidth = layout.horizontalSpacing = layout.verticalSpacing = 0;
        myParent.setLayout(layout);

        ToolBarManager toolBarMgr = new ToolBarManager(SWT.FLAT);
        ToolBar toolBar = toolBarMgr.createControl(myParent);

        GridData gid = new GridData();
        gid.horizontalAlignment = GridData.FILL;
        gid.verticalAlignment = GridData.BEGINNING;
        gid.heightHint = 25;
        toolBar.setLayoutData(gid);
        _execSQLAction = new ExecSQLAction(this, store.getInt(IConstants.MAX_SQL_ROWS));//$NON-NLS-1$
        _openFileAction = new OpenFileAction(this);
        _saveAsAction = new SaveFileAsAction(this);
        _clearTextAction = new ClearTextAction(this);

        toolBarMgr.add(_execSQLAction);
        toolBarMgr.add(_openFileAction);
        toolBarMgr.add(_saveAsAction);
        toolBarMgr.add(_clearTextAction);

        // TODO add extensions to editor
        /*IAction[] toolActions = SQLExplorerPlugin.getDefault().pluginManager.getEditorToolbarActions(this);
        if (toolActions != null) {
            for (int i = 0; i < toolActions.length; i++)
                toolBarMgr.add(toolActions[i]);
        }*/
        
        
        toolBarMgr.update(true);

        ToolItem sep = new ToolItem(toolBar, SWT.SEPARATOR);
        ToolItem sep2 = new ToolItem(toolBar, SWT.SEPARATOR);
        combo = new Combo(toolBar, SWT.READ_ONLY);
        catalogCombo = new Combo(toolBar, SWT.READ_ONLY);
        listener = new SQLEditorSessionListener(this);
        stm.addListener(listener);
        buildCombo(stm);
        combo.setToolTipText("Choose Connection");
        combo.setSize(200, combo.getSize().y);
        // combo.pack ();
        combo.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }


            public void widgetSelected(SelectionEvent e) {
                int selIndex = combo.getSelectionIndex();
                if (selIndex == 0) {
                    _execSQLAction.setEnabled(false);
                    sessionTreeNode = null;
                    catalogCombo.setVisible(false);
                } else {
                    sessionTreeNode = SQLExplorerPlugin.getDefault().stm.getRoot().getSessionTreeNodes()[selIndex - 1];
                }
                if (sessionTreeNode != null) {
                    _execSQLAction.setEnabled(true);
                    setNewDictionary(sessionTreeNode.getDictionary());
                    if (sessionTreeNode.supportsCatalogs()) {
                        catalogCombo.setVisible(true);
                        catalogCombo.removeAll();
                        String catalogs[] = sessionTreeNode.getCatalogs();

                        String currentCatalog = sessionTreeNode.getCatalog();
                        for (int i = 0; i < catalogs.length; i++) {
                            catalogCombo.add(catalogs[i]);
                            if (currentCatalog.equals(catalogs[i])) {
                                catalogCombo.select(catalogCombo.getItemCount() - 1);
                            }
                        }
                    } else {
                        catalogCombo.setVisible(false);

                    }
                } else {
                    setNewDictionary(null);
                    _execSQLAction.setEnabled(false);
                }

            }
        });
        catalogCombo.setToolTipText("Choose Catalog");
        catalogCombo.setSize(200, catalogCombo.getSize().y);
        catalogCombo.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent arg0) {
            }


            public void widgetSelected(SelectionEvent arg0) {
                int selIndex = catalogCombo.getSelectionIndex();
                String newCat = catalogCombo.getItem(selIndex);
                if (sessionTreeNode != null) {
                    try {
                        sessionTreeNode.setCatalog(newCat);
                    } catch (Exception e1) {
                        SQLExplorerPlugin.error("Error changing catalog", e1);
                    }
                }
            }
        });
        sep.setWidth(combo.getSize().x);
        sep.setControl(combo);
        sep2.setWidth(catalogCombo.getSize().x);
        sep2.setControl(catalogCombo);

        toolBar.pack();

        toolBar.update();

        gid = new GridData();
        gid.grabExcessHorizontalSpace = gid.grabExcessVerticalSpace = true;
        gid.horizontalAlignment = gid.verticalAlignment = GridData.FILL;

        sqlTextViewer = new SQLTextViewer(myParent, style, store, null, ruler);
        sqlTextViewer.getControl().setLayoutData(gid);
        sqlTextViewer.getTextWidget().addVerifyKeyListener(new VerifyKeyListener() {

            public void verifyKey(VerifyEvent event) {
                if (event.stateMask == SWT.CTRL && event.keyCode == 13) {
                    event.doit = false;
                    _execSQLAction.run();
                }
            }
        });
        statusMgr = new StatusLineManager();
        statusMgr.createControl(myParent);
        gid = new GridData();
        gid.horizontalAlignment = GridData.FILL;
        gid.verticalAlignment = GridData.BEGINNING;
        statusMgr.getControl().setLayoutData(gid);

        myParent.layout();
        IDocument dc = new Document();
        sqlTextViewer.setDocument(dc);
        if (sessionTreeNode != null)
            setNewDictionary(sessionTreeNode.getDictionary());
        partListener = new IPartListener() {

            public void partActivated(IWorkbenchPart part) {
                if (part == SQLEditor.this) {
                    if (sessionTreeNode != null) {
                        if (sessionTreeNode.supportsCatalogs()) {
                            String catalog = sessionTreeNode.getCatalog();
                            String catalogs[] = catalogCombo.getItems();
                            for (int i = 0; i < catalogs.length; i++) {
                                if (catalog.equals(catalogs[i])) {
                                    catalogCombo.select(i);
                                    break;
                                }
                            }
                        }
                    }
                }
            }


            public void partBroughtToTop(IWorkbenchPart part) {
            }


            public void partClosed(IWorkbenchPart part) {
            }


            public void partDeactivated(IWorkbenchPart part) {
            }


            public void partOpened(IWorkbenchPart part) {
            }
        };
        getEditorSite().getPage().addPartListener(partListener);
        mcl.install(sqlTextViewer);

        return sqlTextViewer;

    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPart#dispose()
     */
    public void dispose() {
        if (partListener != null)
            getEditorSite().getPage().removePartListener(partListener);
        stm.removeListener(listener);
        mcl.uninstall();
        super.dispose();
    }


    /**
     * Save editor content to file.
     * 
     * @see org.eclipse.ui.ISaveablePart#doSaveAs()
     */
    public void doSaveAs() {

        FileDialog dialog = new FileDialog(getSite().getShell(), SWT.SAVE);
        dialog.setText(Messages.getString("SQLEditor.SaveAsDialog.Title"));
        dialog.setFilterExtensions(SUPPORTED_FILETYPES);
        dialog.setFilterNames(SUPPORTED_FILETYPES);
        dialog.setFileName("sql_editor.txt");

        String path = dialog.open();
        if (path == null) {
            return;
        }

        try {

            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }

            file.createNewFile();

            String content = sqlTextViewer.getDocument().get();

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(content, 0, content.length());
            writer.close();

        } catch (Exception e) {

            SQLExplorerPlugin.error("Couldn't save sql history.", e);
            MessageDialog.openError(getSite().getShell(), Messages.getString("SQLEditor.SaveAsDialog.Error"), e.getMessage());
        }

    }


    protected void editorContextMenuAboutToShow(IMenuManager menu) {
        super.editorContextMenuAboutToShow(menu);

        // TODO implement
        
/*        IContributionItem[] iContributionItems = SQLExplorerPlugin.getDefault().pluginManager.getEditorContextMenuActions(this);
        if (iContributionItems != null && iContributionItems.length > 0) {
            menu.add(new Separator());

            for (int i = 0; i < iContributionItems.length; i++) {
                menu.add(iContributionItems[i]);
            }
        }*/
    }


    /**
     * @return
     */
    public SessionTreeNode getSessionTreeNode() {
        return sessionTreeNode;
    }


    public String getSQLToBeExecuted() {
        String sql = sqlTextViewer.getTextWidget().getSelectionText();
        if (sql == null || sql.trim().length() == 0) {
            sql = sqlTextViewer.getTextWidget().getText();

        }
        return sql != null ? sql : "";
    }


    ISourceViewer getViewer() {
        return getSourceViewer();
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IEditorPart#init(org.eclipse.ui.IEditorSite,
     *      org.eclipse.ui.IEditorInput)
     */
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        if (input instanceof SQLEditorInput) {
            SQLEditorInput sqlInput = (SQLEditorInput) input;
            sessionTreeNode = sqlInput.getSessionNode();
            if (sessionTreeNode != null)
                setNewDictionary(sessionTreeNode.getDictionary());
        }
    }


    /**
     * Override method to always return false, since we do not want to save our
     * sql editor and avoid unnecessary save prompts.
     * 
     * @see org.eclipse.ui.ISaveablePart#isSaveOnCloseNeeded()
     */
    public boolean isSaveOnCloseNeeded() {
        return false;
    }


    /**
     * Load one or more files into the editor.
     * 
     * @param files string[] of relative file paths
     * @param filePath path where all files are found
     */
    public void loadFiles(String[] files, String filePath) {

        BufferedReader reader = null;

        try {

            StringBuffer all = new StringBuffer();
            String str = null;
            String delimiter = sqlTextViewer.getTextWidget().getLineDelimiter();

            for (int i = 0; i < files.length; i++) {

                String path = "";
                if (filePath != null) {
                    path += filePath + File.separator;
                }
                path += files[i];

                reader = new BufferedReader(new FileReader(path));

                while ((str = reader.readLine()) != null) {
                    all.append(str);
                    all.append(delimiter);
                }

                if (files.length > 1) {
                    all.append(delimiter);
                }
            }

            sqlTextViewer.setDocument(new Document(all.toString()));

        } catch (Throwable e) {
            SQLExplorerPlugin.error("Error loading document", e);

        } finally {
            try {
                reader.close();
            } catch (java.io.IOException e) {
                // noop
            }
        }

    }


    public void setMessage(String s) {
        statusMgr.setMessage(s);
    }


    public void setNewDictionary(Dictionary dictionary) {
        if (sqlTextViewer != null) {
            sqlTextViewer.setNewDictionary(dictionary);
            sqlTextViewer.refresh();
        }

    }


    /**
     * @param txt
     */
    public void setText(String txt) {
        IDocument dc = new Document(txt);
        sqlTextViewer.setDocument(dc);
        if (sessionTreeNode != null)
            setNewDictionary(sessionTreeNode.getDictionary());

    }

}

class SQLEditorSessionListener implements SessionTreeModelChangedListener {

    SQLEditor editor;


    public SQLEditorSessionListener(SQLEditor editor) {
        this.editor = editor;
    }


    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeModelChangedListener#modelChanged()
     */
    public void modelChanged(SessionTreeNode nd) {
        editor.buildCombo(SQLExplorerPlugin.getDefault().stm);

    }
}
