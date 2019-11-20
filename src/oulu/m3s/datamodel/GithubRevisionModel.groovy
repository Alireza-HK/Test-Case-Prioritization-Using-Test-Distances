package oulu.m3s.datamodel

import oulu.m3s.utils.FileUtil

import java.text.SimpleDateFormat

class GithubRevisionModel {

    /* When writing beans in Groovy, often called POGOs (Plain Old Groovy Objects), you don’t have to create the field and getter / setter yourself, but let the Groovy compiler do it for you.
    Free standing 'field' without modifier visibility actually makes the Groovy compiler to generate a private field and a getter and setter automatically. */
    String project
    Integer bugID
    String revisionID
    Date revisionDate
    String modifiedSources
    String executedTests
    String failedTests
    //
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    GithubRevisionModel(String csvTextLine) {
        String[] values = csvTextLine.split(",")
        this.project = values[0]
        this.bugID = values[1] as Integer
        this.revisionID = values[2]
        this.revisionDate =  dateFormat.parse(values[3])
        this.modifiedSources = values[4]
        this.executedTests = values[5]
        this.failedTests = values[6]
    }

    public Set<String> listExecutedTests(){
        HashSet<String> list = new HashSet<String>()
        getExecutedTests().split(";").each {
            list.add(FileUtil.getFullFileName(it))
        }
        return list
    }

    public Set<String> listModifiedSources(){
        HashSet<String> list = new HashSet<String>()
        getModifiedSources().split(";").each {
            list.add(it.trim())
        }
        return list
    }

    public Set<String> listFailedTests(){
        HashSet<String> list = new HashSet<String>()
        getFailedTests().split(";").each {
            list.add(FileUtil.getFullFileName(it))
        }
        return list
    }


    @Override
    public String toString() {
        return "Defect4JRevisionModel{" +
                "project='" + getProject() + '\'' +
                ", bugID=" + getBugID() +
                ", revisionID='" + getRevisionID() + '\'' +
                ", revisionDate='" + dateFormat.format(getRevisionDate())+ '\'' +
                ", modifiedSources=" + listModifiedSources().size() +
                ", executedTests=" + listExecutedTests().size() +
                ", failedTests=" + listFailedTests().size() +
                '}';
    }
}
