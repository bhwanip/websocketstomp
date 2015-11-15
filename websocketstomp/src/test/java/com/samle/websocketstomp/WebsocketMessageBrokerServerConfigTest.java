package com.samle.websocketstomp;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.RestTemplateXhrTransport;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import com.samle.websocketstomp.client.WebsocketClient;
import com.samle.websocketstomp.consumer.ConsumerStompSessionHandler;
import com.samle.websocketstomp.producer.ProducerStompSessionHandler;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {App.class})
@WebIntegrationTest
public class WebsocketMessageBrokerServerConfigTest  
{
	private final static Logger logger = LoggerFactory.getLogger(WebsocketMessageBrokerServerConfigTest.class);

	public static final Integer USERS_COUNT = 5;


	private int expectedMessageCount = 10;

	private CountDownLatch connectLatch = new CountDownLatch(USERS_COUNT);

	private CountDownLatch subscribeLatch = new CountDownLatch(USERS_COUNT);

	private CountDownLatch messageLatch = new CountDownLatch(USERS_COUNT);

	private CountDownLatch disconnectLatch = new CountDownLatch(USERS_COUNT);

	private AtomicReference<Throwable> failure = new AtomicReference<Throwable>();

	private String wsurl = "ws://{host}:{port}/stomp";

	private String httpurl = "http://{host}:{port}/home";

	private String host = "localhost";

	private int port = 8090;

	/*private final static WebSocketHttpHeaders headers = new WebSocketHttpHeaders();

	private static SockJsClient sockJsClient;

	@BeforeClass
	public static void establishWebSocketSession() {
		List<Transport> transports = new ArrayList<Transport>();
		transports.add(new WebSocketTransport(new StandardWebSocketClient()));
		RestTemplateXhrTransport xhrTransport = new RestTemplateXhrTransport(new RestTemplate());
		xhrTransport.setRequestHeaders(headers);
		transports.add(xhrTransport);
		sockJsClient = new SockJsClient(transports);
	}*/

	@Test
	public void testWS() throws InterruptedException, ExecutionException {
		String[] args = {};
		HttpStatus status = new RestTemplate().getForEntity(httpurl, Void.class, host, port).getStatusCode();
		Assert.state(status == HttpStatus.OK);
		//SpringApplication.run(App.class, args);
		StopWatch stopwatch = new StopWatch("Websocket Stomp Stopwatch");
		stopwatch.start();
		List<ConsumerStompSessionHandler> consumers = new ArrayList<ConsumerStompSessionHandler>();
		for (int i = 0; i < USERS_COUNT; i++) {
			consumers.add(new ConsumerStompSessionHandler(expectedMessageCount, connectLatch, subscribeLatch, 
					messageLatch, disconnectLatch, failure));
			WebsocketClient.getInstance().getStompClient().connect(wsurl, consumers.get(i), host, port).get();
		}
		if(this.failure.get() != null){
			throw new AssertionError("Error : " + this.failure.get());
		}
		if(!connectLatch.await(10, TimeUnit.SECONDS)){
			fail("Not all users are connected, Users left: " + connectLatch.getCount());
		}
		/*if(!subscribeLatch.await(90, TimeUnit.SECONDS)){
			fail("Not all users are subscribed, Users left: " + subscribeLatch.getCount());
		}*/
		stopwatch.stop();
		logger.info("Completed Consumers initialization in: " + stopwatch.getTotalTimeSeconds());

		stopwatch.start();
		ProducerStompSessionHandler producer = new ProducerStompSessionHandler(expectedMessageCount, failure);
		WebsocketClient.getInstance().getStompClient().connect(wsurl, producer, host, port);
		logger.info("Completed Producsers initialization in: " + stopwatch.getTotalTimeSeconds());

		if(!messageLatch.await(60, TimeUnit.SECONDS)){
			fail("Not all users are received messages, Users left: " + messageLatch.getCount());
		}
		if(!disconnectLatch.await(60, TimeUnit.SECONDS)){
			fail("Not all users are disconnected, Users left: " + disconnectLatch.getCount());
		}
	}

}
