package com.samle.websocketstomp.consumer;

import java.lang.reflect.Type;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.ConnectionLostException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

public class ConsumerStompSessionHandler extends StompSessionHandlerAdapter {

	private static final Logger logger = LoggerFactory.getLogger(ConsumerStompSessionHandler.class);

	private final int expectedMessageCount;
	private final CountDownLatch connectLatch;
	private final CountDownLatch subscribeLatch;
	private final CountDownLatch messageLatch;
	private final CountDownLatch disconnectLatch;
	private final AtomicReference<Throwable> failure;
	private AtomicInteger messageCount = new AtomicInteger(0);


	public ConsumerStompSessionHandler(int expectedMessageCount,
			CountDownLatch connectLatch, CountDownLatch subscribeLatch,
			CountDownLatch messageLatch, CountDownLatch disconnectLatch,
			AtomicReference<Throwable> failure) {
		super();
		this.expectedMessageCount = expectedMessageCount;
		this.connectLatch = connectLatch;
		this.subscribeLatch = subscribeLatch;
		this.messageLatch = messageLatch;
		this.disconnectLatch = disconnectLatch;
		this.failure = failure;
	}

	@Override
	public void afterConnected(final StompSession session, StompHeaders connectedHeaders) {
		this.connectLatch.countDown();
		session.setAutoReceipt(true);
		session.subscribe("/topic/hello", new StompFrameHandler() {

			public void handleFrame(StompHeaders arg0, Object arg1) {
				logger.info("Received-->" + arg1.toString() + " For Session: " + session.getSessionId());
				if(messageCount.incrementAndGet() == expectedMessageCount){
					messageLatch.countDown();
					disconnectLatch.countDown();
					session.disconnect();
				}

			}

			public Type getPayloadType(StompHeaders arg0) {
				return String.class;
			}
		}).addReceiptTask(new Runnable() {

			public void run() {
				logger.info("Got Receipt");
				subscribeLatch.countDown();
			}
		});
	}


	@Override
	public void handleTransportError(StompSession session, Throwable exception) {
		exception.printStackTrace();
		logger.error("Received Transport error : "  + exception);
		this.failure.set(exception);
		if(exception instanceof ConnectionLostException){
			this.disconnectLatch.countDown();
		}
	}

	@Override
	public void handleException(StompSession session, StompCommand command,
			StompHeaders headers, byte[] payload, Throwable exception) {
		logger.error("Handle error : "  + exception);
		this.failure.set(exception);
	}

	@Override
	public void handleFrame(StompHeaders headers, Object payload) {
		Exception exception = new Exception(headers.toString());
		logger.error("Received Error Frame : "  + exception);
		this.failure.set(exception);
	}



}
