package yamtl_m2m

import api.App

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.junit.Test
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull
import Utils.StringUtil

import com.google.gson.Gson
import com.google.gson.JsonObject

class YAMTL_m2m_from_flexible_to_mm_test
 {

    @Test
	public void test_cd2db() {
		App app = new App();

		def requestBuilder = new JsonBuilder()
		requestBuilder {
			trafoGroovy  new File('./model/ActivityLoad.groovy').text
			inModel      new File('./model/cd2db_activity.yml').text
			outMetamodel  new File('./model/activity_lang.emf').text
		}


		println(requestBuilder.toString())

		// Creating a request with a JSON body containing a name
		APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
		requestEvent.withBody(requestBuilder.toString())
				.withResource("/yamtl_m2m_one_mm");

		APIGatewayProxyResponseEvent result = app.handleRequest(requestEvent, null);

		assertEquals(200, result.getStatusCode().intValue());
		assertEquals("application/json", result.getHeaders().get("Content-Type"));

		String content = result.getBody();
		assertNotNull(content);
		println(content);

		def jsonSlurper = new JsonSlurper()
		def response = jsonSlurper.parseText(content);
		// Check the result
		def expectedOutput = new File('./model/cd2db_activity.xmi').text
		assertEquals(normalize(expectedOutput), normalize(StringUtil.removeEscapeChars( response['generatedText'].toString()) ))
	}

	  def static normalize(text) {

        text = text.replaceAll(/\s+/, '').replaceAll(/\r\n/,'').replaceAll(/\n/,'')
        return text
    }

}
