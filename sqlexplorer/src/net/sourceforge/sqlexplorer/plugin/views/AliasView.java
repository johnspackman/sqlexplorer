package net.sourceforge.sqlexplorer.plugin.views;

/*
 * Copyright (C) 2002-2004 Andrea Mazzolini
 * andreamazzolini@users.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
import net.sourceforge.sqlexplorer.AliasModel;
import net.sourceforge.sqlexplorer.DriverModel;
import net.sourceforge.sqlexplorer.IdentifierFactory;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.SQLAlias;
import net.sourceforge.sqlexplorer.SqlexplorerImages;
import net.sourceforge.sqlexplorer.dialogs.CreateAliasDlg;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.sqlexplorer.sessiontree.actions.OpenPasswordConnectDialogAction;
import net.sourceforge.squirrel_sql.fw.persist.ValidationException;
import net.sourceforge.squirrel_sql.fw.sql.ISQLAlias;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class AliasView extends ViewPart {

    private Action _changeAliasAction;

    private Action _copyAliasAction;

    private Action _newAliasAction;

    private Action _deleteAliasAction;

    private Action _connectAliasAction;
    
    private TableViewer _tableViewer;

    private DriverModel _driverModel;

    private AliasModel _aliasModel;

    private void createActions() {

        _newAliasAction = new Action() {

            ImageDescriptor img = ImageDescriptor.createFromURL(SqlexplorerImages.getAliasWizard());

            public String getToolTipText() {
                return Messages.getString("AliasView.Actions.CreateAliasToolTip");
            }

            public String getText() {
                return Messages.getString("AliasView.Actions.CreateAlias");
            }

            public ImageDescriptor getHoverImageDescriptor() {
                return img;
            }

            public ImageDescriptor getImageDescriptor() {
                return img;
            };

            public void run() {
                IdentifierFactory factory = IdentifierFactory.getInstance();
                SQLAlias alias = _aliasModel.createAlias(factory.createIdentifier());
                CreateAliasDlg dlg = new CreateAliasDlg(getSite().getShell(), _driverModel, 1, alias, _aliasModel);
                dlg.open();
                _tableViewer.refresh();
                selectFirst();
            }
        };

        _changeAliasAction = new Action() {

            ImageDescriptor img = ImageDescriptor.createFromURL(SqlexplorerImages.getEditAlias());

            public String getText() {
                return Messages.getString("AliasView.Actions.ChangeAlias");
            }

            public String getToolTipText() {
                return Messages.getString("AliasView.Actions.ChangeAliasToolTip");
            }

            public ImageDescriptor getHoverImageDescriptor() {
                return img;
            }

            public ImageDescriptor getImageDescriptor() {
                return img;
            };

            public void run() {

                StructuredSelection sel = (StructuredSelection) _tableViewer.getSelection();
                SQLAlias al = (SQLAlias) sel.getFirstElement();
                if (al != null) {
                    CreateAliasDlg dlg = new CreateAliasDlg(getSite().getShell(), _driverModel, 2, al, _aliasModel);
                    dlg.open();
                    _tableViewer.refresh();
                    selectFirst();
                }
            }

        };

        _copyAliasAction = new Action() {

            ImageDescriptor img = ImageDescriptor.createFromURL(SqlexplorerImages.getCopyAlias());

            public String getToolTipText() {
                return Messages.getString("AliasView.Actions.CopyAliasToolTip");
            }

            public String getText() {
                return Messages.getString("AliasView.Actions.CopyAlias");
            }

            public ImageDescriptor getHoverImageDescriptor() {
                return img;
            }

            public ImageDescriptor getImageDescriptor() {
                return img;
            };

            public void run() {

                StructuredSelection sel = (StructuredSelection) _tableViewer.getSelection();
                SQLAlias al = (SQLAlias) sel.getFirstElement();
                IdentifierFactory factory = IdentifierFactory.getInstance();
                SQLAlias alias = _aliasModel.createAlias(factory.createIdentifier());
                if (al != null) {
                    try {
                        alias.assignFrom(al);
                    } catch (ValidationException e) {
                    }
                    CreateAliasDlg dlg = new CreateAliasDlg(getSite().getShell(), _driverModel, 3, alias, _aliasModel);
                    dlg.open();
                    _tableViewer.refresh();
                    selectFirst();
                }
            }
        };

        _deleteAliasAction = new Action() {

            ImageDescriptor img = ImageDescriptor.createFromURL(SqlexplorerImages.getDeleteAlias());

            public String getToolTipText() {
                return Messages.getString("AliasView.Actions.DeleteAliasToolTip");
            }

            public String getText() {
                return Messages.getString("AliasView.Actions.DeleteAlias");
            }
            
            public ImageDescriptor getHoverImageDescriptor() {
                return img;
            }

            public ImageDescriptor getImageDescriptor() {
                return img;
            };

            public void run() {

                StructuredSelection sel = (StructuredSelection) _tableViewer.getSelection();
                SQLAlias al = (SQLAlias) sel.getFirstElement();
                if (al != null) {
                    _aliasModel.removeAlias(al);
                    _tableViewer.refresh();
                    selectFirst();
                }
            }
        };

        
        _connectAliasAction = new Action() {

            ImageDescriptor img = ImageDescriptor.createFromURL(SqlexplorerImages.getConnectionIcon());

            public ImageDescriptor getHoverImageDescriptor() {
                return img;
            }

            public ImageDescriptor getImageDescriptor() {
                return img;
            };
            
            public String getText() {
                return Messages.getString("AliasView.Actions.ConnectAlias");
            }
            
            public String getToolTipText() {
                return Messages.getString("AliasView.Actions.ConnectAliasToolTip");
            }

            public void run() {

                ISQLAlias al = (ISQLAlias) ((IStructuredSelection) _tableViewer.getSelection()).getFirstElement();
                OpenPasswordConnectDialogAction openDlgAction = new OpenPasswordConnectDialogAction(getSite().getShell(),
                        al, _driverModel, SQLExplorerPlugin.getDefault().getPreferenceStore(), SQLExplorerPlugin.getDefault().getSQLDriverManager());
                openDlgAction.run();
            }
        };
        
    }

    /**
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {

        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, SQLExplorerPlugin.PLUGIN_ID + ".AliasView");
        
        _driverModel = SQLExplorerPlugin.getDefault().getDriverModel();
        _aliasModel = SQLExplorerPlugin.getDefault().getAliasModel();

        createActions();

        IToolBarManager toolBarMgr = getViewSite().getActionBars().getToolBarManager();

        toolBarMgr.add(_newAliasAction);
        toolBarMgr.add(_changeAliasAction);
        toolBarMgr.add(_copyAliasAction);
        toolBarMgr.add(_deleteAliasAction);

        _tableViewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
        //_tableViewer.getControl().setLayoutData(gid);
        _tableViewer.setContentProvider(new AliasContentProvider());
        _tableViewer.setLabelProvider(new AliasLabelProvider());
        _tableViewer.setInput(_aliasModel);

        _tableViewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                if (selection != null) {
                    SQLAlias al = (SQLAlias) selection.getFirstElement();
                    OpenPasswordConnectDialogAction openDlgAction = new OpenPasswordConnectDialogAction(
                            _tableViewer.getTable().getShell(), al, _driverModel,
                            SQLExplorerPlugin.getDefault().getPreferenceStore(),
                            SQLExplorerPlugin.getDefault().getSQLDriverManager());
                    openDlgAction.run();

                }
            }
        });

        selectFirst();

        final Table table = _tableViewer.getTable();
        MenuManager menuMgr = new MenuManager("#AliasMenu");
        menuMgr.setRemoveAllWhenShown(false);
        menuMgr.add(_connectAliasAction);
        menuMgr.add(_newAliasAction);
        menuMgr.add(_changeAliasAction);
        menuMgr.add(_copyAliasAction);
        menuMgr.add(_deleteAliasAction);

        Menu aliasContextMenu = menuMgr.createContextMenu(table);
        _tableViewer.getTable().setMenu(aliasContextMenu);

        parent.layout();

    }

    /**
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {

    }

    protected void selectFirst() {
        if (_aliasModel.getElements().length > 0) {
            Object obj = (_aliasModel.getElements())[0];
            StructuredSelection sel = new StructuredSelection(obj);
            _tableViewer.setSelection(sel);
        }
    }
}

class AliasContentProvider implements IStructuredContentProvider {

    AliasModel iResource;

    public Object[] getElements(Object input) {

        return ((AliasModel) input).getElements();
    }

    public void dispose() {

    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

    }
}

class AliasLabelProvider extends LabelProvider implements ITableLabelProvider {

    private Image _img = ImageDescriptor.createFromURL(SqlexplorerImages.getAliasIcon()).createImage();
    
    AliasLabelProvider() {

    };

    public Image getColumnImage(Object elementx, int i) {
        return _img;
    }

    public String getColumnText(Object element, int i) {

        ISQLAlias al = (ISQLAlias) element;
        return al.getName();
    }

    public boolean isLabelProperty(Object element, String property) {
        return true;
    }

    public void dispose() {

    }

    public void removeListener(ILabelProviderListener listener) {

    }

    public void addListener(ILabelProviderListener listener) {

    }

}