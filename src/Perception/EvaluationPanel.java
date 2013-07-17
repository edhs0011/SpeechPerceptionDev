package Perception;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;

public class EvaluationPanel extends javax.swing.JPanel{

    private final String FLAG_STRING = "Flag";
    private final String COMMENT_STRING = "Comment";
    private final String CANCEL_STRING = "Cancel";
    private final String SUBMIT_STRING = "Submit";
    
    private EvaluationManager evalMan;
    private SystemProgress sysPro;
    private MediaPlayerPanel mediaPlayer;
//    private PanelCollection oldPanelCollection;
    
//  NOTE: ONLY DO FOR ONE FOLDER
    private final int SELECTED_SPEAKER_INDEX = SystemInit.myEval.getSpeakerIndex();
    private final String DEFAULT_SPEAKER = SystemInit.myEval.getSpeakerOrderList().get(SELECTED_SPEAKER_INDEX - 1);
    
    private ButtonGroup btnFluencyGroup;
    
    private String speakerID;
    private int wordID;
    private int utteranceID;
    
//  QUICKFIX:
//  CurrentSpeaker use for preventing the listPane from refreshing if the speakerID == currentSpeaker
//  LoadMediaOnce use for preventing the mediaPane from being refresh because MainFrame.refresh lies in refreshListPanel
//  OnLoad use for to prevent excessive unwanted action when setting slider maximum value during loadtime
    private String currentSpeakerID;
    private int currentTranscriptionOrder;
    private boolean onLoad = false;

    public EvaluationPanel(String speakerID, int utteranceID) {
        initComponents();
        onLoad = true;
        evalMan = new EvaluationManager();
//        oldPanelCollection = new PanelCollection();
        
        System.out.println("[EvaluationPanel] UtteranceID coming in..." + utteranceID);
        currentTranscriptionOrder = evalMan.getCurrentTranscriptOrderIndex();
        int lastSeenUtteranceID = evalMan.getUtteranceID(currentTranscriptionOrder);

        //0 - No speaker is passed; 0 - no utterance seen selected
        this.speakerID = (speakerID.equals("0")) ? DEFAULT_SPEAKER : speakerID;
        this.currentSpeakerID = this.speakerID;
        this.utteranceID = (utteranceID <= 0) ? lastSeenUtteranceID : utteranceID;

        Transcription transcript = evalMan.getLastSeenTranscription();
        this.wordID = transcript.getWordID();

        System.out.println("[EvaluationPanel] Speaker: " + this.speakerID + " | UtteranceID: " + this.utteranceID
                + " | WordID: " + this.wordID);

        makePanel();
        makeShortCut();

        setCursor(Cursor.getDefaultCursor());
    }

    private void makePanel() {

        originalHanPinPane.setLayout(new FlowLayout(FlowLayout.LEADING));
        transcriptHanPinPane.setLayout(new FlowLayout(FlowLayout.LEADING));
        
        mediaPane.setLayout(new BorderLayout());
        mediaPane.setOpaque(false);
        mediaPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        
        makeRadioButton();    
        refreshMediaPanel(speakerID, wordID);
        refreshHanPinPane();
        
        hideFlagPane();
        hideCommentPane();

        onLoad = false;
    }
        
    private void makeRadioButton() {
        btnFluencyGroup = new ButtonGroup();

        btnNativeFluency.setText(IFluencyScore.FLUENCY_NATIVE);
        btnNativeFluency.setActionCommand(IFluencyScore.FLUENCY_NATIVE);
        btnNativeFluency.setToolTipText(IFluencyScore.FLUENCY_NATIVE_TOOLTIP_DESCRIPTION);
        btnFluencyGroup.add(btnNativeFluency);

        btnGoodFluency.setText(IFluencyScore.FLUENCY_GOOD);
        btnGoodFluency.setActionCommand(IFluencyScore.FLUENCY_GOOD);
        btnGoodFluency.setToolTipText(IFluencyScore.FLUENCY_GOOD_TOOLTIP_DESCRIPTION);
        btnFluencyGroup.add(btnGoodFluency);

        btnAverageFluency.setText(IFluencyScore.FLUENCY_AVERAGE);
        btnAverageFluency.setActionCommand(IFluencyScore.FLUENCY_AVERAGE);
        btnAverageFluency.setToolTipText(IFluencyScore.FLUENCY_AVERAGE_TOOLTIP_DESCRIPTION);
        btnFluencyGroup.add(btnAverageFluency);

        btnBadFluency.setText(IFluencyScore.FLUENCY_BAD);
        btnBadFluency.setActionCommand(IFluencyScore.FLUENCY_BAD);
        btnBadFluency.setToolTipText(IFluencyScore.FLUENCY_BAD_TOOLTIP_DESCRIPTION);
        btnFluencyGroup.add(btnBadFluency);
    }

