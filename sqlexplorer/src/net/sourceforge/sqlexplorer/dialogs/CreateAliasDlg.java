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
package net.sourceforge.sqlexplorer.dialogs;

import java.util.HashMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import net.sourceforge.sqlexplorer.ExplorerException;
import net.sourceforge.sqlexplorer.IConstants;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.dbproduct.Alias;
import net.sourceforge.sqlexplorer.dbproduct.DriverManager;
import net.sourceforge.sqlexplorer.dbproduct.ManagedDriver;
import net.sourceforge.sqlexplorer.dbproduct.User;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.preferences.DriverPreferencePage;
import net.sourceforge.sqlexplorer.util.ImageUtil;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.PreferenceDialog;
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
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * Modified by Davy Vanherbergen to include metadata filter expression.
 * 
 */
public class CreateAliasDlg extends TitleAreaDialog {
	
    private static final int SIZING_TEXT_FIELD_WIDTH = 250;

	public enum Type {
		CREATE, CHANGE, COPY
	}

    private Type type;
    private Alias alias;
    private HashMap<Integer, ManagedDriver> comboDriverIndexes = new HashMap<Integer, ManagedDriver>();

    private Text nameField;
    private Combo cboDriver;
    private Text urlField;
    private Button btnNoUsername;
    private Text userField;
    private Text passwordField;
    private Button btnAutoLogon;
    private Button btnActivate;

    public CreateAliasDlg(Shell parentShell, Type type, Alias alias) {
        super(parentShell);
        this.alias = alias;
        this.type = type;
    }


    protected void configureShell(Shell shell) {

        super.configureShell(shell);
        if (type == Type.CREATE) {
            shell.setText(Messages.getString("AliasDialog.Create.Title")); //$NON-NLS-1$
        } else if (type == Type.CHANGE) {
            shell.setText(Messages.getString("AliasDialog.Change.Title")); //$NON-NLS-1$
        } else if (type == Type.COPY) {
            shell.setText(Messages.getString("AliasDialog.Copy.Title")); //$NON-NLS-1$
        }
    }


    protected void createButtonsForButtonBar(Composite parent) {

        super.createButtonsForButtonBar(parent);
        validate();
    }


