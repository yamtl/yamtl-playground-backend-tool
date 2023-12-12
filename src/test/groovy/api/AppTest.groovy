package api

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import org.junit.Test

class AppTest {

    @Test
    void test_get_request() {
        def app = new App()
        def requestEvent = new APIGatewayProxyRequestEvent(resource: "/hello")
        def result = app.handleRequest(requestEvent, null)

        assert result.getStatusCode() == 200
        assert result.getHeaders().get("Content-Type") == "application/json"

        def content = result.getBody()
        assert content
        assert content.contains('message')
        assert content.contains('The endpoint /hello was not found')
        assert content.contains('location')
    }

    @Test
    void test_get_dot() {
        def app = new App()
        def requestEvent = new APIGatewayProxyRequestEvent(resource: "/dot")
        def result = app.handleRequest(requestEvent, null)

        assert result.getStatusCode() == 200
        assert result.getHeaders().get("Content-Type") == "application/json"

        def content = result.getBody()
        assert content.startsWith('dot - graphviz version')
    }

    @Test
    void test_post_request() {
        def app = new App()

        // Creating a request with a JSON body containing a name
        def requestEvent = new APIGatewayProxyRequestEvent(
                body: '{"name":"John"}',
                resource: "/helloName"
        )

        def result = app.handleRequest(requestEvent, null)

        assert result.getStatusCode() == 200
        assert result.getHeaders().get("Content-Type") == "application/json"

        def content = result.getBody()
        assert content
        assert content.contains('message')
        assert content.contains('Hello, John')
    }

}
