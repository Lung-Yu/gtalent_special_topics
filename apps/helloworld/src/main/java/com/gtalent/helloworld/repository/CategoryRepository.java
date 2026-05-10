package com.gtalent.helloworld.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gtalent.helloworld.domain.model.Category;
import com.gtalent.helloworld.domain.valueobject.TypeCategory;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByType(TypeCategory type);

    Optional<Category> findByTypeAndName(TypeCategory type, String name);

    boolean existsByTypeAndName(TypeCategory type, String name);

    List<Category> findByNameIn(List<String> names);
}
