package net.refractions.udig.catalog.internal.wmt.ui.view;

import net.refractions.udig.project.interceptor.MapInterceptor;
import net.refractions.udig.project.internal.Map;

public class WMTOpenMapListener implements MapInterceptor{

    public void run(Map map){
        System.out.println("map opens (add listeners)");
//       // WMTZoomLevelSwitcher zoomLevelSwitcher = WMTZoomLevelSwitcher.getInstance();
//        
//        if (zoomLevelSwitcher != null) {
//            zoomLevelSwitcher.setUpMapListeners(map);
//        }
    }

}
