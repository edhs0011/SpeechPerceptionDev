package Perception;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import javax.sound.sampled.Clip;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MediaPlayerPanel extends javax.swing.JPanel {
//  NOTE: Duplicate with EvaluationPanel, WaveFormProcessor
//    private static final int SELECTED_SPEAKER_INDEX = SystemInit.myEval.getSpeakerIndex();
//    private static final String DEFAULT_SPEAKER = SystemInit.myEval.getSpeakerOrderList().get(SELECTED_SPEAKER_INDEX - 1);
//    private static final int DEFAULT_WORDID = 1;
//    
//    private static final int ANIMATION_DELAY = 1000;    // in milliseconds (= 1s)
//    private static final int INITAL_DELAY = 0;    // in milliseconds (start immediately)
    private final int SELECTED_SPEAKER_INDEX = SystemInit.myEval.getSpeakerIndex();
    private final String DEFAULT_SPEAKER = SystemInit.myEval.getSpeakerOrderList().get(SELECTED_SPEAKER_INDEX - 1);
    private final int DEFAULT_WORDID = 1;
    
    private final int ANIMATION_DELAY = 1000;    // in milliseconds (= 1s)
    private final int INITAL_DELAY = 0;    // in milliseconds (start immediately)

    private MediaController mController;
    private WaveFormProcessor wavePro;
    private WaveFormPanel wavePane;
    
//    private Timer sliderAnimationTimer;
//    private ActionListener sliderAnimation;
    private JSlider seekSlider;
    private JPanel sliderPane;
    private JLabel lblSeek;
    
    private AudioSample sample;
    
    private String speakerID;
    private int wordID;
//    private int currentFramePosition; 
//  Created so that the transcription start/end mark won't be affected (overwritten) by the initial mark change events
    private boolean onLoad;
    private boolean isJustMovingSeek;

    private DecimalFormat df = new DecimalFormat("#.##");
    
    public MediaPlayerPanel(String speakerID, int wordID) {
        
        initComponents();
        onLoad = true;
        isJustMovingSeek = false;
        
        mController = new MediaController(this);
        wavePro = new WaveFormProcessor();
        
        wavePane = null;
        sample = null;

//      0 - No speaker is passed; 0 - no word selected
//      SET DEFAULT if above is true
        this.speakerID = (speakerID.equals("0")) ? DEFAULT_SPEAKER : speakerID;
        this.wordID = (wordID < 0) ? DEFAULT_WORDID : wordID;
//        currentFramePosition = 0;
        
        makePanel(this.speakerID, this.wordID);
        makeShortCut();
    }
    
    private void makePanel(String speakerID, int wordID) {
        
//        sliderAnimation = new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                changeSliderAnimation();
//            }
//        };
//        sliderAnimationTimer = new Timer(ANIMATION_DELAY, sliderAnimation);
//        sliderAnimationTimer.setInitialDelay(INITAL_DELAY);
//        sliderAnimationTimer.setCoalesce(true);
        
//      Must set opaque = false (ie. Transparent) in order for drawings to be shown
        playerPane.setLayout(new BorderLayout());
        playerPane.setOpaque(false);    

        wavePane = wavePro.generateWaveForm(speakerID);

        sliderPane = new JPanel(new BorderLayout());
        seekSlider = new JSlider(SwingConstants.HORIZONTAL);
        seekSlider.setValue(0);
        seekSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                seekSliderChange(evt);
            }
        });    

        lblSeek = new JLabel();
        lblSeek.setText(String.valueOf(df.format(0)));
        sliderPane.add(seekSlider, BorderLayout.CENTER);
        sliderPane.add(lblSeek, BorderLayout.EAST);

        playerPane.add(wavePane, BorderLayout.CENTER);
        playerPane.add(sliderPane, BorderLayout.SOUTH);
        
        Transcription transcript = mController.getLastSeenTranscription();
        mController.createAudioClip(speakerID, transcript);
        setSliderMaxFrame(mController.getAudioInputFrameLength());
        
        Word word = mController.getWordByID(speakerID, wordID);
        lblChiChar.setText(word.getChineseChar());     
        
        startSlider.setValue(transcript.getStartFrame());
        endSlider.setValue(transcript.getEndFrame());
        mController.indicateLoopPoints(transcript.getStartFrame(), transcript.getEndFrame());

        enableStopControlButton(false);
        
        onLoad = false;
    }
    
    private void setSliderMaxFrame(int audioFrameLength) {
        if (seekSlider.getMaximum() != audioFrameLength) {
            seekSlider.setMaximum(audioFrameLength);
        }

        if (startSlider.getMaximum() != audioFrameLength) {
            startSlider.setMaximum(audioFrameLength);
            float startTime = convertFrameToSeconds(startSlider.getValue());
            lblStart.setText(String.valueOf(df.format(startTime)));
        }

        if (endSlider.getMaximum() != audioFrameLength) {
            endSlider.setMaximum(audioFrameLength);
            float endTime = convertFrameToSeconds(endSlider.getValue());
            lblEnd.setText(String.valueOf(df.format(endTime)));
        }
    }
    
    public void enableStopControlButton(boolean isEnable) {
//      Use by mediaController as well to disable the stop button once the audio stop playing    
//        if (!isEnable) {
//            stopSliderAnimation();
//        }
        btnStop.setEnabled(isEnable);
        enablePlayingControlButtons(!isEnable);
    }

    private void enablePlayingControlButtons(boolean isEnable) {
//      WHEN LOOP/PLAY BUTTON ENABLE, STOP IS DISABLE AND VICE VERSA
        btnLoop.setEnabled(isEnable);

//      THE SWAP OF THE ENALE FROM ENABLESTOPCONTROLBUTTON -> ie. IF BTNSTOP IS TRUE, THIS ENABLE IS FALSE
        boolean btnLoopEnable = isEnable;
        String btnPlayPauseText = btnLoopEnable ? "Play" : "Pause";
        btnPlayPause.setText(btnPlayPauseText);
    }
    
    
    private void makeShortCut(){
        btnPlayPauseShortCut();
        btnLoopShortCut();
        btnStopShortCut();
    }
    
    private void btnPlayPauseShortCut() {
        Action playPauseMedia = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                playPauseMedia();
            }
        };
        
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
        InputMap inputMap = btnPlayPause.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = btnPlayPause.getActionMap();
        inputMap.put(key, "PlayPause Media");
        actionMap.put("PlayPause Media", playPauseMedia);
        btnPlayPause.setMnemonic(KeyEvent.VK_P);    // Alt + P
    }

    private void btnLoopShortCut() {
        Action loopMedia = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loopMedia();
            }
        };

        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);
        InputMap inputMap = btnLoop.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = btnLoop.getActionMap();
        inputMap.put(key, "Loop Media");
        actionMap.put("Loop Media", loopMedia);
        btnLoop.setMnemonic(KeyEvent.VK_L);         // Alt + L
    }

    private void btnStopShortCut() {
        Action stopMedia = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopMedia();
            }
        };

        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0);
        KeyStroke anotherKey = KeyStroke.getKeyStroke(KeyEvent.VK_SEMICOLON, 0);
        InputMap inputMap = btnStop.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = btnStop.getActionMap();
        inputMap.put(key, "Stop Media");
        inputMap.put(anotherKey, "Stop Media");
        actionMap.put("Stop Media", stopMedia);
        btnStop.setMnemonic(KeyEvent.VK_O);    // Alt + 'O'
    }
    
