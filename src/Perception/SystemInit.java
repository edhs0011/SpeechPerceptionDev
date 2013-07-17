package Perception;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import javax.swing.JOptionPane;

public class SystemInit {
//  NOTE: MAYBE CAN TRY WITH ENUM ISYSTEM.SPEAKER_FILE_BINARY(ISYSTEM.SPEAKER)

//    private String[] genericBinFileName = {ISystem.SPEAKER_FILE_BINARY, ISystem.TRANSCRIPTION_FILE_BINARY, ISystem.WORD_FILE_BINARY, ISystem.EVALUATOR_FILE_BINARY};
//    private String[] genericBinFileName = {ISystem.SPEAKER_FILE_BINARY, ISystem.WORD_FILE_BINARY, ISystem.EVALUATOR_FILE_BINARY};
    private DatabaseManager dbMan;
    private FileUtils fileUtil;
    private StringUtils stringUtil;
//    private ArrayList<String> evaluatorIDList = new ArrayList<>();
    private ArrayList<String> evaluatorIDList = null;
    private ArrayList<String> speakerIDList = null;
    private ArrayList<String> speakerTranscriptionDetailsList = null;
//    private ArrayList<String> speakerHanyuPinyinList = null;
    private ArrayList<Word> speakerWordList = null;
    private ArrayList<Transcription> speakerTranscriptList = null;
    
//  Static - Only 1 evaluator in the whole program. Facilitates easy access throughtout the program
    public static Evaluator myEval;
//  Static - OTHERWISE WILL RESULT IN INFINITE LOOP -> SYSTEMINIT calls SYSTEMPROGRESS calls SYSTEMINIT
    private static SystemProgress sysPro;

    public SystemInit() {
        dbMan = new DatabaseManager();
        sysPro = new SystemProgress();
        fileUtil = new FileUtils();
        stringUtil = new StringUtils();
    }

    public void initialise() {
        loadConfig();

        try {
//          String parentURIString = Paths.get("progress").toUri().toString();
            String parentURIString = Paths.get(ISystem.PROGRESS_FOLDER_FILENAME).toUri().toString();
            URI parentFolderURI = new URI(parentURIString);
            System.out.println("[SystemInit] ParentURI: " + parentURIString);

//          IF PROGRESS FOLDER DOES NOT EXIST -> NO SAVE DATA
            if (!fileUtil.folderExist(parentFolderURI)) {
                fileUtil.createFolder(parentFolderURI);
            }

            loadProgress();
            initAutoSave();
//            initPanelClean();
        } catch (NullPointerException nullEx){
            System.out.println("[MainFrame] " + nullEx.getMessage());
        } catch (URISyntaxException uriEX) {
            System.out.println("[MainFrame] " + uriEX.getMessage());
        } catch (IOException ioEx) {
            System.out.println("[MainFrame] " + ioEx.getMessage());
        } 
        
        finally {
            dbMan = null;
            sysPro = null;
            fileUtil = null;
            stringUtil = null;
        }
    }

