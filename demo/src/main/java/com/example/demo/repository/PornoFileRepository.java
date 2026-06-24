package com.example.demo.repository;

import com.example.demo.entity.PornFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PornoFileRepository extends JpaRepository<PornFile, Long> {

    
}
