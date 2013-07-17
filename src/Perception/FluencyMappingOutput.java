package Perception;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

public class FluencyMappingOutput extends OutputProcessor{

    private String transcriptionMappingFileURIString;
    private ArrayList<Integer> transcriptionOrderList;
    private HashMap<Integer, Transcription> utteranceTranscriptMap;
    
    public FluencyMappingOutput() {
        super();
        String newFileName = stringUtil.replaceFileNameWithID(IOutput.TRANSCRIPTION_MAPPING_FILENAME, evaluatorID , ISystem.REPLACE_STRING_PATTERN);
        transcriptionMappingFileURIString = generateOutPutFileURIString(newFileName);
    }

    @Override
    public void writeOutputToFile() {
        writeUtteranceTranscriptionMapping();
    }
    
    private void writeUtteranceTranscriptionMapping() {
//      Output result format [TranscriptionOrder] - SpeakerID_UtteranceNum_WordID[FIRST|LAST]_ChineseChar
        ArrayList<String> linesToWrite = new ArrayList<>();

        String speakerEntry = "";
        String utteranceEntry = "";
        String fluencyEntry = "";
        String originalSpeakerWordEntry = "";
        String singleFileEntry = "";

        Word word;

        String speakerID, utteranceNumString, wordIDString, fluencyString ,originalString;
        int mapWordID, fluencyScore;

        utteranceTranscriptMap = SystemInit.myEval.getUtteranceTranscriptionMap();
        transcriptionOrderList = SystemInit.myEval.getTranscriptionOrderList();

        HashMap<Integer, String> originalTranscriptMap = generateOriginalTranscriptMap();
        
        speakerID = getEvaluatedSpeakerID();
        speakerEntry = stringUtil.appendString(emptyEntry, speakerID);

        for (int countIndex = 0; countIndex < transcriptionOrderList.size(); countIndex++) {
            utteranceNumString = "Utt_" + String.format(ISystem.AUDIO_UTTERANCE_FORMAT_STRING, countIndex + 1);

            int utteranceID = transcriptionOrderList.get(countIndex);
            Transcription  transcript = utteranceTranscriptMap.get(utteranceID);
            
//          Find index of corresponding utteranceID from transcriptionOrderList  + 1 -> Array starts from 0
            mapWordID = transcript.getWordID();
            wordIDString = String.valueOf(mapWordID);
            
            utteranceEntry = stringUtil.appendString(emptyEntry, utteranceNumString);
            utteranceEntry = stringUtil.appendString(utteranceEntry, wordIDString);
            

            originalString = stringUtil.appendString(emptyEntry, IOutput.MISSING_INFO);
            
            if (originalTranscriptMap.containsKey(mapWordID)){
                String details = originalTranscriptMap.get(mapWordID);
                originalString = stringUtil.appendString(emptyEntry, details);
            }
            
            originalSpeakerWordEntry = stringUtil.appendString(emptyEntry, originalString);
            
            
            fluencyScore = transcript.getEvaluatedFluencyScore();
            fluencyString = generateCorrespondFluencyString(fluencyScore);
            
            fluencyEntry = stringUtil.appendString(emptyEntry, fluencyString);
            fluencyEntry = stringUtil.appendString(fluencyEntry, String.valueOf(fluencyScore).trim());
            

            singleFileEntry = stringUtil.appendString(emptyEntry, speakerEntry);
            singleFileEntry = stringUtil.appendString(singleFileEntry, utteranceEntry);
            singleFileEntry = stringUtil.appendString(singleFileEntry, originalSpeakerWordEntry);
            singleFileEntry = stringUtil.appendString(singleFileEntry, fluencyEntry);

            linesToWrite.add(singleFileEntry);
        }
        fileUtil.writeToFile(transcriptionMappingFileURIString, linesToWrite);
    }
    
    private HashMap<Integer, String> generateOriginalTranscriptMap(){
        HashMap<Integer, String> originalTranscriptMap = new HashMap<>();
        
        String originalTranscriptDetails = "";
        String speakerID = getEvaluatedSpeakerID();
        String speakerTranscriptionRelative = stringUtil.replaceFileNameWithID(ISystem.SPEAKER_TRANSCRIPTION_WORD_RELATIVE_PATH, speakerID, ISystem.REPLACE_STRING_PATTERN);
        URI originalTranscriptURI = fileUtil.convert_FileName_To_URI(ISystem.RESOURCE_FOLDER, speakerTranscriptionRelative);
        
        ArrayList<String> originalTranscriptList = (ArrayList) fileUtil.readFromFile(originalTranscriptURI.toString());

        for (String transcriptDetails : originalTranscriptList){
//            System.out.println(transcriptDetails);
            String [] splitResult = transcriptDetails.split("\t");
            
            int mapWordID = Integer.parseInt(splitResult[0].trim());
            String originalSpeakerID = splitResult[1].trim();
            int originalWordID = Integer.parseInt(splitResult[2].trim());
            String chineseChar = splitResult[3].trim();
            
//            originalTranscriptDetails = stringUtil.appendString(emptyEntry, String.valueOf(mapWordID));
            originalTranscriptDetails = stringUtil.appendString(emptyEntry, originalSpeakerID);
            originalTranscriptDetails = stringUtil.appendString(originalTranscriptDetails, String.valueOf(originalWordID));
            originalTranscriptDetails = stringUtil.appendString(originalTranscriptDetails, chineseChar);
            
//            System.out.println("[MapOutput] " + originalTranscriptDetails);
            
            originalTranscriptMap.put(mapWordID, originalTranscriptDetails);
        }

        return originalTranscriptMap;
    }
    
//  NOTE: DUPLICATE WITH FLAGOUTPUT
    private String generateCorrespondFluencyString(int fluencyScore) {
        String fluencyString;
        switch (fluencyScore) {
            case IFluencyScore.NATIVE_SCORE:
                fluencyString = IFluencyScore.FLUENCY_NATIVE_TOOLTIP_DESCRIPTION;
                break;
            case IFluencyScore.GOOD_SCORE:
                fluencyString = IFluencyScore.FLUENCY_GOOD_TOOLTIP_DESCRIPTION;
                break;
            case IFluencyScore.AVERAGE_SCORE:
                fluencyString = IFluencyScore.FLUENCY_AVERAGE_TOOLTIP_DESCRIPTION;
                break;
            case IFluencyScore.BAD_SCORE:
                fluencyString = IFluencyScore.FLUENCY_BAD_TOOLTIP_DESCRIPTION;
                break;
            default:
                fluencyString = IFluencyScore.FLUENCY_NOT_DONE;
        }

        return fluencyString;
    }
}
