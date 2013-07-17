package Perception;


import Perception.PanelCollection;
import java.util.Date;
import java.util.TimerTask;

public class AutoReleasePanelTimerTask extends TimerTask{

    @Override
    public void run() {
        System.out.println("[AutoReleasePanel]" + new Date() + "...Cleaning started!");
        PanelCollection.clearCollection();
        System.out.println("[AutoReleasePanel]" + new Date() + "...Cleaning ended!");
    }
    
}
