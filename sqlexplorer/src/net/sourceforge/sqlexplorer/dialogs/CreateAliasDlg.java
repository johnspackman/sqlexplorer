package net.sourceforge.sqlexplorer.dialogs;

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

import net.sourceforge.sqlexplorer.AliasModel;
import net.sourceforge.sqlexplorer.DriverModel;
import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.IdentifierFactory;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.SQLAlias;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.util.ImageUtil;
import net.sourceforge.squirrel_sql.fw.persist.ValidationException;
import net.sourceforge.squirrel_sql.fw.sql.ISQLDriver;
import net.sourceforge.squirrel_sql.fw.util.DuplicateObjectException;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Modified by Davy Vanherbergen to include metadata filter expression.
 * 
 */
public class CreateAliasDlg extends TitleAreaDialog {

    Button _btnAutoLogon;

    private SQLAlias alias;

    private AliasModel aliasModel;

    Button btnActivate;

    Combo combo;

    DriverModel driverModel;

    Text nameField;

    Text passwordField;

    int type;

    Text urlField;

    Text userField;

    private static final int SIZING_TEXT_FIELD_WIDTH = 250;


    public CreateAliasDlg(Shell parentShell, DriverModel dm, int type, SQLAlias al, AliasModel am) {

        super(parentShell);
        driverModel = dm;
        alias = al;
        aliasModel = am;
        this.type = type;
    }


    protected void configureShell(Shell shell) {

        super.configureShell(shell);
        if (type == 1) {
            shell.setText(Messages.getString("AliasDialog.Create.Title")); //$NON-NLS-1$
        } else if (type == 2) {
            shell.setText(Messages.getString("AliasDialog.Change.Title")); //$NON-NLS-1$
        } else if (type == 3) {
            shell.setText(Messages.getString("AliasDialog.Copy.Title")); //$NON-NLS-1$
        }
    }


    protected void createButtonsForButtonBar(Composite parent) {

        super.createButtonsForButtonBar(parent);
        validate();
    }


