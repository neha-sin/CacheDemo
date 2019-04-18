/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.solace.samples.projects;

import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.ConsumerFlowProperties;
import com.solacesystems.jcsmp.EndpointProperties;
import com.solacesystems.jcsmp.FlowReceiver;
import com.solacesystems.jcsmp.InvalidPropertiesException;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPRequestTimeoutException;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.JCSMPStreamingPublishEventHandler;
import com.solacesystems.jcsmp.Queue;
import com.solacesystems.jcsmp.Requestor;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageConsumer;
import com.solacesystems.jcsmp.XMLMessageListener;
import com.solacesystems.jcsmp.XMLMessageProducer;

public class NSECacheRequestor {
    private static int startId;
    private static int endId;
    private static JCSMPSession session;


    public static void initializeSession(String... args) throws JCSMPException {
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
        
        startId = Integer.parseInt(args[4]);
        endId = Integer.parseInt(args[5]);
        System.out.println("MessageId Range:[" + startId + ", " + endId + "]");

        session = JCSMPFactory.onlyInstance().createSession(properties);
        session.connect();

    }

    private static void initializeConsumer(Queue replyTo) throws JCSMPException {
                // Create a Flow be able to bind to and consume messages from the Queue.
                final ConsumerFlowProperties flow_prop = new ConsumerFlowProperties();
                flow_prop.setEndpoint(replyTo);
                flow_prop.setAckMode(JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);
        
                EndpointProperties endpoint_props = new EndpointProperties();
                endpoint_props.setAccessType(EndpointProperties.ACCESSTYPE_EXCLUSIVE);
        
                final FlowReceiver cons = session.createFlow(new XMLMessageListener() {
                    @Override
                    public void onReceive(BytesXMLMessage msg) {
                        if (msg instanceof TextMessage) {
                            System.out.printf("TextMessage: '%s'%n", ((TextMessage) msg).getText());
                        } else {
                            System.out.println("Message received but can't parse");
                        }
                        msg.ackMessage();
                    }
        
                    @Override
                    public void onException(JCSMPException e) {
                        System.out.printf("Consumer received exception: %s%n", e);
                    }
                }, flow_prop, endpoint_props);
        
                // Start the consumer        
                cons.start();
        
    }

    public static void initializeProducer(String publishTopic, Queue replyTo) throws JCSMPException {
        final Topic topic = JCSMPFactory.onlyInstance().createTopic(publishTopic);

        // Time to wait for a reply before timing out
        final int timeoutMs = 10000;
        TextMessage request = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        final String text = startId + ":" + endId;
        request.setText(text);
        request.setReplyTo(replyTo);

        XMLMessageProducer prod = session.getMessageProducer(new JCSMPStreamingPublishEventHandler() {
            @Override
            public void responseReceived(String messageID) {
                System.out.println("Producer received response for msg: " + messageID);

            }
            @Override
            public void handleError(String messageID, JCSMPException e, long timestamp) {
                System.out.printf("Producer received error for msg: %s@%s - %s%n",
                        messageID,timestamp,e);
            }
        });

        prod.send(request,topic);

    }

    public static void main(String... args) throws JCSMPException, InterruptedException {

        initializeSession(args);

        Queue replyTo = session.createTemporaryQueue();
        initializeConsumer(replyTo);
        initializeProducer("nse/cache/requests", replyTo);
        Thread.sleep(60000);
        System.out.println("Exiting...");
        session.closeSession();
    }
}
