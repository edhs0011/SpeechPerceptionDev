package Perception;

import java.awt.Cursor;

public class SaveAndExitRunnable implements Runnable{

    private SystemProgress sysPro;
    private CursorUtils cursor;

    public SaveAndExitRunnable() {
        sysPro = new SystemProgress();
        cursor = new CursorUtils();
    }
    
    @Override
    public void run() {
        cursor.changeCursor(Cursor.WAIT_CURSOR);
        
        System.out.println(Thread.currentThread().getName() + "[SaveExit] Start Saving...");

        sysPro.saveProgram(ISystem.SAVE_MAIN);
        sysPro.saveProgram(ISystem.SAVE_BACKUP);
        
        System.out.println(Thread.currentThread().getName() + "[SaveExit] End Saving...");
        
        sysPro.writeProgressToFile();
        System.out.println("[SaveExit] Result Output Completed!");

        cursor.resetCursor();
        System.exit(0);
    }
}
