package Perception;

import java.io.Serializable;

public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public static final int ORIGINAL_FLAG = 0;
    public static final int TRANSCRIPTION_FLAG = 1;
    public static final int COMMENT = 2;
    
    private String speakerID;
    private int wordID;
    
    private String commentMsg;
    private boolean isFlag;

    public Comment(String speakerID, int wordID, String commentMsg, boolean isFlag) {
        this.speakerID = speakerID;
        this.wordID = wordID;
        this.commentMsg = commentMsg;
        this.isFlag = isFlag;
    }

    public String getSpeakerID() {
        return speakerID;
    }

    public void setSpeakerID(String speakerID) {
        this.speakerID = speakerID;
    }

    public int getWordID() {
        return wordID;
    }

    public void setWordID(int transID) {
        this.wordID = transID;
    }

    public String getCommentMsg() {
        return commentMsg;
    }

    public void setCommentMsg(String commentMsg) {
        this.commentMsg = commentMsg;
    }

    public boolean isAFlag() {
        return isFlag;
    }

    public void setIsFlag(boolean isFlag) {
        this.isFlag = isFlag;
    }
}
