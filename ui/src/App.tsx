import {useState} from 'react'
import './App.css'
import LogViewer from "./LogViewer.tsx";

function App() {
    const [count, setCount] = useState(0)

    return (
        <>
            <h1><a href={'/'}>LogN'Roll</a></h1>
            <LogViewer/>
        </>
    )
}

export default App
