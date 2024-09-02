package am.ik.lognroll.logs;

import java.util.List;

public interface LogStore {

	void addAll(List<Log> logs);

	void clear();

	void vacuum();

}
