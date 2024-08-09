package am.ik.lognroll.syslog;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "lognroll.syslog-drain")
public record SyslogDrainProps(int flushIntervalMills, @DefaultValue("1000") int maxQueueSize) {
}
