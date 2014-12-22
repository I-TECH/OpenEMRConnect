/*
 * App.java
 */
package ke.go.moh.oec.reception.gui;

import javax.swing.JOptionPane;
import ke.go.moh.oec.reception.controller.OECReception;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class App extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup() {
        try {
            show(new LoginDialog(null, true, this));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Application malfunction! " + ex.getMessage() 
                    + ". Please contact your administrator.", OECReception.applicationName(), JOptionPane.WARNING_MESSAGE);
            System.exit(1);
        }
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override
    protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of App
     */
    public static App getApplication() {
        return Application.getInstance(App.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(App.class, args);
    }
    
    
}
