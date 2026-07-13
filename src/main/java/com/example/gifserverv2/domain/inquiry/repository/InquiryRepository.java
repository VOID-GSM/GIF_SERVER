package com.example.gifserverv2.domain.inquiry.repository;

import com.example.gifserverv2.domain.inquiry.entity.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    List<Inquiry> findAllByCreatedByUserIdOrderByCreatedAtDesc(Long userId);
    Page<Inquiry> findAllByCreatedByUserId(Long userId, Pageable pageable);
    Page<Inquiry> findAll(Pageable pageable);
    List<Inquiry> findAllByOrderByCreatedAtDesc();

}