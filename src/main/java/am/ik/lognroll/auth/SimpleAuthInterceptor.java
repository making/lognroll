package am.ik.lognroll.auth;

import java.util.Base64;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

public class SimpleAuthInterceptor implements HandlerInterceptor {

	private final AuthProps props;

	public SimpleAuthInterceptor(AuthProps props) {
		this.props = props;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if (StringUtils.hasText(this.props.token())) {
			String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
			boolean authorized = false;
			if (StringUtils.hasText(authorization)) {
				if (authorization.toLowerCase().startsWith("bearer") || authorization.startsWith("bearer")) {
					final String token = authorization.replace("Bearer ", "").replace("bearer ", "");
					if (Objects.equals(this.props.token(), token)) {
						authorized = true;
					}
				}
				else if (authorization.startsWith("Basic") || authorization.startsWith("basic")) {
					final String basic = authorization.replace("Basic ", "").replace("basic ", "");
					final String token = new String(Base64.getDecoder().decode(basic)).split(":", 2)[1];
					if (Objects.equals(this.props.token(), token)) {
						authorized = true;
					}
				}
			}
			if (!authorized) {
				response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"LogN'Roll API\"");
				response.sendError(HttpStatus.UNAUTHORIZED.value());
				return false;
			}
		}
		return true;
	}

}
