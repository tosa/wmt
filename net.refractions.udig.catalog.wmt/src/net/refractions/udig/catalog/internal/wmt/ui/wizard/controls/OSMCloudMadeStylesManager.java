package net.refractions.udig.catalog.internal.wmt.ui.wizard.controls;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.refractions.udig.catalog.internal.wmt.WMTPlugin;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Provides access to the styles/style-groups from the
 * OSMCloudMadeControl.
 * 
 * @author to.srwn
 * @since 1.1.0
 */
public class OSMCloudMadeStylesManager {
	
	private static final String URL_FEATURED = "http://maps.cloudmade.com/map_styles/gallery_search?group=1&color=-14&text="; //$NON-NLS-1$
	private static final String URL_CLOUDMADE = "http://maps.cloudmade.com/map_styles/gallery_search?group=3&color=-14&text="; //$NON-NLS-1$
	
	private static final String PROP_FEATURED = "WIZ_CLOUDMADE_FEATURED"; //$NON-NLS-1$
    private static final String PROP_CLOUDMADE = "WIZ_CLOUDMADE_CLOUDMADE"; //$NON-NLS-1$
    
    private static final String FALLBACK_FILE_FEATURED = "group-featured.txt"; //$NON-NLS-1$
    private static final String FALLBACK_FILE_CLOUDMADE = "group-cloudmade.txt"; //$NON-NLS-1$
	
	private static final String IMG_DIR = "/icons/wizard/"; //$NON-NLS-1$
	public static final String IMG_DEFAULT = IMG_DIR + "default.png"; //$NON-NLS-1$
	public static final String IMG_LOADING = IMG_DIR + "loading.png"; //$NON-NLS-1$
	
	private Map<String, CloudMadeStyle> styles;
	
	public OSMCloudMadeStylesManager() {
		styles = new HashMap<String, CloudMadeStyle>();
	}
	
	public CloudMadeStyleGroup getGroupFeatured() {
		return cacheGroup(new OSMCloudMadeStylesManager.CloudMadeStyleGroup(
				URL_FEATURED, 
				PROP_FEATURED, 
				FALLBACK_FILE_FEATURED));
	}
	
	public CloudMadeStyleGroup getGroupCloudMade() {
		return cacheGroup(new OSMCloudMadeStylesManager.CloudMadeStyleGroup(
				URL_CLOUDMADE, 
				PROP_CLOUDMADE, 
				FALLBACK_FILE_CLOUDMADE));
	}
	
	private CloudMadeStyleGroup cacheGroup(CloudMadeStyleGroup group) {
		List<CloudMadeStyle> groupStyles = group.getStyles();
		
		for(CloudMadeStyle style : groupStyles) {
			cacheStyle(style);
		}
		
		return group;
	}

	private void cacheStyle(CloudMadeStyle style) {
		if (!styles.containsKey(style.getId())) {
			styles.put(style.getId(), style);
		}
	}
	
	public CloudMadeStyle getStyleFromId(String id) {
		// do we have this style in the cache?
		if (styles.containsKey(id)) {
			return styles.get(id);
		}
		
		// download style information
		String requestUrl = CloudMadeStyleGroup.buildRequestUrl("", id); //$NON-NLS-1$
		List<CloudMadeStyle> styles = new CloudMadeStyleGroup(requestUrl).getStyles();
		
		for (CloudMadeStyle style : styles) {
			if (style.getId().equals(id)) {
				cacheStyle(style);
				
				return style;
			}
		}
		
		// if style could not be found, return NullObject			
		return CloudMadeStyle.EMPTY_STYLE;
	}
	
	public static class CloudMadeStyle {
		private String id;
		private String name;
		private String author;
	    
		public static final String EMPTY_STYLE_ID = "default"; //$NON-NLS-1$
		public static final CloudMadeStyle EMPTY_STYLE = new CloudMadeStyle(EMPTY_STYLE_ID, "not found", "-"); //$NON-NLS-1$ //$NON-NLS-2$
		
		public CloudMadeStyle(String id, String name, String author) {
			this.id = id;
			this.name = name;
			this.author = author;
		}
		
		public String getAuthor() {
			return author;
		}

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}
		
