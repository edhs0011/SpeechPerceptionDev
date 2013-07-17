package Perception;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class SystemProgress {

    private DatabaseManager dbMan;
    private FileUtils fileUtil;
    
    private static SystemInit sysInit = new SystemInit();
            
    public SystemProgress() {
        dbMan = new DatabaseManager();
        fileUtil = new FileUtils();
    }
    
    /**
     * Load the data from file into objects, Evaluator can continue from
     * previous progress
     *
     * @param fileName Name of the file to load from
     * @param objectType <br>
     * SystemInit.SPEAKER - Load Speaker Objects <br>
     * SystemInit.TRANSCRIPTION - Load Transcription Objects <br>
     * SystemInit.WORD - Load Word Objects <br>
     * SystemInit.EVALUATOR - Load Evaluator Object
     *
     */
    public final void loadObject(String fileName, int objectType) {
        Path osFilePath;
        
        FileInputStream fi;
        ObjectInputStream oi;
        try {
            osFilePath = fileUtil.convert_FileName_To_OSPath(ISystem.PROGRESS_FOLDER, fileName);
            System.out.println("[SystemProgress::loadObject] File Path: " + osFilePath);

            fi = new FileInputStream(osFilePath.toString());
            oi = new ObjectInputStream(fi);

            switch (objectType) {
                case ISystem.SPEAKER:
                    dbMan.assignSpeakerList((ArrayList) oi.readObject());
                    resetSpeakerAudioFolderURIString(dbMan.getSpeakerList());
                    break;
//                case ISystem.TRANSCRIPTION:
//                    dbMan.assignTranscriptionMap((HashMap) oi.readObject());
//                    break;
                case ISystem.WORD:
                    dbMan.assignWordList((HashMap) oi.readObject());
                    break;
                case ISystem.EVALUATOR:
                    SystemInit.myEval = (Evaluator) oi.readObject();
                    break;
                default:
                    System.out.println("[SystemProgress] Unknown OBJECT TYPE!!");
                    break;
            }
            
            oi.close();
            fi.close();
        }catch (IOException ioEX) {
            System.out.println(ioEX.getMessage());
        }catch (ClassNotFoundException classEX) {
            System.out.println(classEX.getMessage());
        } 
    }

    /**
     * Save current objects data to file, Evaluator can load it back on next run
     * (See loadObject)
     *
     * @param fileName Name of the file to save to
     * @param objectType <br>
     * SystemInit.SPEAKER - Save Speaker Objects <br>
     * SystemInit.TRANSCRIPTION - Save Transcription Objects <br>
     * SystemInit.WORD - Save Word Objects <br>
     * SystemInit.EVALUATOR - Save Evaluator Object
     */
    private final void saveObject(String fileName, int objectType) {
//      FILEPATH - COS ALL OUTSIDE JAR (WITHIN JAR MUST USE URI)
        Path osFilePath;
        
        FileOutputStream fo;
        ObjectOutputStream oo;
        
        try {
            osFilePath = fileUtil.convert_FileName_To_OSPath(ISystem.PROGRESS_FOLDER, fileName);

            fo = new FileOutputStream(osFilePath.toString());
            oo = new ObjectOutputStream(fo);

            switch (objectType) {
                case ISystem.SPEAKER:
                    oo.writeObject(dbMan.getSpeakerList());
                    System.out.println("[SystemProgress] Speaker Objects Saved!");
                    break;
                case ISystem.WORD:
                    oo.writeObject(dbMan.getWordListMap());
                    System.out.println("[SystemProgress] Word Objects Saved!");
                    break;
                case ISystem.EVALUATOR:
                    oo.writeObject(SystemInit.myEval);
                    System.out.println("[SystemProgress] Evaluator Object Saved!");
                    break;
                default:
                    System.out.println("[SystemProgress] Unknown OBJECT TYPE!!");
                    break;
            }

            oo.flush();
            oo.close();
            
            fo.flush();
            fo.close();
        } catch (IOException ioEx) {
            System.out.println(ioEx.getMessage());
        }
    }
    
    public void loadProgram(String evaluatorID, int loadMode) {
//	NOTE: @ The time LOADPROGRESS is called - EVALUATOR HASN'T BE CREATED
//	Therefore need to pass the evaluatorID as parameter

        System.out.println("[SystemProgress] LoadMode: " + loadMode);
        String[] binFileName = sysInit.generateEvaluatorBinaries(evaluatorID, loadMode);

        loadObject(binFileName[ISystem.SPEAKER], ISystem.SPEAKER);
//        loadObject(binFileName[ISystem.TRANSCRIPTION], ISystem.TRANSCRIPTION);
        loadObject(binFileName[ISystem.WORD], ISystem.WORD);
        loadObject(binFileName[ISystem.EVALUATOR], ISystem.EVALUATOR);
        System.out.println("[SystemProgress] All objects LOADED!");
        
//      NOTE: THIS MEANS THE MAIN_SAVE FILE HAS ERROR THAT WHY MUST LOAD FROM BACKUP
        if (loadMode == ISystem.SAVE_BACKUP){
            saveProgram(ISystem.SAVE_MAIN);
        }
    }
    
    public void saveProgram(int saveMode) {
//      NOTE: SAVEPROGRAM CAN BE CALLED BY THE SHORTCUT KEYS - CTRL + S
//      IF THE EVALUATOR IS FAST, IT MIGHT CREATE MULTIPLE THREADS TO SAVEPROGRAM IN A SHORT AMOUNT OF TIME
//      THEREFORE TO PREVENT RACE CONDITION (i.e. THE FORMER THREAD OVERWRITING THE LATTER THREAD SAVEPROGRAM - SYNCHRONIZED
//      System.out.println("[SystemProgress] SaveMode: " + saveMode);
        String evaluatorID = SystemInit.myEval.getEvaluatorID();
        String[] binFileName = sysInit.generateEvaluatorBinaries(evaluatorID, saveMode);

        saveObject(binFileName[ISystem.SPEAKER], ISystem.SPEAKER);
//        saveObject(binFileName[ISystem.TRANSCRIPTION], ISystem.TRANSCRIPTION);
        saveObject(binFileName[ISystem.WORD], ISystem.WORD);
        saveObject(binFileName[ISystem.EVALUATOR], ISystem.EVALUATOR);
        System.out.println("[SystemProgress] All objects SAVED!");
    }
     
    public void saveEvaluatorProgress(){
//      NOTE: SAVEEVALUATORPROGRESS IS CALLED WHEN THE EVALUATED MARK THE FLUENCY
//      IF THE EVALUATOR IS FAST, IT MIGHT CREATE 2 THREAD TO SAVEEVALUATORPROGRESS IN A SHORT AMOUNT OF TIME
//      THEREFORE TO PREVENT RACE CONDITION (i.e. THE FORMER THREAD OVERWRITING THE LATTER THREAD SAVEEVALUATORPROGRESS - SYNCHRONIZED
        String evaluatorProgressBinFileName = "";
        String evaluatorID = SystemInit.myEval.getEvaluatorID().trim();
        if (evaluatorID.length() > 0) {
            StringUtils stringUtil = new StringUtils();
            evaluatorProgressBinFileName = stringUtil.replaceFileNameWithID(ISystem.EVALUATOR_FILE_BINARY, evaluatorID, ISystem.REPLACE_STRING_PATTERN);
            saveObject(evaluatorProgressBinFileName, ISystem.EVALUATOR);
        }
    }
    
    public void writeProgressToFile() {
        FlagOutput flagOut = new FlagOutput();
        flagOut.writeOutputToFile();
        
        CommentOutput commentOut = new CommentOutput();
        commentOut.writeOutputToFile();
        
        FluencyMappingOutput fluencyMapOut = new FluencyMappingOutput();
        fluencyMapOut.writeOutputToFile();
    }
    
    private void resetSpeakerAudioFolderURIString(ArrayList<Speaker> speakerList) {
//      FOR LOADING THE DIFFERENT SYSTEM PATH(NOT THE ONE THAT GENERATES THE JAR) WHEN THE JAR GETS DEPLOYED
//      SpeakerHomePath is pre-set, different evaluator will have different home path
//      Check if first speaker is not the same as the generatedPathString -> Set new SpeakerHomePath
        
        for (Speaker speaker : speakerList) {
//          System.out.println("[SystemProgress::setSpeakerHomePath] " + speaker.getSpeakerAudioURIString());
            String speakerID = speaker.getSpeakerID();
            String speakerAudioURIString = sysInit.generateSpeakerAudioURIString(speakerID);
            speaker.setSpeakerAudioFolderURIString(speakerAudioURIString);
//          System.out.println("[SystemProgress::setSpeakerHomePath] " + speaker.getSpeakerAudioURIString());
        }
    }
}
