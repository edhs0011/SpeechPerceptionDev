package Perception;

import java.util.Date;
import java.util.TimerTask;

public class AutoSave35TimerTask extends TimerTask{

    private SystemProgress sysPro;
    
    public AutoSave35TimerTask() {
        sysPro = new SystemProgress();
    }

    @Override
    public void run() {
        if (sysPro == null){
            sysPro = new SystemProgress();
        }
        
        System.out.println("[AutoSave35TimerTask]" + new Date() + "...File save completed!");
        String evaluationID = SystemInit.myEval.getEvaluatorID().trim();
        if (evaluationID != null && evaluationID.length() > 0){
            sysPro.saveProgram(ISystem.SAVE_MAIN);
            sysPro = null;
        }
    }
}
