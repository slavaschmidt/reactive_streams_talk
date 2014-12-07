name := "rs"

version := "1.0"

scalaVersion := "2.11.4"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

resolvers += Resolver.mavenLocal

resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "jfrog" at "http://oss.jfrog.org/repo"

resolvers += "spring" at "http://repo.spring.io/libs-milestone"

libraryDependencies ++= Seq(
  "org.scream3r"        % "jssc"              % "2.8.0",
  "org.slf4j"           % "slf4j-api"         % "1.7.7",
  "ch.qos.logback"      % "logback-classic"   % "1.1.2",
  "com.typesafe.akka"   %% "akka-actor"       % "2.3.3",
  "org.scalatest"       %% "scalatest"        % "2.1.7"   % "test"
)

libraryDependencies ++= Seq(
  "io.spray"            %% "spray-can"     % "1.3.1",
  "io.spray"            %% "spray-routing" % "1.3.1",
  "io.spray"            %% "spray-caching" % "1.3.1",
  "io.spray"            %% "spray-http"    % "1.3.1",
  "io.spray"            %% "spray-httpx"   % "1.3.1",
  "io.spray"            %% "spray-util"    % "1.3.1",
  "io.spray"            %% "spray-can"     % "1.3.1",
  "io.spray"            %% "spray-client"  % "1.3.1",
  "io.spray"            %% "spray-json"    % "1.2.6"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka"   %% "akka-stream-experimental" % "0.11",
  "io.reactivex"        % "rxjava-reactive-streams"   % "0.3.0",
  "io.ratpack"          % "ratpack-rx"                % "0.9.10-SNAPSHOT",
  "io.ratpack"          % "ratpack-test"              % "0.9.10-SNAPSHOT",
  "org.projectreactor"  % "reactor-core"              % "2.0.0.M1",
  "io.vertx"            % "vertx-ext"                 % "3.0.0-SNAPSHOT",
  "io.vertx"            % "ext-reactive-streams"      % "3.0.0-SNAPSHOT"
)

seq(Revolver.settings: _*)
