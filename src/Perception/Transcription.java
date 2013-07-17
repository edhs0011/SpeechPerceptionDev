package Perception;

import java.io.Serializable;
import java.util.ArrayList;

public class Transcription implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private int transcriptID;
    private int wordID;
    
//  The starting/ending of the audible part of the audio
    private int startFrame;
    private int endFrame;
    private float frameRate;
    
    private String audioFileName = null;
    private String transcriptHanyuPinyin = null;
    
//  Only 4 values {BAD(1), AVERAGE(2), GOOD(3), NATIVE(4)} - All others(0) Eg. null, useless [For DatbaseFluencyScore] 
    private int evaluatedFluencyScore;
    private String databaseFluencyScore;
    
    private ArrayList<Comment> flagList = null;
    private ArrayList<Comment> commentList = null;
    
    private boolean reviewed;
    private boolean commented;
    private boolean flagged;

    /**
     *
     * @param transcriptID
     * @param wordID
     * @param audioFileName
     * @param transcriptHanyuPinyin
     * @param databaseFluencyScore
     */
    public Transcription(int wordID, String audioFileName, String transcriptHanyuPinyin, String databaseFluencyScore) {
//      WORDID = TRANSCRIPTID
        this.transcriptID = wordID;
        this.wordID = wordID;
        this.audioFileName = audioFileName;
        this.transcriptHanyuPinyin = transcriptHanyuPinyin;

        startFrame = 0;
        endFrame = -1;  // -1 in Java Sound means the last frame of the audio
        frameRate = 0.0f;

//      SET DEFAULT TO NOT EVALUATED
        evaluatedFluencyScore = IFluencyScore.NOT_DONE_SCORE;
        this.databaseFluencyScore = databaseFluencyScore;
        
        flagList = new ArrayList<>();
        commentList = new ArrayList<>();

        reviewed = false;
        commented = false;
        flagged = false;
    }

    public int getTranscriptID() {
        return transcriptID;
    }

    public void setTranscriptID(int transcriptID) {
        this.transcriptID = transcriptID;
    }

    public int getWordID() {
        return wordID;
    }

    public void setWordID(int wordID) {
        this.wordID = wordID;
    }

    public String getAudioFileName() {
        return audioFileName;
    }

    public void setAudioFileName(String fileName) {
        this.audioFileName = fileName;
    }

    public int getStartFrame() {
        return startFrame;
    }

    public void setStartFrame(int startFrame) {
        this.startFrame = startFrame;
    }

    public int getEndFrame() {
        return endFrame;
    }

    public void setEndFrame(int endFrame) {
        this.endFrame = endFrame;
    }

    public float getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(float frameRate) {
        this.frameRate = frameRate;
    }

    /**
     * Check before setting the Start and End Mark to eliminate undesirable
     * cases such as:
     *
     * <p>(1) Start Mark &gt; End Mark <br>
     * (2) End Mark &lt; Start Mark
     *
     * <p>If above is true, assign the value to the opposite mark - <br>
     * Example: Start Mark &gt; End Mark - setStartFrame(end);
     * setEndFrame(start)
     *
     * <p>Else, assign value to their respective mark - <br>
     * Example: Start Mark &lt; End Mark - setStartFrame(start);
     * setEndFrame(end)
     *
     * @param start Starting Mark of the audio indicated by the Evaluator
     * @param end Ending Mark of the audio indicated by the Evaluator
     */
    public void setMark(int start, int end) {
//      System.out.println("[Transcription] Before calibrate - Start: " + getStartFrame() + "End: " + getEndFrame());
        if (start > end) {
            setEndFrame(start);
            setStartFrame(end);
        } else {
            setStartFrame(start);
            setEndFrame(end);
        }
//     System.out.println("[Transcription] After calibrate - Start: " + getStartFrame() + "End: " + getEndFrame());
    }

    public String getHanyuPinyin() {
        return transcriptHanyuPinyin;
    }

    public void setHanyuPinyin(String transcriptHanyuPinyin) {
        this.transcriptHanyuPinyin = transcriptHanyuPinyin;
    }

    public int getEvaluatedFluencyScore() {
        return evaluatedFluencyScore;
    }

    public void setEvaluatedFluencyScore(int score) {
        this.evaluatedFluencyScore = score;
    }

    public String getDatabaseFluencyScore() {
        return databaseFluencyScore;
    }

    /**
     * ****** For Comment *******
     */
    public ArrayList<Comment> getFlagList() {
        return flagList;
    }

    public void addFlag(Comment c) {
        flagList.add(c);
        if (isFlagged() == false) {
            setFlagged(true);
        }
    }

    public ArrayList<Comment> getCommentList() {
        return commentList;
    }

    public void addComment(Comment c) {
        commentList.add(c);
        if (isCommented() == false) {
            setCommented(true);
        }
    }
    
    public boolean isReviewed() {
        return reviewed;
    }

    /**
     * Set the Transcription as review if it is previously not reviewed (ie
     * review = false) <br>
     * Increase the total number of Transcriptions Evaluator has seen if above
     * is true
     *
     * @param review True if the Evaluator has seen the Transcription, <br>
     * Otherwise default is false
     */
    public void setReviewed(boolean review) {
        this.reviewed = review;
    }

    public boolean isCommented() {
        return commented;
    }

    public void setCommented(boolean commented) {
        this.commented = commented;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }
}