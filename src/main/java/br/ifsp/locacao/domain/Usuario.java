package br.ifsp.locacao.domain;

import java.util.UUID;

public record Usuario(UUID id, String nome, String email, String senhaHash) {}