//    private void startSliderAnimation() {
//        changeSliderAnimation();
//        if (sliderAnimationTimer.isRunning()) {
//            sliderAnimationTimer.restart();
//        } 
//        
//        else {
//            sliderAnimationTimer.start();
//        }
//    }
//    
//    private void stopSliderAnimation() {
//        if (sliderAnimationTimer.isRunning()) {
//            sliderAnimationTimer.stop();
//        }
//        changeSliderAnimation();
//    }
    
//    private synchronized void changeSliderAnimation(){
//    private void changeSliderAnimation(){
//        isJustMovingSeek = true;
//        currentFramePosition = mController.getCurrentFramePosition();
//        seekSlider.setValue(currentFramePosition);
//        System.out.println("[MediaPlayerPanel::Change] CurrentFramePosition : " + currentFramePosition);
//        isJustMovingSeek = false;
//    }
    
    private void resetSlider(){
        isJustMovingSeek = true;
        seekSlider.setValue(0);
//        currentFramePosition = mController.getCurrentFramePosition();
//        seekSlider.setValue(currentFramePosition);
//        System.out.println("[MediaPlayerPanel::Change] CurrentFramePosition : " + currentFramePosition);
        isJustMovingSeek = false;
    }
    
    /**
     * Event Handler
     */
    private void seekSliderChange(ChangeEvent evt) {
/**
 *      Check state change - if slider move then line drawn is temporary When
 *      seekSlider change frame, the music also changes
 */
        JSlider source = (JSlider) evt.getSource();
         
        if (! onLoad) {
            drawSeekMark();
            drawStartEndMark();
            if (! isJustMovingSeek){
                mController.stopAndResetClip();
                mController.playClip(source.getValue());
//                startSliderAnimation();
            } 
        }
        
        float seekTime = convertFrameToSeconds(source.getValue());
        lblSeek.setText(String.valueOf(df.format(seekTime)));
    }
        
    private void rangeSliderChange(ChangeEvent evt) {
        JSlider source = (JSlider) evt.getSource();
        Transcription transcript = mController.getLastSeenTranscription();
              
//      Don't set the mark on load, because it might overwrite the one inside Transcription
//      NOTE: Disregard whether start value is GREATER than end value or 
//      end value is SMALLER than start value - Check in Transcription
        
        if (! onLoad) {
            drawSeekMark();
            drawStartEndMark();
            if (! source.getValueIsAdjusting()) {
                transcript.setMark(startSlider.getValue(), endSlider.getValue());
                mController.indicateLoopPoints(transcript.getStartFrame(), transcript.getEndFrame());
            }
        }

        float startTime = convertFrameToSeconds(startSlider.getValue());
        float endTime = convertFrameToSeconds(endSlider.getValue());
        
        lblStart.setText(String.valueOf(df.format(startTime)));
        lblEnd.setText(String.valueOf(df.format(endTime)));
    }
    
