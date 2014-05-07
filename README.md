penfold-listener
================

[![Build Status](https://travis-ci.org/huwtl/penfold-listener.png)](https://travis-ci.org/huwtl/penfold-listener)

Event listener library for [penfold](https://github.com/huwtl/penfold)


Usage
-----

Add the following dependencies to your project:

```
<dependency>
    <groupId>org.huwtl</groupId>
    <artifactId>penfold-listener</artifactId>
    <version>${VERSION}</version>
</dependency>
```

Create and start the event listener

```java
 final EventHandler<EventClass> eventHandler = new EventHandler<EventClass>() {
      @Override public boolean interestedIn(final Event event) {
          return true;
      }

      @Override public void handle(final EventRecord<EventClass> eventRecord) {
          // your custom event handling
      }
 };

 new EventListenerConfiguration("unique tracker id") //
                .readEventsFromMysqlEventStore(eventStoreDataSource) //
                .pollForNewEventsEvery(1, MINUTES) //
                .withMysqlEventTracker(eventTrackerDataSource) //
                .withEventHandlers(eventHandler) //
                .build() //
                .start();
```
