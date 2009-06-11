/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package net.refractions.udig.catalog.internal.wmt.ui.properties;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.internal.wmt.WMTService;
import net.refractions.udig.catalog.internal.wmt.wmtsource.WMTSource;
import net.refractions.udig.catalog.wmt.internal.Messages;
import net.refractions.udig.project.internal.Layer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Property page for WMT layers
 * @author to.srwn
 * @since 1.1.0
 */
public class WMTLayerProperties extends PropertyPage implements IWorkbenchPropertyPage {
    private Map<String, Serializable> properties = null;
    
    private Layer layer;
    private WMTSource wmtSource;
    
    private double mapScale;
    
    //region GUI objects
    private Button btnAutomatic;
    private Button btnManual;
    private Label lblUseZoomLevel;
    private Spinner spZoomLevelValue;
    private Label lblRecommended;
    private Label lblRecommendedValue;
    private Label lblWarning;
    
    private SelectionListener selectionListener = new SelectionListener()
    {

        public void widgetDefaultSelected(SelectionEvent arg0) {}
        
        /**
         * Is called when one of the two radio-buttons is selected.
         */
        public void widgetSelected(SelectionEvent arg0) {
            boolean enableState = btnManual.getSelection();
            
            // disable every component one by one, so that everyone is grayed out
            lblUseZoomLevel.setEnabled(enableState);
            spZoomLevelValue.setEnabled(enableState);
            lblRecommended.setEnabled(enableState);
            lblRecommendedValue.setEnabled(enableState);
            lblWarning.setEnabled(enableState);
        }
        
    };
    //endregion
    
    @Override
    protected Control createContents( Composite parent ) {
        layer = (Layer) getElement();
        
        //region Get GeoResource/Source/WMTSource for this layer
        IGeoResource resource = layer.findGeoResource(WMTSource.class); 
        if (resource == null) return createErrorMessage(parent);
        
        IService service = null;
        try {
            wmtSource = resource.resolve(WMTSource.class, null);
            service = wmtSource.getWmtService();
            //service = resource.service(null); //todo: why does this return null?!
        } catch(IOException exc) {
            wmtSource = null;
            service = null;
            
            return createErrorMessage(parent);
        }
        
        // get persistent properties from service to restore the settings
        properties = service.getPersistentProperties();
        
        // we also need the map-scale to calculate the recommended zoom-level        
        mapScale = layer.getMap().getViewportModel().getScaleDenominator();
        //endregion
               
        //region build GUI
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new RowLayout(SWT.VERTICAL));
        
        
        Group groupBox = new Group(composite, SWT.BORDER);
        groupBox.setLayout(new RowLayout(SWT.VERTICAL));
        groupBox.setText(Messages.Properties_Layer_GroupBox);
        
        // Radion-Button "Automatic Selection"
        btnAutomatic = new Button(groupBox, SWT.RADIO);
        btnAutomatic.addSelectionListener(selectionListener);
        btnAutomatic.setSelection(true);
        btnAutomatic.setText(Messages.Properties_Layer_AutomaticSelection);

        // Radion-Button "Manual Selection"
        btnManual = new Button(groupBox, SWT.RADIO);
        btnManual.setText(Messages.Properties_Layer_ManualSelection);
        
        Composite compositeManual = new Composite(groupBox, SWT.NONE);
        compositeManual.setLayout(new RowLayout(SWT.VERTICAL));
        
        
        Composite compositeZoomLevelSelection = new Composite(compositeManual, SWT.NONE);
        compositeZoomLevelSelection.setLayout(new RowLayout(SWT.HORIZONTAL));
        
        lblUseZoomLevel = new Label (compositeZoomLevelSelection, SWT.HORIZONTAL);
        lblUseZoomLevel.setText(Messages.Properties_Layer_UseZoomLevel);
        
