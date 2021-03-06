import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.filter.ThresholdFilter

String defPattern = '%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n'

String service = System.getProperty('burst.service') ?: 'all'

def logDir = new File('.', 'logs').canonicalFile
if (!logDir) logDir.mkdirs()

appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = defPattern
    }
    filter(ThresholdFilter) {
        level = INFO
    }
}

appender("FILE", RollingFileAppender) {
    file = "${logDir}/burst-${service}-service.log"
    append = true
    encoder(PatternLayoutEncoder) {
        pattern = defPattern
    }
    rollingPolicy(TimeBasedRollingPolicy) {
        maxHistory = 90
        fileNamePattern = "${logDir}/burst-service.%d{yyyy-MM-dd}.log"
    }
}

root(INFO, ['STDOUT', 'FILE'])

logger('ox.softeng', DEBUG)