    protected Control createContents(Composite parent) {

        Control contents = super.createContents(parent);

        if (type == Type.CREATE) {
            setTitle(Messages.getString("AliasDialog.Create.Title")); //$NON-NLS-1$
            setMessage("Create a new alias"); //$NON-NLS-1$
        } else if (type == Type.CHANGE) {
            setTitle(Messages.getString("AliasDialog.Change.Title")); //$NON-NLS-1$
            setMessage("Modify the alias"); //$NON-NLS-1$			
        } else if (type == Type.COPY) {
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
        GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        data.heightHint = 203;
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
        cboDriver = new Combo(nameGroup, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
        final GridData gd_driver = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        gd_driver.widthHint = SIZING_TEXT_FIELD_WIDTH;
        cboDriver.setLayoutData(gd_driver);

        String defaultDriverName = SQLExplorerPlugin.getDefault().getPluginPreferences().getString(IConstants.DEFAULT_DRIVER);
        ManagedDriver defaultDriver = null;
        int defaultDriverIndex = 0;
        populateCombo();
        for (Entry<Integer, ManagedDriver> entry : comboDriverIndexes.entrySet()) {
        	ManagedDriver driver = entry.getValue();
        	if (driver.getName().startsWith(defaultDriverName)) {
        		defaultDriver = driver;
        		defaultDriverIndex = entry.getKey();
        		break;
        	}
        }

        Button btnListDrivers = new Button(nameGroup, SWT.NULL);
        btnListDrivers.setText(Messages.getString("AliasDialog.EditDrivers")); //$NON-NLS-1$
        final GridData gd_btnListDrivers = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        btnListDrivers.setLayoutData(gd_btnListDrivers);
        btnListDrivers.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent event) {

            	PreferenceDialog dlg = PreferencesUtil.createPreferenceDialogOn(getShell(), null, new String[] { DriverPreferencePage.class.getName() }, null);
                if (dlg.open() == IDialogConstants.OK_ID)
                	populateCombo();
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
        new Label(nameGroup, SWT.NONE);

        btnNoUsername = new Button(nameGroup, SWT.CHECK);
        btnNoUsername.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(final SelectionEvent e) {
        		userField.setEnabled(!btnNoUsername.getSelection());
        		passwordField.setEnabled(!btnNoUsername.getSelection());
        	}
        });
        btnNoUsername.setText("Username is not required for this database");
        new Label(nameGroup, SWT.NONE);

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

        btnAutoLogon = new Button(nameGroup, SWT.CHECK);
        final GridData gd_btnAutoLogon = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        gd_btnAutoLogon.horizontalSpan = 2;
        gd_btnAutoLogon.widthHint = SIZING_TEXT_FIELD_WIDTH;
        btnAutoLogon.setLayoutData(gd_btnAutoLogon);
        
        Label label7 = new Label(nameGroup, SWT.WRAP);
        label7.setText("Open on Startup"); //$NON-NLS-1$
        btnActivate = new Button(nameGroup, SWT.CHECK);
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        data.horizontalSpan = 2;
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        btnActivate.setLayoutData(data);

        btnAutoLogon.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				boolean active = ((Button) event.widget).getSelection();
				btnActivate.setEnabled(active);
				if (!active) {
					btnActivate.setSelection(false);
				}
			}
		});

        cboDriver.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                int selIndex = cboDriver.getSelectionIndex();
                ManagedDriver driver = comboDriverIndexes.get(selIndex);
                urlField.setText(driver.getUrl());
                CreateAliasDlg.this.validate();
            };
        });
        
        if (!comboDriverIndexes.isEmpty() && defaultDriver != null) {            
            cboDriver.select(defaultDriverIndex);
            urlField.setText(defaultDriver.getUrl());
        }     
        
        loadData();
        return parentComposite;
    }

    private void populateCombo() {
    	String previous = cboDriver.getText();
    	if (previous != null) {
    		previous = previous.trim();
    		if (previous.length() == 0)
    			previous = null;
    	}
    	if (previous != null)
    		previous = previous.toLowerCase();
        DriverManager driverModel = SQLExplorerPlugin.getDefault().getDriverModel();
        cboDriver.removeAll();
        TreeSet<ManagedDriver> drivers = new TreeSet<ManagedDriver>();
        drivers.addAll(driverModel.getDrivers());
        int index = 0;
        for (ManagedDriver driver : drivers) {
            try {
            	driver.registerSQLDriver();
            } catch(ClassNotFoundException e) {
            	// Nothing
            }
            if (driver.isDriverClassLoaded() == true) {
	            cboDriver.add(driver.getName());
	            comboDriverIndexes.put(new Integer(index), driver);
	            if (previous != null && driver.getName().toLowerCase().startsWith(previous))
	            	cboDriver.select(index);
	            index++;
            }
        }
    }

    private void loadData() {

    	if (type != Type.CREATE)
    		nameField.setText(alias.getName());
    	if (alias.hasNoUserName()) {
    		btnNoUsername.setSelection(true);
    		userField.setEnabled(false);
    		passwordField.setEnabled(false);
    	} else {
    		btnNoUsername.setSelection(false);
    		userField.setEnabled(true);
    		passwordField.setEnabled(true);
	        if (alias.getDefaultUser() != null) {
		        userField.setText(alias.getDefaultUser().getUserName());
		        passwordField.setText(alias.getDefaultUser().getPassword());
	        }
    	}
        btnAutoLogon.setSelection(alias.isAutoLogon());
        if(!alias.isAutoLogon()) {
            btnActivate.setEnabled(false);
            btnActivate.setSelection(false);
        } else {
            btnActivate.setSelection(alias.isConnectAtStartup());
        }
        
        if (type != Type.CREATE) {
        	if (alias.getDriver() != null)
        		cboDriver.setText(alias.getDriver().getName());
            urlField.setText(alias.getUrl());
        }
    }


    protected void okPressed() {

        try {
        	User previousUser = alias.getDefaultUser();
        	
            alias.setName(nameField.getText().trim());
            int selIndex = cboDriver.getSelectionIndex();
            ManagedDriver driver = comboDriverIndexes.get(selIndex);
            alias.setDriver(driver);
            alias.setUrl(urlField.getText().trim());
            if (btnNoUsername.getSelection())
            	alias.setHasNoUserName(true);
            else {
            	alias.setHasNoUserName(false);
	            if (userField.getText().trim().length() > 0)
	            	alias.setDefaultUser(new User(userField.getText().trim(), passwordField.getText().trim()));
            }
            alias.setName(this.nameField.getText().trim());
            alias.setSchemaFilterExpression("");
            alias.setNameFilterExpression("");
            alias.setFolderFilterExpression("");
            alias.setConnectAtStartup(btnActivate.getSelection());
            alias.setAutoLogon(btnAutoLogon.getSelection());
            
            if (type != Type.CHANGE)
                SQLExplorerPlugin.getDefault().getAliasManager().addAlias(alias);
            
            // If we changed the default user and the previous default user is not in use,
            //	remove it (note: Alias maintains one User instance per username, merging 
            //	new additions into the existing instance which is why it is valid to compare
            //	objects even though we have explicitly created a new instance of User above)
            else if (alias.getDefaultUser() != previousUser) {
            	if (!previousUser.isInUse())
            		alias.removeUser(previousUser);
            }
            
        } catch (ExplorerException excp) {
            SQLExplorerPlugin.error("Validation Exception", excp);//$NON-NLS-1$
            // System.out.println(Messages.getString("Error_Validation_Exception_4"));//$NON-NLS-1$
        }
        
        // Notify that ther has been changes
        SQLExplorerPlugin.getDefault().getAliasManager().modelChanged();
        
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
