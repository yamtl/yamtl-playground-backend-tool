package query

import groovy.lang.GroovyShell

import java.nio.file.Files
import java.nio.file.Paths
import java.util.Map
import java.util.function.Supplier
import org.junit.jupiter.api.Assertions
import prettyprinting.plantuml.EMFPlantUMLSerializer
import java.io.IOException

import org.eclipse.emf.ecore.EcorePackage
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


import Utils.Utils
import Utils.StringUtil

class QueryActivityDslText extends YAMTLModule {
	private static final Logger logger = LoggerFactory.getLogger(QueryActivityDslText.class);
	
	
	/**
	 * 
	 * HELPER CODE
	 */
	def EPackage activityPk
	
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

	static QueryActivityDslText createAndConfigure(String ecorePath, String xmiPath, String contextArgsStr) {
		def activityRes = QueryActivityDslText.preloadMetamodel(ecorePath)
		def activityPk = activityRes.getContents().get(0) as EPackage
		
		def xform = new QueryActivityDslText(activityPk)
		YAMTLGroovyExtensions.init(xform)
		xform.selectedExecutionPhases = ExecutionPhase.MATCH_ONLY

		xform.loadMetamodelResource(activityRes)
		xform.loadInputModels(['activity': xmiPath])
		
		GroovyShell shell = new GroovyShell()
		Map contextArgs = shell.evaluate(contextArgsStr)
		xform.context(contextArgs)
		
		return xform
	}

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
		
		def xform = QueryActivityDslText.createAndConfigure(mmPath, modelPath, queryDef)
		xform.execute()
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

	static Object run(String jsonInput) {
		def outputStreamText = ''
		def errorStreamText = ''

		def request = new JsonSlurper().parseText(jsonInput)
		def	response = new JsonBuilder()
		logger.info("Received request: ${request}")

		outputStreamText = captureOutput {
			try {
				def directory = 'QueryDsl'
				// Load metamodel and model
				def mmPath = Utils.saveMetamodelToFile(directory, request, "metamodel") - "file:"
				def modelPath = Utils.saveMetamodelToFile(directory, request, "model") - "file:"

				// Load query text
				String queryText = StringUtil.removeEscapeChars(request['query'].toString())
				GroovyShell shell = new GroovyShell()
				Map contextArgs = shell.evaluate(queryText)
				
				// Initialise engine
				def xform = QueryActivityDslText.createAndConfigure(mmPath, modelPath, queryText)
				xform.execute()
				

			} catch(Exception e) {
				errorStreamText = e.message
			}
		}
		logger.info("Generated response: ${outputStreamText}");
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

}

