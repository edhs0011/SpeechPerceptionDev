package Perception;

import java.io.Serializable;

public class Word implements  Serializable{
    private static final long serialVersionUID = 1L;
    
    private int wordID;
    private String chineseChar = null;
    private String hanyuPinyin = null;
    
    public Word(int wordID, String chineseChar, String hanyuPinyin) {
        this.wordID = wordID;
        this.chineseChar = chineseChar;
        this.hanyuPinyin = hanyuPinyin;
    }

    public int getWordID() {
        return wordID;
    }

    public void setWordID(int wordID) {
        this.wordID = wordID;
    }

    public String getChineseChar() {
        return chineseChar;
    }

    public void setChineseChar(String chineseChar) {
        this.chineseChar = chineseChar;
    }
    
    public String getHanyuPinyin() {
        return hanyuPinyin;
    }

    public void setHanyuPinyin(String hanyuPinyin) {
        this.hanyuPinyin = hanyuPinyin;
    }
}
