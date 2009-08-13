/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2004, Refractions Research Inc.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package net.refractions.udig.catalog.wmt.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "net.refractions.udig.catalog.wmt.internal.messages"; //$NON-NLS-1$

	
	public static final String MapGraphicService_title = "Titel";
    public static final String MapGraphicService_description = "Description";
	public static String WMSCServiceExtension_nottiled;
    public static String WMSCWizardPage_WMSCTitle;
    public static String WMSGeoResourceImpl_bounds_unavailable;
	public static String WMSGeoResourceImpl_downloading_icon;
	public static String WMSGeoResourceImpl_acquiring_task;
    public static String WMSServiceExtension_badService;
    public static String WMSServiceExtension_needsKey;
    public static String WMSServiceExtension_nullURL;
    public static String WMSServiceExtension_nullValue;
    public static String WMSServiceExtension_protocol;
	public static String WMSWizardPage_connectionProblem;
	public static String WMSWizardPage_serverConnectionError;
	public static String WMSWizardPage_title;
	public static String WMSServiceImpl_broken;
	public static String WMSWizardPage_error_invalidURL;
	public static String WMSServiceImpl_could_not_connect;
	public static String WMSWizardPage_label_url_text;
	public static String WMSServiceImpl_connecting_to;
	public static String WMSCTileUtils_preloadtitle;
	public static String WMSCTileUtils_preloadtask;
	public static String WMSCTileUtils_preloadtasksub;
	
	public static String Properties_Layer_Title;
    public static String Properties_Layer_GroupBox;
    public static String Properties_Layer_AutomaticSelection;
    public static String Properties_Layer_ManualSelection;
    public static String Properties_Layer_UseZoomLevel;
    public static String Properties_Layer_Recommended;
    public static String Properties_Layer_Warning;
    public static String Properties_Layer_Error;
    
    public static String ZoomLevelSwitcher_Layer; 
    public static String ZoomLevelSwitcher_ZoomLevel;
    public static String ZoomLevelSwitcher_ZoomIn;
    public static String ZoomLevelSwitcher_ZoomOut;
    
    public static String Wizard_CloudMade_StyleFromGroup;
    public static String Wizard_CloudMade_GroupCloudMade;
    public static String Wizard_CloudMade_GroupFeatured;
    public static String Wizard_CloudMade_StyleFromId;
    public static String Wizard_CloudMade_StyleId;
    public static String Wizard_CloudMade_DefaultStyleId;
    public static String Wizard_CloudMade_RefreshPreview;
    public static String Wizard_CloudMade_StyleEditorInfo;
    public static String Wizard_CloudMade_Preview;
    public static String Wizard_CloudMade_PreviewName;
    public static String Wizard_CloudMade_PreviewId;
    public static String Wizard_CloudMade_PreviewAuthor;
    public static String Wizard_CloudMade_PreviewGetFullMap;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
