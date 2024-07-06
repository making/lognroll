import React, {ChangeEvent, KeyboardEvent, useState} from 'react';
import ScrollToTop from "react-scroll-to-top";
import './LogViewer.css';
// @ts-expect-error TODO
import {JSONToHTMLTable} from "@kevincobain2000/json-to-html-table";
// @ts-expect-error TODO
import logfmt from 'logfmt';
import {MessageBox, MessageStatus} from "./MessageBox.tsx";
import FrequenciesChart, {FrequencyData} from "./FrequenciesChart.tsx";

interface BuildUrlParams {
    size?: number;
    query: string;
    filter?: string;
    cursor?: string;
    from?: string;
    to?: string;
    interval?: number;
}

const buildLogsUrl = ({size, query, filter, cursor, from, to}: BuildUrlParams): string => {
    let url = `/api/logs?size=${size}&query=${encodeURIComponent(query)}`;
    if (filter) {
        url += `&filter=${encodeURIComponent(filter)}`;
    }
    if (cursor) {
        url += `&cursor=${encodeURIComponent(cursor)}`;
    }
    if (from) {
        url += `&from=${encodeURIComponent(convertToIsoUtc(from))}`;
    }
    if (to) {
        url += `&to=${encodeURIComponent(convertToIsoUtc(to))}`;
    }
    return url;
};


const buildCountUrl = (path: string, {query, filter, from, to, interval}: BuildUrlParams): string => {
    let url = `/api/logs${path}?query=${encodeURIComponent(query)}`;
    if (filter) {
        url += `&filter=${encodeURIComponent(filter)}`;
    }
    if (from) {
        url += `&from=${encodeURIComponent(convertToIsoUtc(from))}`;
    }
    if (to) {
        url += `&to=${encodeURIComponent(convertToIsoUtc(to))}`;
    }
    if (interval) {
        url += `&interval=PT${interval}M`
    }
    return url;
};

const convertToIsoUtc = (localDateTime: string): string => {
    const date = new Date(localDateTime);
    return date.toISOString();
};

function convertUtcToLocal(utcDateString: string): string {
    const date = new Date(utcDateString);
    return date.toLocaleString();
}

interface LogsResponse {
    logs: Log[]
}

interface CountResponse {
    totalCount: number
}

interface FrequenciesResponse {
    frequencies: FrequencyData[]
}

interface Log {
    logId: number;
    timestamp: string;
    observedTimestamp: string;
    severityText?: string;
    severityNumber?: number;
    serviceName?: string;
    scope?: string;
    body?: string;
    traceId?: string;
    spanId?: string;
    attributes?: Record<string, object>;
    resourceAttributes?: Record<string, object>;
}

interface Message {
    text: string;
    status: MessageStatus;
}

interface Problem {
    type: string;
    title: string;
    status: number;
    detail: string;
}

const formatDate = (date: Date) => {
    const year = date.getFullYear();
    const month = ('0' + (date.getMonth() + 1)).slice(-2);
    const day = ('0' + date.getDate()).slice(-2);
    const hours = ('0' + date.getHours()).slice(-2);
    const minutes = ('0' + date.getMinutes()).slice(-2);
    const seconds = ('0' + date.getSeconds()).slice(-2);
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};

