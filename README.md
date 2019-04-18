[![Build Status](https://travis-ci.org/SolaceSamples/solace-samples-java.svg?branch=master)](https://travis-ci.org/SolaceSamples/solace-samples-java)

## Prerequisites

This tutorial requires the Solace Java API library. Download the Java API library to your computer from [here](http://dev.solace.com/downloads/).

## Build the Samples

Just clone and build. For example:

  1. clone this GitHub repository
```
git clone https://github.com/neha-sin/CacheDemo 
cd CacheDemo
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

# CacheDemo
