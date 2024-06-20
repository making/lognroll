import {styled} from "styled-components";

const colors = {
    success: '#5CBD9D',
    error: '#E74C3C',
    warning: '#F29C33',
    info: '#3998DB',
};
export type MessageStatus = 'success' | 'info' | 'warning' | 'error';
export const MessageBox = styled.p<{ status: MessageStatus }>`
    padding: 1rem;
    margin-bottom: 1rem;
    margin-left: auto;
    margin-right: auto;
    border: 1px solid ${(props) => colors[props.status]};
    border-radius: 0.25rem;
    color: ${(props) => colors[props.status]};;
    gap: 0.5rem;
    line-height: 1.5;
    width: 800px;
    max-width: 100%;
    border-left: 5px solid ${(props) => colors[props.status]};
    background: snow;
    font-weight: bold;
    strong {
        color: ${(props) => colors[props.status]};
    }
`;