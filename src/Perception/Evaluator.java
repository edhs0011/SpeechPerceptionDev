package Perception;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Evaluator implements Serializable {

    private static final long serialVersionUID = 1L;
    private String evaluatorID;
    private int selectedSpeakerIndex; // Indicate the selected folder to evaluate (Note: Start from 1)
    
//  Key SpeakerID -> ArrayList<UtteranceID> which has comment/flag
//  NOTE: IMPROVE THE PERFORMACE - ELSE HAVE TO LOOP THROUGH EVERY OBJECT TO SEE WHICH HAS COMMENT AND FLAG
    private HashMap<String, ArrayList<Integer>> transcriptWithFlagCommentMap = null;
    private HashMap<Integer, Transcription> utteranceTranscriptionMap = null;
    
//  Range: 1 to (transcriptionOrderList.size())
//  NOTE: SPEAKERORDERLIST STORE THE NUMBER_OF_SPEAKER AVAILABLE
//  TRANSCRIPTIONORDERLIST STORE THE CORRESPONDING TRANSCRIPTION OF THE CORRESPONDING SPEAKER
    private ArrayList<Integer> transcriptionOrderList = null;
    private ArrayList<String> speakerOrderList = null;
    
    private String selectedSpeakerID = "week1";

    private int lastSeenUtterance;
    private int currentTranscriptionOrderIndex;

    public Evaluator(String evaluatorID, int speakerIndex) {
        this.evaluatorID = evaluatorID;
        this.selectedSpeakerIndex = speakerIndex;

        speakerOrderList = new ArrayList<>();
        transcriptionOrderList = new ArrayList<>();

        transcriptWithFlagCommentMap = new HashMap<>();
        utteranceTranscriptionMap = new HashMap<>();
    }

    public String getEvaluatorID() {
        return evaluatorID;
    }

    public void setEvaluatorID(String evaluatorID) {
        this.evaluatorID = evaluatorID;
    }

    public int getSpeakerIndex() {
        return selectedSpeakerIndex;
    }

    public void assignSpeakerIndex(int speakerIndex) {
        this.selectedSpeakerIndex = speakerIndex;
    }
        
    public String getLastSeenSpeakerID() {
        return selectedSpeakerID;
    }

    public void setLastSeenSpeakerID(String lastSeenSpeakerID) {
        this.selectedSpeakerID = lastSeenSpeakerID;
    }
    
    public int getLastSeenUtterance() {
        return lastSeenUtterance;
    }

    public void setLastSeenUtterance(int lastSeenUtterance) {
        this.lastSeenUtterance = lastSeenUtterance;
    }
    
    public int getCurrentTranscriptionOrderIndex() {
        return currentTranscriptionOrderIndex;
    }

    public void increaseTranscriptionOrder() {
        int increment = getCurrentTranscriptionOrderIndex() + 1;
        int newTranscriptionOrderIndex = increment >= transcriptionOrderList.size()
                ? (transcriptionOrderList.size() - 1) : increment;
        setTranscriptionOrderIndex(newTranscriptionOrderIndex);
    }

    public void decreaseTranscriptionOrder() {
        int decrement = getCurrentTranscriptionOrderIndex() - 1;
        int newTranscriptionOrderIndex = (decrement < 0) ? 0 : decrement;
        setTranscriptionOrderIndex(newTranscriptionOrderIndex);
    }

    public void setTranscriptionOrderIndex(int transcriptionOrderIndex) {
        this.currentTranscriptionOrderIndex = transcriptionOrderIndex;
    }
    
    public int getUtteranceID(int transcriptionOrderIndex) {
        return transcriptionOrderList.get(transcriptionOrderIndex);
    }

    public HashMap<Integer, Transcription> getUtteranceTranscriptionMap() {
        return utteranceTranscriptionMap;
    }

    public Transcription getTranscriptionByUtteranceID(int utteranceID) {
        return utteranceTranscriptionMap.get(utteranceID);
    }

    public void populateUtteranceTranscriptionMap(Integer utteranceNumber, Transcription transcript) {
        if (utteranceTranscriptionMap.containsKey(utteranceNumber)) {
            System.out.println("[Evaluator] Keys already exist in map...Overwriting");
            utteranceTranscriptionMap.remove(utteranceNumber);
        }
        utteranceTranscriptionMap.put(utteranceNumber, transcript);
    }

    public void assignUtteranceTranscriptionMap(HashMap<Integer, Transcription> utteranceTranscriptionMap) {
        this.utteranceTranscriptionMap = utteranceTranscriptionMap;
    }

    public HashMap<String, ArrayList<Integer>> getTranscriptWithFlagCommentMap() {
        return transcriptWithFlagCommentMap;
    }

    public ArrayList<Integer> getTranscriptListWithFlagComment(String speakerID) {
        ArrayList<Integer> transcriptList = null;
        if (transcriptWithFlagCommentMap.containsKey(speakerID)) {
            transcriptList = transcriptWithFlagCommentMap.get(speakerID);
        }
        return transcriptList;
    }

    public void storeTranscriptWithFlagComment(String speakerID, ArrayList<Integer> transcriptList) {
        transcriptWithFlagCommentMap.put(speakerID, transcriptList);
    }

    public ArrayList<String> getSpeakerOrderList() {
        return speakerOrderList;
    }

    public void assignSpeakerOrderList(ArrayList<String> speakerOrderList) {
        int previousListSize = this.speakerOrderList.size();
        this.speakerOrderList = speakerOrderList;
        if (previousListSize == 0 && speakerOrderList.size() > 0) {
            setLastSeenSpeakerID(this.speakerOrderList.get(0));
        }
    }
    
    public ArrayList<Integer> getTranscriptionOrderList() {
        return transcriptionOrderList;
    }

    public void assignTranscriptionOrderList(ArrayList<Integer> transcriptionOrderList) {
        this.transcriptionOrderList = transcriptionOrderList;
    }
}