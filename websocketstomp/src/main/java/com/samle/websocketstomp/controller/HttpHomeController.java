package com.samle.websocketstomp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HttpHomeController {

	private static final Logger logger = LoggerFactory.getLogger(HttpHomeController.class);

	@RequestMapping(path = "/home", method = RequestMethod.GET)
	public void hello(){
		logger.info("FOO FOO");
	}
}
