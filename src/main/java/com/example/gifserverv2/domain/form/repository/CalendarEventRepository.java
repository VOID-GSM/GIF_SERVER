package com.example.gifserverv2.domain.form.repository;

import com.example.gifserverv2.domain.form.entity.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {
    @Query("SELECT e FROM CalendarEvent e " +
            "JOIN FETCH e.formFieldAnswer fa " +
            "JOIN FETCH fa.formSubmit fs " +
            "WHERE e.startDate <= :today AND e.endDate >= :today")
    List<CalendarEvent> findAllActiveEventsOnDate(@Param("today") LocalDate today);
}
