import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import Acesso from '../componentes/Acesso.jsx';
import Campo from '../componentes/Campo.jsx';
import { entrar } from '../sessao.js';

export default function Login() {
  const navegar = useNavigate();
  const { state } = useLocation();
  const [email, definirEmail] = useState('');
  const [senha, definirSenha] = useState('');
  const [tentou, definirTentou] = useState(false);
  const [erroApi, definirErroApi] = useState(state?.erro || '');
  const [enviando, definirEnviando] = useState(false);

  const erroEmail = tentou && !email.trim() ? 'Informe o e-mail' : undefined;
  const erroSenha = tentou && !senha ? 'Informe a senha' : undefined;

  async function enviar(evento) {
    evento.preventDefault();
    definirTentou(true);
    definirErroApi('');
    if (!email.trim() || !senha) return;
    definirEnviando(true);
    try {
      await entrar({ email: email.trim(), senha });
      navegar('/locacoes');
    } catch (erro) {
      definirErroApi(erro.status === 401 ? 'E-mail ou senha incorretos.' : erro.message);
      definirEnviando(false);
    }
  }

  return (
    <Acesso>
      <form className="acesso-caixa" onSubmit={enviar} noValidate>
        <h1>Entrar</h1>
        {state?.aviso && !tentou && <div className="aviso aviso-sucesso" role="status"><p>{state.aviso}</p></div>}
        {erroApi && <div className="aviso aviso-erro" role="alert"><p>{erroApi}</p></div>}
        <Campo id="email" rotulo="E-mail" type="email" autoComplete="email" value={email} erro={erroEmail} onChange={(e) => definirEmail(e.target.value)} />
        <Campo id="senha" rotulo="Senha" type="password" autoComplete="current-password" value={senha} erro={erroSenha} onChange={(e) => definirSenha(e.target.value)} />
        <button type="submit" className="btn btn-primario btn-bloco" disabled={enviando}>{enviando ? 'Entrando…' : 'Entrar'}</button>
        <p className="ajuda">Autenticação ainda não conectada: os dados informados não são verificados.</p>
        <p className="acesso-rodape"><span className="suave">Ainda não tem conta?</span><Link className="btn-link" to="/cadastro">Criar conta</Link></p>
      </form>
    </Acesso>
  );
}
