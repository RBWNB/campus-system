package com.example.campus.controller;

import com.example.campus.entity.Announcement;
import com.example.campus.repository.AnnouncementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementRepository announcementRepository;

    /** 获取所有公告 **/
    @GetMapping
    public List<Announcement> list() {
        return announcementRepository.findAll();
    }

    /** 获取单个公告详情 **/
    @GetMapping("/{id}") // 对应前端 /api/announcements/{id} 的调用
    public ResponseEntity<Announcement> getAnnouncementById(@PathVariable Long id) {
        return announcementRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** 创建或更新公告（带置顶唯一性逻辑） **/
    @PostMapping
    public Announcement saveAnnouncement(@RequestBody Announcement announcement) {

        // 检查是否为新公告 (ID 为 null)，只在创建时设置发布时间
        if (announcement.getId() == null) {
            announcement.setPublishedAt(Timestamp.from(Instant.now()));
        } else {
            // 需要从数据库中获取原始时间戳并重新设置，以防止它被 @RequestBody 覆盖为 null。
            // （如果前端没有在 announcementData 中传递 publishedAt，它默认为 null，JPA 可能会尝试更新它）
            Announcement existing = announcementRepository.findById(announcement.getId()).orElse(null);
            if (existing != null) {
                announcement.setPublishedAt(existing.getPublishedAt()); // 保留原始发布时间
            } else {
                // 这种情况理论上不应该发生，但作为B方案
                announcement.setPublishedAt(Timestamp.from(Instant.now()));
            }
        }

        // 先保存公告以确保它有一个有效的ID（特别是对于新建的公告）
        Announcement savedAnnouncement = announcementRepository.save(announcement);

        // 如果当前公告设置为置顶，则取消其他所有置顶
        if (savedAnnouncement.isPinned()) { // 最好使用返回的实体进行检查
            List<Announcement> all = announcementRepository.findAll();
            for (Announcement a : all) {
                //使用 savedAnnouncement.getId() 来比较，避免空指针异常
                if (a.isPinned() && !a.getId().equals(savedAnnouncement.getId())) {
                    a.setPinned(false);
                    announcementRepository.save(a); // 保存被取消置顶的公告
                }
            }
        }
        // 返回最终处理后的公告实体
        return savedAnnouncement;
    }

    /** 获取置顶公告 **/
    @GetMapping("/top")
    public ResponseEntity<?> getTopAnnouncement() {
        return announcementRepository.findFirstByPinnedTrue()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }


    /** 删除公告 **/
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        announcementRepository.deleteById(id);
    }
}
