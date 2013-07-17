package Perception;

public interface ISystem {

    public static final String REPLACE_STRING_PATTERN = "\\[\\w*\\]";
    public static final String AUDIO_UTTERANCE_FORMAT_STRING = "%04d";
    
    public static final int SPEAKER = 0;
//    public static final int TRANSCRIPTION = 1;
    public static final int WORD = 1;
    public static final int EVALUATOR = 2;
    
    public static final int PROGRESS_FOLDER = 4;
    public static final int RESOURCE_FOLDER = 5;
    
    public static final int SAVE_MAIN = 6;
    public static final int SAVE_BACKUP = 7;
        
    public static final String SPEAKER_FILE_READABLE = "config/speaker.txt";
    public static final String EVALUATOR_FILE_READABLE = "config/evaluator.txt";    
    
//  TODO: CHANGE TO THE RESPECTIVE FOLDER NAME ACCORDINGLY
    public static final String RESOURCE_FOLDER_RELATIVE_PATH = "/Perception/resource/";
    public static final String PROGRESS_FOLDER_FILENAME = "progress";
    public static final String RESULT_FOLDER_FILENAME = "result";
    
    public static final String SPEAKER_FILE_BINARY = "speaker_[EVALID].dat";
    public static final String TRANSCRIPTION_FILE_BINARY = "transcription_[EVALUATORID].dat";
    public static final String WORD_FILE_BINARY = "word_[EVALUATORID].dat";
    public static final String EVALUATOR_FILE_BINARY = "evaluator_[EVALUATORID].dat";

    public static final String SPEAKER_FILE_BINARY_BACKUP = "speaker_[EVALUATORID]_bak.dat";
    public static final String TRANSCRIPTION_FILE_BINARY_BACKUP = "transcription_[EVALUATORID]_bak.dat";
    public static final String WORD_FILE_BINARY_BACKUP = "word_[EVALUATORID]_bak.dat";
    public static final String EVALUATOR_FILE_BINARY_BACKUP = "evaluator_[EVALUATORID]_bak.dat";
    
    public static final String SPEAKER_TRANSCRIPTION_HANYU_PINYIN_RELATIVE_PATH = "audio/[SPEAKERID]/saidv.txt";
    public static final String SPEAKER_TRANSCRIPTION_WORD_RELATIVE_PATH = "audio/[SPEAKERID]/transcription.txt";
    public static final String SPEAKER_TRANSCRIPTION_AUDIO_RELATIVE_PATH = "audio/[SPEAKERID]/converted/";
}
    
//    NOTE: CHANGE TO THE FIRST SPEAKER INSIDE THE RESOURCE/AUDIO FOLDER
//    AS OF NOW [200613] - NO USE
//    public static final String DEFAULT_SPEAKER = "ENG-001";
//    public static final String DEFAULT_TRANSCRIPTION_HANYU_PINYIN_RELATIVE_PATH = "audio/" + DEFAULT_SPEAKER + "/saidv.txt";
//    public static final String DEFAULT_TRANSCRIPTION_WORD_RELATIVE_PATH = "audio/" + DEFAULT_SPEAKER + "/transcription.txt";
//    public static final String DEFAULT_TRANSCRIPTION_AUDIO_RELATIVE_PATH = "audio/" + DEFAULT_SPEAKER + "/converted/";

//    public static final int NUM_OF_DUPLICATES_TRANSCRIPTION = 2;