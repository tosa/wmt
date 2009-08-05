package net.refractions.udig.catalog.internal.wmt.wmtsource;

import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.internal.wmt.WMTService;
import net.refractions.udig.catalog.internal.wmt.WMTServiceExtension;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TreeItem;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

public class NASASourceManager {
    private static final String TILESERVICE_FILE = "NASA-GetTileService.xml"; //$NON-NLS-1$
    
    private Element tiledPatterns = null;
    private WMTServiceExtension serviceExtension;
    
    private static NASASourceManager instance = null;    
    public static synchronized NASASourceManager getInstance() {
        if (instance == null) {
            instance = new NASASourceManager();
        }
        
        return instance;
    }
    
    private NASASourceManager() {
        serviceExtension = new WMTServiceExtension();
        
        try{
            URL url = NASASource.class.getResource(TILESERVICE_FILE);
            
            SAXBuilder builder = new SAXBuilder(false); 
            URLConnection connection = url.openConnection();            
            Document dom = builder.build(connection.getInputStream());
            
            Element root = dom.getRootElement();                    
            tiledPatterns = root.getChild("TiledPatterns"); //$NON-NLS-1$
            
        } catch(Exception exc) {
            tiledPatterns = null;
        }
    }
    
    public void buildWizardTree(TreeItem treeItem) {
        List<?> tiledGroups = tiledPatterns.getChildren("TiledGroup"); //$NON-NLS-1$
        
        buildWizardTreeFromTiledGroups(treeItem, tiledGroups, ""); //$NON-NLS-1$
    }
    
    private void buildWizardTreeFromTiledGroups(TreeItem treeItem, List<?> tiledGroups, String groupNames) {
        for(Object obj : tiledGroups) {
            if (obj instanceof Element) {
                Element tiledGroup = (Element) obj;
                
                String newGroupName = tiledGroup.getChildText("Name"); //$NON-NLS-1$
                String newGroupNames = getConcatenatedGroupName(groupNames, newGroupName);
                
                List<?> newTiledGroups = tiledGroup.getChildren("TiledGroup"); //$NON-NLS-1$
                
                // if there are no sub tile-groups
                if (newTiledGroups.isEmpty()) {
                    TreeItem newTreeItem = new TreeItem(treeItem, SWT.NONE);
                    
                    Map<String,Serializable> params = buildParams(newGroupNames);
                    WMTService service = serviceExtension.createService(params);
                    
                    newTreeItem.setText(newGroupName);
                    newTreeItem.setData(service);
                } else {
                    TreeItem newTreeItem = new TreeItem(treeItem, SWT.NONE);
                    newTreeItem.setText(newGroupName);
                    
                    buildWizardTreeFromTiledGroups(newTreeItem, newTiledGroups, newGroupNames);
                }
            }
        }
    }
    
    private Map<String,Serializable> buildParams(String groupName) {
        Map<String,Serializable> params = serviceExtension.createParams(WMTSource.getRelatedServiceUrl(NASASource.class));
        
        params.put(NASASource.KEY_TILEGROUP_NAME, groupName);
        
        return params;
    }

    public Element getTiledGroup(Map<String, Serializable> params) {
        String tileGroupName = (String) params.get(NASASource.KEY_TILEGROUP_NAME);
        
        List<?> tiledGroups = tiledPatterns.getChildren("TiledGroup"); //$NON-NLS-1$
        
        return searchTileGroup(tileGroupName, "", tiledGroups); //$NON-NLS-1$
    }
    
    private Element searchTileGroup(String groupToSearchFor, String groupNames, List<?> tiledGroups) {
        
        for(Object obj : tiledGroups) {
            if (obj instanceof Element) {
                Element tiledGroup = (Element) obj;
                
                String newGroupName = tiledGroup.getChildText("Name"); //$NON-NLS-1$
                String newGroupNames = getConcatenatedGroupName(groupNames, newGroupName);
                
                if (groupToSearchFor.startsWith(newGroupNames)) {
                    List<?> newTiledGroups = tiledGroup.getChildren("TiledGroup"); //$NON-NLS-1$
                    
                    // if there are no sub tile-groups
                    if (newTiledGroups.isEmpty()) {
                        // check if we have found the right tile-group, if not continue
                        if (groupToSearchFor.equals(newGroupNames)) {
                            return tiledGroup;
                        } else {
                            continue;
                        }
                    } else {
                        // search in sub tile-groups
                        Element foundTileGroup = searchTileGroup(groupToSearchFor, newGroupNames, newTiledGroups);
                        
                        if (foundTileGroup != null) {
                            return foundTileGroup;
                        } else {
                            continue;
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    private String getConcatenatedGroupName(String groupNames, String newGroupName) {
        newGroupName = newGroupName.replace('|', ' ');
        
        if (groupNames.isEmpty()) {
            return newGroupName;
        } else {
            return groupNames + "|" + newGroupName; //$NON-NLS-1$
        }
    }
    
    public String getBaseUrl() {
        Element onlineResource = tiledPatterns.getChild("OnlineResource"); //$NON-NLS-1$
        Namespace xlink = onlineResource.getNamespace("xlink"); //$NON-NLS-1$
        Attribute href = onlineResource.getAttribute("href", xlink); //$NON-NLS-1$        
        String baseUrl = href.getValue();
        
        return baseUrl;
    }
}
