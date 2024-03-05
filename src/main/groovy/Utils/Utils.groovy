package Utils

import groovy.io.FileType
import yamtl.core.YAMTLModule
import yamtl_m2m.StringUtil
import api.App

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

}