    private void refreshMediaPanel(String speakerID, int wordID) {
        this.speakerID = speakerID;
        this.wordID = wordID;

        releaseMediaPanelResource();
        mediaPane.removeAll();
        mediaPane.setLayout(new BorderLayout());
        
        this.mediaPlayer = new MediaPlayerPanel(speakerID, wordID);
        mediaPane.add(mediaPlayer, BorderLayout.CENTER);

//      NOTE: Following is the condition for refreshing 
//      Refresh entire screen only when the speaker change or during initial load
//      Refresh the media player only if it is a word change
        if (!(this.currentSpeakerID.equals(speakerID)) || onLoad) {
            this.currentSpeakerID = speakerID;
            MainFrame.refresh();
        }
        
        else {
            MainFrame.refresh(mediaPane.getX(), mediaPane.getY(), mediaPane.getWidth(), mediaPane.getHeight());
        }

//      Refresh the evaluationPanel to update to the latest information
        refreshEvaluationPanel();
//      Loop the audio on start
        mediaPlayer.startPlayingOnStart();
    }
    
//  NOTE: DUPLICATE METHODS IN MAINFRAME
    private void releaseMediaPanelResource(){
//            System.out.println("[EvaluationPanel::ReleaseResource] " + component);
        for (Component component : mediaPane.getComponents()) {
            if (component instanceof JPanel) {
                JPanel panelComp = (JPanel) component;
                Thread releasePanelThread = new Thread(new ReleasePanelResourceRunnable(panelComp));
                releasePanelThread.start();
//                oldPanelCollection.addPanel(panelComp);
            }
            component = null;
//            System.out.println("[EvaluationPanel::ReleaseResource] " + component);
        }
}
    
    private void refreshEvaluationPanel() {

        Transcription newTranscript = evalMan.getLastSeenTranscription();
        Word newWord = evalMan.getWordByID(speakerID, newTranscript.getWordID());

        int utteranceNum = (currentTranscriptionOrder + 1);
        String utteranceString = String.format(ISystem.AUDIO_UTTERANCE_FORMAT_STRING, utteranceNum);
        lblTitle.setText("Utterance " + utteranceString);

//      System.out.println("[EvaluationPanel] Fluency: " + newTranscript.getEvaluatedFluencyScore());

        setSelectedButton(newTranscript.getEvaluatedFluencyScore());
        setNextButtonMode();
        
        refreshHanPinPane();
              
        hideFlagPane();
        hideCommentPane();
    }

    private void setSelectedButton(int fluencyScore) {
        btnFluencyGroup.clearSelection();
        switch (fluencyScore) {
            case IFluencyScore.NATIVE_SCORE:
                btnNativeFluency.setSelected(true);
                break;
            case IFluencyScore.GOOD_SCORE:
                btnGoodFluency.setSelected(true);
                break;
            case IFluencyScore.AVERAGE_SCORE:
                btnAverageFluency.setSelected(true);
                break;
            case IFluencyScore.BAD_SCORE:
                btnBadFluency.setSelected(true);
                break;
            default: 
                setNextButtonMode();
                break;
        }
    }

    private void setNextButtonMode() {
        if (btnFluencyGroup.getSelection() == null) {
            btnNext.setEnabled(false);
        }
        
        else {
            if (btnNext.isEnabled() == false) {
                sysPro = new SystemProgress();
                sysPro.saveEvaluatorProgress();
                btnNext.setEnabled(true);
                sysPro = null;
            }
        }
    }

