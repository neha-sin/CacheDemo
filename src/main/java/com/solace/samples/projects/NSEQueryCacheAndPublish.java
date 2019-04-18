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


import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.JCSMPStreamingPublishEventHandler;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageProducer;

import com.mongodb.client.*;
import com.mongodb.ServerAddress;

import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Arrays;
import com.mongodb.Block;

import static com.mongodb.client.model.Filters.*;
import com.mongodb.client.result.DeleteResult;
import static com.mongodb.client.model.Updates.*;
import com.mongodb.client.result.UpdateResult;



public class NSEQueryCacheAndPublish {
	
    static String textFileName = "";
    
    public static void main(String... args) throws JCSMPException, InterruptedException {
    	// Check command line arguments
    /*    if (args.length < 4) {
            System.out.println("Usage: HelloWorldPub <msg_backbone_ip:port> <vpn> <client-username> <client-password> <topic>");
            System.exit(-1);
        }*/
        
        final JCSMPProperties properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.HOST, args[0]);      // msg-backbone ip:port
        properties.setProperty(JCSMPProperties.VPN_NAME, args[1]);  // message-vpn
        properties.setProperty(JCSMPProperties.USERNAME, args[2]);  // client-username
        properties.setProperty(JCSMPProperties.PASSWORD, args[3]);  // client-password
        final JCSMPSession session =  JCSMPFactory.onlyInstance().createSession(properties);
        
        int startid=Integer.parseInt(args[4]);
        int endid=Integer.parseInt(args[5]);
        
        final Topic topic = JCSMPFactory.onlyInstance().createTopic("nse/cache/get");
        
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

        
        
        
        MongoClient mongoClient = MongoClients.create("mongodb+srv://admin:lditJ3JDZA12eKcM@cluster0-bhxmr.mongodb.net/test?retryWrites=true");
        MongoDatabase database = mongoClient.getDatabase("nse");
        MongoCollection<Document> collection = database.getCollection("stockCache");
        System.out.println(collection.countDocuments());

        
        Block<Document> printBlock = new Block<Document>() {
            @Override
            public void apply(final Document document) {
                System.out.println(document.toJson());
                TextMessage msg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
                msg.setText(document.toJson());
                try {
					prod.send(msg,topic);
				} catch (JCSMPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
     };
     
    // collection.find().forEach(printBlock);
     //collection.find({ "material_id" : 1});
     
     Bson filter = and(gte("material_id", startid), lte("material_id", endid));
     collection.find(filter).forEach(printBlock);

    }
}
