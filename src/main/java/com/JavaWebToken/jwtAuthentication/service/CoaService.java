package com.JavaWebToken.jwtAuthentication.service;



import com.JavaWebToken.jwtAuthentication.dto.CoaDTO;
import com.JavaWebToken.jwtAuthentication.entity.Coa;
import com.JavaWebToken.jwtAuthentication.repository.CoaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CoaService {

    private final CoaRepository coaRepository;

    public Coa createCoa(Coa coa) {
        coa.setCreatedDate(LocalDateTime.now());
        coa.setModifiedDate(LocalDateTime.now());
        return coaRepository.save(coa);
    }

    public Coa updateCoa(Long id, Coa updated) {
        Optional<Coa> opt = coaRepository.findById(id);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("COA not found with id: " + id);
        }
        Coa existing = opt.get();
        existing.setCoaCode(updated.getCoaCode());
        existing.setCoaName(updated.getCoaName());
        existing.setDescription(updated.getDescription());
        existing.setSqlScript(updated.getSqlScript());
        existing.setModifiedBy(updated.getModifiedBy());
        existing.setModifiedDate(LocalDateTime.now());
        return coaRepository.save(existing);
    }

    public List<Coa> listAll() {
        return coaRepository.findAll();
    }

    public Optional<Coa> findById(Long id) {
        return coaRepository.findById(id);
    }

    /**
     * Fetch all COA and convert to DTO
     */
    public List<CoaDTO> getAllCoa() {
        return coaRepository.findAll()
                .stream()
                .map(coa -> {
                    CoaDTO dto = new CoaDTO();
                    dto.setCoaId(coa.getCoaId());
                    dto.setCoaCode(coa.getCoaCode());
                    dto.setCoaName(coa.getCoaName());
                    dto.setDescription(coa.getDescription());
                    dto.setSqlScript(coa.getSqlScript());
                    dto.setCreatedBy(coa.getCreatedBy());
                    return dto;
                })
                .toList();
    }

    public void deleteCoa(Long id) {
        coaRepository.deleteById(id);
    }


}

