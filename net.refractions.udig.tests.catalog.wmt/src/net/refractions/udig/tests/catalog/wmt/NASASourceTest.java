package net.refractions.udig.tests.catalog.wmt;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import net.refractions.udig.catalog.internal.wmt.wmtsource.NASASource;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.junit.Test;

public class NASASourceTest {

    @Test
    public void testNASASource() throws Exception {
//        URL url = NASASource.class.getResource("NASA-GetTileService.xml");
//        
//        SAXBuilder builder = new SAXBuilder(false); 
//        URLConnection connection = url.openConnection();
//        
//        Document dom = builder.build(connection.getInputStream());
//        
//        Element root = dom.getRootElement();
//        System.out.println(root.getName());
//        
//        Element tiledPatterns = root.getChild("TiledPatterns");
//        System.out.println(tiledPatterns.getName());
//        
//        Element onlineResource = tiledPatterns.getChild("OnlineResource");
//        Namespace xlink = onlineResource.getNamespace("xlink");
//        Attribute href = onlineResource.getAttribute("href", xlink);        
//        String baseUrl = href.getValue();
//        
//        System.out.println(baseUrl);
//        
//        List<Element> tiledGroups = tiledPatterns.getChildren("TiledGroup");
//        
//        for(Element tiledGroup : tiledGroups) {
//            System.out.println(tiledGroup.getChildText("Name"));
//            
//            if (tiledGroup.getChild("TilePattern") != null) { //$NON-NLS-1$
//                    NASASource source = new NASASource(tiledGroup, baseUrl);
//            }
//        }
//        
        
    }

}