    private void loadConfig() {
    /**
     * Load the details of the evaluator and speaker text files
     * into their respective list
     */
        URI fileURI;

        fileURI = fileUtil.convert_FileName_To_URI(ISystem.RESOURCE_FOLDER, ISystem.EVALUATOR_FILE_READABLE);
        evaluatorIDList = (ArrayList) fileUtil.readFromFile(fileURI.toString());    //ONLY 1 EVALUATOR @ A TIME
        System.out.println("[SystemInit::LoadConfig] EvaluatorFile: " + fileURI.toString());

        fileURI = fileUtil.convert_FileName_To_URI(ISystem.RESOURCE_FOLDER, ISystem.SPEAKER_FILE_READABLE);
        speakerIDList = (ArrayList) fileUtil.readFromFile(fileURI.toString());      //ONLY 1 SPEAKER @ A TIME
        System.out.println("[SystemInit::LoadConfig] SpeakerFile: " + fileURI.toString());
    }

//  TODO: IF CHECK THAT PROGRESS FILES ARE 0 KB DON'T USE, GO TO NEXT LOAD SEQUENCE
    private void loadProgress() throws URISyntaxException, IOException, NullPointerException {
    /**
     * In order to load program progress, all save files (ie Speaker, Evaluator, Word .dat) 
     * must be inside the Progress folder 
     * Sequence of Load: 
     * 1) MAIN save file 2) BACKUP save file 3) CREATE new save file
     * NOTE: URI/IO THROWS TO INITIALISE
     */
        String evaluatorID = generateEvaluatorID();
        if (evaluatorID != null){
            String[] mainSaveFileName = generateEvaluatorBinaries(evaluatorID, ISystem.SAVE_MAIN);
            URI[] mainSaveFileURI = generateBinaryFileURI(mainSaveFileName);

            if (hasMissingBinaryFile(mainSaveFileURI)) {
                String[] backupSaveFileName = generateEvaluatorBinaries(evaluatorID, ISystem.SAVE_BACKUP);
                URI[] backupSaveFileURI = generateBinaryFileURI(backupSaveFileName);

                if (hasMissingBinaryFile(backupSaveFileURI)) {
                    makeNewBinaryFile(mainSaveFileURI);
                    loadFromFile();
                } else {
                    sysPro.loadProgram(evaluatorID, ISystem.SAVE_BACKUP);
                }
            } else {
                sysPro.loadProgram(evaluatorID, ISystem.SAVE_MAIN);
            }
        }
    }

//  TODO: CHECK FOR RETURN VALUE FOR NULL
    private String generateEvaluatorID() {
    /**
     * Note: EvaluatorIDList is loaded in loadConfig
     * Format of Evaluator text file:
     * [EvaluatorID][tab][SpeakerToDo]
     */
        
        String[] result = new String[2];
        String evaluatorID = null;
        if (evaluatorIDList != null){
            for (String evalDetails : evaluatorIDList) {
                result = evalDetails.split("\\t");
            }
            evaluatorID = result[0].trim();
        }
        System.out.println("[SystemInit] EvaluatorID: " + evaluatorID);
        
        return evaluatorID;
    }

    public String[] generateEvaluatorBinaries(String evaluatorID, int mode) {
    
//      NOTE: GenericFileName : "speaker_[EVALID].dat"
     
        String[] genericBinaryFileName = {ISystem.SPEAKER_FILE_BINARY, ISystem.WORD_FILE_BINARY, ISystem.EVALUATOR_FILE_BINARY};

        if (mode == ISystem.SAVE_BACKUP) {
            genericBinaryFileName[ISystem.SPEAKER] = ISystem.SPEAKER_FILE_BINARY_BACKUP;
            genericBinaryFileName[ISystem.WORD] = ISystem.WORD_FILE_BINARY_BACKUP;
            genericBinaryFileName[ISystem.EVALUATOR] = ISystem.EVALUATOR_FILE_BINARY_BACKUP;
        }

        String[] evaluatorBinFileName = replaceFileNameWithEvaluatorID(genericBinaryFileName, evaluatorID);
        return evaluatorBinFileName;
    }

    private String[] replaceFileNameWithEvaluatorID(String[] genericBinFileName, String evaluatorID) {
        
//      NOTE: Replace pattern : [\w]
 
        String[] evaluatorBinFileName = new String[genericBinFileName.length];
        String newBinFileName;
        int count = 0;
        
        for (String fileName : genericBinFileName) {
            newBinFileName = stringUtil.replaceFileNameWithID(fileName, evaluatorID, ISystem.REPLACE_STRING_PATTERN);
            evaluatorBinFileName[count++] = newBinFileName;
        }
        
        return evaluatorBinFileName;
    }

    private URI[] generateBinaryFileURI(String[] evaluatorBinFileName) {
        
        URI[] evaluatorBinFileURI = new URI[evaluatorBinFileName.length];
        URI binFileURI;
        
        int count = 0;
        for (String binFileName : evaluatorBinFileName) {
            binFileURI = fileUtil.convert_FileName_To_URI(ISystem.PROGRESS_FOLDER, binFileName);
            evaluatorBinFileURI[count++] = binFileURI;
        }

        return evaluatorBinFileURI;
    }

    private boolean hasMissingBinaryFile(URI[] uris) {
        
        boolean hasMissingBinFiles = false;
        for (URI fileURI : uris) {
            if (! fileUtil.fileExist(fileURI)) {
                hasMissingBinFiles = true;
                break;
            }
        }

        return hasMissingBinFiles;    //All files exists
    }

//  IMPROVEMENT: CHECK ONLY THE FILE THAT NEED TO BE CREATED (SAY ONLY A FEW FILES MISSING - IF RECREATE ALL WILL OVERWRITE)
    private void makeNewBinaryFile(URI[] uris) throws URISyntaxException, IOException {
        for (URI fileURI : uris) {
//          System.out.println("[SystemInit::makeBinaryFile] " + fileURI.toString());
            fileUtil.createFile(fileURI);
        }
    }

