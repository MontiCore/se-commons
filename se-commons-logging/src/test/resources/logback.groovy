/* (c) https://github.com/MontiCore/monticore */


import ch.qos.logback.classic.encoder.PatternLayoutEncoder

// this is a very user friendly console appender
// which only prints level >= INFO
appender("CONSOLE", ConsoleAppender) {
  filter(ch.qos.logback.classic.filter.ThresholdFilter) { level = INFO }
  encoder(PatternLayoutEncoder) { pattern = "%-7([%level]) %message%n%exception{0}" }
}

def bySecond = timestamp("yyyy-MM-dd-HHmmss")

// this is a rather technically detailed file appender
appender("FILE", FileAppender) {
  file = "target/test.${bySecond}.log"
  encoder(PatternLayoutEncoder) { pattern = "%date{yyyy-MM-dd HH:mm:ss} %-7([%level]) %logger{26} %message%n" }
}

// everything with level >= DEBUG is logged to the file (see above)
root(DEBUG, ["FILE", "CONSOLE"])