//  NOTE: DUPLICATE WITH FLAGOUTPUT
    private float convertFrameToSeconds(int frameLength) {
        Transcription transcript = mController.getLastSeenTranscription();
        float framesPerSecond = transcript.getFrameRate();
        
        if (framesPerSecond > 0) {
            switch (frameLength) {
                case 0:
                case -1:
                    return frameLength;                   
            }
        }
        return frameLength / framesPerSecond;
    }
    
    private void mediaControlBtnClicked(MouseEvent evt) {
        JButton source = (JButton) evt.getSource();

        if (source.isEnabled()) {
            if (source == btnPlayPause) {
                playPauseMedia();
            } 
            
            else if (source == btnLoop) {
                loopMedia();
            } 
            
            else if (source == btnStop) {
                stopMedia();
            }
        }
    }
    
    private void playPauseMedia(){
        String btnPlayPauseText = btnPlayPause.getText();
        
        if (btnPlayPauseText.equals("Play")) {
            mController.playClip();
            enableStopControlButton(true);
//            startSliderAnimation();
        }
        
        else{
            mController.pauseClip();
//            enableStopControlButton(false);
//            stopSliderAnimation();
        }
    }
    
    private void loopMedia() {
        mController.loopClip(Clip.LOOP_CONTINUOUSLY);
//        startSliderAnimation();
        enableStopControlButton(true);
    }

//  IMPROVEMENT: INSTEAD OF DRAWSEEKMARK - CAN TRY CLEARSEEKMARK    
    private synchronized void stopMedia(){
        isJustMovingSeek = true;
        stopPlaying();
        isJustMovingSeek = false;
    }
    