    protected Control createContents(Composite parent) {

        Control contents = super.createContents(parent);

        if (type == 1) {
            setTitle(Messages.getString("AliasDialog.Create.Title")); //$NON-NLS-1$
            setMessage("Create a new alias"); //$NON-NLS-1$
        } else if (type == 2) {
            setTitle(Messages.getString("AliasDialog.Change.Title")); //$NON-NLS-1$
            setMessage("Modify the alias"); //$NON-NLS-1$			
        } else if (type == 3) {
            setTitle(Messages.getString("AliasDialog.Copy.Title")); //$NON-NLS-1$
            setMessage("Copy the alias"); //$NON-NLS-1$						
        }

        Image image = ImageUtil.getImage("Images.WizardLogo");
        if (image != null) {
            setTitleImage(image);
        }
        contents.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {

                ImageUtil.disposeImage("Images.WizardLogo");
            }
        });
        return contents;
    }


    protected Control createDialogArea(Composite parent) {

        // top level composite
        Composite parentComposite = (Composite) super.createDialogArea(parent);

        // create a composite with standard margins and spacing
        Composite composite = new Composite(parentComposite, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setFont(parentComposite.getFont());

        Composite nameGroup = new Composite(composite, SWT.NONE);
        layout = new GridLayout();
        layout.numColumns = 3;
        layout.marginWidth = 10;
        nameGroup.setLayout(layout);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        nameGroup.setLayoutData(data);

        Label label = new Label(nameGroup, SWT.WRAP);
        label.setText("Name"); //$NON-NLS-1$
        nameField = new Text(nameGroup, SWT.BORDER);
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        data.horizontalSpan = 2;
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        nameField.setLayoutData(data);
        nameField.addKeyListener(new KeyListener() {

            public void keyPressed(org.eclipse.swt.events.KeyEvent e) {

                CreateAliasDlg.this.validate();
            };


            public void keyReleased(org.eclipse.swt.events.KeyEvent e) {

                CreateAliasDlg.this.validate();
            };
        });

        Label label2 = new Label(nameGroup, SWT.WRAP);
        label2.setText("Driver"); //$NON-NLS-1$
        combo = new Combo(nameGroup, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;

        int size = driverModel.size();
        String defaultDriverName = SQLExplorerPlugin.getDefault().getPluginPreferences().getString(IConstants.DEFAULT_DRIVER);
        int defaultDriver = 0;
        for (int i = 0; i < size; i++) {
            String driverName = driverModel.getElement(i).toString();
            if (driverName.startsWith(defaultDriverName)) {
                defaultDriver = i;
            }
            combo.add(driverName);
        }
        combo.setLayoutData(data);

        Button button = new Button(nameGroup, SWT.NULL);
        button.setText(Messages.getString("New_Driver..._9")); //$NON-NLS-1$
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        button.setLayoutData(data);
        button.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent event) {

                final IdentifierFactory factory = IdentifierFactory.getInstance();
                final ISQLDriver driver = driverModel.createDriver(factory.createIdentifier());

                CreateDriverDlg dlg = new CreateDriverDlg(CreateAliasDlg.this.getShell(), driverModel, 1, driver);
                dlg.open();
                combo.removeAll();
                int size = driverModel.size();
                for (int i = 0; i < size; i++)
                    combo.add(driverModel.getElement(i).toString());
            }
        });

        Label label3 = new Label(nameGroup, SWT.WRAP);
        label3.setText("URL"); //$NON-NLS-1$
        urlField = new Text(nameGroup, SWT.BORDER);
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        data.horizontalSpan = 2;
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        urlField.setLayoutData(data);
        urlField.addKeyListener(new KeyListener() {

            public void keyPressed(org.eclipse.swt.events.KeyEvent e) {

                CreateAliasDlg.this.validate();
            };


            public void keyReleased(org.eclipse.swt.events.KeyEvent e) {

                CreateAliasDlg.this.validate();
            };
        });

        Label label4 = new Label(nameGroup, SWT.WRAP);
        label4.setText("User Name"); //$NON-NLS-1$
        userField = new Text(nameGroup, SWT.BORDER);
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        data.horizontalSpan = 2;
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        userField.setLayoutData(data);

        userField.addKeyListener(new KeyListener() {

            public void keyPressed(org.eclipse.swt.events.KeyEvent e) {

                CreateAliasDlg.this.validate();
            };


            public void keyReleased(org.eclipse.swt.events.KeyEvent e) {

                CreateAliasDlg.this.validate();
            };
        });

        Label label5 = new Label(nameGroup, SWT.WRAP);
        label5.setText("Password"); //$NON-NLS-1$
        passwordField = new Text(nameGroup, SWT.BORDER);
        passwordField.setEchoChar('*');
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        data.horizontalSpan = 2;
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        passwordField.setLayoutData(data);

        
        Label label8 = new Label(nameGroup, SWT.WRAP);
        label8.setText(Messages.getString("AliasDialog.AutoLogon"));
        label8.setToolTipText(Messages.getString("AliasDialog.AutoLogonToolTip"));

        _btnAutoLogon = new Button(nameGroup, SWT.CHECK);
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        data.horizontalSpan = 2;
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        _btnAutoLogon.setLayoutData(data);
        
        Label label7 = new Label(nameGroup, SWT.WRAP);
        label7.setText("Open on Startup"); //$NON-NLS-1$
        btnActivate = new Button(nameGroup, SWT.CHECK);
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        data.horizontalSpan = 2;
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        btnActivate.setLayoutData(data);

        _btnAutoLogon.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent event) {

                boolean active = ((Button)event.widget).getSelection();
                btnActivate.setEnabled(active);
               if (!active) {
                   btnActivate.setSelection(false);
               }
            }
        });
        
        

        combo.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {

            public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {

            }


            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {

                int selIndex = combo.getSelectionIndex();
                urlField.setText(driverModel.getElement(selIndex).getUrl());
                CreateAliasDlg.this.validate();
            };
        });
        
        if (size > 0) {            
            combo.select(defaultDriver);
            urlField.setText(driverModel.getElement(defaultDriver).getUrl());
        }     
        
        
        
        loadData();
        return parentComposite;
        
        
        
    }


    private void loadData() {

        nameField.setText(alias.getName());
        userField.setText(alias.getUserName());
        passwordField.setText(alias.getPassword());
        _btnAutoLogon.setSelection(alias.isAutoLogon());
        if(!alias.isAutoLogon()) {
            btnActivate.setEnabled(false);
            btnActivate.setSelection(false);
        } else {
            btnActivate.setSelection(alias.isConnectAtStartup());
        }
        
        if (type != 1) {
            ISQLDriver iSqlDriver = driverModel.getDriver(alias.getDriverIdentifier());
            combo.setText(iSqlDriver.getName());
            urlField.setText(alias.getUrl());
        }
    }


    protected void okPressed() {

        try {
            alias.setName(nameField.getText().trim());
            int selIndex = combo.getSelectionIndex();
            alias.setDriverIdentifier(driverModel.getElement(selIndex).getIdentifier());
            alias.setUrl(urlField.getText().trim());
            alias.setUserName(userField.getText().trim());
            alias.setName(this.nameField.getText().trim());
            alias.setSchemaFilterExpression("");
            alias.setNameFilterExpression("");
            alias.setFolderFilterExpression("");
            alias.setConnectAtStartup(btnActivate.getSelection());
            alias.setPassword(passwordField.getText().trim());
            alias.setAutoLogon(_btnAutoLogon.getSelection());
            if ((this.type == 1) || (type == 3)) {
                aliasModel.addAlias(alias);
            }
        } catch (ValidationException excp) {
            SQLExplorerPlugin.error("Validation Exception", excp);//$NON-NLS-1$
            // System.out.println(Messages.getString("Error_Validation_Exception_4"));//$NON-NLS-1$
        } catch (DuplicateObjectException excp1) {
            SQLExplorerPlugin.error("Duplicate Exception", excp1);//$NON-NLS-1$
            // System.out.println(Messages.getString("Error_DuplicateObjectException_5"));//$NON-NLS-1$
        }
        close();
    }


    protected void setDialogComplete(boolean value) {

        Button okBtn = getButton(IDialogConstants.OK_ID);
        if (okBtn != null)
            okBtn.setEnabled(value);
    }


    protected void setShellStyle(int newShellStyle) {

        super.setShellStyle(newShellStyle | SWT.RESIZE);// Make the dialog
        // resizable
    }


    void validate() {

        if ((urlField.getText().trim().length() > 0) && (nameField.getText().trim().length() > 0))
            setDialogComplete(true);
        else
            setDialogComplete(false);
    }

}
