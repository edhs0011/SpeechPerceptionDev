package Perception;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {

//  Static - Only needs 1 instance of Database
    private static Database db;

    public DatabaseManager() {
        if (db == null){
            db = new Database();
        }
    }

    public void assignSpeakerList(ArrayList<Speaker> speakerList) {
        speakerList.trimToSize();
        db.assignSpeakerList(speakerList);
    }

    public ArrayList<Speaker> getSpeakerList() {
        return db.getSpeakerList();
    }
 
//  TODO: Check for NULL on return    
    public Speaker getSpeaker(String speakerID) {
        Speaker speaker = null;
        for (Speaker currentSpeaker : getSpeakerList()) {
            if (currentSpeaker.getSpeakerID().equals(speakerID)) {
                speaker = currentSpeaker;
                break;
            }
        }
        return speaker;
    }
                
//  For inital startup
    public void populateWordList(String speakerID, ArrayList<Word> wordList) {
        wordList.trimToSize();
        db.populateWordMap(speakerID, wordList);
    }
    
//  For loading
    public void assignWordList(HashMap<String, ArrayList<Word>> wordMap) {
        db.assignWordMap(wordMap);
    }

//  For loading and saving
    public HashMap<String, ArrayList<Word>> getWordListMap(){
        return db.getWordMap();
    }
    
    /**
     * Return only the chinese characters of the word belonging to the speaker
     * @param speakerID
     * @return 
     */
    public String[] getChineseCharList(String speakerID){
        ArrayList<Word> wordList = getWordList(speakerID);
        String [] chineseCharList = new String[wordList.size()];
        int count = 0;
        for (Word currentWord : wordList)
        {
            chineseCharList[count++] = currentWord.getChineseChar();
        }
//      System.out.println("[DatabaseMan] # of Chinese Char: " + count);
        return chineseCharList;
    }

//  TODO: Check for NULL on return
    public Word getWordByChineseChar(String speakerID, String chineseChar) {
        Word word = null;
        for (Word currentWord : getWordList(speakerID)) {
            if (currentWord.getChineseChar().equals(chineseChar)) {
                word = currentWord;
                break;
            }
        }
        return word;
    }

//  TODO: Check for NULL on return
    public Word getWordByID(String speakerID, int wordID) {
        Word word = null;
        for (Word currentWord : getWordList(speakerID)) {
            if (currentWord.getWordID() == wordID) {
                word = currentWord;
                break;
            }
        }
        return word;
    }
    
    /**
     * Returns the whole ArrayList of word object belonging to the speaker
     * @param speakerID
     * @return 
     */
    public ArrayList<Word> getWordList(String speakerID) {
        return db.getWordList(speakerID);
    }
}

//  For inital startup - Temporrary Storage...To be clear after assigning to Evaluator
//    public void populateTranscriptionMap(String speakerID, ArrayList<Transcription> transcriptList) {
//        db.populateTranscriptionMap(speakerID, transcriptList);
//    }
//    
////  For loading
//    public void assignTranscriptionMap(HashMap<String, ArrayList<Transcription>> transcriptionMap) {
//        db.assignTranscriptionMap(transcriptionMap);
//    }
//
//    public void clearTranscriptionMap(){
//        HashMap<String, ArrayList<Transcription>> tempTranscriptionMap = db.getTranscriptionMap();
////        System.out.println("[DatabaseManager] Map Size: " + tempTranscriptionMap.size());
//        for (Map.Entry <String, ArrayList<Transcription>> entry : tempTranscriptionMap.entrySet()){
//            String tempSpeakerKey = entry.getKey();
//            ArrayList<Transcription> tempTranscriptList = entry.getValue();
////            System.out.println("[DatabaseManager] Before Clearing..." + tempTranscriptList.size());
//            for (Transcription transcript : tempTranscriptList){
////                System.out.println(transcript.toString());
//                transcript = null;                
////                System.out.println(transcript);
//            }
//            tempTranscriptList.clear();
////            System.out.println("[DatabaseManager] After Clearing..." + tempTranscriptList.size());
//            tempTranscriptionMap.remove(tempSpeakerKey);
//        }
////        System.out.println("[DatabaseManager] Map Size: " + tempTranscriptionMap.size());
//    }
//    
//    
////  For loading and saving
////    public HashMap<String, ArrayList<Transcription>> getTranscriptionListMap(){
////        return db.getTranscriptionMap();
////    }
//    
//    public ArrayList<Transcription> getSpeakerTranscriptionList(String speakerID) {
//        return db.getTranscriptionList(speakerID);
//    }
      
//    public Transcription getTranscript(String speakerID, int word_trans_ID) {
////      NOTE: WordID = TranscriptionID
//        Transcription tObj = null;
//        for (Transcription t : getSpeakerTranscriptionList(speakerID)) {
//            if (t.getTranscriptID() == (word_trans_ID)) {
//                tObj = t;
//                break;
//            }
//        }
//        return tObj;
//    }