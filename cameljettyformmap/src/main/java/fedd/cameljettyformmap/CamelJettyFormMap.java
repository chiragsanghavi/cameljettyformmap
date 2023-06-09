/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package fedd.cameljettyformmap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.common.HttpMessage;
import org.apache.camel.impl.DefaultCamelContext;

/**
 *
 * @author fedd
 */
public class CamelJettyFormMap {

    public static void main(String[] args) throws Exception {

        // starting camel
        CamelContext ctx = new DefaultCamelContext();

        ObjectMapper mapper = new JsonMapper();

        ctx.addRoutes(new RouteBuilder() {

            @Override
            public void configure() throws Exception {

                // a simple home grown dispatcher for the catch-all uri path
                Processor dispatcher = new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        HttpMessage http = exchange.getIn(HttpMessage.class);
                        HttpServletRequest request = http.getRequest();
                        String method = request.getMethod();
                        if ("POST".equals(method) || "PUT".equals(method)) {
                            Map map = http.getBody(Map.class);
                            String string = http.getBody(String.class);
                            http.setHeader(Exchange.CONTENT_TYPE, "text/plain");
                            http.setBody("json: " + mapper.writeValueAsString(map) + ", and string:" + string);
                        } else {
                            http.setHeader(Exchange.CONTENT_TYPE, "text/html");
                            http.setBody(this.getClass().getResourceAsStream("form.html"));
                        }
                    }
                };

                from("jetty:http://0.0.0.0:8080?matchOnUriPrefix=true&enableMultipartFilter=true")
                        .process(dispatcher);

            }
        });

        ctx.start();

    }
}
