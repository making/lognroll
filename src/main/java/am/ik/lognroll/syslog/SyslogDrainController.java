package am.ik.lognroll.syslog;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

import am.ik.lognroll.logs.Log;
import am.ik.lognroll.logs.LogStore;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SyslogDrainController {

	private final LogStore logStore;

	private final SyslogDrainProps props;

	private final ReentrantLock lock = new ReentrantLock();

	final ConcurrentLinkedQueue<Log> queue = new ConcurrentLinkedQueue<>();

	private final Logger logger = LoggerFactory.getLogger(SyslogDrainController.class);

	public SyslogDrainController(LogStore logStore, SyslogDrainProps props) {
		this.logStore = logStore;
		this.props = props;
	}

	@PostMapping(path = "/drain")
	public void drain(@RequestBody String message) {
		Log log = new SyslogPayload(message).toLog();
		this.lock.lock();
		try {
			this.queue.add(log);
			if (this.queue.size() >= this.props.maxQueueSize()) {
				// Async?
				this.flush();
			}
		}
		finally {
			this.lock.unlock();
		}
	}

	@Scheduled(initialDelayString = "${lognroll.syslog-drain.flush-interval-mills:5000}",
			fixedRateString = "${lognroll.syslog-drain.flush-interval-mills:5000}")
	@PreDestroy
	void flush() {
		if (this.queue.isEmpty()) {
			return;
		}
		logger.trace("Flushing queue");
		ConcurrentLinkedQueue<Log> copy;
		this.lock.lock();
		try {
			copy = new ConcurrentLinkedQueue<>(this.queue);
			this.queue.clear();
		}
		finally {
			this.lock.unlock();
		}
		this.logStore.addAll(copy);
	}

}
