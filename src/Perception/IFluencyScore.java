package Perception;

public interface IFluencyScore {
    public static final String FLUENCY_BAD = "差";
    public static final String FLUENCY_AVERAGE = "一般";
    public static final String FLUENCY_GOOD = "好";
    public static final String FLUENCY_NATIVE = "很好";    
    
    public static final String FLUENCY_BAD_TOOLTIP_DESCRIPTION = "Bad";
    public static final String FLUENCY_AVERAGE_TOOLTIP_DESCRIPTION = "Average";
    public static final String FLUENCY_GOOD_TOOLTIP_DESCRIPTION = "Good";
    public static final String FLUENCY_NATIVE_TOOLTIP_DESCRIPTION = "Native";

    
    public static final int BAD_SCORE = 1;
    public static final int AVERAGE_SCORE = 2;
    public static final int GOOD_SCORE = 3;
    public static final int NATIVE_SCORE = 4;
    
    public static final String FLUENCY_NOT_DONE = "N.A";
    public static final int NOT_DONE_SCORE = 0;
    
}
