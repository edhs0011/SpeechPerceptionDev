package Perception;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayer;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

public class MainFrame extends javax.swing.JFrame {

    private static JPanel myPane;
    private static JFrame myFrame;
    private JLayer<JPanel> jLayer;
    public static final WaitLayerUI layerUI = new WaitLayerUI();
    private SystemInit sysInit;
//    private DatabaseManager dbMan;
    public static ExecutorService executor;

    public MainFrame() {
        systemStartup();
        makeFrame();
        makeShortCut();
//        long megaBytes = 1024 * 1024;
//        System.out.println("Max Memory: " + Runtime.getRuntime().maxMemory()/ megaBytes + " M");
//        System.out.println("Total Memory: " + Runtime.getRuntime().totalMemory() / megaBytes + " M");
//        System.out.println("Free Memory: " + Runtime.getRuntime().freeMemory()/ megaBytes + " M");
    }

    private void systemStartup() {
        System.out.println("[MainFrame] Current Directory: " + new File(".").getAbsolutePath());    // Current Working Directory

        executor = Executors.newSingleThreadExecutor();
        sysInit = new SystemInit();
//        dbMan = new DatabaseManager();

        sysInit.initialise();
        sysInit = null;
//      printCheck();
    }
    
    private void makeFrame() {
        myFrame = new JFrame("Speech Perception Evaluation Tool");
        myPane = new JPanel();

        final Timer stopper = new Timer(4000, new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                layerUI.stop();
            }
        });
        stopper.setRepeats(false);

//      Actual Implmentation Starts from homepage The rest for TESTING
        HomePanel homePane = new HomePanel();
        myPane.add(homePane);

//      EvaluationPanel evaluationPane = new EvaluationPanel("0", 0);
//      myPane.add(evaluationPane);

//      MediaPlayerPanel mediaPane = new MediaPlayerPanel("utterance3", 1);
//      myPane.add(mediaPane);

//        myFrame.getContentPane().add(myPane);
        jLayer = new JLayer<>(myPane, layerUI);
        myFrame.getContentPane().add(jLayer);
        myFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        myFrame.setSize(1064, 720); // Full Screen 1366, 768
        myFrame.setResizable(false);
        myFrame.setLocationRelativeTo(null);
        myFrame.setVisible(true);
        myFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);   // For the window closing event
    }

    public static void changePanel(JPanel panel) {
        releaseMainPaneResource();
        myPane.removeAll();
        myPane.add(panel);
        myPane.requestFocusInWindow();
        refresh();
    }

    //  NOTE: DUPLICATE METHODS IN EVALUATIONPANEL
    private static void releaseMainPaneResource(){
//        System.out.println(myPane.toString());
        for (Component component : myPane.getComponents()) {
//            System.out.println("[MainFrame::ReleaseResource] " + component);
            if (component instanceof JPanel) {
                JPanel panelComp = (JPanel) component;
                Thread releasePanelThread = new Thread(new ReleasePanelResourceRunnable(panelComp));
                releasePanelThread.start();
            }
            component = null;
////            System.out.println("[MainFrame] " + component);
        }
    }
    
    public static void refresh() {
        myFrame.revalidate();
        myFrame.repaint();
    }

    public static void refresh(int x, int y, int width, int height) {
        myFrame.revalidate();
        myFrame.repaint(x, y, width, height);
    }

    public static JPanel getCurrentPanelContainer() {
        return myPane;
    }

    public static void saveAndExit() {
        int evaluatorChoice = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?", "Closing...",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (evaluatorChoice == JOptionPane.YES_OPTION) {
            layerUI.start();
            executor.execute(new SaveAndExitRunnable());
        } 
        
        else if (evaluatorChoice == JOptionPane.NO_OPTION || evaluatorChoice == JOptionPane.CLOSED_OPTION) {
            HomePanel homePane = new HomePanel();
            changePanel(homePane);
        }
    }
    
//  NOTE: For checking correctness  
    private void printCheck() {
//        System.out.println("[MainFrame] EvaluatorID " + SystemInit.myEval.getEvaluatorID() + " - " + SystemInit.myEval.getLastSeenSpeakerID()
//                + " - " + SystemInit.myEval.getSpeakerIndex());
//        for (String speakerID : SystemInit.myEval.getSpeakerOrderList()) {
//            System.out.println(speakerID);
//        }
//
//        for (Speaker speaker : dbMan.getSpeakerList()) {
//            System.out.println(speaker.getSpeakerID() + " - " + speaker.getSpeakerAudioFolderURIString());
//        }
//        sysPro.saveEvaluatorProgress();
    }

    
    private void makeShortCut() {
        saveProgressShortCut();
    }

    private void saveProgressShortCut() {
        Action saveProgress = new AbstractAction("Save Progress") {
            @Override
            public void actionPerformed(ActionEvent e) {
                myPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                executor.execute(new SaveProgressShortcutRunnable());
                myPane.setCursor(Cursor.getDefaultCursor());
            }
        };

        InputMap inputMap = myPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = myPane.getActionMap();
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK);
        inputMap.put(key, "SaveProgress");
        actionMap.put("SaveProgress", saveProgress);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Speech Recognition GUI Tool");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        saveAndExit();
    }//GEN-LAST:event_formWindowClosing

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        saveAndExit();
    }//GEN-LAST:event_formWindowClosed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainFrame();
                
//                for (Map.Entry<Integer, Transcription> entry: SystemInit.myEval.getUtteranceTranscriptionMap().entrySet()){
//                    entry.getValue().setEvaluatedFluencyScore(IFluencyScore.AVERAGE_SCORE);
//                }
                
//                new StressTest().run();
            }
        });
    }
}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
