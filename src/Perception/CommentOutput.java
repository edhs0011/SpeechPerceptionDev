package Perception;

import java.util.ArrayList;
import java.util.HashMap;

public class CommentOutput extends OutputProcessor {

    private String commentFileURIString;
    private ArrayList<Integer> transcriptionOrderList;

    public CommentOutput() {
        super();
        String newFileName = stringUtil.replaceFileNameWithID(IOutput.COMMENT_FILENAME, evaluatorID , ISystem.REPLACE_STRING_PATTERN);
        commentFileURIString = generateOutPutFileURIString(newFileName);
    }

    @Override
    public void writeOutputToFile() {
        writeCommentOutput(commentFileURIString);
    }

    private void writeCommentOutput(String commentFileURIString) {
//      Output result format [Comment] - SpeakerID_WordID[FIRST|LAST]_CommentMsg        
//      NOTE: MAYBE MIGHT JUST WANT TO LOG THOSE THAT HAVE COMMENTS
        ArrayList<String> linesToWrite = new ArrayList<>();

        ArrayList<Integer> commentedUtteranceIDList;
        ArrayList<Comment> commentList;

        String speakerEntry = "";
        String wordEntry = "";
        String commentEntry = "";
        String singleFileEntry = "";

        Transcription transcript;
        Word word;

        String speakerID, utteranceNumString, wordIDString;
        int wordID, utteranceNum;
        String chineseChar;

        boolean hasComment = false;

        HashMap<String, ArrayList<Integer>> commentedTranscriptMap = SystemInit.myEval.getTranscriptWithFlagCommentMap();
        HashMap<Integer, Transcription> utteranceTranscriptMap = SystemInit.myEval.getUtteranceTranscriptionMap();
        transcriptionOrderList = SystemInit.myEval.getTranscriptionOrderList();

        speakerID = getEvaluatedSpeakerID();
        commentedUtteranceIDList = commentedTranscriptMap.get(speakerID);
        speakerEntry = stringUtil.appendString(emptyEntry, speakerID);

        if (commentedUtteranceIDList != null) {
            for (Integer utteranceID : commentedUtteranceIDList) {
                transcript = utteranceTranscriptMap.get(utteranceID);

//              Find index of corresponding utteranceID from transcriptionOrderList  + 1 -> Array starts from 0
                utteranceNum = getTranscriptionOrderListIndex(utteranceID) + 1;
                utteranceNumString = "Utt_" + String.format(ISystem.AUDIO_UTTERANCE_FORMAT_STRING, utteranceNum);
                wordID = transcript.getWordID();
                wordIDString = String.valueOf(wordID);
                word = dbMan.getWordByID(speakerID, wordID);
                chineseChar = word.getChineseChar();

                wordEntry = stringUtil.appendString(emptyEntry, utteranceNumString);
                wordEntry = stringUtil.appendString(wordEntry, wordIDString);
                wordEntry = stringUtil.appendString(wordEntry, chineseChar);

                hasComment = transcript.isCommented();

                if (hasComment) {
                    commentList = transcript.getCommentList();
                    for (Comment comment : commentList) {
                        commentEntry = "Comment: " + comment.getCommentMsg();

                        singleFileEntry = stringUtil.appendString(emptyEntry, speakerEntry);
                        singleFileEntry = stringUtil.appendString(singleFileEntry, wordEntry);
                        singleFileEntry = stringUtil.appendString(singleFileEntry, commentEntry);

                        linesToWrite.add(singleFileEntry);
                    }
                }
            }
        }
        fileUtil.writeToFile(commentFileURIString, linesToWrite);
    }

    private int getTranscriptionOrderListIndex(int utteranceID) {
        int index = 0;
        for (Integer uttID : transcriptionOrderList) {
            if (uttID == utteranceID) {
                break;
            }
            index++;
        }

//      Check if it really the same
//      System.out.println("[CommentOutput] Same: " + (transcriptionOrderList.get(index) == utteranceID));

        return index;
    }
}
