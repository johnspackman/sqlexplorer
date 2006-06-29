package net.sourceforge.sqlexplorer.preferences;

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

import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sqleditor.SQLTextViewer;

import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class SQLPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    public static final String BOLD = "_bold";

    private FontFieldEditor fFontEditor;

    // private ColorEditor fBackgroundColorEditor;
    List fSyntaxColorList;

    ColorEditor fSyntaxForegroundColorEditor;

    Button fBoldCheckBox;

    Button btn4;

    Button btn1, btn2, btn3;

    SQLTextViewer fPreviewViewer;

    OverlayPreferenceStore fOverlayStore;

    // public static final String BOLD= "_bold"; 

    public final OverlayPreferenceStore.OverlayKey[] fKeys = new OverlayPreferenceStore.OverlayKey[] {

    new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IConstants.FONT), 

            new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IConstants.SQL_MULTILINE_COMMENT),
            new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IConstants.SQL_MULTILINE_COMMENT + BOLD),

            new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IConstants.SQL_TABLE),
            new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IConstants.SQL_TABLE + BOLD),

            new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IConstants.SQL_COLUMS),
            new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IConstants.SQL_COLUMS + BOLD),

            new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IConstants.SQL_SINGLE_LINE_COMMENT),
            new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IConstants.SQL_SINGLE_LINE_COMMENT + BOLD),

            new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IConstants.SQL_DEFAULT),
            new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IConstants.SQL_DEFAULT + BOLD),

            new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IConstants.SQL_STRING),
            new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IConstants.SQL_STRING + BOLD),

            new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IConstants.SQL_KEYWORD),
            new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IConstants.SQL_KEYWORD + BOLD),

            new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IConstants.CLIP_EXPORT_COLUMNS),
            new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IConstants.CLIP_EXPORT_SEPARATOR),};


    public SQLPreferencePage() {
        fOverlayStore = new OverlayPreferenceStore(SQLExplorerPlugin.getDefault().getPreferenceStore(), fKeys);
        fOverlayStore.load();
        fOverlayStore.start();
    }


    public void init(IWorkbench workbench) {
    }


    public SQLPreferencePage(OverlayPreferenceStore fOverlayStore) {
        super(Messages.getString("SQL_Preferences_1")); 
        this.fOverlayStore = fOverlayStore;
        this.setTitle(Messages.getString("Sql_Editor_Preferences_2")); 

    }

    final String[][] fSyntaxColorListModel = new String[][] { {Messages.getString("SQL_Table_1"), IConstants.SQL_TABLE}, 
            {Messages.getString("SQL_Column_2"), IConstants.SQL_COLUMS}, 

            {Messages.getString("SQL_Keyword_3"), IConstants.SQL_KEYWORD}, 
            {Messages.getString("SQL_Single_Line_Comment_4"), IConstants.SQL_SINGLE_LINE_COMMENT}, 
            {Messages.getString("SQL_Multi_Line_Comment_5"), IConstants.SQL_MULTILINE_COMMENT}, 
            {Messages.getString("String_6"), IConstants.SQL_STRING}, 
            {Messages.getString("Others_7"), IConstants.SQL_DEFAULT} 
    };


    protected Control createContents(Composite parent) {

        Composite colorComposite = new Composite(parent, SWT.NULL);
        colorComposite.setLayout(new GridLayout());
        Group fntGroup = new Group(colorComposite, SWT.NULL);
        fntGroup.setLayout(new GridLayout());
        fntGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        fntGroup.setText(Messages.getString("Font_Properties_1")); 

        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.marginHeight = 0;
        layout.marginWidth = 0;

        Composite fntComposite = new Composite(fntGroup, SWT.NONE);
        layout = new GridLayout();
        layout.numColumns = 3;
        fntComposite.setLayout(layout);
        fFontEditor = new FontFieldEditor("key", Messages.getString("Text_Font__3"), fntComposite);  //$NON-NLS-2$
        fFontEditor.setLabelText(Messages.getString("Text_Font__4")); 
        fFontEditor.setChangeButtonText(Messages.getString("Change_5")); 
        fFontEditor.setPropertyChangeListener(new IPropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent event) {
                handleFont();
            }
        });

        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 2;
        fntComposite.setLayoutData(gd);

        Group colorGroup = new Group(colorComposite, SWT.NULL);
        colorGroup.setLayout(new GridLayout());
        colorGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        colorGroup.setText(Messages.getString("Text_Properties_6")); 

        Composite editorComposite = new Composite(colorGroup, SWT.NONE);
        layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        editorComposite.setLayout(layout);
        gd = new GridData(GridData.FILL_BOTH);
        editorComposite.setLayoutData(gd);

        fSyntaxColorList = new List(editorComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
        gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = convertHeightInCharsToPixels(5);
        fSyntaxColorList.setLayoutData(gd);

        Composite stylesComposite = new Composite(editorComposite, SWT.NONE);
        layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.numColumns = 2;
        stylesComposite.setLayout(layout);
        stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        Label label = new Label(stylesComposite, SWT.LEFT);
        label.setText(Messages.getString("Color_9")); 
        gd = new GridData();
        gd.horizontalAlignment = GridData.BEGINNING;
        label.setLayoutData(gd);

        fSyntaxForegroundColorEditor = new ColorEditor(stylesComposite);
        Button foregroundColorButton = fSyntaxForegroundColorEditor.getButton();
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.BEGINNING;
        foregroundColorButton.setLayoutData(gd);

        fBoldCheckBox = new Button(stylesComposite, SWT.CHECK);
        fBoldCheckBox.setText(Messages.getString("Bold_10")); 
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.BEGINNING;
        gd.horizontalSpan = 2;
        fBoldCheckBox.setLayoutData(gd);

        Group previewGroup = new Group(colorComposite, SWT.NULL);
        previewGroup.setLayout(new GridLayout());
        previewGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        previewGroup.setText(Messages.getString("Preview_7")); 

        Control previewer = createPreviewer(previewGroup);
        gd = new GridData(GridData.FILL_BOTH);

        previewer.setLayoutData(gd);

        fSyntaxColorList.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }


            public void widgetSelected(SelectionEvent e) {
                handleSyntaxColorListSelection();
            }
        });

        foregroundColorButton.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }


            public void widgetSelected(SelectionEvent e) {
                int i = fSyntaxColorList.getSelectionIndex();
                String key = fSyntaxColorListModel[i][1];

                PreferenceConverter.setValue(fOverlayStore, key, fSyntaxForegroundColorEditor.getColorValue());
            }
        });

        fBoldCheckBox.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }


            public void widgetSelected(SelectionEvent e) {
                int i = fSyntaxColorList.getSelectionIndex();
                String key = fSyntaxColorListModel[i][1];
                fOverlayStore.setValue(key + BOLD, fBoldCheckBox.getSelection());
            }
        });

        Group exportGroup = new Group(colorComposite, SWT.NULL);
        exportGroup.setLayout(new GridLayout());
        exportGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        exportGroup.setText(Messages.getString("Export_to_Clipboard_1")); 
        Label lbt1 = new Label(exportGroup, SWT.NULL);
        lbt1.setText("Separator");
        btn1 = new Button(exportGroup, SWT.RADIO);
        btn1.setText(";");
        btn1.addSelectionListener((new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }


            public void widgetSelected(SelectionEvent e) {

                fOverlayStore.setValue(IConstants.CLIP_EXPORT_SEPARATOR, ";");
            }
        }));
        // btn1.setSelection(true);
        btn2 = new Button(exportGroup, SWT.RADIO);
        btn2.setText("|");
        btn2.addSelectionListener((new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }


            public void widgetSelected(SelectionEvent e) {

                fOverlayStore.setValue(IConstants.CLIP_EXPORT_SEPARATOR, "|");
            }
        }));

        btn3 = new Button(exportGroup, SWT.RADIO);
        btn3.setText("\\t [TAB]");

        btn3.addSelectionListener((new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }


            public void widgetSelected(SelectionEvent e) {

                fOverlayStore.setValue(IConstants.CLIP_EXPORT_SEPARATOR, "\t");
            }
        }));

        btn4 = new Button(exportGroup, SWT.CHECK);
        btn4.setText("Export column names");
        btn4.addSelectionListener((new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }


            public void widgetSelected(SelectionEvent e) {

                fOverlayStore.setValue(IConstants.CLIP_EXPORT_COLUMNS, btn4.getSelection());
            }
        }));

        initialize();
        return colorComposite;
    }


    private Control createPreviewer(Composite parent) {

        // fPreviewViewer= new SQLTextViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL
        // | SWT.BORDER,(IPreferenceStore)fOverlayStore);
        fPreviewViewer = new SQLTextViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER, (IPreferenceStore) fOverlayStore, null);

        fPreviewViewer.setEditable(false);

        String separator = System.getProperty("line.separator"); 

        String content = Messages.getString("select_*_from_MyTable_--_single_line_comment_12") + separator + Messages.getString("/*_multi_line_comment_13") + separator +  //$NON-NLS-2$
                Messages.getString("select_*_14") + separator + 
                Messages.getString("end_multi_line_comment*/_15") + separator + Messages.getString("where_A___1___16");  //$NON-NLS-2$

        IDocument document = new Document(content);

        fPreviewViewer.setDocument(document);

        fOverlayStore.addPropertyChangeListener(new IPropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent event) {
                // String p= event.getProperty();
                fPreviewViewer.invalidateTextPresentation();
            }
        });

        return fPreviewViewer.getControl();
    }


    void handleSyntaxColorListSelection() {
        int i = fSyntaxColorList.getSelectionIndex();

        String key = fSyntaxColorListModel[i][1];
        RGB rgb = PreferenceConverter.getColor(fOverlayStore, key);
        fSyntaxForegroundColorEditor.setColorValue(rgb);
        fBoldCheckBox.setSelection(fOverlayStore.getBoolean(key + BOLD));
    }


    void handleFont() {
        fFontEditor.store();
        FontData[] fData = PreferenceConverter.getFontDataArray(fOverlayStore, IConstants.FONT); 
        String des = fOverlayStore.getString(IConstants.FONT); 
        if (fData.length > 0) {
            JFaceResources.getFontRegistry().put(des, fData);
            fPreviewViewer.getControl().setFont(JFaceResources.getFontRegistry().get(des));
        }
    }


    private void initialize() {

        for (int i = 0; i < fSyntaxColorListModel.length; i++)
            fSyntaxColorList.add(fSyntaxColorListModel[i][0]);
        fSyntaxColorList.getDisplay().asyncExec(new Runnable() {

            public void run() {
                fSyntaxColorList.select(0);
                handleSyntaxColorListSelection();
            }
        });
        fFontEditor.setPreferenceStore(fOverlayStore);
        fFontEditor.setPreferenceName("font"); 
        fFontEditor.setPage(this);
        fFontEditor.load();
        FontData[] fData = PreferenceConverter.getFontDataArray(fOverlayStore, IConstants.FONT); 

        if (fData.length > 0) {
            JFaceResources.getFontRegistry().put(fData[0].toString(), fData);
            fPreviewViewer.getControl().setFont(JFaceResources.getFontRegistry().get(fData[0].toString()));
        }
        btn4.getDisplay().asyncExec(new Runnable() {

            public void run() {
                btn4.setSelection(fOverlayStore.getBoolean(IConstants.CLIP_EXPORT_COLUMNS));
            }
        });
        String sep = fOverlayStore.getString(IConstants.CLIP_EXPORT_SEPARATOR);
        if (sep.equals(";")) {
            btn1.getDisplay().asyncExec(new Runnable() {

                public void run() {
                    btn1.setSelection(true);
                    btn2.setSelection(false);
                    btn3.setSelection(false);
                }
            });
        } else if (sep.equals("|")) {
            btn1.getDisplay().asyncExec(new Runnable() {

                public void run() {
                    btn1.setSelection(false);
                    btn2.setSelection(true);
                    btn3.setSelection(false);
                }
            });
        } else if (sep.equals("\\t")) {
            btn1.getDisplay().asyncExec(new Runnable() {

                public void run() {
                    btn1.setSelection(false);
                    btn2.setSelection(false);
                    btn3.setSelection(true);
                }
            });
        }

    }


    protected void performDefaults() {
        fOverlayStore.loadDefaults();
        handleSyntaxColorListSelection();
        if (fFontEditor != null)
            fFontEditor.loadDefault();
        super.performDefaults();

        fPreviewViewer.invalidateTextPresentation();
        FontData[] fData = PreferenceConverter.getFontDataArray(fOverlayStore, IConstants.FONT); 

        if (fData.length > 0) {
            JFaceResources.getFontRegistry().put(fData[0].toString(), fData);
            fPreviewViewer.getControl().setFont(JFaceResources.getFontRegistry().get(fData[0].toString()));
        }

    }


    public void dispose() {
        /*
         * if (fOverlayStore != null) { fOverlayStore.stop(); fOverlayStore=
         * null; }
         */
        if (fFontEditor != null) {
            fFontEditor.setPreferenceStore(null);
            fFontEditor.setPage(null);
        }
        this.setPreferenceStore(null);
        super.dispose();
    }


    public boolean performOk() {
        if (fFontEditor != null)
            fFontEditor.store();
        fOverlayStore.propagate();

        return true;

    }

}
