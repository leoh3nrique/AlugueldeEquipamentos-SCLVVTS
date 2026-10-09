package br.ifsp.locacao.api;

import br.ifsp.locacao.application.*;
import br.ifsp.locacao.domain.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.math.BigDecimal;
import java.time.*;
import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/locacoes")
public class LocacaoController {
    private final CriarLocacaoService criar;
    private final AlterarLocacaoService alterar;
    private final ConfirmarRetiradaService retirada;
    private final RenovarLocacaoService renovar;
    private final RegistrarDevolucaoService devolver;
    private final CancelarLocacaoService cancelar;
    private final ConsultarLocacoesService consultar;
    public LocacaoController(CriarLocacaoService criar, AlterarLocacaoService alterar,
            ConfirmarRetiradaService retirada, RenovarLocacaoService renovar,
            RegistrarDevolucaoService devolver, CancelarLocacaoService cancelar, ConsultarLocacoesService consultar) {
        this.criar=criar; this.alterar=alterar; this.retirada=retirada; this.renovar=renovar;
        this.devolver=devolver; this.cancelar=cancelar; this.consultar=consultar;
    }
    public record ItemEntrada(String codigo, String descricao, BigDecimal diaria) {
        ItemLocacao dominio() { return new ItemLocacao(new CodigoEquipamento(codigo), descricao, new Dinheiro(diaria)); }
    }
    public record ReservaEntrada(String clienteId, LocalDate inicio, LocalDate fim, List<ItemEntrada> itens) {
        List<ItemLocacao> itensDominio() {
            if (itens == null || itens.stream().anyMatch(Objects::isNull)) throw new IllegalArgumentException("Itens são obrigatórios");
            return itens.stream().map(ItemEntrada::dominio).toList();
        }
        PeriodoLocacao periodo() { return new PeriodoLocacao(inicio, fim); }
    }
    public record DataEntrada(LocalDate data) {}
    public record RenovacaoEntrada(Integer dias, LocalDate data) {}
    public record DevolucaoEntrada(List<String> codigos, LocalDate data) {}
    public record ItemSaida(String codigo, String descricao, BigDecimal diaria, boolean devolvido, LocalDate dataDevolucao, LocalDate fimContratado) {}
    public record LocacaoSaida(UUID id, String clienteId, Instant criadaEm, LocalDate inicio, LocalDate fim,
            EstadoLocacao estado, int renovacoes, BigDecimal valorNormal, BigDecimal multa, BigDecimal valorTotal, List<ItemSaida> itens) {
        static LocacaoSaida de(Locacao l) {
            return new LocacaoSaida(l.getId(), l.getClienteId(), l.getCriadaEm(), l.getPeriodo().inicio(), l.getPeriodo().fim(),
                    l.getEstado(), l.getQuantidadeRenovacoes(), l.getValorNormal().valor(), l.getMulta().valor(), l.getValorTotal().valor(),
                    l.dados().itens().stream().map(i -> new ItemSaida(i.codigo(), i.descricao(), i.diaria(), i.devolucao()!=null, i.devolucao(), i.fimContratado())).toList());
        }
    }
    @PostMapping public ResponseEntity<LocacaoSaida> criar(@RequestBody ReservaEntrada entrada) {
        Locacao l=criar.criar(entrada.clienteId(), entrada.periodo(), entrada.itensDominio());
        return ResponseEntity.created(URI.create("/api/locacoes/"+l.getId())).body(LocacaoSaida.de(l));
    }
    @GetMapping("/{id}") public LocacaoSaida buscar(@PathVariable UUID id) { return LocacaoSaida.de(consultar.buscarPorId(id)); }
    @GetMapping public List<LocacaoSaida> listar(@RequestParam String clienteId, @RequestParam(required=false) EstadoLocacao estado) {
        List<Locacao> resultado=estado==null ? consultar.consultarPorCliente(clienteId) : consultar.consultarPorCliente(clienteId,estado);
        return resultado.stream().map(LocacaoSaida::de).toList();
    }
    @PutMapping("/{id}") public LocacaoSaida alterar(@PathVariable UUID id, @RequestBody ReservaEntrada entrada) {
        return LocacaoSaida.de(alterar.alterar(id, entrada.periodo(), entrada.itensDominio()));
    }
    @PostMapping("/{id}/retirada") public LocacaoSaida retirar(@PathVariable UUID id, @RequestBody DataEntrada entrada) {
        return LocacaoSaida.de(retirada.confirmar(id, entrada.data()));
    }
    @PostMapping("/{id}/renovacoes") public LocacaoSaida renovar(@PathVariable UUID id, @RequestBody RenovacaoEntrada entrada) {
        if (entrada.dias()==null) throw new IllegalArgumentException("Dias são obrigatórios");
        return LocacaoSaida.de(renovar.renovar(id, entrada.dias(), entrada.data()));
    }
    @PostMapping("/{id}/devolucoes") public LocacaoSaida devolver(@PathVariable UUID id, @RequestBody DevolucaoEntrada entrada) {
        if (entrada.codigos()==null) throw new IllegalArgumentException("Códigos são obrigatórios");
        return LocacaoSaida.de(devolver.registrar(id, entrada.codigos().stream().map(CodigoEquipamento::new).toList(), entrada.data()));
    }
    @PostMapping("/{id}/cancelamento") public LocacaoSaida cancelar(@PathVariable UUID id) { return LocacaoSaida.de(cancelar.cancelar(id)); }
}
