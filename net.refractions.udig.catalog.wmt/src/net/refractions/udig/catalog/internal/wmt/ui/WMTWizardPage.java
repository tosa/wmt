
package net.refractions.udig.catalog.internal.wmt.ui;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.internal.wmt.WMTService;
import net.refractions.udig.catalog.internal.wmt.WMTServiceExtension;
import net.refractions.udig.catalog.internal.wmt.wmtsource.OSMCycleMapSource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.OSMMapnikSource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.OSMOsmarenderSource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.OSMSource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.WMTSource;
import net.refractions.udig.catalog.ui.AbstractUDIGImportPage;
import net.refractions.udig.catalog.ui.CatalogTreeViewer;
import net.refractions.udig.catalog.ui.UDIGConnectionPage;
import net.refractions.udig.catalog.ui.workflow.Listener;
import net.refractions.udig.catalog.ui.workflow.State;
import net.refractions.udig.catalog.wmt.internal.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
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
public class WMTWizardPage extends AbstractUDIGImportPage implements Listener, UDIGConnectionPage {

      
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
        
        // Skip the resouce selection wizard page
        IRunnableWithProgress runnable = new IRunnableWithProgress(){
            public void run( IProgressMonitor monitor ) throws InvocationTargetException,
                    InterruptedException {
                System.out.println("getWizard().getWorkflow().next()");
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
        if (tree != null) return;
        
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
        
        //todo: add other services
        
        // Enable first service for usability reasons
        osm.getItems()[0].setChecked(true);
        //endregion
        
        //region Add information textbox
        Text text = new Text (composite,    SWT.WRAP | SWT.MULTI | 
                                            SWT.BORDER | SWT.H_SCROLL |
                                            SWT.V_SCROLL | SWT.READ_ONLY);
        text.setText ("Space for general / copyright information");
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
    
    @Override
    public void setState( State state ) {
        super.setState(state);
        
        System.out.println("setState " + state.getName());
        
        state.getWorkflow().addListener(this);
    }
    
    void close() {
        if( getContainer()!=null && getContainer().getShell()!=null && !getContainer().getShell().isDisposed() ){
            getContainer().getShell().close();
        }
    }

    public void backward( State current, State next ) {
        System.out.println("backward");
        if( current == getState() ){
            current.getWorkflow().previous();
            current.getWorkflow().removeListener(this);
        }
    }

    public void finished( State last ) {
        System.out.println("finished");
        System.out.println("finished");
        
    
    }

    public void forward( State current, State prev ) {
        System.out.println("forward");
        if( current == getState() ){
            current.getWorkflow().next();
        }
    }

    public void started( State first ) {
        System.out.println("started");
    }

    public void stateFailed( State state ) {
        System.out.println("statedfailed");
    }

    public void statePassed( State state ) {
        System.out.println("statePassed");
    }
    
    
    

//    /*
//    /** Can be called during createControl */
//    protected Map<String,Serializable> defaultParams(){
//        System.out.println("defaultParams");
////    	IStructuredSelection selection = (IStructuredSelection)PlatformUI
////			.getWorkbench() .getActiveWorkbenchWindow().getSelectionService()
////			.getSelection();
////        Map<String, Serializable> toParams = toParams( selection );
////        if( !toParams.isEmpty() ){
////        	return toParams;
////        }
////        
////    	WMTConnectionFactory connectionFactory = new WMTConnectionFactory();
////    	Map<String, Serializable> params = connectionFactory.createConnectionParameters( getState().getWorkflow().getContext() );
////    	if( params !=null )
////    		return params;
//    	
//    	return Collections.emptyMap();
//    }
//    /** Retrieve "best" WMS guess of parameters based on provided context */
//    protected Map<String,Serializable> toParams( IStructuredSelection context){
//        System.out.println("toParams");
////        if( context != null ) {
////        	WMTConnectionFactory connectionFactory = new WMTConnectionFactory();
////            for( Iterator itr = context.iterator(); itr.hasNext(); ) {
////				Map<String,Serializable> params = connectionFactory
////                	.createConnectionParameters(itr.next());
////                if( !params.isEmpty() ) return params;
////            }
////        }
//        return Collections.EMPTY_MAP;
//    }
//  
//    public void createControl( Composite parent ) {
//        System.out.println("createControl");
//
//
//        GridData gridData;
//        Composite composite = new Composite(parent, SWT.NULL);
//
//        GridLayout gridLayout = new GridLayout();
//        int columns = 1;
//        gridLayout.numColumns = columns;
//        composite.setLayout(gridLayout);
//
//        gridData = new GridData();
//
//        Label urlLabel = new Label(composite, SWT.NONE);
//        urlLabel.setText(Messages.WMSWizardPage_label_url_text); 
//        urlLabel.setLayoutData(gridData);
//
//        gridData = new GridData(GridData.FILL_HORIZONTAL);
//        gridData.widthHint = 400;
//
//
//        setControl(composite);
//        
//        Button button = new Button(composite, SWT.NONE);
//        button.setText("Add service");
//        
//        button.addMouseListener(new MouseListener (){
//            public void mouseDown( MouseEvent e ) {
//                System.out.println("simulates the selection of a service");
//                setPageComplete(true);                
//                
//                
////                try {
////                    WMTServiceExtension serviceExtension = new WMTServiceExtension();
////                    String urlText = WMTService.SERVICE_URL.toString() + "net.refractions.udig.catalog.internal.wmt.wmtsource.OSMSource";
////                    
////                    System.out.println(urlText);                 
////                    URL url = new URL(null, urlText, CorePlugin.RELAXED_HANDLER);
////                    Map<String,Serializable> params = serviceExtension.createParams(url);
////                    
////                    WMTService service = serviceExtension.createService(null, params);
////                    
////                    // Add georesource
////                    List<IGeoResource> res = service.resources(null);
////                    //CatalogPlugin.getDefault().getLocalCatalog().add(service);
////                    
////                    ApplicationGIS.addLayersToMap(ApplicationGIS.getActiveMap(), res,
////                            -1,ApplicationGIS.getActiveProject(), true); 
////                    
////                } catch (Exception exc) {
////                    // TODO Handle IOException
////                    System.out.println("adding failed: " + exc.getMessage());
////                }
//                
//            }
//
//            public void mouseDoubleClick( MouseEvent e ) {}
//            public void mouseUp( MouseEvent e ) {}      
//        });
//        
//        
//        IRunnableWithProgress runnable = new IRunnableWithProgress(){
//
//            public void run( IProgressMonitor monitor ) throws InvocationTargetException,
//                    InterruptedException {
//                System.out.println("getWizard().getWorkflow().next()");
//                getWizard().getWorkflow().next();
//            }
//            
//        };
//        
////        Display.getCurrent().asyncExec(new Runnable() {
////			public void run() {
////				
////			    System.out.println("run is run");
////			    
////				EndConnectionState currentState = getState();
////				Map<IService, Throwable> errors = currentState.getErrors();
////				if( errors!=null && !errors.isEmpty()){
////					for (Map.Entry<IService, Throwable> entry : errors.entrySet()) {
////						if( entry.getKey() instanceof WMSServiceImpl ){
////							Throwable value = entry.getValue();
////							if( value instanceof ConnectException){
////								setErrorMessage(Messages.WMSWizardPage_serverConnectionError);
////							}else{
////								String message = Messages.WMSWizardPage_connectionProblem+value.getLocalizedMessage();
////								setErrorMessage(message);
////							}
////						}
////					}
////				}
////
////			}
////		});
//    }
//
//    @Override
//    public void setErrorMessage(String newMessage) {
//    	WizardPage page=(WizardPage) getContainer().getCurrentPage();
//    	page.setErrorMessage(newMessage);
//    }
//
//    @Override
//    public void setMessage(String newMessage) {
//    	WizardPage page=(WizardPage) getContainer().getCurrentPage();
//    	page.setMessage(newMessage);
//    }
//    
//    @Override
//    public void setMessage(String newMessage, int messageType) {
//    	WizardPage page=(WizardPage) getContainer().getCurrentPage();
//    	page.setMessage(newMessage, messageType);
//    }
//
//	public EndConnectionState getState() {
//	    System.out.println("getState");
//		return (EndConnectionState) super.getState();
//	}
//    
//    public URL getURL( Map<String,Serializable> params ){
//        System.out.println("getURL");
//        Object value = params.get( WMSServiceImpl.WMS_URL_KEY );
//        if( value == null ) return null;
//        if( value instanceof URL ) return (URL) value;
//        if( value instanceof String) {
//            try {
//                URL url = new URL( (String) value );   
//                return url;
//            }
//            catch( MalformedURLException erp ){                
//            }
//        }
//        return null;        
//    }
//    /**
//     * Double click in list, or return from url control.
//     * 
//     * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
//     * @param e
//     */
//    public void widgetDefaultSelected( SelectionEvent e ) {
//        System.out.println("widgetDefaultSelected");
//        e.getClass();// kill warning
//        if (getWizard().canFinish()) {
//            getWizard().performFinish();
//        }
//    }
//
//    
//    
//    /**
//     * This should be called using the Wizard .. job when next/finish is pressed.
//     */
//    public List<IService> getResources( IProgressMonitor monitor ) throws Exception {
//        System.out.println("List<IService> getResources");
//        URL location = new URL(url);
//        
//        WMSServiceExtension creator = new WMSServiceExtension();
//
//        Map<String, Serializable> params = creator.createParams(location);
//        IService service = creator.createService(location, params);
//        service.getInfo(monitor); // load it
//
//        List<IService> servers = new ArrayList<IService>();
//        servers.add(service);
//
//        /*
//         * Success! Store the URL in history.
//         */
//        //saveWidgetValues();
//
//        return servers;
//    }
//
//    @Override
//    public Collection<IService> getServices() {
//        System.out.println("Collection<IService> MapGraphicWizardPage.getServices");
////        IService service = CatalogPlugin.getDefault().getLocalCatalog().getById(IService.class, MapGraphicService.SERVICE_URL, new NullProgressMonitor());
////        return Collections.singleton(service);
//        
//        return null;
//    }
//    
//    public void modifyText( ModifyEvent e ) {
//        System.out.println("modifyText");
//        try {
//        	getState().getErrors().clear();
//            url = ((Combo) e.getSource()).getText();
//            new URL(url);
//            setErrorMessage(null);
//            setPageComplete(true);
//        } catch (MalformedURLException exception) {
//            setErrorMessage(Messages.WMSWizardPage_error_invalidURL); 
//            setPageComplete(false);
//        }
//        
//        getWizard().getContainer().updateButtons();
//        
////        // Endconnection Service einpflanzen und selektieren
////        EndConnectionState endState = (EndConnectionState) super.getState();
////        Collection<IService> services = endState.getServices();
////        System.out.println(services.size());
////        
////        
////        endState.setSelectedResources(Collections.singletonList(services.iterator().next().getIdentifier()));
////        System.out.println(endState.getSelectedResources().size());
////        
////        getWizard().performFinish();
//        
//    }
//
//
//
//    /**
//     * Adds an entry to a history, while taking care of duplicate history items and excessively long
//     * histories. The assumption is made that all histories should be of length
//     * <code>COMBO_HISTORY_LENGTH</code>.
//     * 
//     * @param history the current history
//     * @param newEntry the entry to add to the history
//     * @return the history with the new entry appended Stolen from
//     *         org.eclipse.team.internal.ccvs.ui.wizards.ConfigurationWizardMainPage
//     */
//    private String[] addToHistory( String[] history, String newEntry ) {
//        ArrayList<String> l = new ArrayList<String>(Arrays.asList(history));
//        addToHistory(l, newEntry);
//        String[] r = new String[l.size()];
//        l.toArray(r);
//        return r;
//    }
//
//    /**
//     * Adds an entry to a history, while taking care of duplicate history items and excessively long
//     * histories. The assumption is made that all histories should be of length
//     * <code>COMBO_HISTORY_LENGTH</code>.
//     * 
//     * @param history the current history
//     * @param newEntry the entry to add to the history Stolen from
//     *        org.eclipse.team.internal.ccvs.ui.wizards.ConfigurationWizardMainPage
//     */
//    private void addToHistory( List<String> history, String newEntry ) {
//        history.remove(newEntry);
//        history.add(0, newEntry);
//
//        // since only one new item was added, we can be over the limit
//        // by at most one item
//        if (history.size() > COMBO_HISTORY_LENGTH)
//            history.remove(COMBO_HISTORY_LENGTH);
//    }
//
//	public Map<String, Serializable> getParams() {
//		System.out.println("getParams");
//	    try {
//			URL location = new URL(url);
//			
//			WMSServiceExtension creator = new WMSServiceExtension();
//            String errorMessage=creator.reasonForFailure(location);
//            if( errorMessage!=null ){
//                setErrorMessage(errorMessage);
//                return Collections.emptyMap();
//            }else
//                return creator.createParams(location);
//		}
//		catch(MalformedURLException e) {
//			return null;
//		}
//	}
//
//	public List<URL> getURLs() {
//		System.out.println("getURLs");
//	    try {
//			ArrayList<URL> l = new ArrayList<URL>();
//			l.add(new URL(url));
//			
//			return l;
//		}
//		catch(MalformedURLException e) {
//			return null;
//		}
//	}
//	*/
    
}

