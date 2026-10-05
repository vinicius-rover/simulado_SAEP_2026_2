package com.example.demo.controllers;

import com.example.demo.dtos.CategoriaDto;
import com.example.demo.dtos.MovimentacaoDto;
import com.example.demo.dtos.ProdutoDto;
import com.example.demo.dtos.UsuarioDto;
import com.example.demo.entities.ProdutoEntity;
import com.example.demo.services.CategoriaService;
import com.example.demo.services.MovimentacaoService;
import com.example.demo.services.ProdutoService;
import com.example.demo.services.UsuarioService;
import com.example.demo.sessoes.SessaoDto;
import com.example.demo.sessoes.SessaoUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
public class PageController {

	private final UsuarioService usuarioService;
	private final CategoriaService categoriaService;
	private final ProdutoService produtoService;
	private final MovimentacaoService movimentacaoService;

	public PageController(UsuarioService usuarioService,
						 CategoriaService categoriaService,
						 ProdutoService produtoService,
						 MovimentacaoService movimentacaoService) {
		this.usuarioService = usuarioService;
		this.categoriaService = categoriaService;
		this.produtoService = produtoService;
		this.movimentacaoService = movimentacaoService;
	}

	@GetMapping("/")
	public String index() {
		return "redirect:/login";
	}

	@GetMapping("/login")
	public String login() {
		return "login";
	}

	@GetMapping("/home")
	public String home(HttpSession session, Model model) {

		SessaoDto sessaoDto = SessaoUtil.ObterSessao(session);

		if (sessaoDto == null) {
			return "redirect:/login";
		}

		model.addAttribute("usuarioLogado", sessaoDto);
		model.addAttribute("isAdmin", sessaoDto.getUserRole() != null && sessaoDto.getUserRole() == 1);

		return "home";
	}

	@GetMapping("/produtos")
	public String produtos(HttpSession session, Model model, @RequestParam(required = false) Long editar,
							@RequestParam(required = false) String busca,
							@RequestParam(required = false) String categoriaBusca) {

		SessaoDto sessaoDto = SessaoUtil.ObterSessao(session);
		if (sessaoDto == null) return "redirect:/login";

		List<ProdutoDto> produtos = produtoService.listarTodos();
		if (busca != null && !busca.isBlank()) {
			String termo = busca.trim().toLowerCase(Locale.ROOT);
			produtos = produtos.stream().filter(p -> p.getNome() != null && p.getNome().toLowerCase(Locale.ROOT).contains(termo)).collect(Collectors.toList());
		}
		if (categoriaBusca != null && !categoriaBusca.isBlank()) {
			String termoCategoria = categoriaBusca.trim().toLowerCase(Locale.ROOT);
			produtos = produtos.stream().filter(p -> p.getCategoriaNome() != null && p.getCategoriaNome().toLowerCase(Locale.ROOT).contains(termoCategoria)).collect(Collectors.toList());
		}

		model.addAttribute("usuarioLogado", sessaoDto);
		model.addAttribute("produtos", produtos);
		model.addAttribute("categorias", categoriaService.obterCategorias());
		model.addAttribute("isAdmin", sessaoDto.getUserRole() != null && sessaoDto.getUserRole() == 1);
		model.addAttribute("busca", busca == null ? "" : busca);
		model.addAttribute("categoriaBusca", categoriaBusca == null ? "" : categoriaBusca);

		ProdutoDto produtoForm = new ProdutoDto();
		if (editar != null && sessaoDto.getUserRole() != null && sessaoDto.getUserRole() == 1) {
			ProdutoDto encontrado = produtoService.buscarPorId(editar);
			if (encontrado != null) produtoForm = encontrado;
		}
		model.addAttribute("produtoForm", produtoForm);
		return "produtos";
	}

	@GetMapping("/usuarios")
	public String usuarios(HttpSession session, Model model, @RequestParam(required = false) Long editar,
							@RequestParam(required = false) String busca) {

		SessaoDto sessaoDto = SessaoUtil.ObterSessao(session);
		if (sessaoDto == null) return "redirect:/login";

		List<UsuarioDto> usuarios = usuarioService.listarTodos();
		if (busca != null && !busca.isBlank()) {
			String termo = busca.trim().toLowerCase(Locale.ROOT);
			usuarios = usuarios.stream().filter(u ->
				(u.getNome() != null && u.getNome().toLowerCase(Locale.ROOT).contains(termo)) ||
				(u.getEmail() != null && u.getEmail().toLowerCase(Locale.ROOT).contains(termo)) ||
				(u.getUserRole() != null && (u.getUserRole() == 1 ? "administrador" : "usuário").contains(termo))
			).collect(Collectors.toList());
		}

		model.addAttribute("usuarioLogado", sessaoDto);
		model.addAttribute("isAdmin", sessaoDto.getUserRole() != null && sessaoDto.getUserRole() == 1);
		model.addAttribute("usuarios", usuarios);
		model.addAttribute("busca", busca == null ? "" : busca);

		UsuarioDto usuarioForm = new UsuarioDto();
		if (editar != null && sessaoDto.getUserRole() != null && sessaoDto.getUserRole() == 1) {
			UsuarioDto encontrado = usuarioService.buscarPorId(editar);
			if (encontrado != null) usuarioForm = encontrado;
		}
		model.addAttribute("usuarioForm", usuarioForm);
		return "usuarios";
	}