    private void refreshHanPinPane(){
        originalHanPinPane.removeAll();
        transcriptHanPinPane.removeAll();
        
        Transcription transcript = evalMan.getLastSeenTranscription();
        Word word = evalMan.getWordByID(speakerID, wordID);
        
        String transcriptHanPin = transcript.getHanyuPinyin();
        String originalHanPin = word.getHanyuPinyin();
        
        ArrayList<JLabel> originalHanPinLabel = evalMan.generateColoredDifferenceDisplayString(originalHanPin, transcriptHanPin);
        ArrayList<JLabel> transcriptHanPinLabel = evalMan.generateColoredDifferenceDisplayString(transcriptHanPin, originalHanPin);
    
        for (JLabel stringLabel : originalHanPinLabel){
            originalHanPinPane.add(stringLabel);
        }

        for (JLabel stringLabel : transcriptHanPinLabel){
            transcriptHanPinPane.add(stringLabel);
        }
    }

    public String getRecentComment_FlagString(int flagType){
        String recentComment_FlagString= "";
        
        Transcription transcript = evalMan.getLastSeenTranscription();
        recentComment_FlagString = evalMan.generateRecentComment_FlagString(transcript, flagType);
        
        return recentComment_FlagString;
    }
    
           
    private void hideFlagPane() {
        btnCancelTranscript.setEnabled(false);
        btnCancelTranscript.setVisible(false);

        String lastFlagString = getRecentComment_FlagString(Comment.TRANSCRIPTION_FLAG);
        tfFlagTranscript.setText(lastFlagString);
        tfFlagTranscript.setEditable(false);
        tfFlagTranscript.setEnabled(false);

        btnFlagSubmit.setText(FLAG_STRING);
    }

    private void hideCommentPane() {
        btnCancelComment.setEnabled(false);
        btnCancelComment.setVisible(false);

        String lastCommentString = getRecentComment_FlagString(Comment.COMMENT);
        commentArea.setText(lastCommentString);
        commentArea.setEditable(false);
        commentArea.setEnabled(false);

        btnCommentSubmit.setText(COMMENT_STRING);
    }

    private void showFlagPane() {
        btnCancelTranscript.setEnabled(true);
        btnCancelTranscript.setVisible(true);

        tfFlagTranscript.setEditable(true);
        tfFlagTranscript.setEnabled(true);
        tfFlagTranscript.requestFocusInWindow();
        
        btnFlagSubmit.setText(SUBMIT_STRING);
    }

    private void showCommentPane() {
        btnCancelComment.setEnabled(true);
        btnCancelComment.setVisible(true);

        commentArea.setEditable(true);
        commentArea.setEnabled(true);
        commentArea.requestFocusInWindow();

        btnCommentSubmit.setText(SUBMIT_STRING);
    }
    
    
    private void makeShortCut() {
        btnPrevShortCut();
        btnNextShortCut();
        btnHomeShortCut();
        radioButtonShortCut();
        btnFlagShortCut();
        btnCommentShortCut();
    }
    
    private void btnPrevShortCut(){
        Action prevButton = new AbstractAction("PrevButton"){

            @Override
            public void actionPerformed(ActionEvent e) {
                JButton source = (JButton) e.getSource();
                nextPrevTranscript(source);
            }
        };
        
        InputMap inputMap = btnPrev.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
       
        ActionMap actionMap = btnPrev.getActionMap();
        inputMap.put(key, "PreviousUtterance");
        actionMap.put("PreviousUtterance", prevButton);

        btnPrev.setMnemonic(KeyEvent.VK_LEFT);
    }
    
