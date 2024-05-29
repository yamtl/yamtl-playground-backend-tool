package Utils

import groovy.io.FileType
import yamtl.core.YAMTLModule
import yamtl_m2m.StringUtil
import api.App


import org.yaml.snakeyaml.Yaml
import groovy.json.JsonSlurper
import groovy.xml.XmlParser
import groovy.xml.XmlUtil

class Utils {
    /**
     * returns the path where the metamodel is stored
     */
    def static String saveMetamodelToFile(String className, Map request, String requestFieldName ) {
        def metamodelContent = request[requestFieldName] as String
        metamodelContent = StringUtil.removeEscapeChars(metamodelContent)

        String metamodelPath = "${App.TMP_DIR}/${className}/${requestFieldName}_metamodel.ecore"
        if (metamodelContent.startsWith("@namespace")) {
            metamodelPath = "${App.TMP_DIR}/${className}/${requestFieldName}_metamodel.emf"
        }

        def file = new File(metamodelPath)

        // Check if the directory exists, and create it if it doesn't
        if(!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }

        file.text = metamodelContent  // Using .text instead of << to overwrite the content, use << if appending is needed

        return "file:///" + file.absolutePath
    }

    def static loadMetamodel(String className, Map request, String requestFieldName ) {
        def metamodelContent = request[requestFieldName] as String
        metamodelContent = StringUtil.removeEscapeChars(metamodelContent)

        String metamodelPath = "${App.TMP_DIR}/${className}/${requestFieldName}_metamodel.ecore"
        if (metamodelContent.startsWith("@namespace")) {
            metamodelPath = "${App.TMP_DIR}/${className}/${requestFieldName}_metamodel.emf"
        }

        def file = new File(metamodelPath)

        // Check if the directory exists, and create it if it doesn't
        if(!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }

        file.text = metamodelContent  // Using .text instead of << to overwrite the content, use << if appending is needed


        def metamodel = YAMTLModule.preloadMetamodel(metamodelPath)
        metamodel
    }

    def static initDirectory(String directoryName) {
        def rootDirectory = new File("${App.TMP_DIR}")
        if (!rootDirectory.exists()) rootDirectory.mkdirs()

        def directory = new File("${App.TMP_DIR}/${directoryName}")

        if (directory.exists()) {
            // Delete the existing directory and its contents recursively
            directory.eachFileRecurse(FileType.FILES) { file ->
                file.delete()
            }
            directory.deleteDir()
        }

        // Create the directory
        directory.mkdirs()
    }



    def static String detectFormat(String text) {
        if (isJson(text)) {
            return "json"
        } else if (isYaml(text)) {
            return "yml"
        } else if (isXml(text)) {
            return "xml"
        } else if (isCsv(text)) {
            return "csv"
        } else {
            return "xmi"
        }
    }

    def static boolean isJson(String text) {
        try {
            new JsonSlurper().parseText(text)
            return true
        } catch (Exception e) {
            return false
        }
    }

    def static boolean isYaml(String text) {
        try {
            new Yaml().load(text)
            return true
        } catch (Exception e) {
            return false
        }
    }

    def static boolean isXml(String text) {
        try {
            new XmlParser().parseText(text)
            return true
        } catch (Exception e) {
            return false
        }
    }

    def static boolean isCsv(String text) {
        def lines = text.split("\n")
        if (lines.size() < 2) {
            return false
        }
        def headers = lines[0].split(",")
        if (headers.size() < 2) {
            return false
        }
        return lines.every { it.split(",").size() == headers.size() }
    }
}


