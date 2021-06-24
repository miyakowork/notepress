package me.wuwenbin.notepress.springboot.container.config.ssl;

import org.springframework.context.annotation.Configuration;

/**
 * 不推荐直接使用内置tomcat进行ssl部署，所以此类废弃
 * created by Wuwenbin on 2019-01-30 at 14:48
 *
 * @author wuwenbin
 */
@Deprecated
@Configuration
public class SslConfig {
/*
    private static final String SSL_ENABLED = "server.ssl.enabled";
    private static final String SERVER_HTTP_PORT = "server.http.port";
    private static final String SERVER_HTTPS_PORT = "server.port";
    private final Environment environment;


    @Autowired
    public SslConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public TomcatServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                if (environment.getProperty(SSL_ENABLED, Boolean.class, Boolean.FALSE)) {
                    SecurityConstraint constraint = new SecurityConstraint();
                    constraint.setUserConstraint("CONFIDENTIAL");
                    SecurityCollection collection = new SecurityCollection();
                    collection.addPattern("/*");
                    constraint.addCollection(collection);
                    context.addConstraint(constraint);
                } else {
                    super.postProcessContext(context);
                }
            }
        };
        if (environment.getProperty(SSL_ENABLED, Boolean.class, Boolean.FALSE)) {
            tomcat.addAdditionalTomcatConnectors(httpConnector());
        }
        return tomcat;
    }


    @Bean
    public Connector httpConnector() {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        //Connector监听的http的端口号
        connector.setPort(environment.getProperty(SERVER_HTTP_PORT, Integer.class, 80));
        connector.setSecure(false);
        //监听到http的端口号后转向到的https的端口号
        connector.setRedirectPort(environment.getProperty(SERVER_HTTPS_PORT, Integer.class, 443));
        return connector;
    }*/
}