//  FOR EVALUATIONPANEL TO CALL THIS METHOD WHEN THE WORD CHANGES
    public void startPlayingOnStart() {
        loopMedia();
//        stopSliderAnimation();
//        resetSlider();
    }
        
    public void stopPlaying(){
        mController.stopAndResetClip();
//        stopSliderAnimation();
//        resetSlider();
    }
    
    private void drawSeekMark(){
        wavePro.markIndicator(seekSlider.getValue(), WaveFormPanel.SEEKMARKER);
    }   
    
    private void drawStartEndMark(){
        wavePro.markIndicator(startSlider.getValue(), WaveFormPanel.STARTMARKER);
        wavePro.markIndicator(endSlider.getValue(), WaveFormPanel.ENDMARKER);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        titlePane = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        lblChiChar = new javax.swing.JLabel();
        controlPane = new javax.swing.JPanel();
        btnPlayPause = new javax.swing.JButton();
        btnLoop = new javax.swing.JButton();
        btnStop = new javax.swing.JButton();
        startSlider = new javax.swing.JSlider();
        endSlider = new javax.swing.JSlider();
        lblStart = new javax.swing.JLabel();
        lblEnd = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        playerPane = new javax.swing.JPanel();

        titlePane.setPreferredSize(new java.awt.Dimension(650, 37));

        jLabel1.setText("Playing: ");

        lblChiChar.setText("[lblChineseChar]");
        lblChiChar.setPreferredSize(new java.awt.Dimension(650, 15));

        javax.swing.GroupLayout titlePaneLayout = new javax.swing.GroupLayout(titlePane);
        titlePane.setLayout(titlePaneLayout);
        titlePaneLayout.setHorizontalGroup(
            titlePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(titlePaneLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblChiChar, javax.swing.GroupLayout.PREFERRED_SIZE, 544, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(106, 106, 106))
        );
        titlePaneLayout.setVerticalGroup(
            titlePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(titlePaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(titlePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblChiChar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        controlPane.setPreferredSize(new java.awt.Dimension(650, 80));

        btnPlayPause.setText("Play");
        btnPlayPause.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnPlayPauseMouseClicked(evt);
            }
        });

        btnLoop.setText("Loop");
        btnLoop.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnLoopMouseClicked(evt);
            }
        });

        btnStop.setText("Stop");
        btnStop.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnStopMouseClicked(evt);
            }
        });

        startSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                startSliderStateChanged(evt);
            }
        });

        endSlider.setPreferredSize(new java.awt.Dimension(150, 23));
        endSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                endSliderStateChanged(evt);
            }
        });

        lblStart.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblStart.setText("0");

        lblEnd.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblEnd.setText("0");

        jLabel2.setText("Start");

        jLabel3.setText("End");

        javax.swing.GroupLayout controlPaneLayout = new javax.swing.GroupLayout(controlPane);
        controlPane.setLayout(controlPaneLayout);
        controlPaneLayout.setHorizontalGroup(
            controlPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controlPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(controlPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(controlPaneLayout.createSequentialGroup()
                        .addComponent(btnPlayPause, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27)
                        .addComponent(btnLoop)
                        .addGap(27, 27, 27)
                        .addComponent(btnStop))
                    .addGroup(controlPaneLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(41, 41, 41)
                        .addComponent(startSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblStart, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel3)
                        .addGap(50, 50, 50)
                        .addComponent(endSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(13, Short.MAX_VALUE))
        );

        controlPaneLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnLoop, btnPlayPause, btnStop});

        controlPaneLayout.setVerticalGroup(
            controlPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controlPaneLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(controlPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnPlayPause)
                    .addComponent(btnLoop)
                    .addComponent(btnStop))
                .addGap(13, 13, 13)
                .addGroup(controlPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(startSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(controlPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblStart)
                        .addComponent(jLabel3))
                    .addComponent(endSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblEnd)
                    .addComponent(jLabel2))
                .addContainerGap())
        );

        playerPane.setPreferredSize(new java.awt.Dimension(650, 250));

        javax.swing.GroupLayout playerPaneLayout = new javax.swing.GroupLayout(playerPane);
        playerPane.setLayout(playerPaneLayout);
        playerPaneLayout.setHorizontalGroup(
            playerPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        playerPaneLayout.setVerticalGroup(
            playerPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 250, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(titlePane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 610, Short.MAX_VALUE)
                    .addComponent(playerPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)
                    .addComponent(controlPane, javax.swing.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(titlePane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(playerPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(controlPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        getAccessibleContext().setAccessibleName("");
    }// </editor-fold>//GEN-END:initComponents

    private void btnPlayPauseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnPlayPauseMouseClicked
        mediaControlBtnClicked(evt);
    }//GEN-LAST:event_btnPlayPauseMouseClicked

    private void btnStopMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnStopMouseClicked
        mediaControlBtnClicked(evt);
    }//GEN-LAST:event_btnStopMouseClicked

    private void btnLoopMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnLoopMouseClicked
        mediaControlBtnClicked(evt);
    }//GEN-LAST:event_btnLoopMouseClicked

    private void startSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_startSliderStateChanged
        rangeSliderChange(evt);
    }//GEN-LAST:event_startSliderStateChanged

    private void endSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_endSliderStateChanged
        rangeSliderChange(evt);
    }//GEN-LAST:event_endSliderStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLoop;
    private javax.swing.JButton btnPlayPause;
    private javax.swing.JButton btnStop;
    private javax.swing.JPanel controlPane;
    private javax.swing.JSlider endSlider;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel lblChiChar;
    private javax.swing.JLabel lblEnd;
    private javax.swing.JLabel lblStart;
    private javax.swing.JPanel playerPane;
    private javax.swing.JSlider startSlider;
    private javax.swing.JPanel titlePane;
    // End of variables declaration//GEN-END:variables
}