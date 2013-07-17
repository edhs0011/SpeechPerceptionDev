package Perception;

import java.awt.Component;
import java.awt.Cursor;
import javax.swing.JPanel;

public class CursorUtils {
    
    private JPanel currentPanelContainer;
    private JPanel currentPanel;
            
    public CursorUtils() {
        currentPanelContainer = MainFrame.getCurrentPanelContainer();
        currentPanel = retrievePanel(currentPanelContainer);
//        System.out.println("[CursorUtils] " + currentPanel.toString());
    }
    
    private JPanel retrievePanel(JPanel panelContainer){
        JPanel workingPanel = null;
        for (Component component : panelContainer.getComponents()){
            if (component instanceof JPanel){
                workingPanel = (JPanel) component;
                break;
            }
        }
        if (workingPanel == null){
            workingPanel = new JPanel();
        }
        return workingPanel;
    }
    
//  NOTE: CURSORTYPE SHOULD FOLLOWS THAT OF THE CURSOR.CONSTANT VALUES
    public void changeCursor(int cursorType){
        if (currentPanel != null){
            currentPanel.setCursor(Cursor.getPredefinedCursor(cursorType));
        }
    }
    
//  NOTE: CURSORTYPE WILL RETURN TO THE NORMAL ARROW STYLE
    public void resetCursor(){
        if (currentPanel != null) {
            currentPanel.setCursor(Cursor.getDefaultCursor());
            new Thread(new ReleasePanelResourceRunnable(currentPanel)).start();
            currentPanel = null;
        }
    }
}
