package oulu.m3s.utils

import org.apache.commons.io.FilenameUtils

class FileUtil {

    public static String getFullFileName(String fileName){
        String name = splitNames(fileName)[0]
        return escapeInnerClass(name).trim()
    }

    private static String[] splitNames(String fileName){
        return fileName.split("::")
    }

    private static String escapeInnerClass(String fileName){
        if(fileName.contains("\$")){
            fileName = fileName.substring(0, fileName.indexOf("\$"))
        }
        return fileName
    }

    public static HashMap<String, String> loadSourceFilesFromDirectory(String revisionDirectoryPath){
        HashMap<String, String> sourceCodeMapByFileName = new HashMap<>()
        File revDirectory = new File(revisionDirectoryPath)
        revDirectory.listFiles().each { file ->
            String name = file.name
            name = FilenameUtils.removeExtension(name).trim()
            sourceCodeMapByFileName.put(name, file.text)
        }
        return sourceCodeMapByFileName
    }
}