    private void loadFromFile() {
    /**
     * NOTE: Called only during first run and when save files are missing
     * When this is called, assume no progress at all Populate the Database
     * with the appropriate information (Speaker, Word) Create a new
     * Evaluator instance After loading, save the progress
     */
        createEvaluator();
        addSpeaker();
        assignSpeakerOrderList();
        assignTranscriptionOrderList();
        sysPro.saveProgram(ISystem.SAVE_MAIN);
    }
    
    private void createEvaluator() {
    /**
     * There exist 2 config files :- Evaluator && Speaker text file
     * Speaker : Consist of all the speaker folder name
     * Evaluator : Depending on the speaker.txt, the SELECTEDSPEAKERORDER is corresponding to the line number of the speaker being evaluated
     */
        
        String evaluatorID;
        int selectedSpeakerOrder;
        String[] result = null;

//      Split the String using regex - String are separated by White Spaces [space] [tab]
//      String store in notepad is [EvalID][tab][SelectSpeakerOrder]
//      result[0] - evalID; result[1] - selectedSpeakerOrder
        for (String evalID : evaluatorIDList) {
            result = evalID.split("\\t");
        }

        evaluatorID = result[0].trim();
        selectedSpeakerOrder = Integer.parseInt(result[1].trim());

        myEval = new Evaluator(evaluatorID, selectedSpeakerOrder);
    }
    
//  IMPROVEMENT: JUST MAKE USE OF TRANSCRIPTION.TXT -> EVERY INFORMATION IS INSIDE ALREADY
//  IMPROVEMENT: NO NEED TO LOAD ALL CAN JUST LOAD THE ONE IN THE EVALUATOR.TXT
//  IMPROVEMENT: Using a config.xml, store all the path within, can change without having to recompile
    private void addSpeaker() {
    /**
     * NOTE: Beside creating new Speaker object, also creates the
     * corresponding Transcription and Word objects
     */
        String speakerAudioFolderURIString;
        String speakerID = getSelectedSpeakerID();
        
        speakerAudioFolderURIString = generateSpeakerAudioURIString(speakerID);
        Speaker speaker = new Speaker(speakerID, speakerAudioFolderURIString);

        addWord_Transcription(speaker);
        
        dbMan.getSpeakerList().add(speaker);
    }
    
    private String getSelectedSpeakerID(){
        ArrayList<String> speakerOrderList = SystemInit.myEval.getSpeakerOrderList();
        
        int speakerListSize = speakerOrderList.size();
        int selectedSpeakerIndex = SystemInit.myEval.getSpeakerIndex();
        int speakerOrderIndex = (selectedSpeakerIndex > speakerListSize || selectedSpeakerIndex < 0) ? 0 : (selectedSpeakerIndex - 1);
        
        String speakerID = speakerIDList.get(speakerOrderIndex);
        
        return speakerID;
    }
    
    public String generateSpeakerAudioURIString(String speakerID) {
    String fileName;
    String speakerAudioFolderURIString;

//      fileName = "audio/" + speakerID + "/converted/"; //Location of the Speaker Transcription
    fileName = stringUtil.replaceFileNameWithID(ISystem.SPEAKER_TRANSCRIPTION_AUDIO_RELATIVE_PATH, speakerID, ISystem.REPLACE_STRING_PATTERN);
    System.out.println("[SystemInit::SpeakerAudioURIString] FileName: " + fileName);

//      speakerAudioFolderURIString = (this.getClass().getResource(ISystem.RESOURCE_FOLDER_RELATIVE_PATH + fileName)).toString();
    speakerAudioFolderURIString = fileUtil.convert_FileName_To_URI(ISystem.RESOURCE_FOLDER, fileName).toString();
    System.out.println("[SystemInit::SpeakerAudioURIString] SpeakerAudioFolder: " + speakerAudioFolderURIString);

    return speakerAudioFolderURIString;
}
    
