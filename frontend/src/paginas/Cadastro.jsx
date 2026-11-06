import { useState } from 'react';
import { Link } from 'react-router-dom';
import Acesso from '../componentes/Acesso.jsx';
import Campo from '../componentes/Campo.jsx';

export default function Cadastro() {
  const [dados, definirDados] = useState({ nome: '', email: '', senha: '', confirmacao: '' });
  const [erros, definirErros] = useState({});
  const [pendente, definirPendente] = useState(false);
  const campo = (nome) => ({ value: dados[nome], erro: erros[nome], onChange: (e) => definirDados({ ...dados, [nome]: e.target.value }) });

  function cadastrar(evento) {
    evento.preventDefault();
    const novos = {};
    if (!dados.nome.trim()) novos.nome = 'Informe seu nome';
    if (!dados.email.trim()) novos.email = 'Informe o e-mail';
    if (!dados.senha) novos.senha = 'Informe a senha';
    if (dados.senha !== dados.confirmacao) novos.confirmacao = 'As senhas não conferem';
    definirErros(novos);
    definirPendente(Object.keys(novos).length === 0);
  }

  return (
    <Acesso>
      <form className="acesso-caixa" onSubmit={cadastrar} noValidate>
        <h1>Criar conta</h1>
        {pendente && <div className="aviso aviso-erro" role="alert"><p>Cadastro ainda não conectado ao servidor. Nenhuma conta foi criada.</p></div>}
        <Campo id="nome" rotulo="Nome completo" type="text" autoComplete="name" {...campo('nome')} />
        <Campo id="email" rotulo="E-mail" type="email" autoComplete="email" {...campo('email')} />
        <Campo id="senha" rotulo="Senha" type="password" autoComplete="new-password" {...campo('senha')} />
        <Campo id="confirmacao" rotulo="Confirmar senha" type="password" autoComplete="new-password" {...campo('confirmacao')} />
        <button type="submit" className="btn btn-primario btn-bloco">Criar conta</button>
        <p className="acesso-rodape"><span className="suave">Já tem conta?</span><Link className="btn-link" to="/login">Entrar</Link></p>
      </form>
    </Acesso>
  );
}
