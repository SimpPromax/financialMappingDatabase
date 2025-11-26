package com.JavaWebToken.jwtAuthentication.repository;



import com.JavaWebToken.jwtAuthentication.entity.Coa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoaRepository extends JpaRepository<Coa, Long> {
}

