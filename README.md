[![Build Status](https://travis-ci.org/SolaceSamples/solace-samples-java.svg?branch=master)](https://travis-ci.org/SolaceSamples/solace-samples-java)

# Getting Started Examples
## Solace Messaging API for Java (JCSMP)

The "Getting Started" tutorials will get you up to speed and sending messages with Solace technology as quickly as possible. There are three ways you can get started:

- Follow [these instructions](https://cloud.solace.com/learn/group_getting_started/ggs_signup.html) to quickly spin up a cloud-based Solace messaging service for your applications.
- Follow [these instructions](https://docs.solace.com/Solace-VMR-Set-Up/Setting-Up-VMRs.htm) to start the Solace VMR in leading Clouds, Container Platforms or Hypervisors. The tutorials outline where to download and how to install the Solace VMR.
- If your company has Solace message routers deployed, contact your middleware team to obtain the host name or IP address of a Solace message router to test against, a username and password to access it, and a VPN in which you can produce and consume messages.

## Contents

This repository contains code and matching tutorial walk throughs for five different basic Solace messaging patterns. For a nice introduction to the Solace API and associated tutorials, check out the [tutorials home page](https://dev.solace.com/samples/solace-samples-java/).

See the individual tutorials for details:

- [Publish/Subscribe](https://dev.solace.com/samples/solace-samples-java/publish-subscribe): Learn how to set up pub/sub messaging on a Solace VMR.
- [Persistence](https://dev.solace.com/samples/solace-samples-java/persistence-with-queues): Learn how to set up persistence for guaranteed delivery.
- [Request/Reply](https://dev.solace.com/samples/solace-samples-java/request-reply): Learn how to set up request/reply messaging.
- [Confirmed Delivery](https://dev.solace.com/samples/solace-samples-java/confirmed-delivery): Learn how to confirm that your messages are received by a Solace message router.
- [Topic to Queue Mapping](https://dev.solace.com/samples/solace-samples-java/topic-to-queue-mapping): Learn how to map existing topics to Solace queues.

## Prerequisites

This tutorial requires the Solace Java API library. Download the Java API library to your computer from [here](http://dev.solace.com/downloads/).

## Build the Samples

Just clone and build. For example:

  1. clone this GitHub repository
```
git clone https://github.com/SolaceSamples/solace-samples-java
cd solace-samples-java
```
  2. `./gradlew assemble`

## Running the Samples

Publisher Flow:

1. NSEPublishFileToSolace: This will read the input file line by line and publish it to the topic “nse/stock/put”

Command to execute:
./build/staged/bin/NSEPublishFileToSolace <msg-vpn> <vpn-name> <userid> <password> <inputfile>
 
 
2. NSEStoreCache: This will read from Queue “nse/cache”. Store the message in MongoDB.

Command to execute:
./build/staged/bin/NSEStoreCache <msg-vpn> <vpn-name> <userid> <password>
 

Consumer Flow:

1. NSECacheRequestor: This will send request to Solace to get the cache data and wait for the response
    Command to execute:
	./build/staged/bin/NSECacheRequestor <msg-vpn> <vpn-name> <userid> <password> <startRange> <endRange>


2. NSECacheQueryReplier: Wait for request from NSECacheRequestor, fetch data and respond back
Command to execute:
./build/staged/bin/NSECacheQueryReplier <msg-vpn> <vpn-name> <userid> <password>

