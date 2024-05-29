package prettyprinter

import Utils.Utils
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import prettyprinting.EMFPrettyPrinter
import Utils.StringUtil

import api.App

/**
 * Parameters:
 * - metamodel: emfatic or xmi representation of Ecore metamodel
 * - model: xmi representation of model
 *
 * Result: output with SVG representation of model
 */
class Xmi2ObjectDiagramPlantUML {
    private static final Logger logger = LoggerFactory.getLogger(Xmi2ObjectDiagramPlantUML.class);

    static Object run(String jsonInput) {
        def request = new JsonSlurper().parseText(jsonInput)
        def response
        logger.info("Received request: ${request}")

        try {
            def directory = 'ecore2cd'

            // store metamoel
            def mmPath = Utils.saveMetamodelToFile(directory, request, "metamodel")

            // store model
            String modelPath = "${App.TMP_DIR}/${directory}/model.xmi"
            def file = new File(modelPath)
            if (file.exists()) file.delete()
            file << StringUtil.removeEscapeChars(request['model'].toString())

            def pumlText = EMFPrettyPrinter.xmi_to_objectDiagram_PlantUML(mmPath, file.absolutePath)

            // Return the xmi contents
            response = new JsonBuilder()
            response {
                generatedText 	pumlText
            }

            logger.info("Generated response: ${response}");

        } catch(Exception e) {
            response {
                output 	e.message
            }

        }

        return response
    }
}