        // "Spinner": Zoom-Level-Selector
        spZoomLevelValue = new Spinner (compositeZoomLevelSelection, SWT.BORDER | SWT.READ_ONLY);
        spZoomLevelValue.setMinimum(wmtSource.getMinZoomLevel());
        spZoomLevelValue.setMaximum(wmtSource.getMaxZoomLevel());
        spZoomLevelValue.setIncrement(1);
        spZoomLevelValue.pack();

        
        lblRecommended = new Label (compositeZoomLevelSelection, SWT.HORIZONTAL);
        lblRecommended.setText(Messages.Properties_Layer_Recommended);
        
        lblRecommendedValue = new Label (compositeZoomLevelSelection, SWT.HORIZONTAL);
        
        lblWarning = new Label (compositeManual, SWT.HORIZONTAL);
        lblWarning.setText(Messages.Properties_Layer_Warning);
        
        // Now load the properties or default values
        loadSettings();
                
        groupBox.pack();
        //endregion
        
        return composite;
    }
    
    //region Load and save properties
    /**
     * Gets the values from the properties and displays them.
     */
    private void loadSettings() {
        boolean selectionAutomatic = false;
        int zoomLevel = -1;
        
        try {
            selectionAutomatic = (Boolean) properties.get(WMTService.KEY_PROPERTY_ZOOM_LEVEL_SELECTION_AUTOMATIC);
            zoomLevel = (Integer) properties.get(WMTService.KEY_PROPERTY_ZOOM_LEVEL_VALUE);
        } catch(Exception exc) {
            // cast failed or properties do not contain the keys
            updateGuiDefaultValues();
            
            return;
        }
        
        updateGui(selectionAutomatic, zoomLevel, getDefaultZoomLevel());
    }
    
    /**
     * Loads the default values and displays them.
     */
    private void updateGuiDefaultValues() {
        // get scale-factor from settings!
        int zoomLevel = getDefaultZoomLevel();
        
        updateGui(true, zoomLevel, zoomLevel);
    }
    
    /**
     * Updates the GUI with the properties.
     *
     * @param selectionAutomatic
     * @param zoomLevel
     * @param recommendedZoomLevel
     */
    private void updateGui(boolean selectionAutomatic, int zoomLevel, int recommendedZoomLevel) {
        // set states of the radio-buttons
        btnAutomatic.setSelection(selectionAutomatic);
        btnManual.setSelection(!selectionAutomatic);
        
        // also call the listener
        selectionListener.widgetSelected(null);
        
        lblRecommendedValue.setText(Integer.toString(recommendedZoomLevel));
        spZoomLevelValue.setSelection(zoomLevel);
    }
    
    /**
     * Takes the values from the GUI and stores them in the
     * properties.
     */
    private void saveSettings() {
        boolean selectionAutomatic = btnAutomatic.getSelection();
        int zoomLevel = spZoomLevelValue.getSelection();
        
        properties.put(WMTService.KEY_PROPERTY_ZOOM_LEVEL_SELECTION_AUTOMATIC, selectionAutomatic);
        properties.put(WMTService.KEY_PROPERTY_ZOOM_LEVEL_VALUE, zoomLevel);
    }
    //endregion
    
    //region Overriden PropertyPage methods
    @Override
    protected void performApply() {
        saveSettings();
    }

    @Override
    public boolean performCancel() {        
        return true;
    }
    
    @Override
    public boolean performOk() {
        performApply();
        
        return super.performOk();
    }
    
    @Override
    protected void performDefaults() {
        updateGuiDefaultValues();
    }
    //endregion
    
    //region Helper methods    
    /**
     * The control which is returned in case of a error.
     */
    private Control createErrorMessage(Composite parent) {
        Label lblError = new Label (parent, SWT.HORIZONTAL);
        lblError.setText(Messages.Properties_Layer_Error);
        
        return parent;
    }
    
    /**
     * Asks the WMTSource to translate map-scale into zoom-level.
     *
     * @return
     */
    private int getDefaultZoomLevel() {
        // todo: get scale-factor from settings!
        return wmtSource.getZoomLevelFromMapScale(mapScale, WMTSource.SCALE_FACTOR);
    }
    
    //endregion

}