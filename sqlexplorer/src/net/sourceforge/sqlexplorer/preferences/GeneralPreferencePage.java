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

import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class GeneralPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private StringFieldEditor _altQryDelimiterField;

    private StringFieldEditor _commentDelimiterField;

    private StringFieldEditor _qryDelimiterField;

    Button _wordWrapButton;
    
    Button _autoOpenEditorButton;

    Button fAssistance;

    Button fAutoCommitBox;

    Button fCommitOnCloseBox;

    public final OverlayPreferenceStore.OverlayKey[] fKeys = new OverlayPreferenceStore.OverlayKey[] {

    new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, IConstants.PRE_ROW_COUNT),
            new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, IConstants.MAX_SQL_ROWS),
            new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IConstants.AUTO_COMMIT),
            new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IConstants.COMMIT_ON_CLOSE),
            new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IConstants.SQL_ASSIST),
            new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IConstants.SQL_QRY_DELIMITER),
            new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IConstants.SQL_ALT_QRY_DELIMITER),
            new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IConstants.SQL_COMMENT_DELIMITER),
            new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IConstants.WORD_WRAP),
            new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IConstants.AUTO_OPEN_EDITOR)};

    private IntegerFieldEditor fMaxSqlRowEditor;

    OverlayPreferenceStore fOverlayStore;

    private IntegerFieldEditor fPreviewRowCountEditor;


    public GeneralPreferencePage() {

        fOverlayStore = new OverlayPreferenceStore(SQLExplorerPlugin.getDefault().getPreferenceStore(), fKeys);

        fOverlayStore.load();
        fOverlayStore.start();
    };


    public GeneralPreferencePage(OverlayPreferenceStore fOverlayStore) {

        this.setTitle(Messages.getString("General_Preferences_1")); //$NON-NLS-1$
        this.fOverlayStore = fOverlayStore;
    }


    protected Control createContents(Composite parent) {

        Composite colorComposite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.marginHeight = 0;
        layout.marginWidth = 0;

        colorComposite.setLayout(layout);
        fPreviewRowCountEditor = new IntegerFieldEditor(IConstants.PRE_ROW_COUNT,
                Messages.getString("Preview_Max_Rows_3"), colorComposite); //$NON-NLS-1$ //$NON-NLS-2$
        fPreviewRowCountEditor.setValidRange(1, 100);
        fPreviewRowCountEditor.setErrorMessage(Messages.getString("Accepted_Range_is__1_-_100_1")); //$NON-NLS-1$

        fMaxSqlRowEditor = new IntegerFieldEditor(IConstants.MAX_SQL_ROWS,
                Messages.getString("SQL_Limit_Rows_2"), colorComposite); //$NON-NLS-1$  //$NON-NLS-2$
        fMaxSqlRowEditor.setValidRange(100, 5000);
        fMaxSqlRowEditor.setErrorMessage(Messages.getString("Accepted_Range_is__100_-_5000_3")); //$NON-NLS-1$

        fAutoCommitBox = new Button(colorComposite, SWT.CHECK);
        fAutoCommitBox.setText(Messages.getString("GeneralPreferencePage.AutoCommit_1")); //$NON-NLS-1$
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.BEGINNING;
        gd.horizontalSpan = 2;
        fAutoCommitBox.setLayoutData(gd);

        fCommitOnCloseBox = new Button(colorComposite, SWT.CHECK);
        fCommitOnCloseBox.setText(Messages.getString("GeneralPreferencePage.Commit_On_Close_2")); //$NON-NLS-1$
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.BEGINNING;
        gd.horizontalSpan = 2;
        fCommitOnCloseBox.setLayoutData(gd);

        fAssistance = new Button(colorComposite, SWT.CHECK);
        fAssistance.setText(Messages.getString("GeneralPreferencePage.Tables_and_columns_auto-completing_assistance._Use_only_with_fast_database_connections_1")); //$NON-NLS-1$
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.BEGINNING;
        gd.horizontalSpan = 2;
        fAssistance.setLayoutData(gd);

        _qryDelimiterField = new StringFieldEditor(IConstants.SQL_QRY_DELIMITER,
                Messages.getString("Preferences.SQLExplorer.QueryDelimiter"), colorComposite);
        _qryDelimiterField.setEmptyStringAllowed(false);
        _qryDelimiterField.setTextLimit(1);
        _qryDelimiterField.setErrorMessage(Messages.getString("Preferences.SQLExplorer.QueryDelimiter.Error"));

        _altQryDelimiterField = new StringFieldEditor(IConstants.SQL_ALT_QRY_DELIMITER,
                Messages.getString("Preferences.SQLExplorer.AltQueryDelimiter"), colorComposite);
        _altQryDelimiterField.setEmptyStringAllowed(true);
        _altQryDelimiterField.setTextLimit(4);

        _commentDelimiterField = new StringFieldEditor(IConstants.SQL_COMMENT_DELIMITER,
                Messages.getString("Preferences.SQLExplorer.CommentDelimiter"), colorComposite);
        _commentDelimiterField.setEmptyStringAllowed(false);
        _commentDelimiterField.setTextLimit(4);
        _commentDelimiterField.setErrorMessage(Messages.getString("Preferences.SQLExplorer.CommentDelimiter.Error"));

        _wordWrapButton = new Button(colorComposite, SWT.CHECK);
        _wordWrapButton.setText(Messages.getString("Preferences.SQLExplorer.WordWrap"));
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.BEGINNING;
        gd.horizontalSpan = 2;
        _wordWrapButton.setLayoutData(gd);

        _autoOpenEditorButton = new Button(colorComposite, SWT.CHECK);
        _autoOpenEditorButton.setText(Messages.getString("Preferences.SQLExplorer.OpenEditorOnConnection"));
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.BEGINNING;
        gd.horizontalSpan = 2;
        _autoOpenEditorButton.setLayoutData(gd);
        
        fAutoCommitBox.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {

            }


            public void widgetSelected(SelectionEvent e) {

                fOverlayStore.setValue(IConstants.AUTO_COMMIT, fAutoCommitBox.getSelection());
                if (fAutoCommitBox.getSelection()) {
                    fCommitOnCloseBox.setEnabled(false);
                } else
                    fCommitOnCloseBox.setEnabled(true);
            }
        });

        fCommitOnCloseBox.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {

            }


            public void widgetSelected(SelectionEvent e) {

                fOverlayStore.setValue(IConstants.COMMIT_ON_CLOSE, fCommitOnCloseBox.getSelection());
            }
        });

        fAssistance.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {

            }


            public void widgetSelected(SelectionEvent e) {

                fOverlayStore.setValue(IConstants.SQL_ASSIST, fAssistance.getSelection());
            }
        });

        _wordWrapButton.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {

            }


            public void widgetSelected(SelectionEvent e) {

                fOverlayStore.setValue(IConstants.WORD_WRAP, _wordWrapButton.getSelection());
            }
        });

        _autoOpenEditorButton.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {

            }


            public void widgetSelected(SelectionEvent e) {

                fOverlayStore.setValue(IConstants.AUTO_OPEN_EDITOR, _autoOpenEditorButton.getSelection());
            }
        });
        
        initialize();

        return colorComposite;
    }


    public void dispose() {

        this.setPreferenceStore(null);

        if (fPreviewRowCountEditor != null) {
            fPreviewRowCountEditor.setPreferenceStore(null);
            fPreviewRowCountEditor.setPage(null);
        }
        if (fMaxSqlRowEditor != null) {
            fMaxSqlRowEditor.setPreferenceStore(null);
            fMaxSqlRowEditor.setPage(null);
        }

        if (_qryDelimiterField != null) {
            _qryDelimiterField.setPreferenceStore(null);
            _qryDelimiterField.setPage(null);
        }

        if (_altQryDelimiterField != null) {
            _altQryDelimiterField.setPreferenceStore(null);
            _altQryDelimiterField.setPage(null);
        }

        if (_commentDelimiterField != null) {
            _commentDelimiterField.setPreferenceStore(null);
            _commentDelimiterField.setPage(null);
        }

        super.dispose();
    }


    public void init(IWorkbench workbench) {

    }


    private void initialize() {

        fMaxSqlRowEditor.setPreferenceStore(fOverlayStore);
        fMaxSqlRowEditor.setPreferenceName(IConstants.MAX_SQL_ROWS); //$NON-NLS-1$
        fMaxSqlRowEditor.setPage(this);
        fMaxSqlRowEditor.load();

        fPreviewRowCountEditor.setPreferenceStore(fOverlayStore);
        fPreviewRowCountEditor.setPreferenceName(IConstants.PRE_ROW_COUNT); //$NON-NLS-1$
        fPreviewRowCountEditor.setPage(this);
        fPreviewRowCountEditor.load();

        _qryDelimiterField.setPreferenceStore(fOverlayStore);
        _qryDelimiterField.setPreferenceName(IConstants.SQL_QRY_DELIMITER);
        _qryDelimiterField.setPage(this);
        _qryDelimiterField.load();
        if (_qryDelimiterField.getStringValue() == null || _qryDelimiterField.getStringValue().length() == 0) {
            _qryDelimiterField.loadDefault();
        }

        _altQryDelimiterField.setPreferenceStore(fOverlayStore);
        _altQryDelimiterField.setPreferenceName(IConstants.SQL_ALT_QRY_DELIMITER);
        _altQryDelimiterField.setPage(this);
        _altQryDelimiterField.load();

        _commentDelimiterField.setPreferenceStore(fOverlayStore);
        _commentDelimiterField.setPreferenceName(IConstants.SQL_COMMENT_DELIMITER);
        _commentDelimiterField.setPage(this);
        _commentDelimiterField.load();
        if (_commentDelimiterField.getStringValue() == null || _commentDelimiterField.getStringValue().length() == 0) {
            _commentDelimiterField.loadDefault();
        }

        fAutoCommitBox.getDisplay().asyncExec(new Runnable() {

            public void run() {

                fCommitOnCloseBox.setSelection(fOverlayStore.getBoolean(IConstants.COMMIT_ON_CLOSE));//$NON-NLS-1$
                fAutoCommitBox.setSelection(fOverlayStore.getBoolean(IConstants.AUTO_COMMIT));//$NON-NLS-1$
                if (fAutoCommitBox.getSelection()) {
                    fCommitOnCloseBox.setEnabled(false);
                } else
                    fCommitOnCloseBox.setEnabled(true);
            }
        });
        fAssistance.getDisplay().asyncExec(new Runnable() {

            public void run() {

                fAssistance.setSelection(fOverlayStore.getBoolean(IConstants.SQL_ASSIST));
            }
        });

        _wordWrapButton.getDisplay().asyncExec(new Runnable() {

            public void run() {

                _wordWrapButton.setSelection(fOverlayStore.getBoolean(IConstants.WORD_WRAP));
            }
        });
        
        _autoOpenEditorButton.getDisplay().asyncExec(new Runnable() {

            public void run() {

                _autoOpenEditorButton.setSelection(fOverlayStore.getBoolean(IConstants.AUTO_OPEN_EDITOR));
            }
        });

    }


    protected void performDefaults() {

        ((OverlayPreferenceStore) fOverlayStore).loadDefaults();
        if (fPreviewRowCountEditor != null) {
            fPreviewRowCountEditor.loadDefault();
        }
        if (fMaxSqlRowEditor != null) {
            fMaxSqlRowEditor.loadDefault();
        }
        if (_qryDelimiterField != null) {
            _qryDelimiterField.loadDefault();
        }
        if (_altQryDelimiterField != null) {
            _altQryDelimiterField.loadDefault();
        }
        if (_commentDelimiterField != null) {
            _commentDelimiterField.loadDefault();
        }

        super.performDefaults();
    }


    public boolean performOk() {

        if (fPreviewRowCountEditor != null) {
            fPreviewRowCountEditor.store();

        }
        if (fMaxSqlRowEditor != null) {
            fMaxSqlRowEditor.store();
        }

        _qryDelimiterField.store();
        _altQryDelimiterField.store();
        _commentDelimiterField.store();

        ((OverlayPreferenceStore) fOverlayStore).propagate();
        return true;
    }

}
