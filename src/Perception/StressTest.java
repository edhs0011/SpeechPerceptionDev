/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Perception;

public class StressTest {

    public StressTest() {
    }
    
    public void run(){
        try {
            while (true) {
//        NOTE: JUST KEEP CREATING NEW OBJECT
                System.out.println("Creating EvaluationPanel \"\"...");
//                Speaker newSpeaker = new Speaker("", "");
                EvaluationPanel evaluationPane = null; 
                evaluationPane = new EvaluationPanel("0", 0);
                MainFrame.changePanel(evaluationPane);
                
                System.out.println("Max Memory: " + Runtime.getRuntime().maxMemory());
                System.out.println("Available Memory: " + Runtime.getRuntime().totalMemory());
                System.out.println("Free Memory: " + Runtime.getRuntime().freeMemory());
                System.out.println("Processor: " + Runtime.getRuntime().availableProcessors());
            }
        } catch (OutOfMemoryError memEx) {
            System.out.println(memEx.getMessage());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
