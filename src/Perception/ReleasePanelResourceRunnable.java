package Perception;

import java.awt.Component;
import javax.swing.JPanel;

public class ReleasePanelResourceRunnable implements Runnable{
    private JPanel paneToClean;

    public ReleasePanelResourceRunnable(JPanel paneToClean) {
        this.paneToClean = paneToClean;
//        System.out.println(paneToClean.getClass());
    }

    @Override
    public void run() {
//        String panelName = extractPanelName(paneToClean.toString());
//        System.out.println("[ReleasePanelThread] Releasing " + panelName + " resource...");
        releaseResource(paneToClean);
//        System.out.println("[ReleasePanelThread] " + panelName + " all set to null...");
        System.gc();
    }
    
    private void releaseResource(JPanel pane){
        System.out.println("[ReleasePanelThread] Releasing..." + extractPanelName(pane.toString()) + " | Componenent Count: " + pane.getComponentCount());
        for (Component component : pane.getComponents()){
            if (component instanceof JPanel){
//                System.out.println("[ReleasePanelThread] " + compName + " is a JPanel");
//                System.out.println("[ReleasePanelThread] Current Component is a JPanel");
                releaseResource((JPanel)component);
            }
//            System.out.println("[ReleasePanelThread] Releasing..." + component.toString());
            component = null;
//            System.out.println("[ReleasePanelThread] Released..." + component);
        }
        pane = null;
}
    
    private String extractPanelName(String panelFullName){
        String panelName = "";
        int endIndex = panelFullName.indexOf('[');
//        System.out.println(endIndex);
//        System.out.println(panelFullName.substring(0,endIndex));
        if ((endIndex >= 0) && (endIndex < panelFullName.length())) {
            panelName = panelFullName.substring(0, endIndex);
        }
        return panelName;
    }
}
