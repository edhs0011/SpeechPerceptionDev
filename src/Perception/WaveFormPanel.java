package Perception;

import java.awt.Color;
import java.awt.Graphics;

public class WaveFormPanel extends javax.swing.JPanel {

//  Indicate what marker it is
    public static final int STARTMARKER = 0;
    public static final int ENDMARKER = 1;
    public static final int SEEKMARKER = 2;
    
//  Line Color for the graph
//    private static final Color START_END_MARKER_COLOR = Color.RED;
//    private static final Color REFERENCE_LINE_COLOR = Color.GRAY;
//    private static final Color WAVEFORM_COLOR = Color.BLACK;
//    private static final Color SEEK_MARKER_COLOR = Color.BLUE;
    private final Color START_END_MARKER_COLOR = Color.RED;
    private final Color REFERENCE_LINE_COLOR = Color.GRAY;
    private final Color WAVEFORM_COLOR = Color.BLACK;
    private final Color SEEK_MARKER_COLOR = Color.BLUE;
    
    private int[][] bytesStreamSample;
    private double biggestSample;
    
    private int curStartPoint;
    private int curEndPoint;
    private int curSeekPoint;
    
    private int prevStartPoint;
    private int prevEndPoint;
    private int prevSeekPoint;
    
    private int drawStartX;
    private int drawEndX;
    private int drawSeekX;
    
    private int increment;
    private double scaleFactor;
    
    private boolean isDrawStartEndMark = false;
    private boolean isDrawSeekMark = false;
//  eps = 0.000001
//  private double eps = 1E-6;

    public WaveFormPanel() {
        bytesStreamSample = null;
        
        prevStartPoint = 0;
        curStartPoint = 0;
        
        prevEndPoint = 0;
        curEndPoint = 0;
        
        curSeekPoint = 0;
        prevSeekPoint = 0;

        scaleFactor = 0.0f;
        
        drawStartX = 0;
        drawEndX = 0;
        drawSeekX = 0;

        increment = 0;
    }

    public void getWaveForm(AudioSample audioSample) throws NullPointerException{
        this.bytesStreamSample = audioSample.getSamplesContainer();
        this.biggestSample = audioSample.getBiggestSample();
       
        curStartPoint = 0;
        curEndPoint = bytesStreamSample.length;

//      for (int i = 0; i < bytesStreamSample.length; i++) {
//          System.out.println("[WaveFormPanel] Size of bytesStreamSample[" + i + "]: " + bytesStreamSample[i].length);
//      }
//      System.out.println("[WaveFormPanel] Biggest Sample: " + biggestSample);

//      Need repaint to call the paintComponent function - otherwise wouldn't drawWaveForm
        repaint();
    }

//  TODO: MAYBE CAN BE REFACTOR TO SETMARKERPOINTS (?)
    public void drawMark(int framePos, int markType) {
//      Draw a straight line from top to the midway line (getHeight()/2) pointed @ FramePos
//      Due to scaling of the graph, has to do scaling on the draw data as well
//      Note: Origin 0,0 start from top left hand

        int newX = scaleFrameToXAxis(framePos);

        switch (markType) {
            case WaveFormPanel.STARTMARKER:
//              After drawing, the current point will be replace by a new value and the previous point will be replace by current value
//              Draw only when the nextPoint to draw is not the same as the currentEndPoint (otherwise will overwrite the endPointMarker on nextPaint)
                if (curStartPoint != newX) {
                    prevStartPoint = curStartPoint;
                    curStartPoint = newX;
                }
                drawStartEndMark();
                break;

            case WaveFormPanel.ENDMARKER:
                if (curEndPoint != newX) {
                    prevEndPoint = curEndPoint;
                    curEndPoint = newX;
                }
                drawStartEndMark();
                break;

            case WaveFormPanel.SEEKMARKER:
                if (curSeekPoint != newX) {
                    prevSeekPoint = curSeekPoint;
                    curSeekPoint = newX;
                }
                drawSeekMark();
                break;

            default:
                System.out.println("[WaveFormPanel] Unknown marker type!");
        }
    }
    
    private int scaleFrameToXAxis(int framePos) {
        return (int) (framePos / increment);
    }

//  TODO: MAYBE CAN REFACTOR TO 2 METHODS - RESETPREVIOUSMARK , SETCURRENTMARK
    private void drawSeekMark(){
        isDrawSeekMark = true;
                
//      Double repaint - First one is to remove the previous mark, second is the one to draw the new mark

        drawStartX = prevEndPoint;
        repaint(prevEndPoint, 0, 1, getHeight());
        drawEndX = prevStartPoint;
        repaint(prevStartPoint, 0, 1, getHeight());
        drawSeekX = prevSeekPoint;
        repaint(prevSeekPoint, 0, 1, getHeight());

        drawStartX = curEndPoint;
        repaint(curEndPoint, 0, 1, getHeight());
        drawEndX = curStartPoint;
        repaint(curStartPoint, 0, 1, getHeight());
        drawSeekX = curSeekPoint;
        repaint(curSeekPoint, 0, 1, getHeight());
    }
    
    private void drawStartEndMark(){
        isDrawStartEndMark = true;
        
        drawStartX = prevEndPoint;
        repaint(prevEndPoint, 0, 1, getHeight());
        drawEndX = prevStartPoint;
        repaint(prevStartPoint, 0, 1, getHeight());

        drawStartX = curEndPoint;
        repaint(curEndPoint, 0, 1, getHeight());
        drawEndX = curStartPoint;
        repaint(curStartPoint, 0, 1, getHeight());
    }
    
