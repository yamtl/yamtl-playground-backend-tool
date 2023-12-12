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
class Ecore2ClassDiagram {
    private static final Logger logger = LoggerFactory.getLogger(Ecore2ClassDiagram.class);

    static Object run(String jsonInput) {
        def request = new JsonSlurper().parseText(jsonInput)
        def response
        logger.info("Received request: ${request}")

        try {
            def directory = 'ecore2cd'

            def mmPath = Utils.saveMetamodelToFile(directory, request, "metamodel")
            def svgPath = mmPath - 'file:///' + '.svg'
            println(mmPath)
            println(svgPath)

            EMFPrettyPrinter.ecore_to_classDiagram_SVG(mmPath, svgPath)

            // Return the xmi contents
            response = new JsonBuilder()
            response {
                diagram 	new File(svgPath).text
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