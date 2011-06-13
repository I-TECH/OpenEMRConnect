/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is OpenEMRConnect.
 *
 * The Initial Developer of the Original Code is International Training &
 * Education Center for Health (I-TECH) <http://www.go2itech.org/>
 *
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * ***** END LICENSE BLOCK ***** */
package ke.go.moh.oec.reception.gui.helper;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 *
 * @author Gitahi Ng'ang'a
 */
public class ComponentManager {

    public static List<Component> getCounterpartList(Component component, Container container) {
        List<Component> counterpartList = new ArrayList<Component>(3);
        String strippedComponentName = "";
        for (Component c : container.getComponents()) {
            if (c instanceof JTextField
                    || c instanceof JRadioButton) {
                if (c.getName().toLowerCase().contains(strippedComponentName.toLowerCase())) {
                    if (c.getName().contains("Accept")) {
                        counterpartList.add(1, c);
                    } else if (c.getName().contains("Reject")) {
                        counterpartList.add(2, c);
                    } else {
                        counterpartList.add(0, c);
                    }
                }
            }
        }
        return counterpartList;
    }
}
