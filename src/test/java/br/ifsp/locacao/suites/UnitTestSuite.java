package br.ifsp.locacao.suites;
import org.junit.platform.suite.api.*;
@Suite
@SelectPackages({"br.ifsp.locacao.application", "br.ifsp.locacao.functional", "br.ifsp.locacao.structural", "br.ifsp.locacao.mutation"})
@IncludeTags("UnitTest")
public class UnitTestSuite {}
