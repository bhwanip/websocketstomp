package com.samle.websocketstomp.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class HelloController {

	@MessageMapping("/hello")
	public String hello(String msg) {
		return new StringBuilder(msg).append(" ").append("World").toString();

	}

}
