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
import java.util.HashMap;
import java.util.Iterator;

import net.sourceforge.sqlexplorer.DriverModel;
import net.sourceforge.sqlexplorer.IdentifierFactory;
import net.sourceforge.sqlexplorer.Messages;
import net.sourceforge.sqlexplorer.SqlexplorerImages;
import net.sourceforge.sqlexplorer.dialogs.CreateDriverDlg;
import net.sourceforge.sqlexplorer.plugin.SQLExplorerPlugin;
import net.sourceforge.squirrel_sql.fw.sql.ISQLDriver;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

/**
 * 
 * @author Davy Vanherbergen
 */
public class DriverView extends ViewPart implements IPersistableElement {

    private Action _newDriverAction;

    private Action _changeDriverAction;

    private Action _copyDriverAction;

    private Action _deleteDriverAction;

    private Action _restoreDriversAction;

    private Action _filterDriversAction;
    
    private DriverFilter _filter;    
    
    public static final String FACTORY_ID = "net.sourceforge.sqlexplorer.views.DriverView";
    
    public static final String FILTER_STATE = "net.sourceforge.sqlexplorer.views.DriverView.filterState";

    private boolean _filterActive = false;

    private DriverModel _driverModel;

    protected TableViewer _tableViewer;
    
    
    
    /**
     * Create all view actions.
     */
    private void createActions() {

        _newDriverAction = new Action() {

            ImageDescriptor img = ImageDescriptor.createFromURL(SqlexplorerImages.getCreateDriverIcon());

            public String getToolTipText() {
                return Messages.getString("DriverView.Actions.CreateDriverToolTip");
            }

            public String getText() {
                return Messages.getString("DriverView.Actions.CreateDriver");
            }

            public ImageDescriptor getHoverImageDescriptor() {
                return img;
            }

            public ImageDescriptor getImageDescriptor() {
                return img;
            };

            public void run() {

                final IdentifierFactory factory = IdentifierFactory.getInstance();
                final ISQLDriver driver = _driverModel.createDriver(factory.createIdentifier());

                CreateDriverDlg dlg = new CreateDriverDlg(getSite().getShell(), _driverModel, 1, driver);
                dlg.open();

                _tableViewer.refresh();
                selectFirst();
            }
        };
        
        
        _changeDriverAction = new Action() {

            ImageDescriptor img = ImageDescriptor.createFromURL(SqlexplorerImages.getEditDriver());


            public String getToolTipText() {
                return Messages.getString("DriverView.Actions.ChangeDriverToolTip");
            }


            public String getText() {
                return Messages.getString("DriverView.Actions.ChangeDriver");
            }


            public ImageDescriptor getHoverImageDescriptor() {
                return img;
            }


            public ImageDescriptor getImageDescriptor() {
                return img;
            };


            public void run() {

                StructuredSelection sel = (StructuredSelection) _tableViewer.getSelection();
                ISQLDriver dv = (ISQLDriver) sel.getFirstElement();
                if (dv != null) {
                    CreateDriverDlg dlg = new CreateDriverDlg(getSite().getShell(), _driverModel, 2, dv);
                    dlg.open();
                    _tableViewer.refresh();
                    selectFirst();
                }
            }

        };
        
        _copyDriverAction = new Action() {

            ImageDescriptor img = ImageDescriptor.createFromURL(SqlexplorerImages.getCopyDriver());


            public String getToolTipText() {
                return Messages.getString("DriverView.Actions.CopyDriverToolTip"); //$NON-NLS-1$
            }


            public String getText() {
                return Messages.getString("DriverView.Actions.CopyDriver"); //$NON-NLS-1$
            }


            public ImageDescriptor getHoverImageDescriptor() {
                return img;
            }


            public ImageDescriptor getImageDescriptor() {
                return img;
            };


            public void run() {

                StructuredSelection sel = (StructuredSelection) _tableViewer.getSelection();
                ISQLDriver dv = (ISQLDriver) sel.getFirstElement();
                if (dv != null) {
                    CreateDriverDlg dlg = new CreateDriverDlg(getSite().getShell(), _driverModel, 3, dv);
                    dlg.open();
                    _tableViewer.refresh();
                }
            }
        };
        
        
        _deleteDriverAction = new Action() {

            ImageDescriptor img = ImageDescriptor.createFromURL(SqlexplorerImages.getDeleteDriver());


            public String getToolTipText() {
                return Messages.getString("DriverView.Actions.DeleteDriverToolTip"); //$NON-NLS-1$
            }

            public String getText() {
                return Messages.getString("DriverView.Actions.DeleteDriver"); //$NON-NLS-1$
            }
            
            public ImageDescriptor getHoverImageDescriptor() {
                return img;
            }


            public ImageDescriptor getImageDescriptor() {
                return img;
            };


            public void run() {
                StructuredSelection sel = (StructuredSelection) _tableViewer.getSelection();
                ISQLDriver dv = (ISQLDriver) sel.getFirstElement();
                if (dv != null) {
                    _driverModel.removeDriver(dv);
                    _tableViewer.refresh();
                    selectFirst();
                }
            }
        };
        
        
        _restoreDriversAction = new Action() {
            
            ImageDescriptor img = ImageDescriptor.createFromURL(SqlexplorerImages.getRestoreDriver());
            
            public String getToolTipText() {
                return Messages.getString("DriverView.Actions.RestoreDriversToolTip");
            }
            
            public String getText() {
                return Messages.getString("DriverView.Actions.RestoreDrivers");
            }
            
            public ImageDescriptor getHoverImageDescriptor() {
                return img;
            }


            public ImageDescriptor getImageDescriptor() {
                return img;
            };


            public void run() {
                _driverModel.restoreDrivers();
                _tableViewer.refresh();
                selectFirst();
            }
        };
                
        
        _filterDriversAction = new Action() {
            
            ImageDescriptor img = ImageDescriptor.createFromURL(SqlexplorerImages.getFilterIcon());
            
            public String getText() {
                return Messages.getString("DriverView.Actions.FilterDrivers");
            }
            
            public ImageDescriptor getHoverImageDescriptor() {
                return img;
            }


            public ImageDescriptor getImageDescriptor() {
                return img;
            };


            public int getStyle() {
                return Action.AS_CHECK_BOX;
            }

            public void run() {
                
                if (isChecked()) {
                    _filterActive = true;
                    if (_filter == null) {
                        _filter = new DriverFilter();
                    }
                    _tableViewer.addFilter(_filter);
                } else {
                    _filterActive = false;
                    _tableViewer.removeFilter(_filter);
                }
                
                _tableViewer.refresh();
                selectFirst();
            }
            
        };

        
    }


