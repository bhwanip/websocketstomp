package com.samle.websocketstomp.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.springframework.web.socket.server.standard.TomcatRequestUpgradeStrategy;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Configuration
@EnableWebSocketMessageBroker
public class WebsocketMessageBrokerServerConfig extends AbstractWebSocketMessageBrokerConfigurer {

	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// The test classpath includes both Tomcat and Jetty, so let's be explicit
		DefaultHandshakeHandler handler = new DefaultHandshakeHandler(new TomcatRequestUpgradeStrategy());
		registry.addEndpoint("/stomp").setHandshakeHandler(handler).withSockJS()/*.setStreamBytesLimit(512 * 1024)
		.setHttpMessageCacheSize(1000)
		.setDisconnectDelay(300 * 1000)*/;
		//.setHeartbeatTime(0);
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/topic");
		registry.setApplicationDestinationPrefixes("/app");
	}

	/*@Override
	public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
		registration.setSendTimeLimit(30*1000);
	}*/
	
	@Override
	public void configureClientOutboundChannel(ChannelRegistration registration) {
		registration.taskExecutor().corePoolSize(50).queueCapacity(100);
	}
	
	@Bean
	public ServletServerContainerFactoryBean createWebSocketContainer() {
		ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
		container.setMaxTextMessageBufferSize(8192);
		container.setMaxBinaryMessageBufferSize(8192);
		container.setMaxSessionIdleTimeout(300 * 1000);
		return container;
	}


}
