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

import java.io.BufferedWriter;
import java.io.File;
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
import net.sourceforge.sqlexplorer.sqleditor.actions.ExecSQLAction;
import net.sourceforge.sqlexplorer.sqleditor.actions.SQLEditorToolBar;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.StatusLineManager;
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
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
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
                                SQLEditor.this.getEditorSite().getWorkbenchWindow().getActivePage().bringToTop(
                                        structureView);
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

    private SQLEditorToolBar _editorToolBar;

    private Button _limitResults;

    private Text _maxResultField;

    SQLEditorSessionListener listener;

    private MouseClickListener mcl = new MouseClickListener();

    private IPartListener partListener;

    SessionTreeNode sessionTreeNode;

    public SQLTextViewer sqlTextViewer;

    StatusLineManager statusMgr;

    SessionTreeModel stm = SQLExplorerPlugin.getDefault().stm;

    IPreferenceStore store;

    public static final String[] SUPPORTED_FILETYPES = new String[] {"*.txt", "*.sql", "*.*"};

    private boolean _enableContentAssist = SQLExplorerPlugin.getDefault().getPluginPreferences().getBoolean(IConstants.SQL_ASSIST);
    
    public SQLEditor() {

        store = SQLExplorerPlugin.getDefault().getPreferenceStore();
        setPreferenceStore(store);

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
        
        if (!_enableContentAssist) {
            return;
        }
        
        Action action = new Action("Auto-Completion") {

            public void run() {
                sqlTextViewer.showAssistance();
            }
        };

        // This action definition is associated with the accelerator Ctrl+Space
        action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
        setAction("ContentAssistProposal", action);

    }


    public void createPartControl(Composite parent) {

        super.createPartControl(parent);
        
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getSourceViewer().getTextWidget(),
                SQLExplorerPlugin.PLUGIN_ID + ".SQLEditor");

        Object adapter = getAdapter(org.eclipse.swt.widgets.Control.class);
        if (adapter instanceof StyledText) {
            StyledText text = (StyledText) adapter;
            text.setWordWrap(SQLExplorerPlugin.getDefault().getPluginPreferences().getBoolean(IConstants.WORD_WRAP));
        }

        
    }


    protected ISourceViewer createSourceViewer(final Composite parent, IVerticalRuler ruler, int style) {
       
        parent.setLayout(new FillLayout());
        final Composite myParent = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout();
        layout.marginHeight = layout.marginWidth = layout.horizontalSpacing = layout.verticalSpacing = 0;
        myParent.setLayout(layout);

        // create tool bar

        _editorToolBar = new SQLEditorToolBar(myParent, this);
        listener = new SQLEditorSessionListener(this);
        stm.addListener(listener);

       
        // create divider line

        Composite div1 = new Composite(myParent, SWT.NONE);
        GridData lgid = new GridData();
        lgid.grabExcessHorizontalSpace = true;
        lgid.horizontalAlignment = GridData.FILL;
        lgid.heightHint = 1;
        lgid.verticalIndent = 1;
        div1.setLayoutData(lgid);
        div1.setBackground(getSite().getShell().getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));

        
        // create text viewer

        GridData gid = new GridData();
        gid.grabExcessHorizontalSpace = gid.grabExcessVerticalSpace = true;
        gid.horizontalAlignment = gid.verticalAlignment = GridData.FILL;

        Dictionary dictionary = null;
        if (sessionTreeNode != null && _enableContentAssist) {
            dictionary = sessionTreeNode.getDictionary();
        }
        sqlTextViewer = new SQLTextViewer(myParent, style, store, dictionary, ruler);
        sqlTextViewer.getControl().setLayoutData(gid);

        
        // create bottom divider line

        Composite div2 = new Composite(myParent, SWT.NONE);
        lgid = new GridData();
        lgid.grabExcessHorizontalSpace = true;
        lgid.horizontalAlignment = GridData.FILL;
        lgid.heightHint = 1;
        lgid.verticalIndent = 0;
        div2.setLayoutData(lgid);
        div2.setBackground(getSite().getShell().getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));

        // create bottom status bar

        Composite statusBar = new Composite(myParent, SWT.NULL);

        GridLayout statusBarLayout = new GridLayout();
        statusBarLayout.numColumns = 3;
        statusBarLayout.verticalSpacing = 0;
        statusBarLayout.marginHeight = 0;
        statusBarLayout.marginWidth = 0;
        statusBarLayout.marginTop = 0;
        statusBarLayout.marginBottom = 0;
        statusBarLayout.marginRight = 5;
        statusBarLayout.horizontalSpacing = 5;
        statusBarLayout.verticalSpacing = 0;

        statusBar.setLayout(statusBarLayout);

        GridData statusBarGridData = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
        statusBarGridData.verticalIndent = 0;
        statusBarGridData.horizontalIndent = 0;
        statusBar.setLayoutData(statusBarGridData);

        // add status line manager

        statusMgr = new StatusLineManager();
        statusMgr.createControl(statusBar);

        GridData c1Grid = new GridData();
        c1Grid.horizontalAlignment = SWT.FILL;
        c1Grid.verticalAlignment = SWT.BOTTOM;
        c1Grid.grabExcessHorizontalSpace = true;
        c1Grid.grabExcessVerticalSpace = false;
        statusMgr.getControl().setLayoutData(c1Grid);

        // add checkbox for limiting results

        GridData c2Grid = new GridData();
        c2Grid.horizontalAlignment = SWT.RIGHT;
        c2Grid.verticalAlignment = SWT.CENTER;
        c2Grid.grabExcessHorizontalSpace = false;
        c2Grid.grabExcessVerticalSpace = false;

        final Button limitResults = new Button(statusBar, SWT.CHECK);
        _limitResults = limitResults;
        limitResults.setText(Messages.getString("SQLEditor.LimitRows"));
        limitResults.setSelection(true);
        limitResults.setLayoutData(c2Grid);

        // add input field for result limit

        GridData c3Grid = new GridData();
        c3Grid.horizontalAlignment = SWT.RIGHT;
        c3Grid.verticalAlignment = SWT.CENTER;
        c3Grid.grabExcessHorizontalSpace = false;
        c3Grid.grabExcessVerticalSpace = false;
        c3Grid.widthHint = 30;

        final Text maxResultText = new Text(statusBar, SWT.BORDER | SWT.SINGLE);
        _maxResultField = maxResultText;
        maxResultText.setText(store.getString(IConstants.MAX_SQL_ROWS));
        maxResultText.setLayoutData(c3Grid);

        limitResults.addMouseListener(new MouseAdapter() {

            // enable/disable input field when checkbox is clicked
            public void mouseUp(MouseEvent e) {

                maxResultText.setEnabled(limitResults.getSelection());
            }
        });

        
        final SQLEditor thisEditor = this;
        sqlTextViewer.getTextWidget().addVerifyKeyListener(new VerifyKeyListener() {

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

                getEditorSite().getPage().activate(getEditorSite().getPart());
            }
            
        });
        
        statusBar.layout();
        myParent.layout();
        
        
        IDocument dc = new Document();
        sqlTextViewer.setDocument(dc);

        mcl.install(sqlTextViewer);

        ControlListener resizeListener = new ControlListener() {

            public void controlMoved(ControlEvent e) {

            }


            public void controlResized(ControlEvent e) {

                myParent.layout(true);
                parent.layout(true);
            }
        };

        _editorToolBar.addResizeListener(resizeListener);

        
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
            MessageDialog.openError(getSite().getShell(), Messages.getString("SQLEditor.SaveAsDialog.Error"),
                    e.getMessage());
        }

    }


    public SQLEditorToolBar getEditorToolBar() {

        return _editorToolBar;
    }


    public Button getLimitResults() {

        return _limitResults;
    }


    public Text getMaxResultField() {

        return _maxResultField;
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


    public void setMessage(String s) {

        statusMgr.setMessage(s);
    }


    public void setNewDictionary(final Dictionary dictionary) {
        
        getSite().getShell().getDisplay().asyncExec(new Runnable() {

            public void run() {
                               
                if (sqlTextViewer != null) {
                    sqlTextViewer.setNewDictionary(dictionary);
                    if (sessionTreeNode != null) {
                        sqlTextViewer.refresh();
                    }
                }
                
            }
        });
        


    }


    public void setSessionTreeNode(SessionTreeNode pSessionTreeNode) {
       
        this.sessionTreeNode = pSessionTreeNode;
        if (sessionTreeNode != null && _enableContentAssist) {
            setNewDictionary(sessionTreeNode.getDictionary());
        } else {
            setNewDictionary(null);
        }
        
    }


    /**
     * @param txt
     */
    public void setText(String txt) {

        IDocument dc = new Document(txt);
        sqlTextViewer.setDocument(dc);
        sqlTextViewer.refresh();
    }
}

class SQLEditorSessionListener implements SessionTreeModelChangedListener {

    SQLEditor sqlEditor;


    public SQLEditorSessionListener(SQLEditor pSqlEditor) {

        this.sqlEditor = pSqlEditor;
    }


    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.sqlexplorer.sessiontree.model.SessionTreeModelChangedListener#modelChanged()
     */
    public void modelChanged(SessionTreeNode nd) {

        SessionTreeNode[] sessionNodes = SQLExplorerPlugin.getDefault().stm.getRoot().getSessionTreeNodes();

        boolean sessionFound = false;
        for (int i = 0; i < sessionNodes.length; i++) {
            if (sqlEditor.sessionTreeNode == sessionNodes[i]) {
                sessionFound = true;
            }
        }

        if (!sessionFound) {

            sqlEditor.setSessionTreeNode(null);
            // do full refresh of toolbar
            sqlEditor.getEditorToolBar().refresh(true);

        } else {

            // only update the combo selection
            sqlEditor.getEditorToolBar().refresh(false);
        }

    }
}
