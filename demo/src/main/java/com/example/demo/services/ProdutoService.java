package com.example.demo.services;


import com.example.demo.dtos.ProdutoDto;
import com.example.demo.entities.CategoriaEntity;
import com.example.demo.entities.MovimentacaoEntity;
import com.example.demo.entities.ProdutoEntity;
import com.example.demo.entities.UsuarioEntity;
import com.example.demo.repositories.CategoriaRepository;
import com.example.demo.repositories.MovimentacaoRepository;
import com.example.demo.repositories.ProdutoRepository;
import com.example.demo.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProdutoService {
    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public ProdutoService(ProdutoRepository produtoRepository,
                          CategoriaRepository categoriaRepository,
                          MovimentacaoRepository movimentacaoRepository,
                          UsuarioRepository usuarioRepository) {
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<ProdutoDto> listarTodos() {
        List<ProdutoDto> produtos = new ArrayList<>();
        for (ProdutoEntity entity : produtoRepository.findAll()) {
            produtos.add(converterEntityParaDto(entity));
        }
        return produtos;
    }

    public ProdutoDto buscarPorId(Long id) {
        Optional<ProdutoEntity> produto = produtoRepository.findById(id);
        return produto.map(this::converterEntityParaDto).orElse(null);
    }

    @Transactional
    public ProdutoDto salvar(ProdutoDto dto, Long usuarioId) {
        ProdutoEntity entity;
        if (dto.getId() != null) {
            Optional<ProdutoEntity> existente = produtoRepository.findById(dto.getId());
            if (existente.isEmpty()) return null;
            entity = existente.get();
        } else {
            entity = new ProdutoEntity();
        }

        entity.setNome(dto.getNome());
        entity.setPreco(dto.getPreco());
        if (dto.getId() == null) {
            entity.setEstoque(dto.getEstoque() == null ? 0 : dto.getEstoque());
        }

        entity.setEstoqueMinimo(dto.getEstoqueMinimo() == null ? 0 : dto.getEstoqueMinimo());
        entity.setDescricao(dto.getDescricao());
        entity.setAtivo(dto.getAtivo() == null ? true : dto.getAtivo());
        entity.setUsuarioId(usuarioId);

        if (dto.getCategoriaId() != null) {
            Optional<CategoriaEntity> categoria = categoriaRepository.findById(dto.getCategoriaId());
            if (categoria.isEmpty()) return null;
            entity.setCategoria(categoria.get());
        } else {
            entity.setCategoria(null);
        }

        boolean novoProduto = entity.getId() == null;
        Integer estoqueInicial = entity.getEstoque();

        ProdutoEntity salvo = produtoRepository.save(entity);

        if (novoProduto && estoqueInicial != null && estoqueInicial > 0 && usuarioId != null) {
            Optional<UsuarioEntity> usuario = usuarioRepository.findById(usuarioId);

            if (usuario.isPresent()) {
                MovimentacaoEntity movimentacao = new MovimentacaoEntity();
                movimentacao.setProduto(salvo);
                movimentacao.setUsuario(usuario.get());
                movimentacao.setTipo("ENTRADA");
                movimentacao.setQuantidade(estoqueInicial);
                movimentacao.setEstoqueAnterior(0);
                movimentacao.setEstoqueAtual(estoqueInicial);
                movimentacao.setData(java.time.LocalDateTime.now());

                movimentacaoRepository.save(movimentacao);
            }
        }

        return converterEntityParaDto(salvo);
    }

    public boolean excluir(Long id) {
        if (!produtoRepository.existsById(id)) return false;

        if (movimentacaoRepository.existsByProdutoId(id)) {
            return false;
        }

        produtoRepository.deleteById(id);
        return true;
    }

    private ProdutoDto converterEntityParaDto(ProdutoEntity entity) {
        ProdutoDto dto = new ProdutoDto();
        dto.setId(entity.getId());
        dto.setNome(entity.getNome());
        dto.setPreco(entity.getPreco());
        dto.setEstoque(entity.getEstoque());
        dto.setEstoqueMinimo(entity.getEstoqueMinimo());
        dto.setDescricao(entity.getDescricao());
        dto.setAtivo(entity.getAtivo());
        dto.setUsuarioId(entity.getUsuarioId());
        if (entity.getCategoria() != null) {
            dto.setCategoriaId(entity.getCategoria().getId());
            dto.setCategoriaNome(entity.getCategoria().getNome());
        }
        return dto;
    }
}
