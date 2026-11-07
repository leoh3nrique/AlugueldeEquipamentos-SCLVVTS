import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Acesso from '../componentes/Acesso.jsx';
import Campo from '../componentes/Campo.jsx';
import { cadastrar } from '../sessao.js';

function validar({ nome, email, senha, confirmacao }) {
  const erros = {};
  if (!nome.trim()) erros.nome = 'Informe seu nome';
  if (!email.trim()) erros.email = 'Informe o e-mail';
  if (!senha) erros.senha = 'Informe a senha';
  if (senha !== confirmacao) erros.confirmacao = 'As senhas não conferem';
  return erros;
}

export default function Cadastro() {
  const navegar = useNavigate();
  const [dados, definirDados] = useState({ nome: '', email: '', senha: '', confirmacao: '' });
  const [tentou, definirTentou] = useState(false);
  const [erroApi, definirErroApi] = useState('');
  const [enviando, definirEnviando] = useState(false);

  const validacao = validar(dados);
  const erros = tentou ? validacao : {};
  const campo = (nome) => ({ value: dados[nome], erro: erros[nome], onChange: (e) => definirDados({ ...dados, [nome]: e.target.value }) });

  async function enviar(evento) {
    evento.preventDefault();
    definirTentou(true);
    definirErroApi('');
    if (Object.keys(validacao).length > 0) return;
    definirEnviando(true);
    try {
      await cadastrar({ nome: dados.nome.trim(), email: dados.email.trim(), senha: dados.senha });
      navegar('/login', { state: { aviso: 'Conta criada. Entre com seu e-mail e senha.' } });
    } catch (erro) {
      definirErroApi(erro.message);
      definirEnviando(false);
    }
  }

  return (
    <Acesso>
      <form className="acesso-caixa" onSubmit={enviar} noValidate>
        <h1>Criar conta</h1>
        {erroApi && <div className="aviso aviso-erro" role="alert"><p>{erroApi}</p></div>}
        <Campo id="nome" rotulo="Nome completo" type="text" autoComplete="name" {...campo('nome')} />
        <Campo id="email" rotulo="E-mail" type="email" autoComplete="email" {...campo('email')} />
        <Campo id="senha" rotulo="Senha" type="password" autoComplete="new-password" {...campo('senha')} />
        <Campo id="confirmacao" rotulo="Confirmar senha" type="password" autoComplete="new-password" {...campo('confirmacao')} />
        <button type="submit" className="btn btn-primario btn-bloco" disabled={enviando}>{enviando ? 'Criando conta…' : 'Criar conta'}</button>
        <p className="acesso-rodape"><span className="suave">Já tem conta?</span><Link className="btn-link" to="/login">Entrar</Link></p>
      </form>
    </Acesso>
  );
}