		public String toString() {
			return name;
		}
	}
	
	public static class CloudMadeStyleGroup {
		private String requestUrl;
		private String backupProperties;
		private String fallBackFile;
		
		/* Set to true if only an url request should be sent*/
		private boolean requestOnly = false;
		
		private List<CloudMadeStyle> styles;
		
		private static final  String stylePatternString = 
			"(\\{)(\"name\": )(\".*?\")(, \"author\": )(\".*?\")(, \"tile_cache\": )(\\d+)(, \"id\": )(\\d+)(\\})"; //$NON-NLS-1$
	    private static final Pattern stylePattern = Pattern.compile(
	    		stylePatternString, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        private IPreferenceStore preferences;
		
	    public CloudMadeStyleGroup(String requestUrl) {
	    	this(requestUrl, null, null);
	    }
	    
		public CloudMadeStyleGroup(String requestUrl, String backupProperties, String fallBackFile) {
			this.requestUrl = requestUrl;
			this.backupProperties = backupProperties;
			this.fallBackFile = fallBackFile;
			
			if (backupProperties == null) {
				requestOnly = true;
			}
			
			preferences = WMTPlugin.getDefault().getPreferenceStore();
			
			loadStyles();
			
			if (styles == null) {
				styles = Collections.emptyList();
			}
			
		}
		
		public List<CloudMadeStyle> getStyles() {
			return styles;
		}
		
		private void loadStyles() {
			try {
				getStylesFromRequest();
				
				return;
			} catch (Exception exc) {
				// download failed, log
			}
			
			// Do not try to get the data from a file
			if (requestOnly) return;
			
			try {
				getStylesFromBackup();
				
				return;
			} catch (Exception exc) {
				// backupe file is broken, log
			}
			
			try {
				getStylesFromFallBackFile();
				
				return;
			} catch (Exception exc) {
				// fallback file is broken, log
				styles = Collections.emptyList();
			}
		}
		
		private void getStylesFromRequest() throws Exception {
			// download request
			
			
			URL url = new URL(requestUrl);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(url.openStream()));
			
			String rawStyles = getStylesFromStream(in);
			
			// do not write the result to a file
			if (requestOnly) return;
			
			// write the plain response to the properties as backup
			try {
			    preferences.setValue(backupProperties, rawStyles);
			} catch(Exception exc) {
				// writing to backup file failed, log
			}
			
			if ((styles == null) || (styles.isEmpty())) {
			    throw new Exception("No Styles loaded"); //$NON-NLS-1$
			}
		}
		
		private void getStylesFromBackup() throws Exception {
			// get from backup in properties
			getStylesFromString(preferences.getString(backupProperties));
            
            if ((styles == null) || (styles.isEmpty())) {
                throw new Exception("No Styles loaded"); //$NON-NLS-1$
            }
		}
		
		private void getStylesFromFallBackFile() throws Exception {
			// get from fallback file
		    URL url = getClass().getResource(fallBackFile);
			getStylesFromFile(url);
		}
		
		private void getStylesFromFile(URL filePath) throws Exception {
			// get from file
			BufferedReader in = new BufferedReader(new InputStreamReader(filePath.openStream()));
			
			getStylesFromStream(in);
		}
		
		private String getStylesFromStream(BufferedReader reader) throws Exception {
			StringBuffer response = new StringBuffer();
			
			try {
				String inputLine;
				
				while ((inputLine = reader.readLine()) != null) {
					response.append(inputLine);
				}	
			} finally {
				reader.close();
			}
			
			String rawStyles = response.toString();
			getStylesFromString(rawStyles);
			
			return rawStyles;
		}
		
		private void getStylesFromString(String rawStyles) throws Exception {
			styles = new ArrayList<CloudMadeStyle>();
			
	        Matcher matcher = stylePattern.matcher(rawStyles);
	        
	        while (matcher.find()) {
	            String nameText = getStringWithoutQuotes(matcher.group(3)).trim();
	            String authorText = getStringWithoutQuotes(matcher.group(5)).trim();
	            String idText = matcher.group(9).trim();
	            
	            CloudMadeStyle style = new CloudMadeStyle(idText, nameText, authorText);
	            styles.add(style);	            
	        }
		}
		
		private String getStringWithoutQuotes(String s) {
			if (s.length() <= 0) return ""; //$NON-NLS-1$
			
			return s.substring(1, s.length() - 1);
		}
		
		public static String buildRequestUrl(String group, String text) {
			return "http://maps.cloudmade.com/map_styles/gallery_search?group=" + group.trim() //$NON-NLS-1$
				+ "&color=-14&text=" + text.trim(); //$NON-NLS-1$
		}
	}
}

