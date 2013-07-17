package Perception;

import java.awt.Color;
import java.util.ArrayList;
import javax.swing.JLabel;

public class EvaluationManager {
    private DatabaseManager dbMan;
    private StringUtils stringUtil;
    
    public EvaluationManager() {
        dbMan = new DatabaseManager();
        stringUtil = new StringUtils();
    }

    public ArrayList<String> getSpeakerOrderList() {
        return SystemInit.myEval.getSpeakerOrderList();
    }  
    
    public ArrayList<Integer> getTranscriptionOrderList() {
        return SystemInit.myEval.getTranscriptionOrderList();
    }

    public int getCurrentTranscriptOrderIndex(){
        return SystemInit.myEval.getCurrentTranscriptionOrderIndex();
    }
    
    public void increaseTranscriptOrderIndex(){
        SystemInit.myEval.increaseTranscriptionOrder();
    }
    
    public void decreaseTranscriptOrderIndex(){
        SystemInit.myEval.decreaseTranscriptionOrder();
    }
    
    public int getUtteranceID(int transcriptOrderIndex){
        ArrayList <Integer> transcriptOrderList = SystemInit.myEval.getTranscriptionOrderList();
        int utteranceID = transcriptOrderList.get(transcriptOrderIndex);
        return utteranceID;
    }
    
//  NOTE: Duplicate methods from SearchManager
    public void setEvaluatorLastSeen(String speakerID, int utteranceID) {
//      "0" - no speaker selected; 0 - no word selected
        if (! speakerID.equals("0")) {
            SystemInit.myEval.setLastSeenSpeakerID(speakerID);
            if (utteranceID != 0) {
                SystemInit.myEval.setLastSeenUtterance(utteranceID);
            }
        }
    }
    
    public Transcription getLastSeenTranscription() {
        int utteranceID = SystemInit.myEval.getLastSeenUtterance();
        Transcription transcript = SystemInit.myEval.getTranscriptionByUtteranceID(utteranceID);
        markTranscriptionReviewed(transcript);
        return transcript;
    }
    
    private void markTranscriptionReviewed(Transcription transcript){
        if (transcript.isReviewed() == false){
            transcript.setReviewed(true);
        }
    }
    
    public String[] getSpeakerChineseCharList(String speakerID) {
        return dbMan.getChineseCharList(speakerID);
    }

    public Word getWordByChiChar(String speakerID, String chineseWord) {
        Word word = dbMan.getWordByChineseChar(speakerID, chineseWord);
        return word;
    }

    public Word getWordByID(String speakerID, int wordID){
        return dbMan.getWordByID(speakerID, wordID);
    }
    
    public int getWordIndex(String speakerID, int wordID) {
        int wordIndex = 0;
        ArrayList<Word> wordList = dbMan.getWordList(speakerID);
        for (Word w : wordList) {
            if (w.getWordID() == wordID) {
                break;
            }
            wordIndex++;
        }
        return wordIndex;
    }
    
    public void markSpeakerSeen(String speakerID) {
        Speaker speaker = dbMan.getSpeaker(speakerID);
        if (speaker.isMark() == false){
            speaker.setMark(true);
        }
    }

    public String getFileNameOnly(String name) {
//      From: http://docs.oracle.com/javase/tutorial/java/data/manipstrings.html
//      Alternatively can change to path using Path path = Paths.get(name)
//      Then return path.getName() - which is the last element of the path (ie the fileName)

        char extension = '/';
        int lastExtension = -1;

        lastExtension = name.lastIndexOf(extension);
//      System.out.println("Last / is @ " + lastExtension);
//      System.out.println("Result: " + name.substring(lastExtension + 1));
        return name.substring(lastExtension + 1);
    }
    
