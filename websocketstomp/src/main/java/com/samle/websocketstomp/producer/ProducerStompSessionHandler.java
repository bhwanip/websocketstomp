package com.samle.websocketstomp.producer;

import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

public class ProducerStompSessionHandler extends StompSessionHandlerAdapter {

	private static final Logger logger = LoggerFactory.getLogger(ProducerStompSessionHandler.class);

	private final int broadCastMessageCount;
	private AtomicReference<Throwable> failure = new AtomicReference<Throwable>();

	public ProducerStompSessionHandler(int broadCastMessageCount, AtomicReference<Throwable> failure) {
		super();
		this.broadCastMessageCount = broadCastMessageCount;
		this.failure = failure;
	}

	@Override
	public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
		try {
			for (int i = 0; i < broadCastMessageCount; i++) {
				session.send("/app/hello", "Hello " + i);
			}
		} catch (Exception e) {
			logger.error("Error : " + e);
			this.failure.set(e);
		}
	}

	@Override
	public void handleException(StompSession session, StompCommand command,
			StompHeaders headers, byte[] payload, Throwable exception) {
		logger.error("Handling Exception : " + exception);
		this.failure.set(exception);
	}

	@Override
	public void handleTransportError(StompSession session, Throwable exception) {
		logger.error("Handling Transport Error : " + exception);
		this.failure.set(exception);
	}

	@Override
	public void handleFrame(StompHeaders headers, Object payload) {
		Exception exception = new Exception(headers.toString());
		logger.error("Handling Frame Error : " + exception);
		this.failure.set(exception);
	}


}