    private void addWord_Transcription(Speaker speaker) {
        
    loadSpeakerTranscription(speaker.getSpeakerID());
        
    speakerWordList = new ArrayList<>();
    speakerTranscriptList = new ArrayList<>();

    int wordID;
    String chineseCharacter, wordHanyuPinyin;   //For Word
    String audioFileName, transcriptHanyuPinyin, databaseFluencyScore; //For Transcription
    Transcription transcript;

//    int wordIndex = 0;
    for (String wordDetails : speakerTranscriptionDetailsList) {
//          Split the String using regex - String are separated by White Spaces [space] [tab]
//          Transcription.txt format:
//          [WordID][tab][SpeakerID][tab][OriginalWordID][tab][ChineseWord][tab][OriginalHanPin][tab][Fluency][tab][TranscriptHanPin][tab][Age][tab][Gender]
//          result[0] - wordID; result[3] - chinese char; result[4] - hanyu pinyin; result[5] - fluency
        String[] result = wordDetails.split("\\s*\\t*\\t");

//          Check split result
//          int count = 0;
//          for (String item : result) {
//              System.out.println("[SystemInit] Result[" +((count ++) % 9) + "]" + item);
//          }

        wordID = Integer.parseInt(result[0].trim());
        chineseCharacter = result[3].trim(); 
        wordHanyuPinyin = result[4].trim();

        Word word = new Word(wordID, chineseCharacter, wordHanyuPinyin);

        audioFileName = generateAudioFileNameFromWordID(wordID);
        databaseFluencyScore = result[5].trim();
        transcriptHanyuPinyin = result[6].trim();
//        transcriptHanyuPinyin = speakerHanyuPinyinList.get(wordIndex++);
        
        transcript = new Transcription(wordID, audioFileName, transcriptHanyuPinyin, databaseFluencyScore);
//            System.out.println("[SystemInit] Transcription " + wordID + ": AudioFileName - " + transcriptAudioFileName
//                    + " Hanyu Pinyin - " + transcriptHanyuPinyin + " Fluency - " + databaseFluencyScore);

        speakerWordList.add(word);
        speakerTranscriptList.add(transcript);
    }

    dbMan.populateWordList(speaker.getSpeakerID(), speakerWordList);
//    dbMan.populateTranscriptionMap(speaker.getSpeakerID(), speakerTranscriptList);
}

    private void loadSpeakerTranscription(String speakerID) {
        String fileName;
        URI fileURI;
        
//      fileName = "audio/" + speakerID + "/saidv.txt"; //Location of the Speaker Transcription HanyuPinyin
//        fileName = stringUtil.replaceFileNameWithID(ISystem.SPEAKER_TRANSCRIPTION_HANYU_PINYIN_RELATIVE_PATH, speakerID, ISystem.REPLACE_STRING_PATTERN); //Location of the Speaker Transcription HanyuPinyin
//        fileURI = fileUtil.convert_FileName_To_URI(ISystem.RESOURCE_FOLDER, fileName);
//        speakerHanyuPinyinList = (ArrayList) fileUtil.readFromFile(fileURI.toString());
//        System.out.println("[SystemInit::addSpdeaker] Saidv File: " + fileURI.toString());

//      fileName = "audio/" + speakerID + "/transcription.txt"; //Location of the Speaker Spoken Words
        fileName = stringUtil.replaceFileNameWithID(ISystem.SPEAKER_TRANSCRIPTION_WORD_RELATIVE_PATH, speakerID, ISystem.REPLACE_STRING_PATTERN); //Location of the Speaker Spoken Words
        fileURI = fileUtil.convert_FileName_To_URI(ISystem.RESOURCE_FOLDER, fileName);
        speakerTranscriptionDetailsList = (ArrayList) fileUtil.readFromFile(fileURI.toString());
        System.out.println("[SystemInit::addSpeaker] Transcription File: " + fileURI.toString());

//        System.out.println("[SystemInit] Saidv: " + speakerHanyuPinyinList.size());
        System.out.println("[SystemInit] Transcription: " + speakerTranscriptionDetailsList.size());
    }
    
    private String generateAudioFileNameFromWordID(int transcriptionID) {
    /**
     * 1-9 : 0001 - 0009.wav
     * 10-99: 0010 - 0099.wav 
     * 100-999: 0100 - 0999.wav
     * 1000-5000: 1000 - 5000.wav
     * NOTE: AUDIO_UTTERANCE_FORMAT_STRING = "%04d
     */

    String transcriptionFileName = null;
    transcriptionFileName = String.format(ISystem.AUDIO_UTTERANCE_FORMAT_STRING, transcriptionID);

//      System.out.println("[SystemInit] " + transcriptionFileName + ".wav");
    return transcriptionFileName + ".wav";
}

