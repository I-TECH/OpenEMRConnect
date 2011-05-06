/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ke.go.moh.oec.kisumuhdss;

import java.util.logging.Level;
import java.util.logging.Logger;
import ke.go.moh.oec.lib.Mediator;

/**
 *
 * @author EWere
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
                // TODO code application logic here
        FindPersonResponder fpr = new FindPersonResponder();
        Mediator.registerCallback(fpr);

        Mediator mediator = new Mediator();
        while(true)
        {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

       
    }

}
