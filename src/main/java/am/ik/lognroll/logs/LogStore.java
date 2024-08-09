package am.ik.lognroll.logs;

import java.util.Collection;

public interface LogStore {

	void addAll(Collection<Log> logs);

	void clear();

}
