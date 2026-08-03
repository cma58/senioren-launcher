import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.jsx'
import { ProgressProvider } from './context/ProgressContext.jsx'
import './index.css'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <ProgressProvider>
      <App />
    </ProgressProvider>
  </React.StrictMode>,
)