    private void assignSpeakerOrderList() {
//      NOTE: FOR EACH EVALUATOR ONLY ONE SPEAKER IS CHOSEN EACH TIME FOR THE TEST(ie. only one folder(eg. week1))AND WILL BE USED

        ArrayList<String> speakerOrderList = new ArrayList<>();
        for (Speaker speaker : dbMan.getSpeakerList()) {
            speakerOrderList.add(speaker.getSpeakerID());
        }

        SystemInit.myEval.assignSpeakerOrderList(speakerOrderList);
        speakerOrderList = null;
    }

    private void assignTranscriptionOrderList() {

//        ArrayList<String> speakerOrderList = SystemInit.myEval.getSpeakerOrderList();
        ArrayList<Integer> transcriptionIDList = new ArrayList<>();

//        ArrayList<Transcription> speakerTranscriptList = new ArrayList<>();

//      IF the speakerIndex is a invalid number (> arrayList size or <= 0) - assign it to the DEFAULT first speaker
//      NOTE: START FROM 0 -> 0 IS THE FIRST SPEAKER (ie. EVALUATIONTEST_1), SPEAKERINDEX STARTS FROM 1
//      CRITICAL: IF SPEAKERORDERLIST.SIZE == 0 -> MEANS NO TRANSCRIPTION/SPEAKER LOADED => WILL HAVE ERROR WHEN LOADED
//        String speakerID;
//        int speakerListSize = speakerOrderList.size();
//        int selectedSpeakerIndex = SystemInit.myEval.getSpeakerIndex();
//        int speakerOrderIndex = (selectedSpeakerIndex > speakerListSize || selectedSpeakerIndex < 0) ? 0 : (selectedSpeakerIndex - 1);

//        if (speakerListSize > 0) {
//            String speakerID = speakerOrderList.get(speakerOrderIndex);
        String speakerID = getSelectedSpeakerID();
        System.out.println("[SystemInit::assignTranscriptionList] Selected_Speaker: " + speakerID);

//        speakerTranscriptList = dbMan.getSpeakerTranscriptionList(speakerID);
        populateEvaluatorUtteranceTranscriptMap(speakerTranscriptList);

        for (Integer utteranceNumb : SystemInit.myEval.getUtteranceTranscriptionMap().keySet()) {
            transcriptionIDList.add(utteranceNumb);
        }

        if (transcriptionIDList.size() > 0 && speakerID.length() > 0) {
            SystemInit.myEval.assignTranscriptionOrderList(transcriptionIDList);
            SystemInit.myEval.setLastSeenSpeakerID(speakerID);
            SystemInit.myEval.setTranscriptionOrderIndex(0);
            SystemInit.myEval.setLastSeenUtterance(transcriptionIDList.get(0));
        }
//        }
//        dbMan.clearTranscriptionMap();
}
    
    private void populateEvaluatorUtteranceTranscriptMap(ArrayList<Transcription> transcriptionList) {
        for (int utteranceCount = 0; utteranceCount < transcriptionList.size(); utteranceCount++) {
            Transcription transcript = transcriptionList.get(utteranceCount);
            int utteranceNumber = utteranceCount + 1;
            SystemInit.myEval.populateUtteranceTranscriptionMap(utteranceNumber, transcript);
            transcript = null;
        }
    }
    
    private void initAutoSave() {
        System.out.println("[Start Time] " + new Date());
        java.util.Timer timer10Mins = new java.util.Timer();
        java.util.Timer timer35Mins = new java.util.Timer();
        
        AutoSave10TimerTask autoSave10mins = new AutoSave10TimerTask();
        AutoSave35TimerTask autoSave35mins = new AutoSave35TimerTask();
        
        long time_10_MinInMilliSec = 10 * 60 * 1000;    // Mins * Sec * Millisec
        long time_35_MinInMilliSec = 31 * 60 * 1000;    // Mins * Sec * Millisec

        timer10Mins.scheduleAtFixedRate(autoSave10mins, (11 * 60 * 1000), time_10_MinInMilliSec);
//        timer10Mins.scheduleAtFixedRate(autoSave10mins, 0, time_10_MinInMilliSec);
        timer35Mins.scheduleAtFixedRate(autoSave35mins, 0, time_35_MinInMilliSec);
    }
    
//    private void initPanelClean() {
//        java.util.Timer timer2Mins = new java.util.Timer();
//        
//        AutoReleasePanelTimerTask autoRelease = new AutoReleasePanelTimerTask();
////        long time_2_MinInMilliSec = 2 * 60 * 1000;    // Mins * Sec * Millisec
//        long time_5000MilliSec = 5000;    // Mins * Sec * Millisec
//
////        timer2Mins.scheduleAtFixedRate(autoRelease, (2 * 60 * 1000), time_2_MinInMilliSec);
//        timer2Mins.scheduleAtFixedRate(autoRelease, 0, time_5000MilliSec);
//    }
}


