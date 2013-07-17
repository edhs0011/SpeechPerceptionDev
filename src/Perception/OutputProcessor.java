package Perception;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class OutputProcessor {

//    protected static final String FIRST_ENTRY_INDICATOR = "_FIRST";
//    protected static final String LAST_ENTRY_INDICATOR = "_LAST";
//    protected static final String emptyEntry = "";
    protected final String FIRST_ENTRY_INDICATOR = "_FIRST";
    protected final String LAST_ENTRY_INDICATOR = "_LAST";
    protected final String emptyEntry = "";
    
    protected FileUtils fileUtil;
    protected StringUtils stringUtil;
    protected DatabaseManager dbMan;
    protected String evaluatorID = SystemInit.myEval.getEvaluatorID();
    
    protected ArrayList<Speaker> speakerList;
    
    public OutputProcessor() {
        fileUtil = new FileUtils();
        stringUtil = new StringUtils();
        dbMan = new DatabaseManager();
        speakerList = dbMan.getSpeakerList();
    }

    public abstract void writeOutputToFile();
    
    protected String getEvaluatedSpeakerID() {
        int speakerIndex = SystemInit.myEval.getSpeakerIndex() - 1;
        String speakerID = SystemInit.myEval.getSpeakerOrderList().get(speakerIndex);
        return speakerID;
    }
    
    protected String generateCommentOccurence(int currentIndex, int commentListSize) {
        String occurrenceSeq = "";

//      The first occurence is always the original transcription from DataBase
        if (currentIndex == 0 || commentListSize == 0) { // First entry or no comment
            occurrenceSeq = FIRST_ENTRY_INDICATOR;
        } 
        
        else if ((currentIndex == commentListSize)) {
            occurrenceSeq = LAST_ENTRY_INDICATOR;
        }

        return occurrenceSeq;
    }

    protected String generateOutPutFileURIString(String outFileName) {
//      Path path = Paths.get("result/" + outFileName);
        Path path = Paths.get(ISystem.RESULT_FOLDER_FILENAME + "/" + outFileName);
        System.out.println("[OutputProcessor] " + path.toUri().toString());
        return path.toUri().toString();
    }
}
