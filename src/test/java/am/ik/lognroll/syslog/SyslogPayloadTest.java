package am.ik.lognroll.syslog;

import java.time.Instant;

import am.ik.lognroll.logs.Log;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SyslogPayloadTest {

	@Test
	void router() {
		Log log = new SyslogPayload(
				"""
						<14>1 2024-08-09T01:01:11.364983+00:00 demo.demo.blog 509e69f4-101f-4175-82cb-c2de2b7291f8 [RTR/0] - [tags@47450 __v1_type="LogMessage" app_id="509e69f4-101f-4175-82cb-c2de2b7291f8" app_name="blog" component="route-emitter" deployment="cf-fd9d3aa8f939c97c06d5" index="471da6b2-70d3-444a-b3ee-e69966932935" instance_id="0" ip="10.0.4.13" job="router" organization_id="4b84793c-f3ea-4a55-92b7-942726aac163" organization_name="demo" origin="gorouter" process_id="509e69f4-101f-4175-82cb-c2de2b7291f8" process_instance_id="49dbefe1-4773-4705-73e6-2159" process_type="web" product="Small Footprint VMware Tanzu Application Service" source_type="RTR" space_id="6755b19d-c543-4e0c-a4b3-cd6e7c9c68a3" space_name="demo" system_domain="sys.sandbox.aws.maki.lol"] blog.apps.sandbox.aws.maki.lol - [2024-08-09T01:01:10.423829188Z] "POST /api/counter HTTP/2.0" 200 15 39 "https://blog.apps.sandbox.aws.maki.lol/entries/814" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36" "10.11.12.13:53465" "10.0.6.10:61010" x_forwarded_for:"10.11.12.13" x_forwarded_proto:"https" vcap_request_id:"f57d0fbe-d79e-4462-702e-941edbc834c3" response_time:0.940925 gorouter_time:0.000264 app_id:"509e69f4-101f-4175-82cb-c2de2b7291f8" app_index:"0" instance_id:"49dbefe1-4773-4705-73e6-2159" x_cf_routererror:"-" traceparent:"00-f57d0fbed79e4462702e941edbc834c3-702e941edbc834c3-01" tracestate:"gorouter=702e941edbc834c3"
						"""
					.trim())
			.toLog();
		assertThat(log.observedTimestamp()).isEqualTo(Instant.parse("2024-08-09T01:01:11.364983+00:00"));
		assertThat(log.serviceName()).isEqualTo("blog");
		assertThat(log.severityText()).isEqualTo("INFO");
		assertThat(log.body()).isEqualToIgnoringWhitespace(
				"""
						blog.apps.sandbox.aws.maki.lol - [2024-08-09T01:01:10.423829188Z] "POST /api/counter HTTP/2.0" 200 15 39 "https://blog.apps.sandbox.aws.maki.lol/entries/814" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36" "10.11.12.13:53465" "10.0.6.10:61010" x_forwarded_for:"10.11.12.13" x_forwarded_proto:"https" vcap_request_id:"f57d0fbe-d79e-4462-702e-941edbc834c3" response_time:0.940925 gorouter_time:0.000264 app_id:"509e69f4-101f-4175-82cb-c2de2b7291f8" app_index:"0" instance_id:"49dbefe1-4773-4705-73e6-2159" x_cf_routererror:"-" traceparent:"00-f57d0fbed79e4462702e941edbc834c3-702e941edbc834c3-01" tracestate:"gorouter=702e941edbc834c3"
						""");
		assertThat(log.traceId()).isEqualTo("f57d0fbed79e4462702e941edbc834c3");
		assertThat(log.spanId()).isEqualTo("702e941edbc834c3");
		assertThat(log.traceFlags()).isEqualTo(1);
		assertThat(log.resourceAttributes().toString()).isEqualToIgnoringWhitespace(
				"""
						{__v1_type=LogMessage, app_id=509e69f4-101f-4175-82cb-c2de2b7291f8, app_name=blog, component=route-emitter, deployment=cf-fd9d3aa8f939c97c06d5, index=471da6b2-70d3-444a-b3ee-e69966932935, instance_id=0, ip=10.0.4.13, job=router, organization_id=4b84793c-f3ea-4a55-92b7-942726aac163, organization_name=demo, origin=gorouter, process_id=509e69f4-101f-4175-82cb-c2de2b7291f8, process_instance_id=49dbefe1-4773-4705-73e6-2159, process_type=web, product=Small Footprint VMware Tanzu Application Service, source_type=RTR, space_id=6755b19d-c543-4e0c-a4b3-cd6e7c9c68a3, space_name=demo, system_domain=sys.sandbox.aws.maki.lol, hostname=demo.demo.blog}
						""");
	}

	@Test
	void cell() {
		Log log = new SyslogPayload(
				"""
						<14>1 2024-08-09T00:59:23.375775+00:00 demo.demo.blog 509e69f4-101f-4175-82cb-c2de2b7291f8 [CELL/0] - [tags@47450 app_id="509e69f4-101f-4175-82cb-c2de2b7291f8" app_name="blog" deployment="cf-fd9d3aa8f939c97c06d5" index="7008e0e1-0af1-49d6-a4cd-fc7ee2b301f4" instance_id="0" ip="10.0.6.10" job="compute" organization_id="4b84793c-f3ea-4a55-92b7-942726aac163" organization_name="demo" origin="rep" process_id="509e69f4-101f-4175-82cb-c2de2b7291f8" process_instance_id="6fbdc2c8-db9b-4730-5cb3-e06f" process_type="web" product="Small Footprint VMware Tanzu Application Service" source_id="509e69f4-101f-4175-82cb-c2de2b7291f8" source_type="CELL" space_id="6755b19d-c543-4e0c-a4b3-cd6e7c9c68a3" space_name="demo" system_domain="sys.sandbox.aws.maki.lol"] Cell 7008e0e1-0af1-49d6-a4cd-fc7ee2b301f4 successfully destroyed container for instance 6fbdc2c8-db9b-4730-5cb3-e06f
						"""
					.trim())
			.toLog();
		assertThat(log.observedTimestamp()).isEqualTo(Instant.parse("2024-08-09T00:59:23.375775+00:00"));
		assertThat(log.serviceName()).isEqualTo("blog");
		assertThat(log.severityText()).isEqualTo("INFO");
		assertThat(log.body()).isEqualToIgnoringWhitespace(
				"""
						Cell 7008e0e1-0af1-49d6-a4cd-fc7ee2b301f4 successfully destroyed container for instance 6fbdc2c8-db9b-4730-5cb3-e06f
						""");
		assertThat(log.traceId()).isNull();
		assertThat(log.spanId()).isNull();
		assertThat(log.resourceAttributes().toString()).isEqualToIgnoringWhitespace(
				"""
						{app_id=509e69f4-101f-4175-82cb-c2de2b7291f8, app_name=blog, deployment=cf-fd9d3aa8f939c97c06d5, index=7008e0e1-0af1-49d6-a4cd-fc7ee2b301f4, instance_id=0, ip=10.0.6.10, job=compute, organization_id=4b84793c-f3ea-4a55-92b7-942726aac163, organization_name=demo, origin=rep, process_id=509e69f4-101f-4175-82cb-c2de2b7291f8, process_instance_id=6fbdc2c8-db9b-4730-5cb3-e06f, process_type=web, product=Small Footprint VMware Tanzu Application Service, source_id=509e69f4-101f-4175-82cb-c2de2b7291f8, source_type=CELL, space_id=6755b19d-c543-4e0c-a4b3-cd6e7c9c68a3, space_name=demo, system_domain=sys.sandbox.aws.maki.lol, hostname=demo.demo.blog}
						""");
	}

	@Test
	void app() {
		Log log = new SyslogPayload(
				"""
						<14>1 2024-08-09T01:01:11.363164+00:00 demo.demo.blog 509e69f4-101f-4175-82cb-c2de2b7291f8 [APP/PROC/WEB/0] - [tags@47450 app_id="509e69f4-101f-4175-82cb-c2de2b7291f8" app_name="blog" deployment="cf-fd9d3aa8f939c97c06d5" index="7008e0e1-0af1-49d6-a4cd-fc7ee2b301f4" instance_id="0" ip="10.0.6.10" job="compute" organization_id="4b84793c-f3ea-4a55-92b7-942726aac163" organization_name="demo" origin="rep" process_id="509e69f4-101f-4175-82cb-c2de2b7291f8" process_instance_id="49dbefe1-4773-4705-73e6-2159" process_type="web" product="Small Footprint VMware Tanzu Application Service" source_id="509e69f4-101f-4175-82cb-c2de2b7291f8" source_type="APP/PROC/WEB" space_id="6755b19d-c543-4e0c-a4b3-cd6e7c9c68a3" space_name="demo" system_domain="sys.sandbox.aws.maki.lol"] {"@timestamp":"2024-08-09T01:01:11.362Z","log.level": "INFO","message":"kind=server method=POST url=\\"https://blog.apps.sandbox.aws.maki.lol/api/counter\\" status=200 duration=937 protocol=\\"HTTP/1.1\\" remote=\\"10.11.12.13\\" user_agent=\\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36\\" referer=\\"https://blog.apps.sandbox.aws.maki.lol/entries/814\\"","ecs.version": "1.2.0","service.name":"blog-frontend","process.thread.name":"http-nio-8080-exec-1","log.logger":"accesslog","traceId":"f57d0fbed79e4462702e941edbc834c3","spanId":"ee86cd0a3e621624"}
						"""
					.trim())
			.toLog();
		assertThat(log.observedTimestamp()).isEqualTo(Instant.parse("2024-08-09T01:01:11.363164+00:00"));
		assertThat(log.serviceName()).isEqualTo("blog");
		assertThat(log.severityText()).isEqualTo("INFO");
		assertThat(log.body()).isEqualToIgnoringWhitespace(
				"""
						{"@timestamp":"2024-08-09T01:01:11.362Z","log.level": "INFO","message":"kind=server method=POST url=\\"https://blog.apps.sandbox.aws.maki.lol/api/counter\\" status=200 duration=937 protocol=\\"HTTP/1.1\\" remote=\\"10.11.12.13\\" user_agent=\\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36\\" referer=\\"https://blog.apps.sandbox.aws.maki.lol/entries/814\\"","ecs.version": "1.2.0","service.name":"blog-frontend","process.thread.name":"http-nio-8080-exec-1","log.logger":"accesslog","traceId":"f57d0fbed79e4462702e941edbc834c3","spanId":"ee86cd0a3e621624"}
						""");
		assertThat(log.traceId()).isEqualTo("f57d0fbed79e4462702e941edbc834c3");
		assertThat(log.spanId()).isEqualTo("ee86cd0a3e621624");
		assertThat(log.traceFlags()).isEqualTo(1);
		assertThat(log.resourceAttributes().toString()).isEqualToIgnoringWhitespace(
				"""
						{app_id=509e69f4-101f-4175-82cb-c2de2b7291f8, app_name=blog, deployment=cf-fd9d3aa8f939c97c06d5, index=7008e0e1-0af1-49d6-a4cd-fc7ee2b301f4, instance_id=0, ip=10.0.6.10, job=compute, organization_id=4b84793c-f3ea-4a55-92b7-942726aac163, organization_name=demo, origin=rep, process_id=509e69f4-101f-4175-82cb-c2de2b7291f8, process_instance_id=49dbefe1-4773-4705-73e6-2159, process_type=web, product=Small Footprint VMware Tanzu Application Service, source_id=509e69f4-101f-4175-82cb-c2de2b7291f8, source_type=APP/PROC/WEB, space_id=6755b19d-c543-4e0c-a4b3-cd6e7c9c68a3, space_name=demo, system_domain=sys.sandbox.aws.maki.lol, hostname=demo.demo.blog}
						""");
	}

	@Test
	void system() {
		Log log = new SyslogPayload(
				"""
						<14>1 2017-01-25T13:25:03.18377Z 192.0.2.10 etcd - - [instance@47450 director="test-env" deployment="cf-c42ae2c4dfb6f67b6c27" group="diego_database" az="us-west1-a" id="83bd66e5-3fdf-44b7-bdd6-508deae7c786"] [INFO] the leader is [https://diego-database-0.etcd.service.cf.internal:4001]
						"""
					.trim())
			.toLog();
		assertThat(log.observedTimestamp()).isEqualTo("2017-01-25T13:25:03.18377Z");
		assertThat(log.serviceName()).isEqualTo("etcd");
		assertThat(log.severityText()).isEqualTo("INFO");
		assertThat(log.body()).isEqualToIgnoringWhitespace("""
				[INFO] the leader is [https://diego-database-0.etcd.service.cf.internal:4001]
				""");
		assertThat(log.traceId()).isNull();
		assertThat(log.spanId()).isNull();
		assertThat(log.resourceAttributes().toString()).isEqualToIgnoringWhitespace(
				"""
						{director=test-env, deployment=cf-c42ae2c4dfb6f67b6c27, group=diego_database, az=us-west1-a, id=83bd66e5-3fdf-44b7-bdd6-508deae7c786, hostname=192.0.2.10}
						""");
	}

}