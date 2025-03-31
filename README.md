# CS647 Akka Demo Code

This is a sample project that contains (almost) equivalent implementations of an echo server, mediated by a proxy, implemented in Akka --- in both Scala and Java.

The tool used to compile and run this project is [sbt](https://www.scala-sbt.org/), which
is a build tool similar to Maven, Gradle, and similar tools, but specifically tailored for Scala and Java.
The recommended way to install ```sbt``` and related tools is via [Coursier](https://get-coursier.io/docs/cli-installation).

To run the Scala version, install sbt and run:
```
sbt run
```

To run the Java version, install sbt and run:
```
sbt "runMain edu.drexel.cs647.java.AkkaDemo"
```
This command is a little more verbose because the runMain subcommand takes an argument.

You can also run an sbt shell, and then just type "compile", "run", or "runMain ..." in the resulting shell.

You can use this as a starting point for your own projects; you can simply delete whichever language you don't want to use and start from the other language's code.
