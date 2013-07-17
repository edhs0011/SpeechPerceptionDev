package Perception;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class FileUtils {

    public FileUtils() {
    }

    /**
     *
     * Read the content of the given file, based on the given file
     *
     * @param fileURIString
     * @return List of data from the file being read
     */
    public List readFromFile(String fileURIString) {
//      Works for both JAR and FILE
        List data = new ArrayList();
        
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        
        URL fileURL = null;
//      System.out.println("[FileUtils::readFromFile] FileURIString: " + fileURIString);

        try {
            fileURL = new URL(fileURIString);
            inputStream = fileURL.openStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            String lineToWrite = bufferedReader.readLine();
            while (lineToWrite != null && lineToWrite.trim().length() > 0) {
//              NOTE: IGNORE ALL THE UNNECESSARY ENTER OR WHITE SPACE IN THE CONTENT
                data.add(lineToWrite);
                lineToWrite = bufferedReader.readLine();
            }
            
            inputStream.close();
            bufferedReader.close();
        } catch (FileNotFoundException fileEX) {
            System.out.println(fileEX.getMessage());
        } catch (IOException ioEx) {
            System.out.println(ioEx.getMessage());
        } 

        return data;
    }

    public void writeToFile(String outputFileURIString, ArrayList<String> messageToWrite) {
//      NOTE: WORK FOR UNIX / WINDOWS
        try {
            URI parentFolderURI = new URI(extractParentURIString(outputFileURIString));
            URI outputFileURI = new URI(outputFileURIString);

            if (!folderExist(parentFolderURI)) {
                createFolder(parentFolderURI);
            }

//            System.out.println("ParentFolderURI: " + parentFolderURI.toString());
//            System.out.println("OutputFolderURI: " + outputFileURI.toString());
//            System.out.println("OutputPath: " + outputPath.toString());

            File file = new File(outputFileURI);
            FileOutputStream fileStream;
            OutputStreamWriter outputWriter;
            try {
                fileStream = new FileOutputStream(file);
                outputWriter = new OutputStreamWriter(fileStream, "UTF-8");
                try (BufferedWriter bw = new BufferedWriter(outputWriter)) {
                    for (String message : messageToWrite) {
                        bw.write(message.trim() + "\n");
                    }
                } 
                
                fileStream.flush();
                fileStream.close();

                outputWriter.flush();
                outputWriter.close();
            } catch (IOException ioEx) {
                System.out.println(ioEx.getMessage());
            }
        } catch (URISyntaxException uriEX) {
            System.out.println(uriEX.getMessage());
        }
    }
    
    public boolean fileExist(URI fileURI) {
//      NOTE: FILE EXIST CANNOT USE FOR JAR, MUST USE JARENTRY
        boolean exist = false;

        Path path = Paths.get(fileURI);
//      System.out.println("[FileUtils::FileExist] FilePath - " + path.toAbsolutePath().toString());

        if (Files.exists(path)) {
            exist = true;
        }

        return exist;
    }
    
    public boolean folderExist(URI fileURI) {
        return  fileExist(fileURI);
    } 
        
    public void createFile(URI fileURI) throws IOException {
//      NOTE: THIS ARE FOR CREATE THE MISSING PROGRESS FILES/ RESULT FILES
        File newFile = new File(fileURI);
        setPermission(newFile);
        newFile.createNewFile();
        System.out.println("File " + fileURI.toString() + " created.");
    }
    
    public void createFolder(URI folderURI){
        File newFolder = new File(folderURI);
        setPermission(newFolder);
        newFolder.mkdir();
        System.out.println("Folder " + folderURI.toString() + " created.");
    }
    
    private void setPermission(File file){
        file.setWritable(true);
        file.setReadable(true);
        file.setExecutable(true);
    }
    
    
    public Path convert_FileName_To_OSPath(int folderType, String fileName) {
//      NOTE: FOR FILES OUTSIDE OF JAR ONLY - ELSE PATHS.GET(uri) will return error
        URI fileURI = null;
        Path osFilePath = null;

        try {
            fileURI = convert_FileName_To_URI(folderType, fileName);
            osFilePath = Paths.get(fileURI);
        } catch (NullPointerException nullEX) {
            System.out.println(nullEX.getMessage());
        }

//      System.out.println("[SystemProgress::convertOSPath] " + osFilePath);
        return osFilePath;
    }
    
    public URI convert_FileName_To_URI(int folderType, String fileName) {
        URI uri = null;
        String parentFolder = null;

        try {
            parentFolder = generateParentURIString(folderType);

//          System.out.println("[SystemInit::convertFileNameToURI] FolderName: " + parentFolder);
//          System.out.println("[SystemInit::convertFileNameToURI] FileName: " + fileName);

            String fileURIString = parentFolder + fileName;
//          System.out.println("[SystemInit::convertFileNameToURI] FileURI " + fileURIString);

//            boolean isJarPath = (fileURIString.startsWith("jar"));
//          System.out.println("Jar: " + isJarPath);

            uri = new URI(fileURIString);
//          System.out.println("[SystemInit::convertFileNameToURI] URI: " + uri.toString());

        } catch (URISyntaxException uriEX) {
            System.out.println("[SystemInit::convertFileNameToURI] " + uriEX.getMessage());
        }

        return uri;
    }

//  TODO: IF the package is rename, make the relevant changes in ISystem.RESOURCE_FOLDER_RELATIVE_PATH    
    private String generateParentURIString(int folderType) throws URISyntaxException {

        String parentFolder = null;
        switch (folderType) {
            case ISystem.PROGRESS_FOLDER:
//              parentFolder = Paths.get("progress").toUri().toString();
                parentFolder = Paths.get(ISystem.PROGRESS_FOLDER_FILENAME).toUri().toString();
                break;

            case ISystem.RESOURCE_FOLDER:
//              parentFolder = this.getClass().getResource("/Perception/resource/").toURI().toString();
                parentFolder = this.getClass().getResource(ISystem.RESOURCE_FOLDER_RELATIVE_PATH).toURI().toString();
                break;

            default:
                JOptionPane.showMessageDialog(null, "Unknown File Type!", "File not supported", JOptionPane.ERROR_MESSAGE);
                break;
        }
//      System.out.println("[SystemInit::generateParentString] ParentFolder: " + parentFolder);
        return parentFolder;
    }
    
    public String extractParentURIString(String fileURIString){
//      Logic: Find the last slash - Anything before that is the parentURI - after that is the fileName
        
//      System.out.println("[FileUtils::Parent] FileURI: " + fileURIString);
//      System.out.println(fileURIString.lastIndexOf("/"));
        
        int lastIndex = fileURIString.lastIndexOf("/");
        String parentURIString = fileURIString.substring(0,lastIndex);
        
//      System.out.println("[FileUtils::Parent] ParentURI:" + parentURIString);
        return parentURIString;
    }
}


