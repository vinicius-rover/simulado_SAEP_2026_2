package com.example.demo.services;


import com.example.demo.dtos.UsuarioDto;
import com.example.demo.entities.UsuarioEntity;
import com.example.demo.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;


    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioDto autenticar(String email, String senha) {
        Optional<UsuarioEntity> usuarioEntityOptional = usuarioRepository.findByEmailAndSenha(email, senha);

        if (usuarioEntityOptional.isEmpty()) {
            return null;
        }

        return converterEntityParaDto(usuarioEntityOptional.get());
    }

    public List<UsuarioDto> listarTodos() {
        List<UsuarioDto> usuarios = new ArrayList<>();

        for (UsuarioEntity usuarioEntity : usuarioRepository.findAll()) {
            usuarios.add(converterEntityParaDto(usuarioEntity));
        }

        return usuarios;
    }

    public UsuarioDto buscarPorId(Long id) {
        Optional<UsuarioEntity> usuarioEntity = usuarioRepository.findById(id);

        if (usuarioEntity.isEmpty()) {
            return null;
        }

        return converterEntityParaDto(usuarioEntity.get());
    }

    public UsuarioDto salvar(UsuarioDto usuarioDto) {
        UsuarioEntity usuarioEntity;

        if (usuarioDto.getId() != null) {
            Optional<UsuarioEntity> usuarioExistente = usuarioRepository.findById(usuarioDto.getId());

            if (usuarioExistente.isEmpty()) {
                return null;
            }

            usuarioEntity = usuarioExistente.get();
            usuarioEntity.setNome(usuarioDto.getNome());
            usuarioEntity.setEmail(usuarioDto.getEmail());
            usuarioEntity.setUserRole(usuarioDto.getUserRole() == null ? 0 : usuarioDto.getUserRole());
            // A senha nao e alterada pelo cadastro/edicao normal.
        } else {
            usuarioEntity = converterDtoParaEntity(usuarioDto);
            if (usuarioEntity.getSenha() != null && !usuarioEntity.getSenha().isBlank()) {
                usuarioEntity.setSenha(usuarioEntity.getSenha());
            }
        }

        usuarioEntity = usuarioRepository.save(usuarioEntity);
        return converterEntityParaDto(usuarioEntity);
    }

    public void excluir(Long id) {
        usuarioRepository.deleteById(id);
    }



    private UsuarioEntity converterDtoParaEntity(UsuarioDto usuarioDto) {
        UsuarioEntity usuarioEntity = new UsuarioEntity();

        usuarioEntity.setId(usuarioDto.getId());
        usuarioEntity.setNome(usuarioDto.getNome());
        usuarioEntity.setEmail(usuarioDto.getEmail());
        usuarioEntity.setSenha(usuarioDto.getSenha());
        usuarioEntity.setUserRole(usuarioDto.getUserRole() == null ? 0 : usuarioDto.getUserRole());

        return usuarioEntity;
    }

    private UsuarioDto converterEntityParaDto(UsuarioEntity usuarioEntity) {
        UsuarioDto usuarioDto = new UsuarioDto();

        usuarioDto.setId(usuarioEntity.getId());
        usuarioDto.setNome(usuarioEntity.getNome());
        usuarioDto.setEmail(usuarioEntity.getEmail());
        usuarioDto.setSenha(usuarioEntity.getSenha());
        usuarioDto.setUserRole(usuarioEntity.getUserRole());

        return usuarioDto;
    }
}
