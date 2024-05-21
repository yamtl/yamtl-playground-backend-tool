package prettyprinter

import Utils.Utils
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import yamtl.core.YAMTLModule
import org.eclipse.emf.ecore.EcorePackage

/**
 * Parameters:
 * - metamodel: emfatic or xmi representation of Ecore metamodel
 *
 * Result:
 * - output with YAMTL JSON representation of metamodel
 */
class Emf2Json extends YAMTLModule {
    private static final Logger logger = LoggerFactory.getLogger(Ecore2ClassDiagram.class);

    public Emf2Json() {
		header().in("in",EcorePackage.eINSTANCE)
	}

    static Object run(String jsonInput) {
        def request = new JsonSlurper().parseText(jsonInput)
        def response
        logger.info("Received request: ${request}")

        try {
            def directory = 'emf2json'

            def mmPath = Utils.saveMetamodelToFile(directory, request, "metamodel")
            println("IN Emf2Json: " + mmPath)
            
            def xform = new Emf2Json()
            def pk = xform.loadMetamodel(mmPath - 'file:///')
            xform.loadInputResources(["in": pk])
            def jsonText = xform.exportUntypedModelToJson("in");

            // Return the xmi contents
            response = new JsonBuilder()
            response {
                generatedText 	jsonText
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