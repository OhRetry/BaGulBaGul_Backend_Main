package com.BaGulBaGul.BaGulBaGul.domain.user.alarm.repository;

import com.BaGulBaGul.BaGulBaGul.domain.user.Alarm;
import com.BaGulBaGul.BaGulBaGul.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    @Query(
            value = "SELECT al FROM Alarm al WHERE al.user.id = :userId ORDER BY al.time DESC",
            countQuery = "SELECT count(*) FROM Alarm al WHERE al.user.id = :userId"
    )
    Page<Alarm> findAlarmPageOrderByTime(@Param("userId") Long userId, Pageable pageable);

    @Modifying
    @Query(
            value = "DELETE FROM Alarm al WHERE al.user.id = :userId"
    )
    void deleteAllByUserId(@Param("userId") Long userId);
}