    private void btnNextShortCut(){
        Action nextButton = new AbstractAction("NextButton") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JButton source = (JButton) e.getSource();
                nextPrevTranscript(source);
//                System.out.println("NextButton Shortcut pressed!");
//                mediaPlayer.stopPlaying();
//                evalMan.increaseTranscriptOrderIndex();
//                currentTranscriptionOrder = evalMan.getCurrentTranscriptOrderIndex();
//                utteranceID = evalMan.getUtteranceID(currentTranscriptionOrder);
//                evalMan.setEvaluatorLastSeen(speakerID, utteranceID);
//                Transcription newTranscript = evalMan.getLastSeenTranscription();
//                refreshMediaPanel(speakerID, newTranscript.getWordID());
            }
        };

        InputMap inputMap = btnNext.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        KeyStroke keyRightArrow = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
        KeyStroke keyBackSlash = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SLASH, 0);
       
        ActionMap actionMap = btnNext.getActionMap();
        inputMap.put(keyRightArrow, "NextUtterance");
        inputMap.put(keyBackSlash, "NextUtterance");
        actionMap.put("NextUtterance", nextButton);

        btnNext.setMnemonic(KeyEvent.VK_RIGHT);
    }
    
    private void btnHomeShortCut(){
        Action homeButton = new AbstractAction("HomeButton") {
            @Override
            public void actionPerformed(ActionEvent e) {
                returnHome();
            }
        };

        InputMap inputMap = btnHome.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0);

        ActionMap actionMap = btnHome.getActionMap();
        inputMap.put(key, "Home");
        actionMap.put("Home", homeButton);

        btnHome.setMnemonic(KeyEvent.VK_H);
    }
    
    private void radioButtonShortCut(){
        Action radioButton = new AbstractAction("RadioButton") {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkSelectedRadioButton(e);
            }
        };

        InputMap inputMapBad = btnBadFluency.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
//        KeyStroke keyBad = KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0);
        KeyStroke keyBad = KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0);
        ActionMap actionMapBad = btnBadFluency.getActionMap();
        inputMapBad.put(keyBad, "Bad");
        actionMapBad.put("Bad", radioButton);

        btnBadFluency.setMnemonic(KeyEvent.VK_1);
        
        InputMap inputMapAverage = btnAverageFluency.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
//        KeyStroke keyAverage = KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0);
        KeyStroke keyAverage = KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0);
        ActionMap actionMapAverage = btnAverageFluency.getActionMap();
        inputMapAverage.put(keyAverage, "Average");
        actionMapAverage.put("Average", radioButton);
        
        btnAverageFluency.setMnemonic(KeyEvent.VK_2);
        
        InputMap inputMapGood = btnGoodFluency.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
//        KeyStroke keyGood = KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0);
        KeyStroke keyGood = KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, 0);
        ActionMap actionMapGood = btnGoodFluency.getActionMap();
        inputMapGood.put(keyGood, "Good");
        actionMapGood.put("Good", radioButton);
        
        btnGoodFluency.setMnemonic(KeyEvent.VK_3);
        
        InputMap inputMapNative = btnNativeFluency.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
