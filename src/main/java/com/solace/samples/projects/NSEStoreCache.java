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
 *  HelloWorldQueueSub
 *
 *  This sample shows the basics of creating session, connecting a session,
 *  and subscribing to a queue and provisioning it if it does not exist. This 
 *  is meant to be a very basic example for demonstration purposes.
 */

package com.solace.samples.projects;

import org.apache.commons.lang.StringUtils;

import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.ConsumerFlowProperties;
import com.solacesystems.jcsmp.EndpointProperties;
import com.solacesystems.jcsmp.FlowReceiver;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.Queue;
import com.solacesystems.jcsmp.TextMessage;


import com.mongodb.client.*;

import org.bson.Document;


public class NSEStoreCache {

    public static void main(String... args) throws JCSMPException, InterruptedException {
        // Check command line arguments
        if (args.length < 4) {
            System.out.println("Usage: NSEStoreCache <msg_backbone_ip:port> <vpn> <client-username> <queue-name>");
            System.out.println();
            System.out.println(" Note: the client-username provided must have adequate permissions in its client");
            System.out.println("       profile to send and receive guaranteed messages, and to create endpoints.");
            System.out.println("       Also, the message-spool for the VPN must be configured with >0 capacity.");
            System.exit(-1);
        }
        System.out.println("NSEStoreCache initializing...");
        // Create a JCSMP Session
        final JCSMPProperties properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.HOST, args[0]);      // msg-backbone ip:port
        properties.setProperty(JCSMPProperties.VPN_NAME, args[1]);  // message-vpn
        properties.setProperty(JCSMPProperties.USERNAME, args[2]);  // client-username
        properties.setProperty(JCSMPProperties.PASSWORD, args[3]);  // client-password
        final JCSMPSession session =  JCSMPFactory.onlyInstance().createSession(properties);
        final String queueName = "nse/cache";
       
        System.out.println("MongoDB initializing...");

        MongoClient mongoClient = MongoClients.create("mongodb+srv://admin:lditJ3JDZA12eKcM@cluster0-bhxmr.mongodb.net/test?retryWrites=true");
        MongoDatabase database = mongoClient.getDatabase("nse");
        MongoCollection<Document> collection = database.getCollection("stockCache");
        

        System.out.printf("Attempting to provision the queue '%s' on the appliance.%n",queueName);
        final EndpointProperties endpointProps = new EndpointProperties();
        endpointProps.setPermission(EndpointProperties.PERMISSION_CONSUME);
        endpointProps.setAccessType(EndpointProperties.ACCESSTYPE_EXCLUSIVE);
        final Queue queue = JCSMPFactory.onlyInstance().createQueue(queueName);
        session.provision(queue, endpointProps, JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);

        System.out.printf("Attempting to bind to the queue '%s' on the appliance.%n",queueName);
        final ConsumerFlowProperties flow_prop = new ConsumerFlowProperties();
        flow_prop.setEndpoint(queue);
        flow_prop.setAckMode(JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);
        EndpointProperties endpoint_props = new EndpointProperties();
        endpoint_props.setAccessType(EndpointProperties.ACCESSTYPE_EXCLUSIVE);
        final FlowReceiver cons = session.createFlow(null, flow_prop, endpoint_props);
        // Start the consumer
        System.out.println("Connected. Awaiting message...");
        cons.start();
        // Consume-only session is now hooked up and running!
        while (true) {
        BytesXMLMessage msg = cons.receive();  // wait max 10 minutes for a message
        if (msg != null) {
            System.out.printf(((TextMessage)msg).getText());
            String key = StringUtils.substringBefore(((TextMessage)msg).getText(), ",");
            String value = StringUtils.substringAfter(((TextMessage)msg).getText(), ",");
            
            Document document = new Document("material_id", Integer.parseInt(key))
                    .append("value", value);
            
            collection.insertOne(document);
           
            msg.ackMessage();
        } else {
            System.out.println("No message received... timed out.");
            break;
        } 
       }
        // Close consumer
        cons.close();
        System.out.println("Exiting.");
        session.closeSession();
    }
}
