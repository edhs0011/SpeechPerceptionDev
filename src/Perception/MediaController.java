package Perception;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JOptionPane;

public class MediaController extends EvaluationManager implements LineListener {
//  The final frame of the Clip object is indicated by -1
//    private static final int LASTFRAME = -1;
    private final int LASTFRAME = -1;
    
    private AudioInputStream audioInput;
    private AudioFormat audioFormat;
    
    private Clip audioClip;
    private MediaPlayerPanel mediaPlayer;
    
    private int clipLoopStart;
    private int clipLoopEnd;
    
    private int clipStartPoint;
    private int clipEndPoint;

    private int pauseFramePosition;
    private boolean isInitClip;
    
    public MediaController(MediaPlayerPanel mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
        
        clipLoopStart = 0;
        clipLoopEnd = LASTFRAME;
        
        clipStartPoint = clipLoopStart;
        clipEndPoint = clipLoopEnd;

        pauseFramePosition = 0;
        
        audioClip = null;
        audioFormat = null;
        audioInput = null;
                
        isInitClip = false;
    }

    public void createAudioClip(String speakerID, Transcription transcript) {

//      If either one is null means the audio hasn't been setup yet
        if (audioClip == null || audioInput == null) {
            setupAudio(speakerID, transcript);
        }
    }

    private void setupAudio(String speakerID, Transcription transcript) {
        if (audioInput == null) {
            generateAudioInput(speakerID, transcript);
            if (audioClip == null) {
                generateClip();
                audioClip.addLineListener(this);
            }
        }
    }

    private void generateAudioInput(String speakerID, Transcription transcript) {
/**
 *      Get the home path of the speaker + Transcription fileName to access
 *      the FILE For now, only can play .wav file Have to declare here cannot
 *      use AudioSample - sounds would not play if use getAudioInputStream
 *      from AudioSample
 */
        URI uri = null;
        String transcriptURIString = super.generateTranscriptFileURIString(speakerID, transcript.getAudioFileName());
        
//      Eg: file:/C:/Users/YuanJian/Desktop/Learning%20SWING/SpeechRecognition/build/classes/IO/resource/audio/ENG-001/hs/0001.wav
        System.out.println("[MediaController::generateAudioInput] Original Path: " + transcriptURIString);
        
//      System.out.println("[MediaController::generateAudioInput]  File Path: " + transcriptPathString.substring(transcriptPathString.indexOf("file")));
        
        try {
//          Eg. C:\Users\YuanJian\Desktop\Learning SWING\SpeechRecognition\build\classes\IO\resource\audio\ENG-001\hs\0001.wav
//          NOTE: JarURLConnection instances can only be used to read from JAR files
//          So if we need any file from a jar, we just avoid File and stick with URL :) 
            if (transcriptURIString.startsWith("jar")){
                URL url = new URL(transcriptURIString);
                JarURLConnection connection = (JarURLConnection) url.openConnection();
//              System.out.println("[MediaController::generateAudioInput] Connection: " + connection.getJarFileURL().toURI().toString());
                audioInput = AudioSystem.getAudioInputStream(url);
            }
            
            else{
                File audioFile = new File(new URI(transcriptURIString));
                audioInput = AudioSystem.getAudioInputStream(audioFile);
            }

            System.out.println("[MediaController::generateAudioInput] AudioFormat Result: " + audioInput.getFormat().toString());
            transcript.setFrameRate(audioInput.getFormat().getFrameRate());
        } catch (URISyntaxException ex) {
            System.out.println("[MediaController::URISyntaxEx] " + ex.getMessage());
        } catch (IOException ex2) {
            System.out.println("[MediaController::IOEx] " + ex2.getMessage());
        } catch (UnsupportedAudioFileException ex3) {
            System.out.println("[MediaController::UnsupportedAudioEx] " + ex3.getMessage());
        }
    }

    private void generateClip() {
        try {
            audioClip = AudioSystem.getClip();
        } catch (LineUnavailableException ex) {
            System.out.println(ex.getMessage());
        }
    }

//  For play button
    public void playClip() {
        reinitialiseClip();
        
        System.out.println("[MediaController::PlayClip_1] Previous Pause Frame:" +  pauseFramePosition);
        System.out.println("[MediaController::PlayClip_1] AudioClip Length:" + audioClip.getFrameLength());
        System.out.println("[MediaController::PlayClip_1] ClipLoopStart:" + clipLoopStart);
        
//      CHECK IF THE PAUSE FRAME HAS REACH THE END - IF YES RESET IT
        pauseFramePosition = (pauseFramePosition >= audioClip.getFrameLength()) ? clipLoopStart : pauseFramePosition;
        
//      CHECK IF THE FRAME IS AT BEGINNING - IF YES SET IT TO WHERE THE CLIP IS SUPPOSE TO START PLAYING INDICATED BY EVALUATOR
        pauseFramePosition = (pauseFramePosition == 0) ? clipLoopStart : pauseFramePosition;
        
        System.out.println("[MediaController::PlayClip_1] Current Pause Frame:" +  pauseFramePosition);
        audioClip.setFramePosition(pauseFramePosition);
        System.out.println("[MediaController::PlayClip_1] Audio Initial Frame: " + audioClip.getFramePosition());
        
        audioClip.start();
    }

//  For seek slider
    public void playClip(int framePos) {

        reinitialiseClip();
        System.out.println("[MediaController::playClip_2] Audio Initial Frame: " + audioClip.getFramePosition());
        
        if (framePos == audioClip.getFrameLength()){
            stopAndResetClip();
        }
        
        else {
            if (framePos >= clipLoopEnd) {
                this.clipStartPoint = framePos;
                this.clipEndPoint = LASTFRAME;
            } 
            
            else {
                this.clipStartPoint = framePos;
                this.clipEndPoint = clipLoopEnd;
            }
            audioClip.setLoopPoints(this.clipStartPoint, this.clipEndPoint);
            audioClip.setFramePosition(clipStartPoint);

            System.out.println("[MediaController::playClip_2] SeekSlider Value : " + framePos);
            System.out.println("[MediaController::playClip_2] SeekSlider Start : " + clipStartPoint);
            System.out.println("[MediaController::playClip_2] SeekSlider End : " + clipEndPoint);
            System.out.println("[MediaController::playClip_2] Audio Current Frame: " + audioClip.getFramePosition());

            audioClip.loop(0);
        }
    }

