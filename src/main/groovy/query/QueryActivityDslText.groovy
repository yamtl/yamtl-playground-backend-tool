package query

import groovy.lang.GroovyShell

import java.nio.file.Files
import java.nio.file.Paths
import java.util.Map
import java.util.function.Supplier
import org.junit.jupiter.api.Assertions
import java.io.IOException

import org.eclipse.emf.ecore.EcorePackage
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EDataType
import org.eclipse.emf.ecore.EOperation
import org.eclipse.emf.ecore.EPackage
import static org.junit.Assert.assertTrue

import yamtl.core.YAMTLModule
import yamtl.core.YAMTLModule.ExecutionPhase
import untypedModel.ERecord
import untypedModel.UntypedModelPackage
import untypedModel.impl.ERecordImpl

import static yamtl.dsl.Rule.*
import static yamtl.dsl.Helper.*

import prettyprinting.EMFPrettyPrinter

import yamtl.groovy.YAMTLGroovyExtensions
import yamtl.groovy.YAMTLGroovyExtensions_dynamicEMF

import groovy.lang.GroovyShell
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import Utils.Utils
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import api.App

import Utils.Utils
import Utils.StringUtil

class QueryActivityDslText extends YAMTLModule {
	private static final Logger logger = LoggerFactory.getLogger(QueryActivityDslText.class);
	

	def EPackage activityPk
	def static directory = 'QueryDsl'

	/**
	 * 
	 * Function that executes a query over a metamodel-based model.
	 * 
	 * The tool function is flexible: query, metamodel and model can be changed.
	 * 
	 * Constraints: the query will only execute with the dependencies
	 * included in this project. Additional dependencies required by new queries
	 * require modifying this function.
	 * 
	 * Parameters:
	 * metamodel: metamodel
	 * model: model
	 * query: text for the query
	 * returns response in the field output
	 */
	static Object run(String jsonInput) {
		def outputStreamText = ''
		def errorStreamText = ''

		def request = new JsonSlurper().parseText(jsonInput)
		def	response = new JsonBuilder()
		logger.info("Received request: ${request}")


		// Load metamodel and model
		def mmPath = Utils.saveMetamodelToFile(directory, request, "metamodel") - "file:"

		// store model
		String modelPath = "${App.TMP_DIR}/${directory}/model.xmi"
		def file = new File(modelPath)
		if (file.exists()) file.delete()
		file << StringUtil.removeEscapeChars(request['model'].toString())


		logger.info("""METAMODEL: ${new File(mmPath).text}""")
		logger.info("""MODEL: ${new File(modelPath).text}""")


		// Load query text
		String queryText = StringUtil.removeEscapeChars(request['query'].toString())

		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		GroovyShell shell = new GroovyShell(contextClassLoader);
		logger.info("GroovyShell ClassLoader: " + contextClassLoader);
		Map contextArgs = shell.evaluate(queryText)

		// Initialise engine
		def xform = QueryActivityDslText.createAndConfigure(mmPath, modelPath, contextArgs)
		logger.info("xform ClassLoader: " + xform.getClass().getClassLoader());


		outputStreamText = captureOutput {
			try {				
				xform.execute()
			} catch(Exception e) {
				errorStreamText = e.message
			}
		}

		
		
		
		inspectTmpDirectory()

		logger.info("outputStreamText: ${outputStreamText}");
		logger.info("errorStreamText: ${errorStreamText}");
		if (errorStreamText) {
			response {
				output 	errorStreamText
			}
		} else {
			response {
				output	outputStreamText
			}
		}
		logger.info("Generated response: ${response}");


		return response
    }



	public QueryActivityDslText(EPackage activityPk) {
		YAMTLGroovyExtensions_dynamicEMF.init(this)
		this.activityPk=activityPk
		header().in('activity', activityPk)
	}
	
	void context(Map args) {
		def contextType = args.contextType
		def where = args.where
		def query = args.query
		
		ruleStore([
			rule('Query')
				.in('self', YAMTLGroovyExtensions_dynamicEMF.findEClass(activityPk, contextType))
				.filter({ where.call(self) })
				.query()
				.endWith({ query.call(self) })
		])
	}

	static QueryActivityDslText createAndConfigure(String ecorePath, String xmiPath, Map contextArgs) {
		def activityRes = QueryActivityDslText.preloadMetamodel(ecorePath)
		def activityPk = activityRes.getContents().get(0) as EPackage
		
		def xform = new QueryActivityDslText(activityPk)
		xform.selectedExecutionPhases = ExecutionPhase.MATCH_ONLY
		xform.context(contextArgs)
		YAMTLGroovyExtensions.init(xform)
		
		xform.loadMetamodelResource(activityRes)
		xform.loadInputModels(['activity': xmiPath])

		return xform
	}

    static void inspectTmpDirectory() {
        def tmpDir = new File("${App.TMP_DIR}/${directory}")
        tmpDir.eachFile { file ->
            logger.info("File in /tmp: ${file.name}, Size: ${file.length()}")
        }
    }



	def static captureOutput(Closure closure) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream()
		PrintStream ps = new PrintStream(baos)
		PrintStream oldOut = System.out

		try {
			System.setOut(ps)
			closure.call()
		} finally {
			System.setOut(oldOut)
			ps.close()
		}

		return baos.toString("UTF-8")
	}


	static void main(String[] args) {
		def mmPath = './model-test/education_platform/activity_lang.ecore'
		def modelPath = './model-test/education_platform/cd2db_activity.xmi'
		
		def queryDef = '''[
			contextType: 'Action',
			where: { /* it.outputType == 'puml' */ },
			query: { 
				println("""
${it.sourcePanel?.id} |-{${it.sourceButton?.id ?: 'MISSING'}(${
    it.arguments.collect { it.key + '=' + it.value }.join(', ')
})}-> ${it.output?.id} [${it.outputConsole?.id ?: ''}]
""")
			}
		]'''

		GroovyShell shell = new GroovyShell()
		Map contextArgs = shell.evaluate(queryDef)
		def xform = QueryActivityDslText.createAndConfigure(mmPath, modelPath, contextArgs)
		xform.execute()

		def resModel = xform.getModelResource('activity')
		def activityContainer = resModel.contents[0]
		println(activityContainer.activities.collect{it.actions.collect{it.outputType}})
	}

}

