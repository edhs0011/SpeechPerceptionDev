package Perception;

import java.io.Serializable;

public class Speaker implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String speakerID;
    private String speakerAudioFolderURIString;
    private boolean mark;

    public Speaker(String speakerID, String speakerAudioFolderURIString) {
        this.speakerID = speakerID;
        this.speakerAudioFolderURIString = speakerAudioFolderURIString;
        mark = false;
    }

    public String getSpeakerID() {
        return speakerID;
    }

    public void setSpeakerID(String speakerID) {
        this.speakerID = speakerID;
    }

    public String getSpeakerAudioFolderURIString() {
        return speakerAudioFolderURIString;
    }

    public void setSpeakerAudioFolderURIString(String speakerAudioFolderURIString) {
//      NOTE: AudioFileRawString in this format - file:/C:/Users/YuanJian/Desktop/Learning%20SWING/SpeechRecognition/build/classes/IO/resource/
        this.speakerAudioFolderURIString = speakerAudioFolderURIString;
    }

    public void setMark(boolean mark) {
        this.mark = mark;
    }

    public boolean isMark() {
        return mark;
    }
}
