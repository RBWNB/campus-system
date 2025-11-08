package com.example.campus.repository;
import java.util.Optional;
import com.example.campus.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    Optional<Announcement> findFirstByPinnedTrue();
}
