package net.refractions.udig.catalog.internal.wmt.wmtsource.ww;

import net.refractions.udig.catalog.internal.wmt.tile.WWTile.WWTileName;

import org.apache.commons.lang.NotImplementedException;
import org.jdom.Element;

/**
 * Represents &lt;WMSAccessor&gt; inside a &lt;ImageAccessor&gt;
 * 
 * @author to.srwn
 * @since 1.1.0
 */
public class WMSTileService extends TileService {

    public WMSTileService(Element child) {
        throw new NotImplementedException();
    }

    @Override
    public String getTileRequest(WWTileName tileName) {
        return null;
    }

}
