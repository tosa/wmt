package net.refractions.udig.catalog.internal.wmt.ui.view;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

public class WMTZoomLevelToolbar extends WorkbenchWindowControlContribution {

    @Override
    protected Control createControl(Composite parent) {
        parent.setLayout(new RowLayout(SWT.HORIZONTAL));
        Composite composite = new Composite(parent, SWT.NONE);   
        composite.setLayout(new RowLayout(SWT.HORIZONTAL));
                 
        //region Label "Layer"
        Label lblLayer = new Label (composite, SWT.HORIZONTAL);
        lblLayer.setText("test");
        ComboViewer cvLayers = new ComboViewer(composite, SWT.READ_ONLY);
        cvLayers.setContentProvider(new ArrayContentProvider());
        
        FontData[] fontData = cvLayers.getCombo().getFont().getFontData();
        fontData[0].height = 5;
        System.out.println("--------------------- Font: " + fontData[0].height);
        Font font = new Font(parent.getDisplay(), new FontData("Courier New", 5, SWT.NORMAL));//fontData[0]);
        cvLayers.getCombo().setFont(font);
        
        
        cvLayers.setInput(new String[]{"a","bsds","sdf"}); 
        cvLayers.getCombo().setLayoutData(new RowData(120, 30));//.setSize(width, height)(40, 16);
        
        
        composite.pack();
        
        return composite;
    }

}
