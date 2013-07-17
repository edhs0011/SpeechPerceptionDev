package Perception;

public class WaveFormProcessor extends EvaluationManager {
    
    private AudioSample sample;
    private WaveFormPanel wave;

    public WaveFormProcessor() {
        sample = null;  
        wave =  new WaveFormPanel();
    }

    public WaveFormPanel generateWaveForm(String speakerID){
        Transcription transcript = super.getLastSeenTranscription();

        if (sample == null) {
            createAudioSample(speakerID, transcript);
        }
        
        try{
            wave.getWaveForm(sample);
            sample = null;
        }catch(NullPointerException ex){
//          File Not Exist - WaveFromPanel :: getWaveForm -> will return NullPointerException
            System.out.println(ex.getMessage());
        }
        
        finally{
            return wave;
        }
    }

    private void createAudioSample(String speakerID, Transcription transcript) {
        String transcriptFileString = super.generateTranscriptFileURIString(speakerID, transcript.getAudioFileName());
//      System.out.println("[WaveFormProcessor] Full Path of Transcription " + transcript.getTranscriptID() + ": " + transFileAbsPath);
        sample = new AudioSample(transcriptFileString);
    }

//      TODO: In case getSample is called before sample is generated - create a Default WaveForm
    public AudioSample getAudioSample() {
        if (sample == null) {
            System.out.println("[WaveFormProcessor] Sample not yet generated!");
        }
        return sample;
    }
    
    public void markIndicator(int markValue, int markType) {
        wave.drawMark(markValue, markType);
    }
}
