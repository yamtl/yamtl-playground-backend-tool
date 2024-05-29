package query

import api.App

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.junit.Test
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull


import com.google.gson.Gson
import com.google.gson.JsonObject

import Utils.StringUtil

class QueryDsl_test {

    @Test
	public void test_query() {
		App app = new App();

		def requestBuilder = new JsonBuilder()
		requestBuilder {
			metamodel  new File('./model/activity_lang.emf').text
			model      new File('./model/cd2db_activity.xmi').text
			query	   '''[
	contextType: 'Action',
	where: { it.outputType == 'puml' },
	query: { 
		println("""
${it.sourcePanel?.id} |-{${it.sourceButton?.id ?: 'MISSING'}(${
it.arguments.collect { it.key + '=' + it.value }.join(', ')
})}-> ${it.output?.id} [${it.outputConsole?.id ?: ''}]
""")
	}
]'''
		}


		println(requestBuilder.toString())

		// Creating a request with a JSON body containing a name
		APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
		requestEvent.withBody(requestBuilder.toString())
				.withResource("/yamtl_queryDsl");

		APIGatewayProxyResponseEvent result = app.handleRequest(requestEvent, null);

		assertEquals(200, result.getStatusCode().intValue());
		assertEquals("application/json", result.getHeaders().get("Content-Type"));

		String content = result.getBody();
		assertNotNull(content);

		def jsonSlurper = new JsonSlurper()
		def response = jsonSlurper.parseText(content);
		println("""RESPONSE ----------------------------------------
${response}
""")
		
		// Check the result
		def expectedOutput = '''
panel-smm |-{MISSING(metamodel=panel-smm)}-> panel-smm-diagram []

panel-tmm |-{MISSING(metamodel=panel-tmm)}-> panel-tmm-diagram []

panel-smodel |-{MISSING(metamodel=panel-smm, model=panel-smodel)}-> panel-smodel-diagram []

panel-tmodel |-{MISSING(metamodel=panel-tmm, model=panel-tmodel)}-> panel-tmodel-diagram []
'''
		assertEquals(
			normalize(expectedOutput), 
			normalize(StringUtil.removeEscapeChars( response['output'].toString() ))
		)
	}

	def static normalize(String text) {
		text.replaceAll('\r\n', '').replaceAll('\n', '').replaceAll(' ', '')
	}
}
