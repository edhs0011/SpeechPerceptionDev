package Perception;

import java.util.ArrayList;
import java.util.HashMap;

public class Database {
    private static ArrayList<Speaker> speakerList;
    private ArrayList<Word> wordList;
    private static HashMap<String, ArrayList<Word>> speakerWordMap;
    
    public Database() {
        speakerList = new ArrayList<>();
        speakerWordMap = new HashMap<>();
    }

    public ArrayList<Speaker> getSpeakerList() {
        return speakerList;
    }
    
    public void assignSpeakerList(ArrayList<Speaker> speakerList) {
        Database.speakerList = speakerList;
    }

    /**
     * For saving / loading - WordList
     * 
     * @return The entire Word list - including the keys SpeakerID and its values Word arrayList
     */
    public HashMap<String, ArrayList<Word>> getWordMap() {
        return speakerWordMap;
    }

    /**
     * For retrieving a particular speaker list of Words
     * 
     * @param speakerID Key to access the HashMap
     * @return Word arrayList of a speaker corresponding to the SpeakerID
     */
    public ArrayList<Word> getWordList(String speakerID) {
        wordList = null;
        if (speakerWordMap.containsKey(speakerID)) {
            wordList = speakerWordMap.get(speakerID);
        }
        return wordList;
    }

/**
 * Intial Startup Database is EMPTY - Fill it up with Words
 * Use with DatabaseInit.LoadFromFile()
 * 
 * @param speakerID - Key
 * @param wordMap - Value
 */
    public void populateWordMap(String speakerID, ArrayList<Word> wordMap) {
        if (Database.speakerWordMap.containsKey(speakerID)) {
            System.out.println("[Database] Key already inside HashTable - continue putting wordList - Will overwrite old one!");
//            Database.speakerWordMap.remove(speakerID);
        } 
        
        Database.speakerWordMap.put(speakerID, wordMap);
    }

    public void assignWordMap(HashMap<String, ArrayList<Word>> wordMap) {
        Database.speakerWordMap = wordMap;
    }
}

//    private ArrayList<Word> wordList;
//    private static HashMap<String, ArrayList<Word>> speakerWordMap = new HashMap<>();

    /**
     * For saving / loading - TranscriptionList
     * 
     * @return The entire Transcription list - including the keys SpeakerID and its values Transcription arrayList
     */
//    public HashMap<String, ArrayList<Transcription>> getTranscriptionMap() {
//        return speakerTranscriptionMap;
//    }
//
//    /**
//     * For retrieving a particular speaker list of Transcriptions
//     * 
//     * @param speakerID Key to access the HashMap
//     * @return Transcription arrayList of a speaker corresponding to the SpeakerID
//     */
//    public ArrayList<Transcription> getTranscriptionList(String speakerID) {
//        transcriptionList = null;
//        
//        if (speakerTranscriptionMap.containsKey(speakerID)) {
//            transcriptionList = speakerTranscriptionMap.get(speakerID);
//        }
//
//        return transcriptionList;
//    }
//    
//    /**
//     * Initial Startup Database is EMPTY - Fill it up with Transcriptions Use
//     * with DatabaseInit.LoadFromFile()
//     *
//     * @param speakerID Key
//     * @param transcriptionMap Value
//     */
//    public void populateTranscriptionMap(String speakerID, ArrayList<Transcription> transcriptionMap) {
////      NOTE: IF GOT DUPLICATE SPEAKERID HOW? WILL RETURN ERROR SINCE KEY IS UNIQUE??
//        if (Database.speakerTranscriptionMap.containsKey(speakerID)) {
//           System.out.println("[Database] Key already inside HashTable - continue putting transcriptList - Will overwrite old one!");
//           Database.speakerTranscriptionMap.remove(speakerID);
//        } 
//        
//         Database.speakerTranscriptionMap.put(speakerID, transcriptionMap);
//    }
//
//    public void assignTranscriptionMap(HashMap<String, ArrayList<Transcription>> transcriptionMap) {
//        Database.speakerTranscriptionMap = transcriptionMap;
//    }