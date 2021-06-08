# Overview
Scala3 stream project
# Usage

This is a normal sbt project. You can compile code with `sbt compile`, run it with `sbt run`, and `sbt console` will start a Scala 3 REPL.

For more information on the sbt-dotty plugin, see the
[scala3-example-project](https://github.com/scala/scala3-example-project/blob/main/README.md).
# Content
1. mqttSource => transformFlow => kafkaSink
2. kafkaSource => transformFlow => tsDB
3. kafkaSource => transformFlow => ch
# Steps
1. `./run_emqx.sh` to run emqx mqtt source then you can visit emqx on `18083` port
   - username: admin
   - password: public
2. edit [mqttSource.scala](./src/main/scala/org/heathkang/scala3_stream/mqttSource.scala) `topic` and `connectionSettings`
3. `sbt run` to run app
4. publish some message to mqtt, and you would see some output in terminal