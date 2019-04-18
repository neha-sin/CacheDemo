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

import java.io.IOException;

import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.JCSMPStreamingPublishEventHandler;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageConsumer;
import com.solacesystems.jcsmp.XMLMessageListener;
import com.solacesystems.jcsmp.XMLMessageProducer;

import com.mongodb.client.*;
import com.mongodb.ServerAddress;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.Block;

import static com.mongodb.client.model.Filters.*;


public class NSECacheQueryReplier {

    public void run(String... args) throws JCSMPException {
        System.out.println("NSECacheQueryReplier initializing...");
        final JCSMPProperties properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.HOST, args[0]);      // msg-backbone ip:port
        properties.setProperty(JCSMPProperties.VPN_NAME, args[1]);  // message-vpn
        properties.setProperty(JCSMPProperties.USERNAME, args[2]);  // client-username
        properties.setProperty(JCSMPProperties.PASSWORD, args[3]);  // client-password
        final JCSMPSession session = JCSMPFactory.onlyInstance().createSession(properties);
        session.connect();        

        final Topic topic = JCSMPFactory.onlyInstance().createTopic("nse/cache/requests");
        
        System.out.println("Connecting to MongoDB...");
        MongoClient mongoClient = MongoClients.create("mongodb+srv://admin:lditJ3JDZA12eKcM@cluster0-bhxmr.mongodb.net/test?retryWrites=true");
        MongoDatabase database = mongoClient.getDatabase("nse");
        MongoCollection<Document> collection = database.getCollection("stockCache");

        /** Anonymous inner-class for handling publishing events */
        final XMLMessageProducer producer = session.getMessageProducer(new JCSMPStreamingPublishEventHandler() {
            @Override
            public void responseReceived(String messageID) {
                System.out.println("Producer received response for msg: " + messageID);
            }

            @Override
            public void handleError(String messageID, JCSMPException e, long timestamp) {
                System.out.printf("Producer received error for msg: %s@%s - %s%n", messageID, timestamp, e);
            }
        });

        /** Anonymous inner-class for request handling **/
        final XMLMessageConsumer cons = session.getMessageConsumer(new XMLMessageListener() {
            @Override
            public void onReceive(BytesXMLMessage request) {
            	
                Block<Document> printBlock = new Block<Document>() {
                    @Override
                    public void apply(final Document document) {
                        System.out.println(document.toJson());
                        TextMessage reply = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
                        reply.setText(document.toJson());
                        try {
                        	producer.sendReply(request,reply);
        				} catch (JCSMPException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				}
                    }
                };

                if (request.getReplyTo() != null) {
                    System.out.printf("Received Message Dump:%n%s%n",request.dump());
                    
                    String payload = ((TextMessage) request).getText();
                    System.out.println("Payload:" + payload);
                    String[] tokens = payload.split(":");
                    int startId = Integer.parseInt(tokens[0]);
                    int endId = Integer.parseInt(tokens[1]);
                    System.out.println("Start:" + startId);
                    System.out.println("End:" + endId);

                    System.out.println("Received request, generating response to " + request.getReplyTo());
               //     TextMessage reply = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
                    
                    Bson filter = and(gte("material_id", startId), lte("material_id", endId));
                    collection.find(filter).forEach(printBlock);

                } else {
                    System.out.println("Received message without reply-to field");
                }

            }

            public void onException(JCSMPException e) {
                System.out.printf("Consumer received exception: %s%n", e);
            }
        });
                

        session.addSubscription(topic);
        cons.start();

        // Consume-only session is now hooked up and running!
        System.out.println("Listening for request messages on topic " + topic + " ... Press enter to exit");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Close consumer
        cons.close();
        System.out.println("Exiting.");
        session.closeSession();

    }

    public static void main(String... args) throws JCSMPException {

        // Check command line arguments
     /*   if (args.length < 2 || args[1].split("@").length != 2) {
            System.out.println("Usage: BasicReplier <host:port> <client-username@message-vpn> [client-password]");
            System.out.println();
            System.exit(-1);
        }
        if (args[1].split("@")[0].isEmpty()) {
            System.out.println("No client-username entered");
            System.out.println();
            System.exit(-1);
        }
        if (args[1].split("@")[1].isEmpty()) {
            System.out.println("No message-vpn entered");
            System.out.println();
            System.exit(-1);
        } */

        NSECacheQueryReplier replier = new NSECacheQueryReplier();
        replier.run(args);
    }
}
