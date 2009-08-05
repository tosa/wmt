package net.refractions.udig.catalog.internal.wmt.ui;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.internal.wmt.WMTService;
import net.refractions.udig.catalog.internal.wmt.WMTServiceExtension;
import net.refractions.udig.catalog.internal.wmt.wmtsource.MQSource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.NASASource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.NASASourceManager;
import net.refractions.udig.catalog.internal.wmt.wmtsource.OSMCycleMapSource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.OSMMapnikSource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.OSMOsmarenderSource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.OSMSource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.WMTSource;
import net.refractions.udig.catalog.ui.AbstractUDIGImportPage;
import net.refractions.udig.catalog.ui.UDIGConnectionPage;
import net.refractions.udig.catalog.wmt.internal.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * 
 * Based on WmsWizardPage
 * <p>
 *
 * </p>
 * @author to.srwn
 * @since 1.1.0
 */
public class WMTWizardPage extends AbstractUDIGImportPage implements UDIGConnectionPage {

      
    private WMTServiceExtension serviceExtension;
    private Tree tree;
    

    public WMTWizardPage() {
        super(Messages.WMSWizardPage_title); 
        
        serviceExtension = new WMTServiceExtension();
    }

    public String getId() {
        return "net.refractions.udig.catalog.ui.WMT"; //$NON-NLS-1$
    }

    @Override
    public Collection<URL> getResourceIDs() {
        System.out.println("Collection<URL> getResourceIDs()");
        return super.getResourceIDs();
    }

    @Override
    public boolean leavingPage() {
        System.out.println("leavingPage");
        
        // Skip the resource selection wizard page
        IRunnableWithProgress runnable = new IRunnableWithProgress(){
            public void run( IProgressMonitor monitor ) throws InvocationTargetException,
                    InterruptedException {
                getWizard().getWorkflow().next();
            }
        };

        try {
            getContainer().run(true, false, runnable);
        } catch (InvocationTargetException e2) {
            throw (RuntimeException) new RuntimeException().initCause(e2);
        } catch (InterruptedException e2) {
            throw (RuntimeException) new RuntimeException().initCause(e2);
        }

        return super.leavingPage();
    }

    /**
     * Loops the tree and returns selected services.
     */
    @Override
    public Collection<IService> getServices() {
        System.out.println("Collection<IService> WMTWizardPage.getServices");
        
        Collection<IService> services = new ArrayList<IService>();
        
        for (int i = 0; i < tree.getItemCount(); i++) {
            TreeItem parentItem = tree.getItem(i);
            
            /**
             * only check children if parent is checked
             * or grayed (which means that not all children are checked)
             */
            if (parentItem.getChecked() || parentItem.getGrayed()) {
                for (int j = 0; j < parentItem.getItemCount(); j++) {
                    TreeItem childItem = parentItem.getItem(j);
                    
                    if (childItem.getChecked() && childItem.getData() != null) {
                        IService service = (IService) childItem.getData();
                        System.out.println(service.getTitle());
                        
                        services.add(service);
                    }
                    
                }
                
            }
        }
        
        return services;
    }

    /**
     * Creates a service from a given WMTSource class,
     * adds this service to a new TreeItem as data object
     * and sets the name for the TreeItem 
     *
     * @param treeItem The parent TreeItem.
     * @param sourceClass The class for which the service should be created.
     */
    private void addWMTSourceToTree(TreeItem treeItem, Class<? extends WMTSource> sourceClass) {
        TreeItem newTreeItem = new TreeItem(treeItem, SWT.NONE);
        
        WMTService service = serviceExtension.createService(sourceClass);
        
        newTreeItem.setText(service.getSource().getName());
        newTreeItem.setData(service);
    }
    
    public void createControl( Composite parent ) {
        System.out.println("createControl");
        
        // only when this is called for the first time
        if (tree != null && !tree.isDisposed()) return;
        
        // otherwise build control
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);        
        composite.setLayout(new FillLayout());