//  For DUPLICATE AND RANDOM SHUFFLE OF TRANSCRIPTIONLIST
//    private void assignTranscriptionOrderList() {
//
////      TranscriptionOrderRandomGenerator randomGenerator = new TranscriptionOrderRandomGenerator();
//
//        ArrayList<String> speakerOrderList = SystemInit.myEval.getSpeakerOrderList();
//        ArrayList<Integer> transcriptionShuffleList = new ArrayList<>();
//
////      ArrayList<Transcription> duplicateTranscriptList = new ArrayList<>();
//        ArrayList<Transcription> speakerTranscriptList = new ArrayList<>();
//
////      IF the speakerIndex is a invalid number (> arrayList size or <= 0) - assign it to the DEFAULT first speaker
////      NOTE: START FROM 0 -> 0 IS THE FIRST SPEAKER (ie. EVALUATIONTEST_1), SPEAKERINDEX STARTS FROM 1
////      CRITICAL: IF SPEAKERORDERLIST.SIZE == 0 -> MEANS NO TRANSCRIPTION/SPEAKER LOADED => WILL HAVE ERROR WHEN LOADED
//        String speakerID = "";
//        int selectedSpeakerIndex = SystemInit.myEval.getSpeakerIndex();
//        int speakerOrderIndex = (selectedSpeakerIndex > speakerOrderList.size() || selectedSpeakerIndex < 0) ? 0 : (selectedSpeakerIndex - 1);
//
////      System.out.println("[SystemInit::assignTranscriptionList] SpeakerOrderSize: " + speakerOrderList.size());
////      System.out.println("[SystemInit::assignTranscriptionList] SelectedSpeakerIndex: " + selectedSpeakerIndex);
////      System.out.println("[SystemInit::assignTranscriptionList] SpeakerOrderIndex: " + speakerOrderIndex);
////
//        if (speakerOrderList.size() > 0) {
//            speakerID = speakerOrderList.get(speakerOrderIndex);
//            System.out.println("[SystemInit::assignTranscriptionList] Selected_Speaker: " + speakerID);
//            speakerTranscriptList = dbMan.getSpeakerTranscriptionList(speakerID);
//            populateEvaluatorUtteranceTranscriptMap(speakerTranscriptList);
////      duplicateTranscriptList = randomGenerator.makeDuplicateTranscription(speakerTranscriptList, ISystem.NUM_OF_DUPLICATES_TRANSCRIPTION);
////      populateEvaluatorUtteranceTranscriptMap(duplicateTranscriptList);
//
//            for (Integer utteranceNumb : SystemInit.myEval.getUtteranceTranscriptionMap().keySet()) {
//                transcriptionShuffleList.add(utteranceNumb);
//            }
////
////      Collections.sort(transcriptionShuffleList);
////
////      System.out.println("[SystemInit::assignTranscriptionList] Before RANDOMIZING: ");
////      System.out.println("[SystemInit::assignTranscriptionList] Size: " + transcriptionShuffleList.size());
////      for (Integer i : transcriptionShuffleList) {
////          System.out.println("[SystemInit]" + i);
////      }
////
////      randomGenerator.randomize(transcriptionShuffleList);
////
////      System.out.println("[SystemInit::assignTranscriptionList] After RANDOMIZING: ");
////      System.out.println("[SystemInit::assignTranscriptionList] Size: " + transcriptionShuffleList.size());
////      for (Integer i : transcriptionShuffleList) {
////           System.out.println("[SystemInit]" + i);
////      }
//        }
//
//        dbMan.clearTranscriptionMap();
//
//        if (transcriptionShuffleList.size() > 0 && speakerID.length() > 0) {
//            SystemInit.myEval.assignTranscriptionOrderList(transcriptionShuffleList);
//            SystemInit.myEval.setLastSeenSpeakerID(speakerID);
//            SystemInit.myEval.setTranscriptionOrderIndex(0);
//            SystemInit.myEval.setLastSeenUtterance(transcriptionShuffleList.get(0));
//        }
//    }

