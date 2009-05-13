
package net.refractions.udig.catalog.internal.wmt.ui;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;

import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.internal.wmt.WMTServiceExtension;
import net.refractions.udig.catalog.internal.wmt.wmtsource.OSMMapnikSource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.OSMSource;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
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

   
    private CatalogTreeViewer treeviewer;
    
    private WMTServiceExtension serviceExtension;
    

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


    @Override
    public Collection<IService> getServices() {
        System.out.println("Collection<IService> WMTWizardPage.getServices");
        
        //todo: check which services have been selected, return those
        URL url = OSMSource.getRelatedServiceUrl(OSMMapnikSource.class);        
        IService service = serviceExtension.createService(null, serviceExtension.createParams(url));
        
        
        System.out.println(url.toString());
        //IService service = CatalogPlugin.getDefault().getLocalCatalog().getById(IService.class, url, new NullProgressMonitor());
        
        
        return Collections.singleton(service);
    }

    public void createControl( Composite parent ) {
        System.out.println("createControl");
        
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        
//        setPageComplete(false);
//        getWizard().getContainer().updateButtons();
        
        //Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        // Create the tree viewer to display the file tree
        CheckboxTreeViewer tv = new CheckboxTreeViewer(composite, SWT.NONE);
        tv.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
//        
//        tv.setContentProvider(new ContentProvider());
//        tv.setLabelProvider(new LabelProvider());
        
        TreeItem osm = new TreeItem(tv.getTree(), SWT.NONE);
        osm.setText("OpenStreetMap");
        osm.setExpanded(true);
        

        TreeItem mapnik = new TreeItem(osm, SWT.NONE);
        mapnik.setText("Mapnik");

        TreeItem osmar = new TreeItem(osm, SWT.NONE);
        osmar.setText("Osmarender");
        
        
//        new WMTTreeItem(osm, new OSMMapnikSource());
//        new WMTTreeItem(osm, new OSMOsmarenderSource());
//        new WMTTreeItem(osm, new OSMCycleMapSource());
        
//        tv.setContentProvider(new FileTreeContentProvider());
//        tv.setLabelProvider(new FileTreeLabelProvider());
    //   tv.setInput("root"); // pass a non-null that will be ignored
        
//      
//        GridData gridData;
//      Composite composite = new Composite(parent, SWT.NULL);
//
//      GridLayout gridLayout = new GridLayout();
//      int columns = 1;
//      gridLayout.numColumns = columns;
//      composite.setLayout(gridLayout);
//
//      gridData = new GridData();
//
//      Label urlLabel = new Label(composite, SWT.NONE);
//      urlLabel.setText(Messages.WMSWizardPage_label_url_text); 
//      urlLabel.setLayoutData(gridData);
//
//      gridData = new GridData(GridData.FILL_HORIZONTAL);
//      gridData.widthHint = 400;
//
//
//      setControl(composite);
//      
////      setPageComplete(false);
////      getWizard().getContainer().updateButtons();
//      
//      Button button = new Button(composite, SWT.NONE);
//      button.setText("Add service");
//      
//      button.addMouseListener(new MouseListener (){
//          public void mouseDown( MouseEvent e ) {
//              System.out.println("simulates the selection of a service");
//              setPageComplete(true);   
//        
//        
////        IRunnableWithProgress runnable = new IRunnableWithProgress(){
////
////            public void run( IProgressMonitor monitor ) throws InvocationTargetException,
////                    InterruptedException {
////                System.out.println("getWizard().getWorkflow().next()");
////                getWizard().getWorkflow().next();
////            }
////            
////        };
////          
////        
////        
////                try {
////            getContainer().run(true, false, runnable);
////        } catch (InvocationTargetException e2) {
////            throw (RuntimeException) new RuntimeException( ).initCause( e2 );
////        } catch (InterruptedException e2) {
////            throw (RuntimeException) new RuntimeException( ).initCause( e2 );
////        }
//          
//      }
//        public void mouseDoubleClick( MouseEvent e ) {}
//      public void mouseUp( MouseEvent e ) {}      
//  });
      }
      
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

