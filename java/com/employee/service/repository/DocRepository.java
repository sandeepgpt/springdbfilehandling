package com.employee.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.employee.service.entity.Doc;

public interface DocRepository extends JpaRepository<Doc, Integer>{

}
