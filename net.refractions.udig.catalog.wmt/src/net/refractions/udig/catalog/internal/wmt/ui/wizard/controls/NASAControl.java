package net.refractions.udig.catalog.internal.wmt.ui.wizard.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class NASAControl extends WMTWizardControl {

    @Override
    protected Control buildControl(Composite composite) {
        Text text = new Text(composite, SWT.WRAP | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.READ_ONLY);
        text.setText("NASA..");

        control = text;
        
        return text;
    }

}
