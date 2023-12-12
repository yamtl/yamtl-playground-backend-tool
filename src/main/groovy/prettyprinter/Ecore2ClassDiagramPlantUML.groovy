package prettyprinter

import Utils.Utils
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import prettyprinting.EMFPrettyPrinter

/**
 * Parameters:
 * - metamodel: emfatic or xmi representation of Ecore metamodel
 *
 * Result:
 * - output with SVG representation of metamodel
 */
class Ecore2ClassDiagramPlantUML {
    private static final Logger logger = LoggerFactory.getLogger(Ecore2ClassDiagramPlantUML.class);

    static Object run(String jsonInput) {
        def request = new JsonSlurper().parseText(jsonInput)
        def response
        logger.info("Received request: ${request}")

        try {
            def directory = 'ecore2cd'

            def mmPath = Utils.saveMetamodelToFile(directory, request, "metamodel")
            def pumlText = EMFPrettyPrinter.ecore_to_classDiagram_PlantUML(mmPath)

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