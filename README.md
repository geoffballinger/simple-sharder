simple-sharder
==============

A simple demonstration of sharding in an HTTP based system in Scala using [Twitter's Finagle library](http://twitter.github.io/finagle/) in support of my talk at [Mostly Functional](http://mostlyfunctional.com/) in August 2013.

The talk itself is now up [on Slideshare](http://www.slideshare.net/geoffballinger/finagle).

Note that this is in no way intended as production code and in particular omits any form of error checking or handling in order to maximise clarity!

To build - on Mac at least - YMMV on other platforms:

1. Install Scala and SBT - e.g. using Homebrew
2. Run `sbt assembly` to create a convenient jar
3. For convenience `ln -s target/scala-2.10/simple-sharder.jar`

To run a simple three shard system with a single worker in each shard:

1. `java -cp simple-sharder.jar uk.co.geoffballinger.simplesharder.Worker 8080 foo &`
2. `java -cp simple-sharder.jar uk.co.geoffballinger.simplesharder.Worker 8081 bar &`
3. `java -cp simple-sharder.jar uk.co.geoffballinger.simplesharder.Worker 8082 baz &`
4. `java -cp simple-sharder.jar uk.co.geoffballinger.simplesharder.Sharder 8083 127.0.0.1:8080 127.0.0.1:8081 127.0.0.1:8082`

Now in your browser try hitting the individual workers on port [8080](http://127.0.0.1:8080/), [8081](http://127.0.0.1:8081/), and [8082](http://127.0.0.1:8082/); followed by the whole system on port [8083](http://127.0.0.1:8083/).

Geoff Ballinger
