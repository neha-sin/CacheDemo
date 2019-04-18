/**
 *  Copyright 2012-2018 Solace Corporation. All rights reserved.
 *
 *  http://www.solace.com
 *
 *  This source is distributed under the terms and conditions
 *  of any contract or contracts between Solace and you or
 *  your company. If there are no contracts in place use of
 *  this source is not authorized. No support is provided and
 *  no distribution, sharing with others or re-use of this
 *  source is authorized unless specifically stated in the
 *  contracts referred to above.
 *
 *  HelloWorldPub
 *
 *  This sample shows the basics of creating session, connecting a session,
 *  and publishing a direct message to a topic. This is meant to be a very
 *  basic example for demonstration purposes.
 */

package com.solace.samples.projects;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.JCSMPStreamingPublishEventHandler;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageProducer;

public class NSEPublishFileToSolace {
	
    static String textFileName = "";
    
    /**
     * @param args
     * @throws JCSMPException
     * @throws InterruptedException
     * @throws IOException
     */
    public static void main(String... args) throws JCSMPException, InterruptedException, IOException {
    	// Check command line arguments
        if (args.length < 4) {
            System.out.println("Usage: NSEPublishFileToSolace <msg_backbone_ip:port> <vpn> <client-username> <client-password> <Inputfile>");
            System.exit(-1);
        }
        System.out.println("NSEPublishFileToSolace initializing...");

    	// Create a JCSMP Session
        final JCSMPProperties properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.HOST, args[0]);      // msg-backbone ip:port
        properties.setProperty(JCSMPProperties.VPN_NAME, args[1]);  // message-vpn
        properties.setProperty(JCSMPProperties.USERNAME, args[2]);  // client-username
        properties.setProperty(JCSMPProperties.PASSWORD, args[3]);  // client-password
        final JCSMPSession session =  JCSMPFactory.onlyInstance().createSession(properties);
        
        final Topic topic = JCSMPFactory.onlyInstance().createTopic("nse/stock/put");
        
        session.connect();
        /** Anonymous inner-class for handling publishing events */
        XMLMessageProducer prod = session.getMessageProducer(new JCSMPStreamingPublishEventHandler() {
            public void responseReceived(String messageID) {
                System.out.println("Producer received response for msg: " + messageID);
            }
            public void handleError(String messageID, JCSMPException e, long timestamp) {
                System.out.printf("Producer received error for msg: %s@%s - %s%n",
                        messageID,timestamp,e);
            }
        });

        
        String filename = args[4];
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filename), StandardCharsets.UTF_8)) {
            textFileName = filename;
            for (String line = null; (line = br.readLine()) != null;) {
            	System.out.println(line.toString());
                TextMessage msg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
                msg.setText(line.toString());
                prod.send(msg,topic);
            }
        }
        System.out.println("text file done!");
        System.out.println("Text File Messages sent. Exiting.");
        session.closeSession();
    }
}
