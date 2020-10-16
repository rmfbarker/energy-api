# energy-api

A distributed energy grid for battery devices to post charging events.

Events are processed using Kafka Streams and  stored to a database.

Device state can be queried via an API call

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein ring server

