package com.example.demo.repositories;

import com.example.demo.entities.CategoriaEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<CategoriaEntity, Long> {

}
