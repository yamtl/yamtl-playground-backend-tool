package yamtl_m2m

import Utils.Utils
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.resource.Resource
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.io.FileType
import yamtl.core.YAMTLModule
import yamtl.groovy.YAMTLGroovyExtensions

import api.App

class RunYAMTL_m2m_from_flexible_to_mm {
	private static final Logger logger = LoggerFactory.getLogger(RunYAMTL_m2m_from_flexible_to_mm.class);
	
	/**
	 * 
	 * Function that executes basic M2M transformation where one model is received as input and 
	 * another one is produced.
	 * 
	 * The tool function is flexible: trafo, metamodels and models can be changed.
	 * 
	 * Constraints: the transformation will only execute with the dependencies
	 * included in this project. Additional dependencies required by new transformations
	 * require modifying this function.
	 * 
	 * Parameters:
	 * metamodel: metamodel
	 * returns response with a field outModel: xmi of output model
	 */
	static Object run(String jsonInput) {
		def request = new JsonSlurper().parseText(jsonInput)
		def	response = new JsonBuilder()
		logger.info("Received request: ${request}")

		try {
			String trafoGroovy = StringUtil.removeEscapeChars(request['trafoGroovy'].toString())
			def matcher = (trafoGroovy =~ /(?s).*class\s+(\w+)\s+extends\s+YAMTLModule.*/)
			String className = matcher[0][1]
			if (!className) throw new RuntimeException("The script must contain a YAMTLModule specialization defining a model transformation.")
			
				
			// create tmp file for transformation under the root folder of the project
			Utils.initDirectory(className)
				
				
			GroovyClassLoader classLoader = new GroovyClassLoader();
			Class<?> xformModuleClass = classLoader.parseClass(new GroovyCodeSource(trafoGroovy, ("${className}.groovy"), className));
	
			// running YAMTL using dynamic EMF
			
			// Load metamodels
			def outRes = Utils.loadMetamodel(className, request, "outMetamodel") as Resource
			def outPk = outRes?.contents[0] as EPackage
			
			// Initialise engine
			def xform = xformModuleClass.newInstance(outPk) as YAMTLModule
			YAMTLGroovyExtensions.init(xform)
			
			// Load input model
			def inputContents = StringUtil.removeEscapeChars(request['inModel'].toString())
			

			def inDomainName = xform.getInDomains().find { it.value == 'https://yamtl.github.io/emfsyncer/untypedModel' }.key
			String inModelPath = "${App.TMP_DIR}/${className}/inModel.${Utils.detectFormat(inputContents)}"
			def file = new File(inModelPath)
			file << inputContents
			xform.loadInputModels([(inDomainName):inModelPath])

			// Execute transformations
			xform.execute()

			// Get the output model
			def outDomainName = xform.getOutDomains().find { it.value == outPk.getNsURI() }.key
			String outModelPath = "${App.TMP_DIR}/${className}/outModel.xmi"
			xform.saveOutputModels([(outDomainName):outModelPath])


			// Return the xmi contents
			response {
				generatedText 	new File(outModelPath).text
				output			xform.toStringStats()
//				error 			System.err.toString()
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