//        KeyStroke keyNative = KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0);
        KeyStroke keyNative = KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, 0);
        ActionMap actionMapNative = btnNativeFluency.getActionMap();
        inputMapNative.put(keyNative, "Native");
        actionMapNative.put("Native", radioButton);
        
        btnNativeFluency.setMnemonic(KeyEvent.VK_4);
    }
                
    private void checkSelectedRadioButton(ActionEvent evt) {
//      NOTE: The .setSelected in this method won't calls the actionEvent handler - radioButtonSelected
//      Therefore, has to call it explicitly at the end
        
        JRadioButton source = (JRadioButton) evt.getSource();
        String actionCommandString = source.getActionCommand();
//      System.out.println("[EvaluationPanel::checkRadioBtn] " + actionCommandString);
        
        switch (actionCommandString) {
            case IFluencyScore.FLUENCY_NATIVE:
                btnNativeFluency.setSelected(true);
                break;
                
            case IFluencyScore.FLUENCY_GOOD:
                btnGoodFluency.setSelected(true);
                break;
                
            case IFluencyScore.FLUENCY_AVERAGE:
                btnAverageFluency.setSelected(true);
                break;
                
            case IFluencyScore.FLUENCY_BAD:
                btnBadFluency.setSelected(true);
                break;
        }
        radioButtonSelected(evt);
    }
    
    private void btnFlagShortCut(){
        Action homeButton = new AbstractAction("FlagButton") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JButton source = (JButton)e.getSource();
                String buttonText = source.getText();
                if (buttonText.equals(SUBMIT_STRING)) {
                    makeFlagComment(source);
                } 
                
                else {
                    setDisplayPane(source);
                }
            }
        };

        InputMap inputMap = btnFlagSubmit.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);

        ActionMap actionMap = btnFlagSubmit.getActionMap();
        inputMap.put(key, "Flag");
        actionMap.put("Flag", homeButton);

        btnFlagSubmit.setMnemonic(KeyEvent.VK_F);
    }
    
    private void btnCommentShortCut(){
        Action commentButton = new AbstractAction("CommentButton") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JButton source = (JButton) e.getSource();
                String buttonText = source.getText();
                if (buttonText.equals(SUBMIT_STRING)) {
                    makeFlagComment(source);
                } else {
                    setDisplayPane(source);
                }
            }
        };
        InputMap inputMap = btnCommentSubmit.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_QUOTE, 0);

        ActionMap actionMap = btnCommentSubmit.getActionMap();
        inputMap.put(key, "Comment");
        actionMap.put("Comment", commentButton);

        btnCommentSubmit.setMnemonic(KeyEvent.VK_C);
    }
    
    /**
     * Events Handler
     */
    private void btnPrevNextTranscriptChanged(MouseEvent evt) {
        JButton source = (JButton) evt.getSource();
        if (source.isEnabled()) {
            nextPrevTranscript(source);
        }
    }

    private void nextPrevTranscript(JButton btnSource) {
        int prevTranscriptionOrder = evalMan.getCurrentTranscriptOrderIndex();
        mediaPlayer.stopPlaying();
        if (btnSource == btnNext) {
            evalMan.increaseTranscriptOrderIndex();
        } 
        
        else if (btnSource == btnPrev) {
            evalMan.decreaseTranscriptOrderIndex();
        }

        this.currentTranscriptionOrder = evalMan.getCurrentTranscriptOrderIndex();
        if (btnSource == btnPrev && prevTranscriptionOrder == currentTranscriptionOrder) {
            displayStartDialog();
        } 
        
        else if (btnSource == btnNext && prevTranscriptionOrder == currentTranscriptionOrder) {
            displayEndDialog();
        } 
        
        else {
            generateNewTranscript();
        }
    }
    
    private void displayStartDialog(){
        JOptionPane.showMessageDialog(null, "You are at the start...", "Let's Go!", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void displayEndDialog(){
        JOptionPane.showMessageDialog(null, "Congrats!!!...You are done!!", "Thank You!", JOptionPane.INFORMATION_MESSAGE);
        HomePanel homePane = new HomePanel();
        MainFrame.changePanel(homePane);
    }
    
    private void generateNewTranscript(){
        this.utteranceID = evalMan.getUtteranceID(currentTranscriptionOrder);
        evalMan.setEvaluatorLastSeen(speakerID, utteranceID);
        Transcription newTranscript = evalMan.getLastSeenTranscription();
        refreshMediaPanel(speakerID, newTranscript.getWordID());
    }
    
    
    private void radioButtonSelected(ActionEvent evt) {
//      System.out.println("[EvaluationPanel] RadioButtonCount: " + btnFluencyGroup.getButtonCount());
//      System.out.println("[EvaluationPanel] " + btnFluencyGroup.getSelection().toString());
//      System.out.println("[EvaluationPanel] Selected Radio Button: " + evt.getActionCommand());
        JRadioButton source = (JRadioButton) evt.getSource();
        String actionCommandString = source.getActionCommand();
        Transcription transcriptDetails = evalMan.getLastSeenTranscription();
//      System.out.println("[EvaluationPanel] Transcription: " + transcriptDetails.getAudioFileName());
//      System.out.println("[EvaluationPanel] ActionCommandString: " + source.getActionCommand());
        
        int fluencyScore;
        switch (actionCommandString) {
            case IFluencyScore.FLUENCY_NATIVE:
                fluencyScore = IFluencyScore.NATIVE_SCORE;
                break;
            case IFluencyScore.FLUENCY_GOOD:
                fluencyScore = IFluencyScore.GOOD_SCORE;
                break;
            case IFluencyScore.FLUENCY_AVERAGE:
                fluencyScore = IFluencyScore.AVERAGE_SCORE;
                break;
            case IFluencyScore.FLUENCY_BAD:
                fluencyScore = IFluencyScore.BAD_SCORE;
                break;
            default:
                fluencyScore = IFluencyScore.NOT_DONE_SCORE;
                break;
        }

        transcriptDetails.setEvaluatedFluencyScore(fluencyScore);
        setNextButtonMode();
    }
    
    
    private void createFlagComment(MouseEvent evt) {
        JButton source = (JButton) evt.getSource();
        String buttonText = source.getText();
        if (buttonText.equals(SUBMIT_STRING)) {
            makeFlagComment(source);
        } 
        
        else {
            setDisplayPane(source);
        }
    }

    private void setDisplayPane(JButton btnSource) {
        String buttonText = btnSource.getText();
        switch (buttonText) {
            case COMMENT_STRING:
                showCommentPane();
                break;
            case FLAG_STRING:
                showFlagPane();
                break;
            case CANCEL_STRING:
                if (btnSource == btnCancelComment) {
                    hideCommentPane();
                } 
                
                else {
                    hideFlagPane();
                }
                break;
        }
    }

    private void makeFlagComment(JButton btnSource){
        String message = "";
        boolean isFlag = false;
        
        if (btnSource == btnFlagSubmit) {
            message = tfFlagTranscript.getText().trim();
            isFlag = true;
            addFlagComment(message, isFlag);
            hideFlagPane();
        } 
        
        else if (btnSource == btnCommentSubmit) {
            message = commentArea.getText().trim();
            isFlag = false;
            addFlagComment(message, isFlag);
            hideCommentPane();
        }
    }
    
    private void addFlagComment(String message, boolean isFlag){
        if (message.length() > 0) {
            evalMan.addComment(speakerID, wordID, message, isFlag);
        }
    }
    
    
    private void returnHome() {
        HomePanel homePane = new HomePanel();
        MainFrame.changePanel(homePane);
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mediaPane = new javax.swing.JPanel();
        navigatePane = new javax.swing.JPanel();
        btnPrev = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        lblTitle = new javax.swing.JLabel();
        btnHome = new javax.swing.JButton();
        fluencyPane = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        btnBadFluency = new javax.swing.JRadioButton();
        btnAverageFluency = new javax.swing.JRadioButton();
        btnGoodFluency = new javax.swing.JRadioButton();
        btnNativeFluency = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        originalHanPinPane = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        transcriptHanPinPane = new javax.swing.JPanel();
        tfFlagTranscript = new javax.swing.JTextField();
        btnFlagSubmit = new javax.swing.JButton();
        btnCancelTranscript = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        commentArea = new javax.swing.JTextArea();
        btnCommentSubmit = new javax.swing.JButton();
        btnCancelComment = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(1064, 720));

        mediaPane.setPreferredSize(new java.awt.Dimension(745, 425));

        javax.swing.GroupLayout mediaPaneLayout = new javax.swing.GroupLayout(mediaPane);
        mediaPane.setLayout(mediaPaneLayout);
        mediaPaneLayout.setHorizontalGroup(
            mediaPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 750, Short.MAX_VALUE)
        );
        mediaPaneLayout.setVerticalGroup(
            mediaPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 425, Short.MAX_VALUE)
        );

        btnPrev.setText("<<");
        btnPrev.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnPrevMouseClicked(evt);
            }
        });

        btnNext.setText(">>");
        btnNext.setPreferredSize(new java.awt.Dimension(85, 80));
        btnNext.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnNextMouseClicked(evt);
            }
        });
        btnNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextActionPerformed(evt);
            }
        });

        lblTitle.setFont(new java.awt.Font("Tahoma", 0, 48)); // NOI18N
        lblTitle.setText("Title");

        btnHome.setText("Home");
        btnHome.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnHome.setPreferredSize(new java.awt.Dimension(85, 80));
        btnHome.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnHomeMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout navigatePaneLayout = new javax.swing.GroupLayout(navigatePane);
        navigatePane.setLayout(navigatePaneLayout);
        navigatePaneLayout.setHorizontalGroup(
            navigatePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(navigatePaneLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnPrev, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 224, Short.MAX_VALUE)
                .addComponent(lblTitle)
                .addGap(206, 206, 206)
                .addComponent(btnNext, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnHome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(99, 99, 99))
        );

        navigatePaneLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnNext, btnPrev});

        navigatePaneLayout.setVerticalGroup(
            navigatePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(navigatePaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(navigatePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(btnHome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNext, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPrev)
                    .addComponent(lblTitle))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        navigatePaneLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnNext, btnPrev});

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel1.setText("Fluency");

        btnBadFluency.setText("Bad");
        btnBadFluency.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBadFluencyActionPerformed(evt);
            }
        });

        btnAverageFluency.setText("Average");
        btnAverageFluency.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAverageFluencyActionPerformed(evt);
            }
        });

        btnGoodFluency.setText("Good");
        btnGoodFluency.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGoodFluencyActionPerformed(evt);
            }
        });

        btnNativeFluency.setText("Native");
        btnNativeFluency.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNativeFluencyActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout fluencyPaneLayout = new javax.swing.GroupLayout(fluencyPane);
        fluencyPane.setLayout(fluencyPaneLayout);
        fluencyPaneLayout.setHorizontalGroup(
            fluencyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fluencyPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(fluencyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(fluencyPaneLayout.createSequentialGroup()
                        .addGroup(fluencyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnBadFluency, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnGoodFluency))
                        .addGap(60, 60, 60)
                        .addGroup(fluencyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnAverageFluency, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnNativeFluency)))
                    .addComponent(jLabel1))
                .addGap(31, 31, 31))
        );
        fluencyPaneLayout.setVerticalGroup(
            fluencyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fluencyPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(fluencyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnBadFluency)
                    .addComponent(btnAverageFluency))
                .addGap(27, 27, 27)
                .addGroup(fluencyPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnGoodFluency, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnNativeFluency, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap(29, Short.MAX_VALUE))
        );

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel2.setText("Word Hanyu Pinyin");
        jLabel2.setPreferredSize(new java.awt.Dimension(60, 30));

        originalHanPinPane.setPreferredSize(new java.awt.Dimension(780, 35));

        javax.swing.GroupLayout originalHanPinPaneLayout = new javax.swing.GroupLayout(originalHanPinPane);
        originalHanPinPane.setLayout(originalHanPinPaneLayout);
        originalHanPinPaneLayout.setHorizontalGroup(
            originalHanPinPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 780, Short.MAX_VALUE)
        );
        originalHanPinPaneLayout.setVerticalGroup(
            originalHanPinPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 35, Short.MAX_VALUE)
        );

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel3.setText("Transcript Hanyu Pinyin");
        jLabel3.setPreferredSize(new java.awt.Dimension(60, 30));

        transcriptHanPinPane.setPreferredSize(new java.awt.Dimension(780, 35));

        javax.swing.GroupLayout transcriptHanPinPaneLayout = new javax.swing.GroupLayout(transcriptHanPinPane);
        transcriptHanPinPane.setLayout(transcriptHanPinPaneLayout);
        transcriptHanPinPaneLayout.setHorizontalGroup(
            transcriptHanPinPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 780, Short.MAX_VALUE)
        );
        transcriptHanPinPaneLayout.setVerticalGroup(
            transcriptHanPinPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 35, Short.MAX_VALUE)
        );

        tfFlagTranscript.setText("[FlagTranscript]");
        tfFlagTranscript.setPreferredSize(new java.awt.Dimension(780, 35));

        btnFlagSubmit.setText("Flag");
        btnFlagSubmit.setPreferredSize(new java.awt.Dimension(85, 35));
        btnFlagSubmit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnFlagSubmitMouseClicked(evt);
            }
        });

        btnCancelTranscript.setText("Cancel");
        btnCancelTranscript.setPreferredSize(new java.awt.Dimension(85, 35));
        btnCancelTranscript.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnCancelTranscriptMouseClicked(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel4.setText("Remarks");

        commentArea.setColumns(20);
        commentArea.setLineWrap(true);
        commentArea.setRows(5);
        jScrollPane1.setViewportView(commentArea);

        btnCommentSubmit.setText("Comment");
        btnCommentSubmit.setPreferredSize(new java.awt.Dimension(75, 35));
        btnCommentSubmit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnCommentSubmitMouseClicked(evt);
            }
        });

        btnCancelComment.setText("Cancel");
        btnCancelComment.setPreferredSize(new java.awt.Dimension(85, 35));
        btnCancelComment.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnCancelCommentMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(btnCommentSubmit, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancelComment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCommentSubmit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancelComment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(29, 29, 29)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(originalHanPinPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tfFlagTranscript, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(transcriptHanPinPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnFlagSubmit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnCancelTranscript, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(mediaPane, javax.swing.GroupLayout.PREFERRED_SIZE, 750, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fluencyPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(54, 54, 54))))
            .addGroup(layout.createSequentialGroup()
                .addComponent(navigatePane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(navigatePane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(mediaPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(fluencyPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(originalHanPinPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(transcriptHanPinPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFlagTranscript, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGap(41, 41, 41)
                            .addComponent(btnFlagSubmit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(btnCancelTranscript, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(64, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnHomeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnHomeMouseClicked
        returnHome();
    }//GEN-LAST:event_btnHomeMouseClicked

    private void btnNextMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnNextMouseClicked
        btnPrevNextTranscriptChanged(evt);
    }//GEN-LAST:event_btnNextMouseClicked

    private void btnPrevMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnPrevMouseClicked
        btnPrevNextTranscriptChanged(evt);
    }//GEN-LAST:event_btnPrevMouseClicked

    private void btnGoodFluencyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGoodFluencyActionPerformed
        radioButtonSelected(evt);
    }//GEN-LAST:event_btnGoodFluencyActionPerformed

    private void btnAverageFluencyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAverageFluencyActionPerformed
        radioButtonSelected(evt);
    }//GEN-LAST:event_btnAverageFluencyActionPerformed

    private void btnNativeFluencyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNativeFluencyActionPerformed
        radioButtonSelected(evt);
    }//GEN-LAST:event_btnNativeFluencyActionPerformed

    private void btnBadFluencyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBadFluencyActionPerformed
        radioButtonSelected(evt);
    }//GEN-LAST:event_btnBadFluencyActionPerformed

    private void btnFlagSubmitMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnFlagSubmitMouseClicked
        createFlagComment(evt);
    }//GEN-LAST:event_btnFlagSubmitMouseClicked

    private void btnCancelTranscriptMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCancelTranscriptMouseClicked
        createFlagComment(evt);
    }//GEN-LAST:event_btnCancelTranscriptMouseClicked

    private void btnCommentSubmitMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCommentSubmitMouseClicked
        createFlagComment(evt);
    }//GEN-LAST:event_btnCommentSubmitMouseClicked

    private void btnCancelCommentMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCancelCommentMouseClicked
        createFlagComment(evt);
    }//GEN-LAST:event_btnCancelCommentMouseClicked

    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnNextActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton btnAverageFluency;
    private javax.swing.JRadioButton btnBadFluency;
    private javax.swing.JButton btnCancelComment;
    private javax.swing.JButton btnCancelTranscript;
    private javax.swing.JButton btnCommentSubmit;
    private javax.swing.JButton btnFlagSubmit;
    private javax.swing.JRadioButton btnGoodFluency;
    private javax.swing.JButton btnHome;
    private javax.swing.JRadioButton btnNativeFluency;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPrev;
    private javax.swing.JTextArea commentArea;
    private javax.swing.JPanel fluencyPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel mediaPane;
    private javax.swing.JPanel navigatePane;
    private javax.swing.JPanel originalHanPinPane;
    private javax.swing.JTextField tfFlagTranscript;
    private javax.swing.JPanel transcriptHanPinPane;
    // End of variables declaration//GEN-END:variables
}

    
//    private void commentAreaShortCut(){
//        Action taComment = new AbstractAction("CommentArea") {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                commentArea.requestFocusInWindow();
//            }
//        };
//
//        InputMap inputMap = commentArea.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
//        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.SHIFT_DOWN_MASK);
//
//        ActionMap actionMap = commentArea.getActionMap();
//        inputMap.put(key, "CommentArea");
//        actionMap.put("CommentArea", taComment);
//    }

//    private void tFFlagShortCut(){
//        Action tfFlag = new AbstractAction("FlagTextField") {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                tfFlagTranscript.requestFocusInWindow();
//            }
//        };
//
//        InputMap inputMap = tfFlagTranscript.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
//        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.SHIFT_DOWN_MASK);
//
//        ActionMap actionMap = tfFlagTranscript.getActionMap();
//        inputMap.put(key, "FlagTextField");
//        actionMap.put("FlagTextField", tfFlag);
//    }
