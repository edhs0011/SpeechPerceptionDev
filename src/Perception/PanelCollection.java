/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Perception;

import java.util.ArrayList;
import javax.swing.JPanel;

/**
 *
 * @author YuanJian
 */
public class PanelCollection {

    private static ArrayList<JPanel> panelCollection = new ArrayList<>();

    public PanelCollection() {
    }

    public synchronized void addPanel(JPanel oldPanel) {
        System.out.println("[PanelCollection] Adding..." + oldPanel + " @ " + panelCollection.size());
        panelCollection.add(oldPanel);
    }

    public static synchronized void clearCollection() {
        
        System.out.println("[PanelCollection] Clearing Collection..." + panelCollection.size());
        for (JPanel oldPane : panelCollection) {
            Thread releasePanelThread = new Thread(new ReleasePanelResourceRunnable(oldPane));
            releasePanelThread.start();
        }
        panelCollection.clear();
        panelCollection.trimToSize();
    }
}
