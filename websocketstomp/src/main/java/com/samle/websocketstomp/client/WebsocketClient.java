package com.samle.websocketstomp.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.RestTemplateXhrTransport;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

public class WebsocketClient {

	private volatile static WebsocketClient client;

	private final WebSocketStompClient stompClient;

	private WebsocketClient(WebSocketStompClient stompClient){
		this.stompClient = stompClient;
	}

	public static WebsocketClient getInstance(){
		if(client == null){
			synchronized (WebsocketClient.class) {
				if(client == null){
					client = createClient();
				} 
			}
		}
		return client;
	}
	

	public WebSocketStompClient getStompClient() {
		return stompClient;
	}

	private static WebsocketClient createClient() {
		List<Transport> transports = new ArrayList<Transport>();
		transports.add(new WebSocketTransport(new StandardWebSocketClient()));
		RestTemplateXhrTransport xhrTransport = new RestTemplateXhrTransport(new RestTemplate());
		xhrTransport.setRequestHeaders(new WebSocketHttpHeaders());
		transports.add(xhrTransport);
		SockJsClient sockJsClient = new SockJsClient(transports);
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.afterPropertiesSet();
		WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
		//stompClient.setReceiptTimeLimit(60000);
		stompClient.setMessageConverter(new StringMessageConverter());
		stompClient.setTaskScheduler(scheduler);
		stompClient.setDefaultHeartbeat(new long[] {0,0});
		return new WebsocketClient(stompClient);
	}

}