    void selectFirst() {

        if (_driverModel.getElements().length > 0) {
            Object obj = (_driverModel.getElements())[0];
            StructuredSelection sel = new StructuredSelection(obj);
            _tableViewer.setSelection(sel);
        }
    }
    
    /**
     * @return FACTORY_ID to be used for our memento
     */
    public String getFactoryId() {        
        return FACTORY_ID;
    }


    /**
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {
        
        createActions();

        IToolBarManager toolBarMgr = getViewSite().getActionBars().getToolBarManager();
        
        toolBarMgr.add(_newDriverAction);
        toolBarMgr.add(_changeDriverAction);
        toolBarMgr.add(_copyDriverAction);
        toolBarMgr.add(_deleteDriverAction);           
        toolBarMgr.add(_restoreDriversAction);
               
        IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
        menuManager.add(_filterDriversAction);
        _filterDriversAction.setChecked(_filterActive);

        
        _driverModel = SQLExplorerPlugin.getDefault().getDriverModel();        
        _tableViewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
        
        GridData gid = new GridData();
        gid.grabExcessHorizontalSpace = gid.grabExcessVerticalSpace = true;
        gid.horizontalAlignment = gid.verticalAlignment = GridData.FILL;
        gid.horizontalSpan = 2;
        _tableViewer.getControl().setLayoutData(gid);

        _tableViewer.setContentProvider(new DriverContentProvider());
        final DriverLabelProvider dlp = new DriverLabelProvider();
        _tableViewer.setLabelProvider(dlp);
        
        _tableViewer.getTable().addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {

                dlp.dispose();

            }
        });
        _tableViewer.setInput(_driverModel);
        selectFirst();
        
        final Table table = _tableViewer.getTable();
        MenuManager menuMgr = new MenuManager("#DriverMenu"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(false);

        menuMgr.add(_newDriverAction);
        menuMgr.add(_changeDriverAction);
        menuMgr.add(_copyDriverAction);
        menuMgr.add(_deleteDriverAction);
        
        Menu driverContextMenu = menuMgr.createContextMenu(table);
        _tableViewer.getTable().setMenu(driverContextMenu);
        
        // activate filter on startup 
        if (_filterActive) {
            if (_filter == null) {
                _filter = new DriverFilter();
            }
            _tableViewer.addFilter(_filter);
        }
                        
        parent.layout();
        
        
        _tableViewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {

                StructuredSelection sel = (StructuredSelection) event.getSelection();
                ISQLDriver dv = (ISQLDriver) sel.getFirstElement();
                if (dv != null) {
                    CreateDriverDlg dlg = new CreateDriverDlg(getSite().getShell(), _driverModel, 2, dv);
                    dlg.open();
                    _tableViewer.refresh();
                    selectFirst();
                }
                
            }
         });
    }


    /**
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {

    }


    /**
     * Load filter settings.
     * 
     * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite,
     *      org.eclipse.ui.IMemento)
     */
    public void init(IViewSite site, IMemento memento) throws PartInitException {

        super.init(site, memento);

        if (memento != null) {
            Integer filterActive = memento.getInteger(FILTER_STATE);
            if (filterActive != null && filterActive.intValue() > 0) {
                _filterActive = true;
            }
        }

    }