    public String generateTranscriptFileURIString(String speakerID, String transcriptFileName) {
//      NOTE: URI FORMAT: file:\C:\... or jar:file:\C:\...

        Speaker speaker = dbMan.getSpeaker(speakerID);
        String speakerFolderURIString = speaker.getSpeakerAudioFolderURIString();
        String transcriptAudioURIString = speakerFolderURIString + transcriptFileName;
        System.out.println("[EvalManager] AudioURIString - " + transcriptFileName + ": " + (transcriptAudioURIString));
        
        return transcriptAudioURIString;
    }

//  FOR EVALUATIONPANEL TO DISPLAY THE DIFFERENCE BETWEEN THE ORIGINAL AND TRANSCRIPT HANPIN
    public ArrayList<JLabel> generateColoredDifferenceDisplayString(String originalString, String comparedString){
        String returnString = stringUtil.replaceHyphensWithSpace(originalString.trim());
        String comparisonString = stringUtil.replaceHyphensWithSpace(comparedString.trim());
//      System.out.println("[EvalMan::DisplayString] ReturnString: " + returnString);
//      System.out.println("[EvalMan::DisplayString] ComparisonString: " + comparisonString);
        ArrayList<JLabel> labelsCollectionList = new ArrayList<>();
        
        if (stringUtil.isSameString(returnString, comparisonString)){
            JLabel segmentLabel = new JLabel();
            segmentLabel.setText(returnString);
            labelsCollectionList.add(segmentLabel);
        }
        
        else{
            labelsCollectionList = constructDisplayLabel(returnString, comparisonString);
        }
//      System.out.println("[EvalMan::DisplayString] ListSize: " + labelsCollectionList.size());
        return labelsCollectionList;
    }
    
    private ArrayList<JLabel> constructDisplayLabel(String originalString, String comparedString){
//      NOTE: THIS METHOD IS CALLED WHEN ORIGINAL STRING IS DIFFERENT FROM COMPAREDSTRING
        String [] originalStringSegment = originalString.split("[ ]+");
        String [] comparedStringSegment = comparedString.split("[ ]+");
        
//      System.out.println("[OriginalSize]" + originalStringSegment.length);
//      System.out.println("[ComparedSize]" + comparedStringSegment.length);
        
        int smallestArraySize = (originalStringSegment.length <= comparedStringSegment.length) ? originalStringSegment.length : comparedStringSegment.length;
        int stringIndex;
        
        ArrayList<JLabel> newStringLabelsCollectionList = new ArrayList<>();
        for (stringIndex = 0; stringIndex < smallestArraySize; stringIndex ++){
            String originalText = originalStringSegment[stringIndex];
            String comparedText = comparedStringSegment[stringIndex];
            
            JLabel segmentLabel = new JLabel();
            segmentLabel.setText(originalText);
            
            if (! stringUtil.isSameString(originalText, comparedText)){
                segmentLabel.setForeground(Color.red);
            }
            newStringLabelsCollectionList.add(segmentLabel);
        }
        
//      OriginalString is longer -> Therefore append the remaining to LIST
        if (originalStringSegment.length > smallestArraySize){
            for (stringIndex = smallestArraySize; stringIndex < originalStringSegment.length; stringIndex++){
                String originalText = originalStringSegment[stringIndex];
                JLabel segmentLabel = new JLabel();
                segmentLabel.setText(originalText);
                segmentLabel.setForeground(Color.red);
                newStringLabelsCollectionList.add(segmentLabel);
            }
        }
        
//      TODO: CHECK TO SEE IF THE LABELLIST GENERATED HAS THE SAME LENGTH AS THE ARRAY FROM THE SPLITTING OF THE STRING
//      System.out.println("[EvalMan::DisplayLabel] ArraySize: " + originalStringSegment.length);
//      System.out.println("[EvalMan::DisplayLabel] ListSize: " + newStringLabelsCollectionList.size());
//      System.out.println("[EvalMan::DisplayLabel] SameSize: " + (originalStringSegment.length == newStringLabelsCollectionList.size()));
        
        return newStringLabelsCollectionList;
    }

//  HANDLE THE CREATE NEW COMMENT AND RETURN MOST RECENT COMMENT
    public void addComment(String speakerID, int wordID, String commentMsg, boolean isFlag) {
//      Logic: New comment is based on the following:
//      [1] The commentMsg must not be null / empty String
//      [2] The commentMsg must not be the same as the lastRecently edited comment
//      [3] The commentMsg must not be the same as the initial Transcript HanyuPinyin (For Flag only)
//      NOTE: ORIGINAL HANPIN MAY HAVE HYPHENS - USE SPACE FORMAT FOR COMPARISON WRITE BACK IN HYPHENS (?)
//      WRITE OUTPUT IN HYPHENS INSTEAD STORE IN SPACE FORMAT
        
        int utteranceID = SystemInit.myEval.getLastSeenUtterance();
        Transcription transcript = getLastSeenTranscription();
        String originalTranscriptHanPin = transcript.getHanyuPinyin();
        String trimCommentMsg = commentMsg.trim();
        Comment comment;

        if (trimCommentMsg.length() > 0) {
            if (isFlag) {
                String lastFlag = generateRecentComment_FlagString(transcript, Comment.TRANSCRIPTION_FLAG);

                lastFlag = stringUtil.replaceHyphensWithSpace(lastFlag);
                trimCommentMsg = stringUtil.replaceHyphensWithSpace(trimCommentMsg);
                originalTranscriptHanPin = stringUtil.replaceHyphensWithSpace(originalTranscriptHanPin);

                boolean isDifferentFromOriginalHanPin = ! (trimCommentMsg.equals(originalTranscriptHanPin));
                boolean isDifferentFromLastFlag = ! (trimCommentMsg.equals(lastFlag));

                if (isDifferentFromOriginalHanPin && isDifferentFromLastFlag) {
                    comment = new Comment(speakerID, wordID, trimCommentMsg, isFlag);
                    transcript.addFlag(comment);
                    System.out.println("[EvalMan] Flag added.");
                }
            } 
            
            else {
                String lastComment = generateRecentComment_FlagString(transcript, Comment.COMMENT);
                
                boolean isDifferentFromLastComment = ! (trimCommentMsg.equals(lastComment));
                
                if (isDifferentFromLastComment) {
                    comment = new Comment(speakerID, wordID, trimCommentMsg, isFlag);
                    transcript.addComment(comment);
                    System.out.println("[EvalMan] Comment added.");
                }
            }

            markTranscriptCommented(speakerID, utteranceID);
        }
    }

