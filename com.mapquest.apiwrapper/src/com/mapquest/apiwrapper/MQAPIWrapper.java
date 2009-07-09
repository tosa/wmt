package com.mapquest.apiwrapper;

import com.mapquest.Exec;
import com.mapquest.LatLng;
import com.mapquest.MapState;
import com.mapquest.Session;

public class MQAPIWrapper {
    static final String MQ_MAP_SERVER_NAME          = "map.free.mapquest.com"; //$NON-NLS-1$
    static final String MQ_MAP_SERVER_PATH          = "mq"; //$NON-NLS-1$
    static final int    MQ_MAP_SERVER_PORT          =  80;
            
    private Exec mapClient;
           
    
    public MQAPIWrapper() {
        mapClient = new Exec();
        
        mapClient.setServerName(MQ_MAP_SERVER_NAME);
        mapClient.setServerPath(MQ_MAP_SERVER_PATH);
        mapClient.setServerPort(MQ_MAP_SERVER_PORT);
        mapClient.setClientId("your-client-id"); //$NON-NLS-1$
        mapClient.setPassword("your-password"); //$NON-NLS-1$
    }
    
    public String getUrl(int scale, double x, double y, int width, int height) throws Exception {
        MapState mapState = new MapState();
        
        // build request
        mapState.setWidthPixels(width);
        mapState.setHeightPixels(height);
        
        mapState.setMapScale(scale);
        mapState.setCenter(new LatLng(y, x));
                    
        Session mqSession = new Session();            
        mqSession.addOne(mapState);
        
        // create a new MapQuest session on the server
        String sessionId = mapClient.createSessionEx(mqSession);
        
        // client-based call which creates the url
        return mapClient.getMapFromSessionURL(sessionId);
    }
}