//    -- FOR WINDOWS ONLY --
//    public void writeToFile(String outputFileURIString, ArrayList<String> messageToWrite) {  
///*      Only for writing to FILE - NOT JAR
// *      Logic: 
// *      - Check if file exist - If not, then create one and write every data in the arraylist to the file
// *      - Else If it exist, the writer has to specify the append option as true else will rewrite the whole file
// */
//        PrintWriter printWriter = null;
//        BufferedWriter bufferWriter = null;
////      System.out.println("[FileUtils::writeFromFile] FileURIString: " + fileURIString);
//             
////      for (String s : messageToWrite){
////          System.out.println(s);
////      }
//        
//        try {
//            URI parentFolderURI = new URI(extractParentURIString(outputFileURIString));
//            URI outputFileURI = new URI(outputFileURIString);
//        
//            Path path = Paths.get(outputFileURI);
//            
//            if (folderExist(parentFolderURI)){
//                if (! fileExist(outputFileURI)){
//                    createFile(outputFileURI);
//                }
//            }
//            
//            else{
//                createFolder(parentFolderURI);
//                createFile(outputFileURI);
//            }
//
////            printWriter = new PrintWriter(new FileWriter(path.toFile(), false));
//            printWriter = new PrintWriter((new FileWriter(path.toFile(), false)));
//            
//            for (String message : messageToWrite){
//                printWriter.println(message);
//            }           
//        }catch (IOException ex){
//            System.out.println("[FileUtils::writeFromFile] " + ex.getMessage());
//        }catch (URISyntaxException ex1){
//            System.out.println("[FileUtils::writeFromFile] " + ex1.getMessage());
//        }
//        
//        finally{
//            printWriter.flush();
//            printWriter.close();
//        }
//    }