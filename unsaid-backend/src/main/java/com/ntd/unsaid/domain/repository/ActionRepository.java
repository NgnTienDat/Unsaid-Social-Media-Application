package com.ntd.unsaid.domain.repository;

import com.ntd.unsaid.domain.entity.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionRepository extends JpaRepository<Action, String> {

}