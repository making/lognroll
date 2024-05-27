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
    severity?: string;
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
            <input type="text" placeholder={`Filter (e.g. severity=='ERROR', attributes["response_code"]>=400)`}
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
            </label>&nbsp;
            <button onClick={fetchLogs}
                    disabled={isLoading}
            >View Logs
            </button>

            <table className="table">
                <thead>
                <tr>
                    <th>timestamp</th>
                    <th>observed_timestamp</th>
                    <th>severity</th>
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
                {logs.map(log => <tr key={log.logId}>
                    <td>{useLocalTimezone ? convertUtcToLocal(log.timestamp) : log.timestamp}</td>
                    <td>{useLocalTimezone ? convertUtcToLocal(log.observedTimestamp) : log.observedTimestamp}</td>
                    <td>{log.severity}</td>
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
                </tr>)}
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