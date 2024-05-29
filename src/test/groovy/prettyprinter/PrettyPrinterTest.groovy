package prettyprinter

import api.App
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.junit.Test
import Utils.StringUtil

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue

class PrettyPrinterTest {
    // @Test
    public void test_mm2cd() {
        App app = new App();

        def requestBuilder = new JsonBuilder()
        requestBuilder {
            metamodel  new File('./model/CD.emf').text
        }

        // Creating a request with a JSON body containing a name
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.withBody(requestBuilder.toString())
                .withResource("/ecore2cd");

        APIGatewayProxyResponseEvent result = app.handleRequest(requestEvent, null);


        println(result)

        assertEquals(200, result.getStatusCode().intValue());
        assertEquals("application/json", result.getHeaders().get("Content-Type"));


        String content = result.getBody();
        assertNotNull(content);

        def jsonSlurper = new JsonSlurper()
        def response = jsonSlurper.parseText(content);
        // Check the result
        def expectedOutput = new File('./model/cd.svg').text
        assertEquals(expectedOutput, StringUtil.removeEscapeChars( response['diagram'].toString() ))
    }

    @Test
    public void test_mm2cd_puml() {
        App app = new App();

        def requestBuilder = new JsonBuilder()
        requestBuilder {
            metamodel  new File('./model/CD.emf').text
        }

        // Creating a request with a JSON body containing a name
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.withBody(requestBuilder.toString())
                .withResource("/ecore2cd_puml");

        APIGatewayProxyResponseEvent result = app.handleRequest(requestEvent, null);


        println(result)

        assertEquals(200, result.getStatusCode().intValue());
        assertEquals("application/json", result.getHeaders().get("Content-Type"));


        String content = result.getBody();
        assertNotNull(content);

        def jsonSlurper = new JsonSlurper()
        def response = jsonSlurper.parseText(content);
        // Check the result
        def expectedOutput = new File('./model/cd.puml').text
        assertEquals(normalizePUML(expectedOutput), normalizePUML( response['generatedText'].toString() ))
    }


    @Test
    public void test_emf2json() {
        App app = new App();

        def requestBuilder = new JsonBuilder()
        requestBuilder {
            metamodel  new File('./model/CD.emf').text
        }

        // Creating a request with a JSON body containing a name
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.withBody(requestBuilder.toString())
                .withResource("/emf2json");

        APIGatewayProxyResponseEvent result = app.handleRequest(requestEvent, null);


        assertEquals(200, result.getStatusCode().intValue());
        assertEquals("application/json", result.getHeaders().get("Content-Type"));


        String content = result.getBody();
        assertNotNull(content);

        def jsonSlurper = new JsonSlurper()
        def response = jsonSlurper.parseText(content);
        // Check the result
        def expectedOutput = new File('./model/CD.json').text
        assertEquals(normalizePUML(expectedOutput), normalizePUML(response['generatedText'].toString()) )
    }











    // @Test
    public void test_model2od() {
        App app = new App();

        def requestBuilder = new JsonBuilder()
        requestBuilder {
            metamodel  new File('./model/CD.emf').text
            model new File('./model/sourceModel.xmi').text
        }

        // Creating a request with a JSON body containing a name
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.withBody(requestBuilder.toString())
                .withResource("/xmi2od");

        APIGatewayProxyResponseEvent result = app.handleRequest(requestEvent, null);


        println(result)

        assertEquals(200, result.getStatusCode().intValue());
        assertEquals("application/json", result.getHeaders().get("Content-Type"));


        String content = result.getBody();
        assertNotNull(content);

        // Check the result
        def jsonSlurper = new JsonSlurper()
        def response = jsonSlurper.parseText(content);
        assertTrue(response['diagram'].contains('<svg xmlns="http://www.w3.org/2000/svg'))
        assertTrue(response['diagram'].contains('"Order"'))
        assertTrue(response['diagram'].contains('"Customer"'))
        assertTrue(response['diagram'].contains('"Item"'))
        assertTrue(response['diagram'].contains('"product"'))
        assertTrue(response['diagram'].contains('"date"'))
        assertTrue(response['diagram'].contains('"items"'))
    }


//    @Test
    public void test_model2od_puml() {
        App app = new App();

        def requestBuilder = new JsonBuilder()
        requestBuilder {
            metamodel  new File('./model/CD.emf').text
            model new File('./model/sourceModel.xmi').text
        }

        // Creating a request with a JSON body containing a name
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.withBody(requestBuilder.toString())
                .withResource("/xmi2od_puml");

        APIGatewayProxyResponseEvent result = app.handleRequest(requestEvent, null);


        println(result)

        assertEquals(200, result.getStatusCode().intValue());
        assertEquals("application/json", result.getHeaders().get("Content-Type"));


        String content = result.getBody();
        assertNotNull(content);

        // Check the result
        def jsonSlurper = new JsonSlurper()
        def response = jsonSlurper.parseText(content);
        def expectedOutput = new File('./model/sourceModel.puml').text

        println(response['generatedText'].toString())
        println(normalizePUML(response['generatedText'].toString()))

        assertEquals(normalizePUML(expectedOutput), normalizePUML( response['generatedText'].toString() ))

    }

    def static normalizePUML(text) {

        text = text.replaceAll(/\b\d+\b/, ' ')
        // Remove white spaces and line breaks
        text = text.replaceAll(/\s+/, '').replaceAll(/\r\n/,'').replaceAll(/\n/,'')

        // Remove dynamic attributes (e.g., timestamps)
//        text = text.replaceAll(/(timestamp|version)="[^"]*"/, '')


        return text
    }

    def static normalizeSVG(svg) {
        // Remove white spaces and line breaks
        svg = svg.replaceAll(/\s+/, ' ')

        // Remove dynamic attributes (e.g., timestamps)
        svg = svg.replaceAll(/(timestamp|version)="[^"]*"/, '')

        return svg
    }

}
