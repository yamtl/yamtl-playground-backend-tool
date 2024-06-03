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

		requestBuilder {
			language 'groovy'
			metamodel '@namespace(uri="http://www.example.org/activity_lang", prefix="activity_lang")\npackage activity_lang;\n\n\n@namespace(uri="https://mde-network.com/toolSpec", prefix="t")\npackage ToolSpecification {\n\tclass PanelDefinition {\n\t\tval Button[*] buttons;\n\t}\n\n\tclass Button extends ActivityConfiguration.ButtonOrButtonRef {\n\t}\n\n}\n\n@namespace(uri="https://mde-network.com/activitySpec", prefix="a")\npackage ActivityConfiguration {\n\tabstract class ButtonOrButtonRef {\n\t\tattr EString ~id;\n\t\tattr EString icon;\n\t\tattr EString hint;\n\t\tattr EString internalFunction;\n\t\tref Panel[1] targetPanel;\n\t}\n\n\tclass ButtonRef extends ButtonOrButtonRef {\n\t\tref ToolSpecification.Button[0..1] ~ref;\n\t}\n\n\tclass Panel {\n\t\tattr EString ~id;\n\t\tattr EString name;\n\t\tattr EString file;\n\t\tref ToolSpecification.PanelDefinition[1] ~ref;\n\t\tval ButtonOrButtonRef[*] buttons;\n\t\tattr String refId;\n\t}\n\n\tclass EditorDefinitionPanel extends Panel {\n\t\tref Activity editorActivity;\n\t\tref Panel editorPanel;\n\t}\n\n\tclass ActivityConfiguration {\n\t\tval Activity[*] activities;\n\t}\n\n\tclass CompositePanel extends Panel {\n\t\tval Panel[+] childPanels;\n\t}\n\n\tclass Action {\n\t\tref Panel[1] sourcePanel;\n\t\tref ToolSpecification.Button[1] sourceButton;\n\t\tval Argument[*] arguments;\n\t\tref Panel[1] output;\n\t\tref Panel[0..1] outputConsole;\n\t\tattr EString outputType;\n\t}\n\t\n\tclass LayoutRow {\n\t\tref Panel[+] columns;\n\t}\n\n\tclass ToolURL {\n\t\tattr EString urlPossiblyToRewrite;\n\t}\n\n\tclass Activity {\n\t\tattr EString ~id;\n\t\tattr EString title;\n\t\tattr EString icon;\n\t\tval Panel[*] panels;\n\t\tval LayoutRow[*] layout;\n\t\tval Action[*] actions;\n\t\tattr String[*] tools;\n\t\tval ToolURL[*] toolURLs;\n\t}\n\n\tclass Argument {\n\t\tattr EString key;\n\t\tattr EString value;\n\t}\n\n}\n\n'
			model '<?xml version="1.0" encoding="ISO-8859-1"?>\n<a:ActivityConfiguration xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:a="https://mde-network.com/activitySpec">\n  <activities id="yamtl-cd2db-activity" title="CD2DB" icon="yamtl">\n    <panels id="panel-yamtl" name="Transformation(YAMTL)" refId="yamtl"/>\n    <panels xsi:type="a:CompositePanel" id="smm-panel-composite" name="Source Metamodel" refId="composite-panel">\n      <buttons xsi:type="a:ButtonRef" id="show-editor-button" icon="editor" hint="Toggle editor" internalFunction="toggle" targetPanel="//@activities.0/@panels.1/@childPanels.0"/>\n      <buttons xsi:type="a:ButtonRef" id="show-diagram-button" icon="diagram" hint="Toggle diagram" internalFunction="toggle" targetPanel="//@activities.0/@panels.1/@childPanels.1"/>\n      <childPanels id="panel-smm" name="EMFatic" refId="emfatic"/>\n      <childPanels id="panel-smm-diagram" name="Diagram" refId="plantuml"/>\n    </panels>\n    <panels xsi:type="a:CompositePanel" id="tmm-panel-composite" name="Target Metamodel" refId="composite-panel">\n      <buttons xsi:type="a:ButtonRef" id="show-editor-button" icon="editor" hint="Toggle editor" internalFunction="toggle" targetPanel="//@activities.0/@panels.2/@childPanels.0"/>\n      <buttons xsi:type="a:ButtonRef" id="show-diagram-button" icon="diagram" hint="Toggle diagram" internalFunction="toggle" targetPanel="//@activities.0/@panels.2/@childPanels.1"/>\n      <childPanels id="panel-tmm" name="EMFatic" refId="emfatic"/>\n      <childPanels id="panel-tmm-diagram" name="Diagram" refId="plantuml"/>\n    </panels>\n    <panels xsi:type="a:CompositePanel" id="smodel-panel-composite" name="Source Model" refId="composite-panel">\n      <buttons xsi:type="a:ButtonRef" id="show-editor-button" icon="editor" hint="Toggle editor" internalFunction="toggle" targetPanel="//@activities.0/@panels.3/@childPanels.0"/>\n      <buttons xsi:type="a:ButtonRef" id="show-diagram-button" icon="diagram" hint="Toggle diagram" internalFunction="toggle" targetPanel="//@activities.0/@panels.3/@childPanels.1"/>\n      <childPanels id="panel-smodel" name="XMI" refId="xmi"/>\n      <childPanels id="panel-smodel-diagram" name="Diagram" refId="plantuml"/>\n    </panels>\n    <panels xsi:type="a:CompositePanel" id="tmodel-panel-composite" name="Target Model" refId="composite-panel">\n      <buttons xsi:type="a:ButtonRef" id="show-editor-button" icon="editor" hint="Toggle editor" internalFunction="toggle" targetPanel="//@activities.0/@panels.4/@childPanels.0"/>\n      <buttons xsi:type="a:ButtonRef" id="show-diagram-button" icon="diagram" hint="Toggle diagram" internalFunction="toggle" targetPanel="//@activities.0/@panels.4/@childPanels.1"/>\n      <childPanels id="panel-tmodel" name="XMI" refId="xmi"/>\n      <childPanels id="panel-tmodel-diagram" name="Diagram" refId="plantuml"/>\n    </panels>\n    <panels id="panel-console" name="Console" refId="console"/>\n    <layout columns="//@activities.0/@panels.0 //@activities.0/@panels.1 //@activities.0/@panels.2"/>\n    <layout columns="//@activities.0/@panels.5 //@activities.0/@panels.3 //@activities.0/@panels.4"/>\n    <actions sourcePanel="//@activities.0/@panels.0" output="//@activities.0/@panels.4/@childPanels.0" outputConsole="//@activities.0/@panels.5" outputType="code">\n      <arguments key="trafoGroovy" value="panel-yamtl"/>\n      <arguments key="inModel" value="panel-smodel"/>\n      <arguments key="inMetamodel" value="panel-smm"/>\n      <arguments key="outMetamodel" value="panel-tmm"/>\n    </actions>\n    <actions sourcePanel="//@activities.0/@panels.1/@childPanels.0" output="//@activities.0/@panels.1/@childPanels.1" outputType="puml">\n      <arguments key="metamodel" value="panel-smm"/>\n    </actions>\n    <actions sourcePanel="//@activities.0/@panels.2/@childPanels.0" output="//@activities.0/@panels.2/@childPanels.1" outputType="puml">\n      <arguments key="metamodel" value="panel-tmm"/>\n    </actions>\n    <actions sourcePanel="//@activities.0/@panels.3/@childPanels.0" output="//@activities.0/@panels.3/@childPanels.1" outputType="puml">\n      <arguments key="metamodel" value="panel-smm"/>\n      <arguments key="model" value="panel-smodel"/>\n    </actions>\n    <actions sourcePanel="//@activities.0/@panels.4/@childPanels.0" output="//@activities.0/@panels.4/@childPanels.1" outputType="puml">\n      <arguments key="metamodel" value="panel-tmm"/>\n      <arguments key="model" value="panel-tmodel"/>\n    </actions>\n    <tools>https://yamtl.github.io/playground-interfaces/static.emf/emf-tool.yml</tools>\n    <tools>https://yamtl.github.io/playground-interfaces/static.yamtlgroovy/yamtl-m2m.yml</tools>\n  </activities>\n</a:ActivityConfiguration>'
			query '''
[
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
//			[\n\tcontextType: 'Action',\n\twhere: { true },\n\tquery: { \n\t\tprintln("""\n${it.sourcePanel?.id} |-{${it.sourceButton?.id ?: MISSING}(${\n    it.arguments.collect { it.key + '=' + it.value }.join(', ')\n})}-> ${it.output?.id} [${it.outputConsole?.id ?: ''}]\n""")\n\t}\n]'
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
