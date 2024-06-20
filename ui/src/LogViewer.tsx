import React, {ChangeEvent, KeyboardEvent, useState} from 'react';
import ScrollToTop from "react-scroll-to-top";
import './LogViewer.css';
// @ts-expect-error TODO
import {JSONToHTMLTable} from "@kevincobain2000/json-to-html-table";
// @ts-expect-error TODO
import logfmt from 'logfmt';

interface BuildUrlParams {
    size: number;
    query: string;
    filter?: string;
    cursor?: string;
    from?: string;
    to?: string;
}

const buildUrl = ({size, query, filter, cursor, from, to}: BuildUrlParams): string => {
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

const convertToIsoUtc = (localDateTime: string): string => {
    const date = new Date(localDateTime);
    return date.toISOString();
};

function convertUtcToLocal(utcDateString: string): string {
    const date = new Date(utcDateString);
    return date.toLocaleString();
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

const LogViewer: React.FC = () => {
    const [logs, setLogs] = useState<Log[]>([]);
    const [query, setQuery] = useState<string>('');
    const [filter, setFilter] = useState<string>('');
    const [size, setSize] = useState<number>(30);
    const [from, setFrom] = useState<string>('');
    const [to, setTo] = useState<string>('');
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [jsonToTable, setJsonToTable] = useState<boolean>(false);
    const [useLocalTimezone, setUseLocalTimezone] = useState<boolean>(true);
    const [useOccurredTimestamp, setuseOccurredTimestamp] = useState<boolean>(false);
    const [timestampLabel, setTimestampLabel] = useState<'observed_timestamp' | 'timestamp'>('observed_timestamp');
    const [useSeverityText, setUseSeverityText] = useState<boolean>(true);
    const [severityLabel, setSeverityLabel] = useState<'severity_text' | 'severity_number'>('severity_text');
    const [showLoadMore, setShowLoadMore] = useState<boolean>(false);

    const fetchLogs = async () => {
        const url = buildUrl({size, query, filter, from, to});
        setIsLoading(true);
        try {
            const response = await fetch(url);
            const data: Log[] = await response.json();
            setLogs(data);
            setShowLoadMore(data.length >= size);
        } catch (error) {
            console.error('Error fetching logs:', error);
        } finally {
            setIsLoading(false);
        }
    };

    const fetchMoreLogs = async () => {
        if (logs.length === 0) {
            return;
        }
        const lastLog = logs[logs.length - 1];
        const url = buildUrl({
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
            const moreLogs: Log[] = await response.json();
            setLogs([...logs, ...moreLogs]);
            setShowLoadMore(moreLogs.length >= size);
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
            <input type="text" placeholder={`Filter (e.g. severityText=='ERROR', attributes["status"]>=400)`}
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