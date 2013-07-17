package Perception;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.jar.JarInputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioSample {

    private static final int NUM_BITS_PER_BYTE = 8;
    private AudioInputStream sampleInputStream;
    private int sampleMax = 0;
    private int sampleMin = 0;
    private double biggestSample = 0.0f;
    private int[][] samplesContainer;
    private byte[] bytes;

    public AudioSample(String uriFileString) {
        bytes = null;
        sampleInputStream = null;
        samplesContainer = null;
        generateWaveFormData(uriFileString);
    }

    private void generateWaveFormData(String uriFileString) {
//      RESOURCE: http://docs.oracle.com/javase/6/docs/api/javax/sound/sampled/AudioInputStream.html
//      RESOURCE: http://www.javafaq.nu/java-example-code-479.html
//      RESOURCE: http://codeidol.com/java/swing/Audio/Build-an-Audio-Waveform-Display/
//      Loading the file into the AudioInputStream
//      Length is expressed in sample frames -> Convert to bytes
        
//      Store sampleInputStream into a byte array
//      For plotting of the graph
//      sampleInputStream.getFrameLength returns # of frames instead of bytes
//      sampleInputStream.getFormat().getFrameSize() returns bytes/frame
//      NOTE: FrameLength return # of frames not bytes -> THerefore need to multiply by getFrameSize
        
        try {
            System.out.println("[AudioSample::generateWaveFormData] URI Audio Path: " + uriFileString);
                       
//          Both jar and file can use the following methods to retrieve audio
            URL url = new URL(uriFileString);
            this.sampleInputStream = AudioSystem.getAudioInputStream(url);

            InputStream in = new BufferedInputStream(sampleInputStream);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            for (int b; (b = in.read()) != -1;){
                out.write(b);
            }
            bytes = out.toByteArray();
        } catch (UnsupportedAudioFileException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException ex1) {
            System.out.println(ex1.getMessage());
        } 

        createSampleArrayCollection();
    }

//  Populate the sampleInputStream with Data for the WaveFormToDraw
    private void createSampleArrayCollection() {
        if (sampleInputStream == null) {
            System.out.println("[AudioSample] There is nothing in the stream! XD");
        } 
        
        else {
            try {
                
//              Check if the byte array is properly written with AudioInputStream
//              int count = 0;
//              for (byte b : bytes)
//              {
//                 System.out.println("[AudioSample] Byte #" + count + ": "+b);
//                 count++;
//               }
//               System.out.println("[AudioSample] # of bytes read: " + bytes.length);
                
                this.samplesContainer = constructSixteenthBitArray(bytes);
                cutSilenceSample(this.samplesContainer);
                computeBiggestSample();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }

    }

//  Algorithm for PCM_SIGNED little-endian encoding only (not for mulaw/ulaw/alaw) - 16 bits sample (2 bytes)
//  For our case - 16kHz
    private int[][] constructSixteenthBitArray(byte[] eightBitByteArray) {
        int channels = getNumberOfChannels();
        System.out.println("[AudioSample] Length of array before conversion: " + eightBitByteArray.length + " with " + channels + " channels.");

//      NOTE: Why eightBitByteArray.length / (2 * getNumberOfChannels())
//      eightBitByteArray.length - Low and High is 2 array index for a sixteen bits - therefore /2 to get the length needed to store 16 bits
//      2 * getNumberOfChannels() - because if there only one channel just /2 earlier will do
//      but if there is 2 channels - have to first split the sample into 2 equal portion and then /2 because of the 8 bit to 16 bit conversion 
//      likewise 3 channels divide sample to 3 equal portion and then /2 for the 8bit-16bit conversion
//      Therefore the pattern is the original byte array length / 2 / # of channels = byte array length / (2 * # of channel)
        
//        int[][] toReturn = new int[getNumberOfChannels()][eightBitByteArray.length / (2 * getNumberOfChannels())];
        int[][] toReturn = new int[channels][eightBitByteArray.length / (2 * channels)];
        int index = 0;

//      int count = 0;
//      for (byte b : eightBitByteArray) {
//          System.out.println("Byte" + count + ": " + b + "\n");
//          count++;
//      }
        
//      System.out.println("# of iterations in bytes array " + count);
//      System.out.println("Index 0: " + eightBitByteArray[0] + "Index 1: " + eightBitByteArray[1]);

//      loop thru byte[]
//      RESOURCE: http://codeidol.com/java/swing/Audio/Build-an-Audio-Waveform-Display/
//      RESOURCE: http://en.wikipedia.org/wiki/Endian
//      NOTE: wav file is little-endian
//      - First byte is the least significant bit (LSB) ; Second byte is the most significant bit (MSB)
//      - That's explain why low first then high
//      - To process 16bit together - high + low [NOT addition but CONCATENATION]
//      - high << 8 - shift 8 places to left = high(8 bits) 0000 0000
//      - low & 0x00ff - which which is 0000 0000 1111 1111 & low = 0000 0000 low(8bits)
        
        for (int t = 0; t < eightBitByteArray.length;) {
//            for (int a = 0; a < getNumberOfChannels(); a++) {
            for (int a = 0; a < channels; a++) {
                int low = (int) eightBitByteArray[t];
                t++;
                int high = (int) eightBitByteArray[t];
                t++;

//              For a 16 bits sample - 0000 0000 0000 0000
//              byte array is only 8 bit (0000 0000) so ...
//              since the audio is little-endian the left is lower byte right is higher byte
//              To form them to 16 bit used for drawing the waveform...the array has to be reconstructed
//              From MSB to LSB
                int sample = (high << 8) + (low & 0x00ff);

//              Finding the maximum sample Height and minimum sample help - helps to construct a proportionate waveform
                if (sample < sampleMin) {
                    sampleMin = sample;
                } else if (sample > sampleMax) {
                    sampleMax = sample;
                }
                
                toReturn[a][index] = sample;
            }
            index++;
        }
        
//      System.out.println("[AudioSample] Length of Array after conversion: " + toReturn[0].length);
        return toReturn;
    }
    
    private void cutSilenceSample(int[][] sample) {
        int cutIndexStart = 0;
        int cutIndexEnd = sample[0].length;
        
        for(int i = 0; i < sample[0].length / 2; i++) {
            int rareI = sample[0].length - i - 1;
            if (Math.abs(sample[0][i]) < 1000 && cutIndexStart == i - 1)
                cutIndexStart = i;
            if (Math.abs(sample[0][rareI]) < 1000 && cutIndexEnd == rareI + 1)
                cutIndexEnd = rareI;
        }
        /*oj*/
        
        
        cutIndexStart = (cutIndexStart - 16000) > 0 ? cutIndexStart - 16000 : 0;
        cutIndexEnd = (cutIndexEnd + 8000) < sample[0].length ? cutIndexEnd + 8000 : sample[0].length;
        
        int utterId = SystemInit.myEval.getLastSeenUtterance();
        Transcription trans = SystemInit.myEval.getTranscriptionByUtteranceID(utterId);
        trans.setStartFrame(cutIndexStart);
        trans.setEndFrame(cutIndexEnd); 
    }
    
    private void computeBiggestSample(){
//      Find the largest y value so that we can scale accordingly
//      NOTE: Within the samplesContainer there are negative values
        if (sampleMax > sampleMin) {
            biggestSample = sampleMax;
        } else {
            biggestSample = Math.abs(((double) sampleMin));
        }
    }
    
    public AudioInputStream getSampleInputStream() {
        return sampleInputStream;
    }
    
    public void setSampleInputStream(AudioInputStream sampleInputStream) {
        this.sampleInputStream = sampleInputStream;
    }

    public double getBiggestSample() {
        return biggestSample;
    }

    public int[][] getSamplesContainer() {
        return samplesContainer;
    }

    public int getSampleMax() {
        return sampleMax;
    }

    public int getSampleMin() {
        return sampleMin;
    }

    private int getNumberOfChannels() {
//      Since the sample is distributed equally among the channels :-
//      First find the byteSize of the sample - Total # bits/ (8bits/bytes)
//      Then find the totalFrameSize(bytes)/ numBytesPerSample to get the # of channel
        
        int numBytesPerSample = sampleInputStream.getFormat().getSampleSizeInBits() / NUM_BITS_PER_BYTE;
        return sampleInputStream.getFormat().getFrameSize() / numBytesPerSample;
    }
}