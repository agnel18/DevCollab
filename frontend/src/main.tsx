import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { Board } from './pages/Board'
import './index.css'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <Board />
  </StrictMode>,
)