        //region Create tree component
        tree = new Tree(composite, SWT.BORDER | SWT.CHECK);
        tree.addListener(SWT.Selection, new org.eclipse.swt.widgets.Listener(){
            public void handleEvent( Event event ) {
                if (event.detail == SWT.CHECK) {
                    TreeItem item = (TreeItem) event.item;
                    
                    // Check child items
                    boolean checked = item.getChecked();
                    checkItems(item, checked);
                    checkPath(item.getParentItem(), checked, false);
                    
                    // now update the buttons
                    if (noItemChecked()) {
                        setPageComplete(false); 
                    } else {
                        setPageComplete(true);
                    }
                    getWizard().getContainer().updateButtons();
                }
            }
        });
        
        //region Add OpenStreeMap services
        TreeItem osm = new TreeItem(tree, SWT.NONE);
        osm.setText(OSMSource.NAME);

        addWMTSourceToTree(osm, OSMMapnikSource.class);
        addWMTSourceToTree(osm, OSMOsmarenderSource.class);
        addWMTSourceToTree(osm, OSMCycleMapSource.class);

        osm.setExpanded(true);
        //endregion
        
        //region Add MapQuest services
        TreeItem mq = new TreeItem(tree, SWT.NONE);
        mq.setText(MQSource.NAME);

        addWMTSourceToTree(mq, MQSource.class);

        mq.setExpanded(true);
        //endregion
        
        //region Add NASA services
        TreeItem nasa = new TreeItem(tree, SWT.NONE);
        nasa.setText("NASA");
        
        NASASourceManager nasaManager = NASASourceManager.getInstance();
        
        nasaManager.buildWizardTree(nasa);

        nasa.setExpanded(true);
        //endregion
        
        //todo: add other services
        
        // Enable first service for usability reasons 
        osm.getItems()[0].setChecked(true);
        osm.setChecked(true);
        osm.setGrayed(true);
        //endregion
        
        //region Add information textbox
        Text text = new Text (composite,    SWT.WRAP | SWT.MULTI | 
                                            SWT.BORDER | SWT.H_SCROLL |
                                            SWT.V_SCROLL | SWT.READ_ONLY);
        text.setText ("Space for general / copyright information / map samples / ..");
        //endregion


        composite.pack();

      }
    
    //region GUI helper methods    
    /**
     * Loops the tree and counts checked items
     * 
     * @return (selectedItemCount <= 0)
     */
    private boolean noItemChecked() {
        int selectedItemCount = 0;
        
        for (int i = 0; i < tree.getItemCount(); i++) {
            TreeItem parentItem = tree.getItem(i);
            
            if (parentItem.getChecked() || parentItem.getGrayed()) {
                for (int j = 0; j < parentItem.getItemCount(); j++) {
                    TreeItem childItem = parentItem.getItem(j);
                    
                    if (childItem.getChecked()) {
                        selectedItemCount++;
                    }                    
                }                
            }
        }           
            
        return selectedItemCount <= 0;
    }
    
    //region Check children when parent is checked
    /**
     * GUI: helper method for Tree
     * 
     * (Un-)checks all children of an item recursive.
     * 
     * http://dev.eclipse.org/viewcvs/index.cgi/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet274.java?view=co
     *
     * @param item
     * @param checked
     * @param grayed
     */
    private void checkPath(TreeItem item, boolean checked, boolean grayed) {
        if (item == null) return;
        if (grayed) {
            checked = true;
        } else {
            int index = 0;
            TreeItem[] items = item.getItems();
            while (index < items.length) {
                TreeItem child = items[index];
                if (child.getGrayed() || checked != child.getChecked()) {
                    checked = grayed = true;
                    break;
                }
                index++;
            }
        }
        item.setChecked(checked);
        item.setGrayed(grayed);
        checkPath(item.getParentItem(), checked, grayed);
    }

    /**
     * GUI: helper method for Tree
     * 
     * (Un-)checks all children of an item recursive.
     * 
     * http://dev.eclipse.org/viewcvs/index.cgi/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet274.java?view=co
     *
     * @param item
     * @param checked
     */
    private void checkItems(TreeItem item, boolean checked) {
        item.setGrayed(false);
        item.setChecked(checked);
        TreeItem[] items = item.getItems();
        for (int i = 0; i < items.length; i++) {
            checkItems(items[i], checked);
        }
    }
    //endregion
    //endregion
    
    void close() {
        if( getContainer()!=null && getContainer().getShell()!=null && !getContainer().getShell().isDisposed() ){
            getContainer().getShell().close();
        }
    }
}

