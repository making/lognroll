import {Bar, BarChart, CartesianGrid, Tooltip, XAxis, YAxis} from 'recharts';
// @ts-expect-error TODO
import {JSONToHTMLTable} from "@kevincobain2000/json-to-html-table";
import {TooltipProps} from "recharts/types/component/Tooltip";
import React from "react";

export type FrequencyData = { date: string, count: number };
const fillMissingData = (data: FrequencyData[], interval: number) => {
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
            filledData.push({time: currentTime.toLocaleString(), count: givenData.count});
            index++;
        } else {
            filledData.push({time: currentTime.toLocaleString(), count: 0});
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

interface FrequenciesChartProps {
    data: FrequencyData[];
    interval: number;
}

const FrequenciesChart: React.FC<FrequenciesChartProps> = ({data, interval}) => {
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
            <XAxis dataKey="time"/>
            <YAxis/>
            <Tooltip content={<TooltipContent/>}/>
            <Bar dataKey="count" fill="#8884d8"/>
        </BarChart>
        <span>interval: {interval}min</span>
    </>
};

export default FrequenciesChart;
