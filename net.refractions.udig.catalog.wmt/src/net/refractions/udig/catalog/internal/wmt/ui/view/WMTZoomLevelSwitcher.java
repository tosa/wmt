package net.refractions.udig.catalog.internal.wmt.ui.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.internal.wmt.wmtsource.WMTSource;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.IMapCompositionListener;
import net.refractions.udig.project.MapCompositionEvent;
import net.refractions.udig.project.internal.commands.SetScaleCommand;
import net.refractions.udig.project.render.IViewportModelListener;
import net.refractions.udig.project.render.ViewportModelEvent;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.emf.common.util.URI;

public class WMTZoomLevelSwitcher extends ViewPart {

    private ComboViewer cvLayers;
    private List<ILayer> layerList;
    private URI mapId = null;
    private ComboViewer cvZoomLevels;
    
    private ISelectionChangedListener listenerZoomLevel;
    private IMapCompositionListener listenerMap;
    private IViewportModelListener listenerViewport;
    
    private Composite parentControl;
    
    private Button btnZoomOut;
    private Button btnZoomIn;
    
    private double[] scales;
    private Integer[] zoomLevels;

    private static WMTZoomLevelSwitcher instance = null;
    public static WMTZoomLevelSwitcher getInstance() {
        return WMTZoomLevelSwitcher.instance;
    }
    
    public WMTZoomLevelSwitcher() {
        super();
        
        WMTZoomLevelSwitcher.instance = this;
        
        listenerMap = new IMapCompositionListener(){
            public void changed(MapCompositionEvent event) {
                System.out.println("Layer(s) added/removed/replaced");
                
                if (parentControl == null) return;
                parentControl.getDisplay().syncExec(new Runnable() {
                    public void run(){
                        updateGUI();
                    }
                });
                // http://udig.refractions.net/files/docs/api-udig/net.refractions.udig.project/net/refractions/udig/project/MapCompositionEvent.EventType.html
                // http://udig.refractions.net/files/docs/api-udig/net.refractions.udig.project/net/refractions/udig/project/MapCompositionEvent.html
            }
            
        };
        
        listenerViewport = new IViewportModelListener() {
            public void changed(ViewportModelEvent event) {
                System.out.println("changed(ViewportModelEvent event) " + event.getOldValue() + " " + event.getNewValue());
                
                if (parentControl == null) return;
                
                if (layerUpdateRequired()) {
                    listenerMap.changed(null);
                    return;
                }
                
                // when the scale changes, update the zoom-level ComboBox  
                parentControl.getDisplay().syncExec(new Runnable() {
                    public void run(){
                        updateGUIFromScale();
                    }
                });
            }
        };
    }
    
    public void setUpMapListeners(IMap map) {
        // assure that the listener is only one in the list
        map.removeMapCompositionListener(listenerMap);        
        map.addMapCompositionListener(listenerMap);
       
        map.getViewportModel().removeViewportModelListener(listenerViewport);
        map.getViewportModel().addViewportModelListener(listenerViewport);
        
        if (parentControl != null) {
            updateGUI();
        }
    }
    
