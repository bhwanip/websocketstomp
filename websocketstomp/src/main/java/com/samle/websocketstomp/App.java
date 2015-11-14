package com.samle.websocketstomp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App implements CommandLineRunner 
{
	private final static Logger logger = LoggerFactory.getLogger(App.class);
	
    public static void main( String[] args )
    {
    	SpringApplication.run(App.class, args);
    }

	public void run(String... arg0) throws Exception {
		logger.info("You can press ctl+c for shutdown.");
		Thread.currentThread().join();
	}
}
