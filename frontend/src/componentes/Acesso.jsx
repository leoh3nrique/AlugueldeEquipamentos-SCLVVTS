export default function Acesso({ children }) {
  return (
    <div className="acesso">
      <section className="acesso-marca">
        <p className="logo"><span className="logo-marca" aria-hidden="true" />Locação de Equipamentos</p>
        <h2>Reserve, retire, renove e devolva seus equipamentos em um só lugar.</h2>
      </section>
      <main className="acesso-form">{children}</main>
    </div>
  );
}