    public void drawWaveForm(Graphics g, int[] pointsToPlot) {
        /**
         * - Drawing the waveform - Read individual point from the
         * bytesStreamSample array (have to take care of hex/binary ?) - Do a
         * scaling of the panel such that all points inside the
         * bytesStreamSample can fit into the panel (Both how to find this
         * scale?) - Now plot the graph =) drawLine between the 2 points
         * (point1:(x1,y1) | point2:(x2,y2)) to join them all up
         */
        

//      Check content of pointsToPlot
//      int count = 0;
//      for (int i : pointsToPlot){
//          System.out.println(count + " - " + i);  //For jar, the behind all 0 :(
//          count ++;
//      }
//      System.out.println("[WaveFormPanel::DrawWaveForm] Size of pointsToPlot[]: " + String.valueOf(pointsToPlot.length));
//      Note: xIndex and oldX always the same, only y differs

        int oldX = 0;
        int oldY = (int) (getHeight() / 2);
        int xIndex = 0;


//      Because it is not visible to plot ALL the points within the array
//      Pick only certain points sufficient to plot out the graph - increment
//      number of samples / (number of samples * horizontal scale factor)
//      - ((double) getWidth() / samples.length) = Total number of ticks needed
//      - samples.length * ((double) getWidth() / samples.length) = (?) X point scaling?
//      - samples.length / (samples.length * ((double) getWidth() / samples.length)) = Value spearating each tick

//      NOTE: Decrease increment to plot more points
        increment = (int) (pointsToPlot.length / (pointsToPlot.length * ((double) getWidth() / pointsToPlot.length)));
//      System.out.println("[WaveFormPanel::DrawWaveForm] # of ticks in total - " + (pointsToPlot.length / increment));
//      System.out.println("[WaveFormPanel::DrawWaveForm] Increment is - " + increment);

        g.setColor(WAVEFORM_COLOR);
        int t = 0;

        for (t = 0; t < increment; t += increment) {
//          INDICATING the first point : (OldX = 0, OldY = getHeight()/2, xIndex = 0)
//          System.out.format("[WaveFormPanel] #" + xIndex + " - Point 1 (%d , %d) - Point 2 (%d , %d)\n", oldX, oldY, xIndex, oldY);
            g.drawLine(oldX, oldY, xIndex, oldY);
            xIndex++;
            oldX = xIndex;
        }

        for (; t < pointsToPlot.length; t += increment) {
//          Find the scale for each ticks on y-axis
//          *2 because biggestSample = largest point (and also the lowest point) value
//          * 1.2? - Set the amplitude of the wave
//          [Updated] From physics-class I remembered that you can change the amplitude of a sine-wave by multiplying the sine-value with a number between 0 and 1.
//          From: http://javaproblems.hopto.org/audio-change-volume-of-samples-in-byte-array/
//          getHeight()/2 - scaledSample - Because 0,0 start from top left. Scaled sample is measure from top left therefore to plot the graph from (origin half of the screen)
//          NOTE: DECREASE [1.2] to make the graph bigger, INCREASE make graph smaller
//          NOTE: Origin starts from TOP LEFT of the frame            
            
            scaleFactor = getHeight() / (biggestSample * 2 * 1.2);
            
            double scaledSample = pointsToPlot[t] * scaleFactor;
            int y = (int) ((getHeight() / 2) - (scaledSample));

//          System.out.format("[WaveFormPanel] #" + xIndex + " Point 1 (%d , %d) - Point 2 (%d , %d)\n", oldX, oldY, xIndex, y);
            g.drawLine(oldX, oldY, xIndex, y);

            xIndex++;
            oldX = xIndex;
            oldY = y;
        }

//      System.out.println("[WaveFormPanel] Total Points Drawn:" + xIndex);
//      System.out.println("[WaveFormPanel] Total Points Selected:" + ((pointsToPlot.length / increment) + 1));
//      System.out.println("[WaveFormPanel] IsTotalPointsDrawnEquivalent (No. of Points Select X Points Drawn) :" + ((pointsToPlot.length / increment) == xIndex);
    }
    
//  TODO: prevent this from redrawing everything the marker is set
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(REFERENCE_LINE_COLOR);
        
        g.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);    //Draw a horizontal line as the X axis

//      If bytesStreamSample is null - No waveform has been drawn yet
//      If don't do this check, each time the repaint is called - drawWaveForm will keep drawing over and over
//      But this is unnecessary because super.paintComponent(g); will paint any previous image onto the canvas
        if (bytesStreamSample != null) {
//          Since # of channel is always one for our case - always return the first array
//          Else pass the whole array and let drawWaveForm handle the rest
            drawWaveForm(g, bytesStreamSample[0]);
        }

//      NOTE: Double repaint is called all @ once - ie Executed in one single thread (Event Dispatch Thread ?) 
        if (isDrawStartEndMark) {
            g.setColor(START_END_MARKER_COLOR);
            g.drawLine(drawStartX, 0, drawStartX, getHeight());
            g.drawLine(drawEndX, 0, drawEndX, getHeight());
            isDrawStartEndMark = false;
        }

        if (isDrawSeekMark) {
            g.setColor(START_END_MARKER_COLOR);
            g.drawLine(drawStartX, 0, drawStartX, getHeight());
            g.drawLine(drawEndX, 0, drawEndX, getHeight());
            g.setColor(SEEK_MARKER_COLOR);
            g.drawLine(drawSeekX, 0, drawSeekX, getHeight());
            isDrawSeekMark = false;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(1000, 640));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1000, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 640, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
