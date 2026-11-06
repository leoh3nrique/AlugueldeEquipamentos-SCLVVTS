import { Navigate, Route, Routes } from 'react-router-dom';
import Sistema from './componentes/Sistema.jsx';
import Detalhes from './paginas/Detalhes.jsx';
import FormularioReserva from './paginas/FormularioReserva.jsx';
import Lista from './paginas/Lista.jsx';
import { iniciarSessao } from './sessao.js';

iniciarSessao();

export default function App() {
  return (
    <div className="app">
      <Routes>
        <Route element={<Sistema />}>
          <Route path="/locacoes" element={<Lista />} />
          <Route path="/locacoes/nova" element={<FormularioReserva />} />
          <Route path="/locacoes/:id" element={<Detalhes />} />
          <Route path="/locacoes/:id/alterar" element={<FormularioReserva />} />
        </Route>
        <Route path="*" element={<Navigate to="/locacoes" replace />} />
      </Routes>
    </div>
  );
}
