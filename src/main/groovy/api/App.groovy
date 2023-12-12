package api

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import groovy.json.JsonSlurper
import prettyprinter.Ecore2ClassDiagramPlantUML
import prettyprinter.Xmi2ObjectDiagram
import prettyprinter.Xmi2ObjectDiagramPlantUML
import yamtl_m2m.RunYAMTL_m2m_groovy
import prettyprinter.Ecore2ClassDiagram

class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    def APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        def message
        def resourcePath = input.getResource()
        switch (resourcePath) {

            case '/yamtl_m2m':
                message = RunYAMTL_m2m_groovy.run(input.getBody())
                break
            case '/ecore2cd':
                message = Ecore2ClassDiagram.run(input.getBody())
                break
            case '/ecore2cd_puml':
                message = Ecore2ClassDiagramPlantUML.run(input.getBody())
                break
            case '/xmi2od':
                message = Xmi2ObjectDiagram.run(input.getBody())
                break
            case '/xmi2od_puml':
                message = Xmi2ObjectDiagramPlantUML.run(input.getBody())
                break

                /**
             * SMOKE TESTS
             */
            case '/helloName':
                def params = new JsonSlurper().parseText(input.getBody())
                message = [message: "Hello, ${params.name}"]
                break
            case '/location':
                try {
                    def location = new URL('https://checkip.amazonaws.com').text
                    message = [location: location]
                } catch (Exception e) {
                    return generateResponse(500, [error: e.message])
                }
                break
            case '/dot':
                def process = "dot -V".execute()
                process.waitFor()
                message = process.err.text

                if (!message)
                    return generateResponse(500, [error: "dot is not installed"])

                break

            default:
                try {
                    def textMessage = "The endpoint ${input.getResource()} was not found"
                    def location = new URL('https://checkip.amazonaws.com').text
                    message = [
                            message: textMessage,
                            location: location,
                            path: input.getResource()]
                } catch (Exception e) {
                    return generateResponse(500, [error: e.message])
                }
        }
        return generateResponse(200, message)
    }

    private APIGatewayProxyResponseEvent generateResponse(int statusCode, def body) {
        def headers = [
                'Content-Type': 'application/json',
                'X-Custom-Header': 'application/json',
                // to enable CORS
//                'Access-Control-Allow-Origin': 'https://yamtl.github.io',
                'Access-Control-Allow-Origin': '*', // for all or specify the origin you want to allow
                'Access-Control-Allow-Headers':	'Content-Type',
                'Access-Control-Allow-Methods':	'OPTIONS,POST,GET' // Allowed methods
        ]

        new APIGatewayProxyResponseEvent().with {
            setHeaders(headers)
            setStatusCode(statusCode)
            setBody(body as String)
            return it
        }
    }

    def callDot() {


        return JsonOutput.toJson(response)
    }
}
