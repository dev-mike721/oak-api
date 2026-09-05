package com.abarrotes.oakapi.repository;

import com.abarrotes.oakapi.model.LogAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Long> {
    List<LogAuditoria> findByUsuarioIdOrderByFechaHoraDesc(Integer usuarioId);
}
