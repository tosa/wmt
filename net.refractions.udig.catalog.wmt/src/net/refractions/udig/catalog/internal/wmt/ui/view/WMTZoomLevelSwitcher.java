package net.refractions.udig.catalog.internal.wmt.ui.view;

import java.awt.Color;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.refractions.udig.catalog.internal.wmt.wmtsource.WMTSource;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMapCompositionListener;
import net.refractions.udig.project.MapCompositionEvent;
import net.refractions.udig.project.internal.commands.SetScaleCommand;
import net.refractions.udig.project.render.IViewportModelListener;
import net.refractions.udig.project.render.ViewportModelEvent;
import net.refractions.udig.project.render.ViewportModelEvent.EventType;
import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

public class WMTZoomLevelSwitcher extends ViewPart {

    private ComboViewer comboViewer;
    private List<ILayer> layerList;
    private JSlider zoomLevelSlider;
    private Button btnZoomOut;
    private Button btnZoomIn;

    public WMTZoomLevelSwitcher() {
        super();
    }
    
    @Override
    public void createPartControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);   
        composite.setLayout(new RowLayout(SWT.HORIZONTAL));
                      
        Label lblUseZoomLevel = new Label (composite, SWT.HORIZONTAL);
        lblUseZoomLevel.setText("Layer: ");
        
        //region Layer ComboBox
        comboViewer = new ComboViewer(composite, SWT.READ_ONLY);
               
        comboViewer.setContentProvider(new ArrayContentProvider());
        comboViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof ILayer) {
                    return ((ILayer) element).getName();
                } else {
                    return super.getText(element);
                }
            }            
        });
        //endregion
        
        //region Zoom-Level Slider
        // create Swing Slider 
        zoomLevelSlider = new JSlider(JSlider.HORIZONTAL, 2, 18, 11);

        zoomLevelSlider.setMajorTickSpacing(5);
        zoomLevelSlider.setMinorTickSpacing(1);
        
        Hashtable labelTable = new Hashtable();
        labelTable.put( new Integer( 2 ), new JLabel("-") );
        labelTable.put( new Integer( 18), new JLabel("+") );
        zoomLevelSlider.setLabelTable(labelTable);

        zoomLevelSlider.setPaintTicks(true);
        zoomLevelSlider.setPaintLabels(true);
        zoomLevelSlider.setSnapToTicks(true);
        zoomLevelSlider.setBorder(BorderFactory.createEmptyBorder());
        
        // change background color
        org.eclipse.swt.graphics.Color swtColor = parent.getBackground();
        zoomLevelSlider.setBackground(new Color(swtColor.getRed(), swtColor.getGreen(), swtColor.getBlue()));
        
        // create SWT/AWT bridge
        Composite compositeSlider = new Composite(composite, SWT.EMBEDDED | SWT.NO_BACKGROUND);
        compositeSlider.setLayoutData(new RowData(150, 50));
         
        compositeSlider.setLayout(new FillLayout());
        
        Frame awtFrame = SWT_AWT.new_Frame(compositeSlider);
        awtFrame.add(zoomLevelSlider);
        
        zoomLevelSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider)e.getSource();
                if (!source.getValueIsAdjusting()) {

                    System.out.println("Zoom to " + source.getValue());
                }                
            }
            
        });
        //endregion
        
        //region Zoom-In/Zoom-Out Buttons
        btnZoomOut = new Button(composite, SWT.PUSH);
        btnZoomOut.setText("-"); //todo: replace with icon
        btnZoomOut.setLayoutData(new RowData(32, 32));
        
        btnZoomIn = new Button(composite, SWT.PUSH);
        btnZoomIn.setText("+");
        btnZoomIn.setLayoutData(new RowData(32, 32));
        
        btnZoomIn.addSelectionListener(new SelectionListener() {

            public void widgetSelected( SelectionEvent e ) {
                ApplicationGIS.getActiveMap().sendCommandASync(new SetScaleCommand(1000000));

            }

            public void widgetDefaultSelected( SelectionEvent e ) {
            }
            
        });
        //endregion
        
        // Setup listener
        ApplicationGIS.getActiveMap().addMapCompositionListener(new IMapCompositionListener(){

            public void changed(MapCompositionEvent event) {
                System.out.println("Layer(s) added/removed/replaced");
                // http://udig.refractions.net/files/docs/api-udig/net.refractions.udig.project/net/refractions/udig/project/MapCompositionEvent.EventType.html
                // http://udig.refractions.net/files/docs/api-udig/net.refractions.udig.project/net/refractions/udig/project/MapCompositionEvent.html
            }
            
        });
       
        
        ApplicationGIS.getActiveMap().getViewportModel().addViewportModelListener(new IViewportModelListener() {

            public void changed(ViewportModelEvent event) {
                if (event.getType() == EventType.BOUNDS) {
                    // only when the scale changes
                    System.out.println("bounds changed");
                    // todo: update slider
                }
            }
        });

        updateLayerList();
        
        //ApplicationGIS.
        parent.pack();
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
        
        comboViewer.setInput(layerList);
        setSelectedLayer(selectedLayer);
        
        enableComponents(!layerList.isEmpty());
    }

    private ILayer getSelectedLayer() {
        StructuredSelection selection = (StructuredSelection) comboViewer.getSelection();
        
        if (selection.isEmpty()) {
            return null;
        } else {
            return (ILayer) selection.getFirstElement();
        }
    }
    
    private void setSelectedLayer(ILayer layer) {
        if (layer == null) {
            // try to get the first layer
            if (layerList.isEmpty()) {
                // no layer there to select
                return;
            } else {
                layer = layerList.get(0);
            }
        } 
        
        // set the layer selected
        List<ILayer> selectedList = new ArrayList<ILayer>(1);
        selectedList.add(layer);      
        ISelection selection = new StructuredSelection(selectedList);
      
        comboViewer.setSelection(selection);
    }
    
    private void enableComponents(boolean enabled) {
        comboViewer.getCombo().setEnabled(enabled);
        zoomLevelSlider.setEnabled(enabled);
        btnZoomIn.setEnabled(enabled);
        btnZoomOut.setEnabled(enabled);        
    }
    
    @Override
    public void setFocus() {
        
    }


}
