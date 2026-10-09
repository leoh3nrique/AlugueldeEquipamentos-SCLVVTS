package br.ifsp.locacao.suites;
import org.junit.platform.suite.api.*;
@Suite
@SelectPackages({"br.ifsp.locacao.application", "br.ifsp.locacao.functional"})
@IncludeTags("UnitTest")
public class UnitTestSuite {}