	@GetMapping("/estoque")
	public String estoque(HttpSession session, Model model, @RequestParam(required = false) String busca) {

		SessaoDto sessaoDto = SessaoUtil.ObterSessao(session);
		if (sessaoDto == null) return "redirect:/login";

		List<ProdutoEntity> estoque = movimentacaoService.listarEstoqueOrdenado();
		if (busca != null && !busca.isBlank()) {
			String termo = busca.trim().toLowerCase(Locale.ROOT);
			estoque = estoque.stream().filter(p -> p.getNome() != null && p.getNome().toLowerCase(Locale.ROOT).contains(termo)).collect(Collectors.toList());
		}

		model.addAttribute("usuarioLogado", sessaoDto);
		model.addAttribute("produtos", produtoService.listarTodos());
		model.addAttribute("estoque", estoque);
		model.addAttribute("baixoEstoque", movimentacaoService.listarBaixoEstoque());
		model.addAttribute("movimentacoes", movimentacaoService.listarTodos());
		model.addAttribute("isAdmin", sessaoDto.getUserRole() != null && sessaoDto.getUserRole() == 1);
		model.addAttribute("busca", busca == null ? "" : busca);
		return "estoque";
	}

	@PostMapping("/login")
	public String realizarLogin(@RequestParam String email,
								@RequestParam String senha,
								Model model,
								HttpSession session) {

		UsuarioDto usuarioDto = usuarioService.autenticar(email, senha);

		if (usuarioDto == null) {
			model.addAttribute("erro", "E-mail ou senha invalidos.");
			model.addAttribute("email", email);
			return "login";
		}

		SessaoDto sessaoDto = new SessaoDto();
		sessaoDto.setUsuarioId(usuarioDto.getId());
		sessaoDto.setUsuarioNome(usuarioDto.getNome());
		sessaoDto.setUserRole(usuarioDto.getUserRole());

		SessaoUtil.RegistrarSessao(session, sessaoDto);

		System.out.println("Sessão: ");
		System.out.println(sessaoDto.getUsuarioId());
		System.out.println(sessaoDto.getUsuarioNome());


		return "redirect:/home";
	}


	@GetMapping("/movimentacoes")
	public String movimentacoes(HttpSession session, Model model,
							@RequestParam(required = false) String buscaProduto,
							@RequestParam(required = false) String tipo,
							@RequestParam(required = false) String buscaUsuario) {

		SessaoDto sessaoDto = SessaoUtil.ObterSessao(session);
		if (sessaoDto == null) return "redirect:/login";

		List<MovimentacaoDto> movimentacoes = movimentacaoService.listarTodos();
		if (buscaProduto != null && !buscaProduto.isBlank()) {
			String termo = buscaProduto.trim().toLowerCase(Locale.ROOT);
			movimentacoes = movimentacoes.stream().filter(m -> m.getProdutoNome() != null && m.getProdutoNome().toLowerCase(Locale.ROOT).contains(termo)).collect(Collectors.toList());
		}
		if (tipo != null && !tipo.isBlank()) {
			movimentacoes = movimentacoes.stream().filter(m -> tipo.equalsIgnoreCase(m.getTipo())).collect(Collectors.toList());
		}
		if (buscaUsuario != null && !buscaUsuario.isBlank()) {
			String termo = buscaUsuario.trim().toLowerCase(Locale.ROOT);
			movimentacoes = movimentacoes.stream().filter(m -> m.getUsuarioNome() != null && m.getUsuarioNome().toLowerCase(Locale.ROOT).contains(termo)).collect(Collectors.toList());
		}

		model.addAttribute("usuarioLogado", sessaoDto);
		model.addAttribute("movimentacoes", movimentacoes);
		model.addAttribute("buscaProduto", buscaProduto == null ? "" : buscaProduto);
		model.addAttribute("tipo", tipo == null ? "" : tipo);
		model.addAttribute("buscaUsuario", buscaUsuario == null ? "" : buscaUsuario);
		return "movimentacoes";
	}

	@GetMapping("/categorias")
	public String categorias(HttpSession session, Model model, @RequestParam(required = false) Long editar,
							@RequestParam(required = false) String busca) {

		SessaoDto sessaoDto = SessaoUtil.ObterSessao(session);
		if (sessaoDto == null) return "redirect:/login";

		List<CategoriaDto> categorias = categoriaService.obterCategorias();
		if (busca != null && !busca.isBlank()) {
			String termo = busca.trim().toLowerCase(Locale.ROOT);
			categorias = categorias.stream().filter(c -> c.getNome() != null && c.getNome().toLowerCase(Locale.ROOT).contains(termo)).collect(Collectors.toList());
		}

		model.addAttribute("usuarioLogado", sessaoDto);
		model.addAttribute("isAdmin", sessaoDto.getUserRole() != null && sessaoDto.getUserRole() == 1);
		model.addAttribute("categorias", categorias);
		model.addAttribute("busca", busca == null ? "" : busca);

		CategoriaDto categoriaForm = new CategoriaDto();
		if (editar != null && sessaoDto.getUserRole() != null && sessaoDto.getUserRole() == 1) {
			CategoriaDto encontrado = categoriaService.obterCategoriaPorId(editar);
			if (encontrado != null) categoriaForm = encontrado;
		}
		model.addAttribute("categoriaForm", categoriaForm);
		return "categorias";
	}

	@GetMapping("/categoria")
	public String categoriaLegada() {
		return "redirect:/categorias";
	}
}