    private void markTranscriptCommented(String speakerID, int utteranceID){
       ArrayList<Integer> transcriptionMarkedList = SystemInit.myEval.getTranscriptListWithFlagComment(speakerID);
       if (transcriptionMarkedList == null){
           transcriptionMarkedList = new ArrayList<>();
       }
       
       if (! transcriptionMarkedList.contains(utteranceID)){
            transcriptionMarkedList.add(utteranceID);
            SystemInit.myEval.storeTranscriptWithFlagComment(speakerID, transcriptionMarkedList);
       }
    }

    public String generateRecentComment_FlagString(Transcription transcript, int flagType){
        ArrayList<Comment> containerList = new ArrayList<>();
        String returnString = "";
        
        if (transcript == null) {
            returnString = "";
        }
        
        else {
            switch (flagType) {
                case Comment.TRANSCRIPTION_FLAG:
                    containerList = transcript.getFlagList();
                    String transcriptHanPin = transcript.getHanyuPinyin();
                    returnString = (containerList.isEmpty()) ? 
                                    transcriptHanPin : getRecentComment_FlagString(containerList);
                    break;
                case Comment.COMMENT:
                    containerList = transcript.getCommentList();
                    returnString = (containerList.isEmpty()) ? 
                                    "" : getRecentComment_FlagString(containerList);
                    break;
            }
        }
//      System.out.println("[EvalManager::LastComment] " + returnString);
        return returnString;
        }
    
    private String getRecentComment_FlagString(ArrayList<Comment> commentList){
        Comment lastComment_Flag;
        String lastComment_FlagString = "";
        if (commentList.size() > 0) {
            int lastIndex = commentList.size() - 1;
            lastComment_Flag = commentList.get(lastIndex);
            lastComment_FlagString = lastComment_Flag.getCommentMsg().trim();
        }
        return lastComment_FlagString;
    }
}
