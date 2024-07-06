import {Bar, BarChart, CartesianGrid, Tooltip, XAxis, YAxis} from 'recharts';
// @ts-expect-error TODO
import {JSONToHTMLTable} from "@kevincobain2000/json-to-html-table";
import {TooltipProps} from "recharts/types/component/Tooltip";
import React from "react";

export type VolumeData = { date: string, count: number };
const fillMissingData = (data: VolumeData[], interval: number) => {
    if (data.length <= 1) {
        return data;
    }
    const filledData = [];
    const currentTime = new Date(data[0].date);
    const endTime = new Date(data[data.length - 1].date);
    let index = 0;
    while (currentTime <= endTime) {
        const givenData = data[index];
        if (currentTime.getTime() === new Date(givenData.date).getTime()) {
            filledData.push({date: currentTime.toLocaleString(), count: givenData.count});
            index++;
        } else {
            filledData.push({date: currentTime.toLocaleString(), count: 0});
        }
        currentTime.setMinutes(currentTime.getMinutes() + interval);
    }
    return filledData;
};

const TooltipContent = (props: TooltipProps<string, string>) => {
    if (!props.payload || props.payload.length < 1) {
        return <></>;
    }
    return <JSONToHTMLTable data={props.payload[0].payload}
                            tableClassName="table"/>;
};

interface VolumesChartProps {
    data: VolumeData[];
    interval: number;
    onClick: (date: string) => void;
}

const VolumesChart: React.FC<VolumesChartProps> = ({data, interval, onClick}) => {
    const filled = fillMissingData(data, interval);
    return <>
        <BarChart
            width={1000}
            height={200}
            data={filled}
            margin={{
                top: 15
            }}>
            <CartesianGrid strokeDasharray="3 3"/>
            <XAxis dataKey="date"/>
            <YAxis/>
            <Tooltip content={<TooltipContent/>}/>
            <Bar dataKey="count" fill="#8884d8" onClick={x => onClick(x.date)}/>
        </BarChart>
        <span>interval: {interval}min</span>
    </>
};

export default VolumesChart;
