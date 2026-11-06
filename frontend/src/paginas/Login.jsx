import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Acesso from '../componentes/Acesso.jsx';
import Campo from '../componentes/Campo.jsx';
import { iniciarSessao } from '../sessao.js';

export default function Login() {
  const navegar = useNavigate();
  const [email, definirEmail] = useState('');
  const [senha, definirSenha] = useState('');
  const [erros, definirErros] = useState({});

  function entrar(evento) {
    evento.preventDefault();
    const novos = {};
    if (!email.trim()) novos.email = 'Informe o e-mail';
    if (!senha) novos.senha = 'Informe a senha';
    definirErros(novos);
    if (Object.keys(novos).length > 0) return;
    iniciarSessao();
    navegar('/locacoes');
  }

  return (
    <Acesso>
      <form className="acesso-caixa" onSubmit={entrar} noValidate>
        <h1>Entrar</h1>
        <Campo id="email" rotulo="E-mail" type="email" autoComplete="email" value={email} erro={erros.email} onChange={(e) => definirEmail(e.target.value)} />
        <Campo id="senha" rotulo="Senha" type="password" autoComplete="current-password" value={senha} erro={erros.senha} onChange={(e) => definirSenha(e.target.value)} />
        <button type="submit" className="btn btn-primario btn-bloco">Entrar</button>
        <p className="ajuda">Autenticação ainda não conectada: os dados informados não são verificados.</p>
        <p className="acesso-rodape"><span className="suave">Ainda não tem conta?</span><Link className="btn-link" to="/cadastro">Criar conta</Link></p>
      </form>
    </Acesso>
  );
}
