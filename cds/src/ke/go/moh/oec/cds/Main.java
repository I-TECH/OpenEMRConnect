/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ke.go.moh.oec.cds;

import ke.go.moh.oec.lib.Mediator;

/**
 *
 * @author Administrator
 */
public class Main {

    private static Mediator mediator;

    public static Mediator getMediator() {
        return mediator;
    }

    public static void main(String[] args) {
        Thread.currentThread().setName("Cds");
        Cds cds = new Cds();
           mediator = new Mediator();
        Mediator.registerCallback(cds);
        while (true) {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException ex) {
            }
        }
    }
}