    /**
     * We save state of our filter.
     * 
     * @see org.eclipse.ui.IPersistable#saveState(org.eclipse.ui.IMemento)
     */
    public void saveState(IMemento memento) {

        super.saveState(memento);

        int state = 0;
        if (_filterActive) {
            state = 1;
        }

        memento.putInteger(FILTER_STATE, state);

    }

}

class DriverContentProvider implements IStructuredContentProvider {

    public Object[] getElements(Object input) {

        return ((DriverModel) input).getElements();
    }


    public void dispose() {}


    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
}

class DriverLabelProvider extends LabelProvider implements ITableLabelProvider {

    private HashMap imageCache = new HashMap(11);


    DriverLabelProvider() {

    };

    ImageDescriptor okDescriptor = ImageDescriptor.createFromURL(SqlexplorerImages.getOkDriver());

    ImageDescriptor errDescriptor = ImageDescriptor.createFromURL(SqlexplorerImages.getErrorDriver());


    public Image getColumnImage(Object element, int i) {

        ISQLDriver dv = (ISQLDriver) element;
        ImageDescriptor descriptor = null;
        if (dv.isJDBCDriverClassLoaded() == true)
            descriptor = okDescriptor;
        else
            descriptor = errDescriptor;
        Image image = (Image) imageCache.get(descriptor);
        if (image == null) {
            image = descriptor.createImage();
            imageCache.put(descriptor, image);
        }
        return image;
    }


    public String getColumnText(Object element, int i) {

        ISQLDriver dv = (ISQLDriver) element;
        return dv.getName();
    }


    public boolean isLabelProperty(Object element, String property) {

        return true;
    }


    public void dispose() {

        for (Iterator i = imageCache.values().iterator(); i.hasNext();) {
            ((Image) i.next()).dispose();
        }
        imageCache.clear();
    }


    public void removeListener(ILabelProviderListener listener) {

    }


    public void addListener(ILabelProviderListener listener) {

    }
}

/**
 * Filter for the Drivers view.  This filter allows to hide
 * any non-active drivers.
 * 
 * @author Davy Vanherbergen
 */
class DriverFilter extends ViewerFilter {


    /**
     * Check if a driver it should be included in the filter.
     * @return true when a driver is loaded. 
     * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    public boolean select(Viewer viewer, Object parentElement, Object element) {

        ISQLDriver driver = (ISQLDriver) element;        
        return driver.isJDBCDriverClassLoaded();
        
    }

}


