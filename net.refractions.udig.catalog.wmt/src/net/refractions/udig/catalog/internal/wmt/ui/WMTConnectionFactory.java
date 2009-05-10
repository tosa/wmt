package net.refractions.udig.catalog.internal.wmt.ui;


import java.io.Serializable;
import java.net.URL;
import java.util.Map;
import net.refractions.udig.catalog.internal.wmt.WMTService;
import net.refractions.udig.catalog.ui.UDIGConnectionFactory;


public class WMTConnectionFactory extends UDIGConnectionFactory {

    public boolean canProcess(Object context) {
        return false;
    }

    public Map<String, Serializable> createConnectionParameters(Object context) {
        return null;
    }

    public URL createConnectionURL(Object context) {
        System.out.println("WMTConnectionFactory.createConnectionURL : " + WMTService.SERVICE_URL.toString());
        return WMTService.SERVICE_URL;
    }
}