    @Override
    public void createPartControl(final Composite parent) {
        System.out.println("WMTZoomLevelSwitcher.createPartControl");
        parentControl = parent;
        
        Composite composite = new Composite(parent, SWT.NONE);   
        composite.setLayout(new RowLayout(SWT.HORIZONTAL));
                      
        Label lblUseZoomLevel = new Label (composite, SWT.HORIZONTAL);
        lblUseZoomLevel.setText("Layer: ");
        
        //region Layer ComboBox
        cvLayers = new ComboViewer(composite, SWT.READ_ONLY);
               
        cvLayers.setContentProvider(new ArrayContentProvider());
        cvLayers.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof ILayer) {
                    return ((ILayer) element).getName();
                } else {
                    return super.getText(element);
                }
            }            
        });
        
        cvLayers.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged( SelectionChangedEvent event ) {
                System.out.println("Layer Selection changed");
                updateZoomLevels();
                updateGUIFromScale();                    
            }
            
        });
        //endregion
        
        //region Zoom-Level ComboBox
        cvZoomLevels = new ComboViewer(composite, SWT.READ_ONLY);
               
        cvZoomLevels.setContentProvider(new ArrayContentProvider());
        cvZoomLevels.setLabelProvider(new LabelProvider());
        //endregion
        
        //region Zoom-In/Zoom-Out Buttons
        btnZoomOut = new Button(composite, SWT.PUSH);
        btnZoomOut.setText("-"); //todo: replace with icon
        btnZoomOut.setLayoutData(new RowData(32, 32));
        
        btnZoomOut.addSelectionListener(new SelectionListener() {

            public void widgetSelected( SelectionEvent e ) {
                zoomOut();
            }

            public void widgetDefaultSelected( SelectionEvent e ) {}
        });
        
        btnZoomIn = new Button(composite, SWT.PUSH);
        btnZoomIn.setText("+");
        btnZoomIn.setLayoutData(new RowData(32, 32));
        
        btnZoomIn.addSelectionListener(new SelectionListener() {

            public void widgetSelected( SelectionEvent e ) {
                zoomIn();
            }

            public void widgetDefaultSelected( SelectionEvent e ) {}            
        });
        //endregion
        
        //region Setup listeners
        listenerZoomLevel = new ISelectionChangedListener() {
            public void selectionChanged( SelectionChangedEvent event ) {
                System.out.println("Zoom-Level Selection changed");
                zoomToZoomLevel(getSelectedZoomLevel());
            }
            
        };
        
        cvZoomLevels.addSelectionChangedListener(listenerZoomLevel);
        
        setUpMapListeners(ApplicationGIS.getActiveMap());
        //endregion
    }
    
    private void updateGUI() {
        updateLayerList();
        updateZoomLevels();
        updateGUIFromScale(); 
        
        parentControl.pack();       
    }
    
    private void updateLayerList() {
        if (layerList == null) {
            layerList = new ArrayList<ILayer>();
        } 
        
        // remember the layer which is selected at the moment
        ILayer selectedLayer = getSelectedLayer();
        
        layerList.clear();
        List<ILayer> mapLayers = ApplicationGIS.getActiveMap().getMapLayers();
        
        // look for layers which have WMTSource as georesource
        for (ILayer layer : mapLayers) {
            if ((layer != null) && (layer.findGeoResource(WMTSource.class) != null)) {
                // valid layer
                layerList.add(layer);
                System.out.println("added " + layer.getName() + " " + layer.toString());
            }            
        }
        
        cvLayers.setInput(layerList);
        setSelectedLayer(selectedLayer);
        
        enableComponents(!layerList.isEmpty());
        
        // remember to which map these layers belong to
        mapId = ApplicationGIS.getActiveMap().getID();
    }
    
    private boolean layerUpdateRequired(){
        return !((mapId != null) && 
                mapId.equals(ApplicationGIS.getActiveMap().getID()));
    }

    private void updateZoomLevels() {
        WMTSource wmtSource = getWMTSourceOfSelectedLayer();
        
        if (wmtSource == null) {
            cvZoomLevels.setInput(null);
        } else {
            scales = wmtSource.getScaleList();

            int minZoomLevel = wmtSource.getMinZoomLevel();
            int maxZoomLevel = wmtSource.getMaxZoomLevel();
            
            generateZoomLevels(minZoomLevel, maxZoomLevel);
            
            cvZoomLevels.setInput(zoomLevels);
        }
    }
    
    private void updateGUIFromScale() {
        WMTSource wmtSource = getWMTSourceOfSelectedLayer();
        
        if (wmtSource == null) {
            // todo: ?
        } else {            
            double scale = ApplicationGIS.getActiveMap().getViewportModel().getScaleDenominator();
            
            // get the zoom-level for this scale
            int zoomLevel = wmtSource.getZoomLevelFromMapScale(scale, WMTSource.SCALE_FACTOR);
            
            setSelectedZoomLevel(zoomLevel);
            updateZoomButtons(zoomLevel);
        }        
    }
    
    private void setSelectedZoomLevel(int zoomLevel) {
        
        List<Integer> selectedZoomLevels = new ArrayList<Integer>(1);
        selectedZoomLevels.add(zoomLevel);      
        ISelection selection = new StructuredSelection(selectedZoomLevels);

        cvZoomLevels.removeSelectionChangedListener(listenerZoomLevel);
        cvZoomLevels.setSelection(selection);  
        cvZoomLevels.addSelectionChangedListener(listenerZoomLevel);
    }
    
    private int getSelectedZoomLevel() {
        StructuredSelection selection = (StructuredSelection) cvZoomLevels.getSelection();
        
        if (selection.isEmpty()) {
            return zoomLevels[0];
        } else {
            return (Integer) selection.getFirstElement();
        }
    }
    
    private void updateZoomButtons(int zoomLevel) {
        boolean zoomInEnabled = true;
        boolean zoomOutEnabled = true;
        
        if (zoomLevel <= zoomLevels[0]) {
            zoomOutEnabled = false;
        } else if (zoomLevel >= zoomLevels[zoomLevels.length-1]) {
            zoomInEnabled = false;
        }
        
        btnZoomIn.setEnabled(zoomInEnabled);
        btnZoomOut.setEnabled(zoomOutEnabled);
    }
    
    private void generateZoomLevels(int minZoomLevel, int maxZoomLevel) {
        int length = maxZoomLevel - minZoomLevel + 1;
        zoomLevels = new Integer[length];
        
        int zoomLevel = minZoomLevel;
        for (int i = 0; i < length; i++) {
            zoomLevels[i] = zoomLevel++;
        }
    }
    
    private WMTSource getWMTSourceOfSelectedLayer() {
        ILayer layer = getSelectedLayer();
        
        if (layer == null) return null;
        
        IGeoResource resource = layer.findGeoResource(WMTSource.class); 
        if (resource == null) return null;
        
        try {
            WMTSource wmtSource = resource.resolve(WMTSource.class, null);
            
            return wmtSource;
        } catch (IOException e) {
            return null;
        }
    }
    
    private ILayer getSelectedLayer() {
        StructuredSelection selection = (StructuredSelection) cvLayers.getSelection();
        
        if (selection.isEmpty()) {
            return null;
        } else {
            return (ILayer) selection.getFirstElement();
        }
    }
    
    private void setSelectedLayer(ILayer layer) {
        if (layer == null || !layerList.contains(layer)) {
            // try to get the first layer
            if (layerList.isEmpty()) {
                // no layer there to select
                return;
            } else {
                layer = layerList.get(0);
            }
        } 
        
        // set the layer selected
        List<ILayer> selectedLayers = new ArrayList<ILayer>(1);
        selectedLayers.add(layer);      
        ISelection selection = new StructuredSelection(selectedLayers);
      
        cvLayers.setSelection(selection);
    }
    
    private void enableComponents(boolean enabled) {
        cvLayers.getCombo().setEnabled(enabled);
        cvZoomLevels.getCombo().setEnabled(enabled);
        btnZoomIn.setEnabled(enabled);
        btnZoomOut.setEnabled(enabled);        
    }
    
    private void zoomIn() {
        int zoomLevel = getSelectedZoomLevel();
        
        if (zoomLevel < zoomLevels[zoomLevels.length-1]){
            zoomToZoomLevel(zoomLevel+1);
        }
    }
    
    private void zoomOut() {
        int zoomLevel = getSelectedZoomLevel();
        
        if (zoomLevel > zoomLevels[0]){
            zoomToZoomLevel(zoomLevel-1);
        }
    }
    
    private void zoomToZoomLevel(int zoomLevel) {
        zoomToScale(scales[zoomLevel]);
    }
    
    private void zoomToScale(double scale) {
        ApplicationGIS.getActiveMap().sendCommandASync(new SetScaleCommand(scale));
    }
    
    
    @Override
    public void setFocus() {}


}
