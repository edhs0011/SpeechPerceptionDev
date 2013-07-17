package Perception;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class FlagOutput extends OutputProcessor {

    private String flagFileURIString;

    public FlagOutput() {
        super();
        String newFileName = stringUtil.replaceFileNameWithID(IOutput.FLAG_FILENAME, evaluatorID , ISystem.REPLACE_STRING_PATTERN);
        flagFileURIString = generateOutPutFileURIString(newFileName);
    }

    @Override
    public void writeOutputToFile() {
        writeFlagOutput(flagFileURIString);
    }

    private void writeFlagOutput(String flagFileURIString) {
//      Output result format [Flag] - SpeakerID_WordID[FIRST|LAST]_ChineseChar_OriginalHanPin_TranscriptHanPin_OldFluency_NewFluency_Start_End_FlagMsg
//      NOTE: ONLY GET THE ONE THE EVALUATOR IS DOING ;D
        ArrayList<String> linesToWrite = new ArrayList<>();
        HashMap uttTranscriptionMap;

//      Contains the utteranceNumber - RandomlySorted
        ArrayList<Integer> transcriptionOrderList;
        ArrayList<Comment> flagList;

        Transcription transcript;
        Word word;

        String speakerEntry = "";
        String wordIDEntry = "";
        String wordEntry = "";
        String fluencyEntry = "";
        String audioEntry = "";
        String flagEntry = "";
        String singleFileEntry = "";

        String speakerID, wordIDString, utteranceIDString;
        int wordID;
        
        String chineseChar = "";
        String originalHanyuPinyin = "";
        String transcriptHanyuPinyin = "";
        String databaseFluency = "";
        String evaluatorFluency = "";
        String flagString = "";
        int startFrame,endFrame;
        float frameRate, startTime, endTime;
        String startTimeString = "";
        String endTimeString = "";
        
        int transcriptionFlagCount = 0;
        int countOrderIndex = 0;
        
        boolean hasFlag = false;

        uttTranscriptionMap = SystemInit.myEval.getUtteranceTranscriptionMap();
        transcriptionOrderList = SystemInit.myEval.getTranscriptionOrderList();
        speakerID = getEvaluatedSpeakerID();
        speakerEntry = stringUtil.appendString(emptyEntry, speakerID);

        for (Integer utteranceNumber : transcriptionOrderList) {
            ++ countOrderIndex;
            transcriptionFlagCount = 0;
            transcript = (Transcription) uttTranscriptionMap.get(utteranceNumber);
            wordID = transcript.getWordID();
            utteranceIDString = "Utt_" + String.format(ISystem.AUDIO_UTTERANCE_FORMAT_STRING, countOrderIndex);

            word = dbMan.getWordByID(speakerID, wordID);
            chineseChar = word.getChineseChar();
            originalHanyuPinyin = stringUtil.replaceSpaceWithHyphens(word.getHanyuPinyin().trim());
            transcriptHanyuPinyin = stringUtil.replaceSpaceWithHyphens(transcript.getHanyuPinyin().trim());

            wordEntry = stringUtil.appendString(emptyEntry, chineseChar);
            wordEntry = stringUtil.appendString(wordEntry, originalHanyuPinyin);
            wordEntry = stringUtil.appendString(wordEntry, transcriptHanyuPinyin);

            databaseFluency = transcript.getDatabaseFluencyScore().trim();
            evaluatorFluency = generateCorrespondFluencyString(transcript.getEvaluatedFluencyScore()).trim();

            fluencyEntry = stringUtil.appendString(emptyEntry, databaseFluency);
            fluencyEntry = stringUtil.appendString(fluencyEntry, evaluatorFluency);

            startFrame = transcript.getStartFrame();
            endFrame = transcript.getEndFrame();
            frameRate = transcript.getFrameRate();
            startTime = convertFramesToSeconds(startFrame, frameRate);
            endTime = convertFramesToSeconds(endFrame, frameRate);
//          System.out.println("Start Time: " + startTime);
//          System.out.println("End Time: " + endTime);
            DecimalFormat df = new DecimalFormat("#.##");
            startTimeString = (startTime == 0) ? String.valueOf(0) : df.format(startTime);
            endTimeString = (endTime == -1) ? String.valueOf(-1) : df.format(endTime);
            
            audioEntry = stringUtil.appendString(emptyEntry, startTimeString);
            audioEntry = stringUtil.appendString(audioEntry, endTimeString);

            hasFlag = transcript.isFlagged();
            flagList = transcript.getFlagList();

            for (transcriptionFlagCount = 0; transcriptionFlagCount < flagList.size() + 1; transcriptionFlagCount++) {
                flagEntry = "";
                
                wordIDString = String.valueOf(wordID);
                wordIDString += generateCommentOccurence(transcriptionFlagCount, flagList.size());
                
                wordIDEntry = stringUtil.appendString(emptyEntry, utteranceIDString);
                wordIDEntry = stringUtil.appendString(wordIDEntry, wordIDString);

                if (hasFlag && transcriptionFlagCount > 0) {
                    flagString = flagList.get(transcriptionFlagCount - 1).getCommentMsg();
                    flagEntry = stringUtil.replaceSpaceWithHyphens(flagString);
                }

                singleFileEntry = stringUtil.appendString(emptyEntry, speakerEntry);
                singleFileEntry = stringUtil.appendString(singleFileEntry, wordIDEntry);
                singleFileEntry = stringUtil.appendString(singleFileEntry, wordEntry);
                singleFileEntry = stringUtil.appendString(singleFileEntry, fluencyEntry);
                singleFileEntry = stringUtil.appendString(singleFileEntry, audioEntry);
                singleFileEntry = stringUtil.appendString(singleFileEntry, flagEntry);

//              System.out.println("[OutputProcessor]" + singleFileEntry);
                linesToWrite.add(singleFileEntry);
            }// End Transcript
        }// End Speaker
//        fileUtil.writeToFile(flagFileURIString, linesToWrite);
        fileUtil.writeToFile(flagFileURIString, linesToWrite);
    }
                
    private float convertFramesToSeconds(int frameLength, float framesPerSecond){
        if (framesPerSecond > 0){
            switch (frameLength){
                case 0: return frameLength;
                case -1: return frameLength;
                default: return frameLength / framesPerSecond;  
            }
        }
        return frameLength;
    }
    
//  NOTE: DUPLICATE WITH FLUENCYOUTPUT
    private String generateCorrespondFluencyString(int fluencyScore) {
        String fluencyString;
        switch (fluencyScore) {
            case IFluencyScore.NATIVE_SCORE:
                fluencyString = IFluencyScore.FLUENCY_NATIVE;
                break;
            case IFluencyScore.GOOD_SCORE:
                fluencyString = IFluencyScore.FLUENCY_GOOD;
                break;
            case IFluencyScore.AVERAGE_SCORE:
                fluencyString = IFluencyScore.FLUENCY_AVERAGE;
                break;
            case IFluencyScore.BAD_SCORE:
                fluencyString = IFluencyScore.FLUENCY_BAD;
                break;
            default:
                fluencyString = IFluencyScore.FLUENCY_NOT_DONE;
        }

        return fluencyString;
    }
}
