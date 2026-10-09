package br.ifsp.locacao.api;
import java.util.NoSuchElementException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataAccessException;

@RestControllerAdvice
public class ApiExceptionHandler {
    public record Erro(String mensagem) {}
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Erro> validacao(IllegalArgumentException e) { return ResponseEntity.badRequest().body(new Erro(e.getMessage())); }
    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<Erro> conflito(IllegalStateException e) { return ResponseEntity.status(409).body(new Erro(e.getMessage())); }
    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<Erro> ausente(NoSuchElementException e) { return ResponseEntity.status(404).body(new Erro(e.getMessage())); }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    ResponseEntity<Erro> formato(Exception e) { return ResponseEntity.badRequest().body(new Erro("Requisição inválida: confira datas, identificadores e campos obrigatórios")); }
    @ExceptionHandler(DataAccessException.class)
    ResponseEntity<Erro> banco(DataAccessException e) { return ResponseEntity.status(503).body(new Erro("Banco indisponível; tente novamente")); }
}
