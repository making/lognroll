package am.ik.lognroll.config;

import java.util.List;

import am.ik.lognroll.logs.LogQuery.Cursor;
import am.ik.pagination.web.CursorPageRequestHandlerMethodArgumentResolver;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(new CursorPageRequestHandlerMethodArgumentResolver<>(Cursor::valueOf,
				props -> props.withSizeDefault(30)));
	}

}
