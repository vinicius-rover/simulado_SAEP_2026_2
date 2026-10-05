package com.example.demo.repositories;

import com.example.demo.entities.UsuarioEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    Optional<UsuarioEntity> findByEmailAndSenha(String email, String senha);

    Optional<UsuarioEntity> findByEmail(String email);
}
