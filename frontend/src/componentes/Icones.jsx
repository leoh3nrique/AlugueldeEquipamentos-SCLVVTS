function Icone({ tamanho = 18, largura = 2.4, children }) {
  return (
    <svg width={tamanho} height={tamanho} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={largura} strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      {children}
    </svg>
  );
}

export const IconeMais = () => <Icone><path d="M12 5v14M5 12h14" /></Icone>;
export const IconeFechar = ({ tamanho }) => <Icone tamanho={tamanho} largura={2.2}><path d="M6 6l12 12M18 6L6 18" /></Icone>;
export const IconeVoltar = () => <Icone largura={2.2}><path d="M15 5l-7 7 7 7" /></Icone>;
export const IconeConfirmar = () => <Icone tamanho={20}><path d="M5 13l4 4L19 7" /></Icone>;
