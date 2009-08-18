package net.refractions.udig.catalog.internal.wmt.wmtsource.ww;

import net.refractions.udig.catalog.internal.wmt.tile.WWTile.WWTileName;

import org.apache.commons.lang.NotImplementedException;
import org.jdom.Element;

public class WMSTileService extends TileService {

    public WMSTileService(Element child) {
        throw new NotImplementedException();
    }

    @Override
    public String getTileRequest( WWTileName tileName ) {
        return null;
    }

}