    public void pauseClip() {
        audioClip.stop();
    }
    
    public void loopClip(int loopTimes) {
        reinitialiseClip();

        System.out.println("[MediaController::LoopClip] Initial Current Frame: " + audioClip.getFramePosition());
        
        this.clipStartPoint = clipLoopStart;
        this.clipEndPoint = clipLoopEnd;
        
        audioClip.setLoopPoints(clipStartPoint, clipEndPoint);
        audioClip.setFramePosition(clipStartPoint);

        System.out.println("[MediaController::LoopClip] Looping from Frame: " + clipLoopStart);
        System.out.println("[MediaController::LoopClip] Clip endPoint: " + clipLoopEnd);
        System.out.println("[MediaController::LoopClip] Clip startPoint: " + clipStartPoint);
        System.out.println("[MediaController::LoopClip] Audio Current Frame: " + audioClip.getFramePosition());

        audioClip.loop(loopTimes);
    }
  
    private void reinitialiseClip() {
//      NOTE: Reason for this isInitClip:
//      - because this calls stopClip() -> Stop LineEvent will be handled
//      - It event calls the mediaPlayerPanel enableStopControlButton
//      - This makes the play/loop button display prematurely
        
        isInitClip = true;
        if (!audioClip.isOpen()) {
            openClip();
        }

        stopAndResetClip();

        isInitClip = false;
    }

    private void openClip() {

        if (audioInput != null) {   // wav file cannot be found
            try {
                audioClip.open(audioInput);
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            } catch (LineUnavailableException ex1) {
                System.out.println(ex1.getMessage());
            }

        } else {
            JOptionPane.showMessageDialog(null, "Audio cannot be open!", "Open failed!", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void stopAndResetClip() {
//      NOTE: audioClip.stop() doesn't reset the point to 0, it will only stop at its current frame
        audioClip.stop();
        audioClip.setFramePosition(0);
    }

    public void indicateLoopPoints(int startMark, int endMark){
//      System.out.println("[MediaController] Loop points: " + startMark + "-" + endMark + "");
//      NOTE: LoopPoints ranges from 0 to -1 (Last Frame)
//      Last frame cannot be indicated by the last frame index of the audio - This will cause exception
//      TEST: EndMark < StartMark || StartMark == EndMark == audioInput.maxframelength
        
        if (audioInput != null) {
            this.clipLoopStart = (startMark == audioInput.getFrameLength()) ? ((int)audioInput.getFrameLength() -1) : startMark;
            this.clipLoopEnd = (endMark == audioInput.getFrameLength() || endMark == 0) ? LASTFRAME : endMark;
            System.out.println("[MediaController::LoopPoints] Start/End points: " + clipLoopStart + "-" + clipLoopEnd);
        }
    }
    
    public int getAudioInputFrameLength() {
//      Set default length in case audioInput == null (wav file cannot be found)
        int frameLength = 0;
        if (audioInput != null) {
            frameLength = (int) audioInput.getFrameLength();
//          System.out.println("[MediaController] Frame Length: " + audioInput.getFrameLength());
        }
        return frameLength;
    }

//    public int getCurrentFramePosition(){
//        int currentFramePosition = 0;
//        if (audioClip != null){
//            currentFramePosition = audioClip.getFramePosition();
//        }
//        return currentFramePosition;
//    }
    
    /**
     * Event Handler
     */
    @Override
    public void update(LineEvent event) {
        if (event.getType().equals(LineEvent.Type.START)) {
            mediaPlayer.enableStopControlButton(true);
            System.out.println("[MediaController::Update.START] Start Playing from Frame " + audioClip.getFramePosition());
        } 
        
        else if (event.getType().equals(LineEvent.Type.STOP)) {
//          System.out.println("[MediaController::Update.STOP] End Frame @ " + audioClip.getFramePosition());
            if (! isInitClip){
                pauseFramePosition = audioClip.getFramePosition() % audioClip.getFrameLength();
                mediaPlayer.enableStopControlButton(false);
            }            
            System.out.println("[MediaController::Update.STOP] Current Pause Position: " + pauseFramePosition);
            System.out.println("[MediaController::Update.STOP] Current Frame Position " + audioClip.getFramePosition());
        } 
        
        else if (event.getType().equals(LineEvent.Type.CLOSE)) {
            audioClip.flush();
            audioClip.close();
        }
        
        else if (event.getType().equals(LineEvent.Type.OPEN)) {
            System.out.println("[MediaController::Update.Open] Clip open!");
            audioClip.setFramePosition(0);
            pauseFramePosition = 0;
        } 
    }
}