const calcInterval = (from: string, to: string) => {
    if (!from) {
        return 60; // 1 hour
    }
    const startDate = new Date(from);
    const endDate = to ? new Date(to) : new Date();
    const diffTime = Math.abs(endDate.getTime() - startDate.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    if (diffDays < 3) {
        return 10;
    } else if (diffDays < 4) {
        return 20;
    } else if (diffDays < 5) {
        return 30
    } else {
        return 60;
    }
};

const LogViewer: React.FC = () => {
    const [logs, setLogs] = useState<Log[]>([]);
    const [count, setCount] = useState<number | string>();
    const [query, setQuery] = useState<string>('');
    const [filter, setFilter] = useState<string>('');
    const [size, setSize] = useState<number>(30);
    const [from, setFrom] = useState<string>(formatDate(new Date(new Date().getTime() - 12 * 60 * 60 * 1000)));
    const [to, setTo] = useState<string>('');
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [jsonToTable, setJsonToTable] = useState<boolean>(false);
    const [useLocalTimezone, setUseLocalTimezone] = useState<boolean>(true);
    const [useOccurredTimestamp, setuseOccurredTimestamp] = useState<boolean>(false);
    const [timestampLabel, setTimestampLabel] = useState<'observed_timestamp' | 'timestamp'>('observed_timestamp');
    const [useSeverityText, setUseSeverityText] = useState<boolean>(true);
    const [severityLabel, setSeverityLabel] = useState<'severity_text' | 'severity_number'>('severity_text');
    const [showLoadMore, setShowLoadMore] = useState<boolean>(false);
    const [message, setMessage] = useState<Message | null>(null);
    const [frequencies, setFrequencies] = useState<FrequencyData[]>([]);
    const [interval, setInterval] = useState<number>(10);

    const setProblemMessage = (data: Problem) => setMessage({
        status: 'error',
        text: data.detail
    });

    const fetchLogs = async () => {
        setIsLoading(true);
        setMessage(null);
        try {
            const logsResponse = await fetch(buildLogsUrl({size, query, filter, from, to}));
            if (logsResponse.status === 200) {
                const logsData: LogsResponse = await logsResponse.json();
                setLogs(logsData.logs);
                setCount('Counting...');
                setShowLoadMore(logsData.logs.length >= size);
            } else {
                const data: Problem = await logsResponse.json();
                setProblemMessage(data);
            }
        } catch (error) {
            console.error('Error fetching logs:', error);
        } finally {
            setIsLoading(false);
        }
        try {
            const countResponse = await fetch(buildCountUrl('/count', {query, filter, from, to}));
            const interval = calcInterval(from, to);
            const frequenciesResponse = await fetch(buildCountUrl('/frequencies', {query, filter, from, to, interval}));
            if (countResponse.status === 200) {
                const countData: CountResponse = await countResponse.json();
                setCount(countData.totalCount);
            } else {
                const data: Problem = await countResponse.json();
                setProblemMessage(data);
            }
            if (frequenciesResponse.status === 200) {
                const frequenciesData: FrequenciesResponse = await frequenciesResponse.json();
                setFrequencies(frequenciesData.frequencies);
                setInterval(interval);
            } else {
                const data: Problem = await frequenciesResponse.json();
                setProblemMessage(data);
            }
        } catch (error) {
            console.error('Error fetching logs:', error);
        }
    };

    const fetchMoreLogs = async () => {
        if (logs.length === 0) {
            return;
        }
        const lastLog = logs[logs.length - 1];
        const url = buildLogsUrl({
            size,
            query,
            filter,
            from,
            to,
            cursor: `${lastLog.timestamp},${lastLog.observedTimestamp}`
        });
        setIsLoading(true);
        try {
            const response = await fetch(url);
            if (response.status == 200) {
                const {logs: moreLogs}: LogsResponse = await response.json();
                setLogs([...logs, ...moreLogs]);
                setShowLoadMore(moreLogs.length >= size);
            } else {
                const data: Problem = await response.json();
                setProblemMessage(data);
            }
        } catch (error) {
            console.error('Error fetching more logs:', error);
        } finally {
            setIsLoading(false);
        }
    };

    const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Enter') {
            fetchLogs().then();
        }
    };

    const shouldJsonToTable = (log: Log) => jsonToTable && log.body && log.body.startsWith('{') && log.body.endsWith('}');
    const shouldLogfmtToTable = (log: Log) => jsonToTable && log.body && /^[a-zA-Z0-9_]+=/.test(log.body);

    return (
        <div id={'log-viewer'}>
            {isLoading && <div className="overlay">Loading...</div>}

            <input type="text"
                   placeholder="Search Query"
                   onChange={(e: ChangeEvent<HTMLInputElement>) => setQuery(e.target.value)}
                   onKeyDown={handleKeyDown}
                   disabled={isLoading}
                   style={{width: '200px'}}
            />&nbsp;
            <input type="text" placeholder={`Filter (e.g. severity_text=='ERROR', attributes["status"]>=400)`}
                   onChange={(e: ChangeEvent<HTMLInputElement>) => setFilter(e.target.value)}
                   onKeyDown={handleKeyDown}
                   disabled={isLoading}
                   style={{width: '400px'}}
            />&nbsp;
            <input type="number"
                   min="1"
                   placeholder="Size"
                   onChange={(e: ChangeEvent<HTMLInputElement>) => setSize(Number(e.target.value))}
                   onKeyDown={handleKeyDown}
                   disabled={isLoading}
                   style={{width: '50px'}}
            />&nbsp;
            <label>From: <input
                type="datetime-local"
                placeholder="From"
                onChange={(e: ChangeEvent<HTMLInputElement>) => setFrom(e.target.value)}
                value={from}
            /></label>&nbsp;
            <label>To: <input
                type="datetime-local"
                placeholder="To"
                onChange={(e: ChangeEvent<HTMLInputElement>) => setTo(e.target.value)}
                value={to}
            /></label>&nbsp;
            <br/>
            <label>
                <input
                    type="checkbox"
                    checked={jsonToTable}
                    onChange={(e: ChangeEvent<HTMLInputElement>) => setJsonToTable(e.target.checked)}
                    disabled={isLoading}
                />
                to table
            </label>&nbsp;
            <label>
                <input
                    type="checkbox"
                    checked={useLocalTimezone}
                    onChange={(e: ChangeEvent<HTMLInputElement>) => setUseLocalTimezone(e.target.checked)}
                    disabled={isLoading}
                />
                use local timezone
            </label>
            <br/>
            timestamp:&nbsp;
            <label>
                <input name="timestamp"
                       type="radio"
                       checked={useOccurredTimestamp}
                       onChange={(e: ChangeEvent<HTMLInputElement>) => {
                           setuseOccurredTimestamp(e.target.checked);
                           setTimestampLabel('timestamp');
                       }}
                       disabled={isLoading}/>
                occurred
            </label>
            <label>
                <input name="timestamp"
                       type="radio"
                       checked={!useOccurredTimestamp}
                       onChange={(e: ChangeEvent<HTMLInputElement>) => {
                           setuseOccurredTimestamp(!e.target.checked);
                           setTimestampLabel('observed_timestamp');
                       }}
                       disabled={isLoading}/>
                observed
            </label>&nbsp;
            severity:&nbsp;
            <label>
                text<input name="severity"
                           type="radio"
                           checked={useSeverityText}
                           onChange={(e: ChangeEvent<HTMLInputElement>) => {
                               setUseSeverityText(e.target.checked);
                               setSeverityLabel('severity_text');
                           }}
                           disabled={isLoading}/>
            </label>
            <label>
                number<input name="severity"
                             type="radio"
                             checked={!useSeverityText}
                             onChange={(e: ChangeEvent<HTMLInputElement>) => {
                                 setUseSeverityText(!e.target.checked);
                                 setSeverityLabel('severity_number');
                             }}
                             disabled={isLoading}/>
            </label>&nbsp;
            <button onClick={fetchLogs}
                    disabled={isLoading}
            >View Logs
            </button>
            {message && <MessageBox status={message.status}>{message.text}</MessageBox>}
            {frequencies.length > 0 && <FrequenciesChart data={frequencies} interval={interval}/>}
            {count !== undefined && <p>Total Count: <strong>{count.toLocaleString()}</strong></p>}
            <table className="table">
                <thead>
                <tr>
                    <th>{timestampLabel}</th>
                    <th>{severityLabel}</th>
                    <th>service_name</th>
                    <th>scope</th>
                    <th>body</th>
                    <th>trace_id</th>
                    <th>span_id</th>
                    <th>attributes</th>
                    <th>resource_attributes</th>
                </tr>
                </thead>
                <tbody>
                {logs.map(log => {
                    const timestamp = useOccurredTimestamp ? log.timestamp : log.observedTimestamp;
                    return <tr key={log.logId}>
                        <td>{useLocalTimezone ? convertUtcToLocal(timestamp) : timestamp}</td>
                        <td>{useSeverityText ? log.severityText : log.severityNumber}</td>
                        <td>{log.serviceName}</td>
                        <td>{log.scope}</td>
                        <td>{log.body && shouldJsonToTable(log) ? <JSONToHTMLTable data={JSON.parse(log.body)}
                                                                                   tableClassName="table"/> : (shouldLogfmtToTable(log) ?
                            <JSONToHTMLTable data={logfmt.parse(log.body)}
                                             tableClassName="table"/> : log.body)}</td>
                        <td>{log.traceId}</td>
                        <td>{log.spanId}</td>
                        <td>
                            {jsonToTable ? <JSONToHTMLTable
                                data={log.attributes || []}
                                tableClassName="table"
                            /> : logfmt.stringify(log.attributes)}
                        </td>
                        <td>
                            {jsonToTable ? <JSONToHTMLTable
                                data={log.resourceAttributes || []}
                                tableClassName="table"
                            /> : logfmt.stringify(log.resourceAttributes)}
                        </td>
                    </tr>;
                })}
                </tbody>
            </table>
            {showLoadMore && <button
                id={'load-more'}
                onClick={fetchMoreLogs}
                disabled={isLoading}>Load More</button>}
            <ScrollToTop smooth style={{paddingLeft: '5px'}}/>
        </div>
    );
};

export default LogViewer;