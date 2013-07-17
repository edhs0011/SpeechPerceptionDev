/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Perception;

import java.awt.Component;
import java.awt.Cursor;
import javax.swing.JPanel;

public class SaveProgressShortcutRunnable implements Runnable {

    private SystemProgress sysPro;
    private CursorUtils cursor;

    public SaveProgressShortcutRunnable() {
        sysPro = new SystemProgress();
        cursor = new CursorUtils();
    }

    @Override
    public void run() {
        saveAndWriteProgress();
    }

    private synchronized void saveAndWriteProgress() {

        cursor.changeCursor(Cursor.WAIT_CURSOR);
        System.out.println(Thread.currentThread().getName() + "[SaveShortCut] Start Saving...");

        sysPro.saveEvaluatorProgress();
        sysPro.writeProgressToFile();

        System.out.println(Thread.currentThread().getName() + "[SaveShortCut] End Saving...");

        cursor.resetCursor();

        sysPro = null;
        cursor = null;
    }
}