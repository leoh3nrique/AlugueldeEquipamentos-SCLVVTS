export default function Campo({ id, rotulo, erro, ajuda, mono, ...entrada }) {
  const classes = ['entrada', mono && 'entrada-mono', erro && 'invalida'].filter(Boolean).join(' ');
  const idDescricao = erro || ajuda ? `${id}-descricao` : undefined;
  return (
    <div className="campo">
      <label htmlFor={id}>{rotulo}</label>
      <input id={id} className={classes} aria-invalid={Boolean(erro)} aria-describedby={idDescricao} {...entrada} />
      {erro && <p className="erro-campo" id={idDescricao}>{erro}</p>}
      {!erro && ajuda && <p className="ajuda" id={idDescricao}>{ajuda}</p>}
    </div>
  );
}
