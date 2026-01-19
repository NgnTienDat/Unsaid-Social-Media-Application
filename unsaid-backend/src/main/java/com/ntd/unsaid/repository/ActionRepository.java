package com.ntd.unsaid.repository;

import com.ntd.unsaid.entity.Action;
import com.ntd.unsaid.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionRepository extends JpaRepository<Action, String> {

}