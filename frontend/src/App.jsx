import { Navigate, Route, Routes } from 'react-router-dom';
import Sistema from './componentes/Sistema.jsx';
import Lista from './paginas/Lista.jsx';
import { iniciarSessao } from './sessao.js';

iniciarSessao();

export default function App() {
  return (
    <div className="app">
      <Routes>
        <Route element={<Sistema />}>
          <Route path="/locacoes" element={<Lista />} />
        </Route>
        <Route path="*" element={<Navigate to="/locacoes" replace />} />
      </Routes>
    </div>
  );
}
