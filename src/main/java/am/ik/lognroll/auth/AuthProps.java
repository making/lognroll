package am.ik.lognroll.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "lognroll.auth")
public record AuthProps(@DefaultValue("changeme") String token) {

}
