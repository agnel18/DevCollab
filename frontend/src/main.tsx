import { StrictMode, useMemo } from 'react'
import { createRoot } from 'react-dom/client'
import { Board } from './pages/Board'
import { Boards } from './pages/Boards'
import './index.css'

function App() {
  const path = useMemo(() => {
    return window.location.pathname;
  }, []);

  return path === '/boards' ? <Boards /> : <Board />;
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
