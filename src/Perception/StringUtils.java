package Perception;

public class StringUtils {

//  GENERAL METHODS
    public boolean isEmptyString(String text) {
        boolean empty = true;
        String givenString = trimTrailingSpace(text);
        if (givenString.length() > 0) {
            empty = false;
        }
        return empty;
    }

    public boolean isSameString(String text, String wordToMatch) {
        String originalString = trimTrailingSpace(text);
        originalString = replaceHyphensWithSpace(originalString);
        String comparedString = trimTrailingSpace(wordToMatch);
        comparedString = replaceHyphensWithSpace(comparedString);
        
        return originalString.equals(comparedString);
    }

//  USE BY SYSTEMINIT/ SYSTEMPROGRESS
    public String replaceFileNameWithID(String originalFileName, String newName, String replacePattern){    
        
        String nameWithoutSpace = replaceSpaceWithHyphens(newName.trim());
        String newFileName = originalFileName.replaceAll(replacePattern, nameWithoutSpace);
//      System.out.println("[StringUtil::ReplaceFileName]" + newFileName);
        
        return newFileName;
    }
    
    
//  USE BY EVALUATIONPANEL
    public String replaceSpaceWithHyphens(String textWithSpaces) {
        
        String textWithHyphens = trimTrailingSpace(textWithSpaces);
        textWithHyphens = textWithHyphens.replaceAll(" ", "-");
//      System.out.println("[StringUtils::replaceSpace] Before: " + textWithSpaces + "| After: " + textWithHyphens);
        
        return trimTrailingSpace(textWithHyphens);
    }
    
    public String replaceHyphensWithSpace(String textWithHyphens) {
        
        String textWithSpaces = trimTrailingSpace(textWithHyphens);
        textWithSpaces = textWithSpaces.replaceAll("-", " ");
//      System.out.println("[StringUtils::replaceHyphens] Before: " + textWithHyphens + "| After: " + textWithSpaces);
        
        return trimTrailingSpace(textWithSpaces);
    }
    
    public String extractRawCommentMessage(String originalComment){
//      NOTE: BECAUSE I APPEND COMMENT: [COMMENT] IN THE EVALUATIONPANEL -> SO HAVE TO EXTRACT
        int separatorIndex = originalComment.indexOf(":");
        String rawCommentMsg = originalComment.substring(separatorIndex + 1);
        return trimTrailingSpace(rawCommentMsg);
    }
    
    
//  USE BY FILEOUTPUT
    public String appendString(String originalString, String newInformation) {
        if (originalString.length() == 0) {
            originalString = newInformation;
        } else if (newInformation.length() > 0) {
            originalString += "\t" + newInformation;
        }
        return originalString;
    }
        
    private String trimTrailingSpace(String textWithTrailingSpaces) {
        return textWithTrailingSpaces.trim();
    }
}
