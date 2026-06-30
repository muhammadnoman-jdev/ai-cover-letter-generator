package com.noman.coverletter.repository;

import com.noman.coverletter.entity.CoverLetter;
import com.noman.coverletter.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoverLetterRepository extends JpaRepository<CoverLetter, Long> {
    List<CoverLetter> findByUserOrderByCreatedAtDesc(User user);
    Optional<CoverLetter> findByIdAndUser(Long id, User user);
    long countByUser(User user);
}