
import ke.go.moh.oec.lib.Mediator;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jim Grace
 */
public class Test {

    static void main(String[] args) {
        Mediator mediator = new Mediator();
        String instanceName = Mediator.getProperty("Instance.Name");
        System.out.println("Instance.Name = '" + instanceName + "'");
    }
}
