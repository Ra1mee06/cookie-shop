// ProductRepository.java
package com.cookieshop.repository;

import com.cookieshop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByTitleContainingIgnoreCase(String title);
    List<Product> findByOrderByPriceAsc();
    List<Product> findByOrderByPriceDesc();
}