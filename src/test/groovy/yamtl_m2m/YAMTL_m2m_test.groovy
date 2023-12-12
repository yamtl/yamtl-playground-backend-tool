package yamtl_m2m

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

class YAMTL_m2m_test {

    @Test
	public void test_cd2db() {
		App app = new App();

		def requestBuilder = new JsonBuilder()
		requestBuilder {
			trafoGroovy  new File('./model/CD2DB.groovy').text
			inMetamodel  new File('./model/CD.emf').text
			inModel      new File('./model/sourceModel.xmi').text
			outMetamodel new File('./model/Relational.emf').text
		}


		println(requestBuilder.toString())

		// Creating a request with a JSON body containing a name
		APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
		requestEvent.withBody(requestBuilder.toString())
				.withResource("/yamtl_m2m");

		APIGatewayProxyResponseEvent result = app.handleRequest(requestEvent, null);

		assertEquals(200, result.getStatusCode().intValue());
		assertEquals("application/json", result.getHeaders().get("Content-Type"));

		String content = result.getBody();
		assertNotNull(content);

		def jsonSlurper = new JsonSlurper()
		def response = jsonSlurper.parseText(content);
		// Check the result
		def expectedOutput = new File('./model/targetModel.xmi').text
		assertEquals(expectedOutput, StringUtil.removeEscapeChars( response['generatedText'].toString() ))
	}
}
