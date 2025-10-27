package ru.practicum.shareit.booking.dao;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
        List<Booking> findByBookerId(Long bookerId, Sort sort);

        List<Booking> findByBookerIdAndEndIsBefore(Long bookerId, LocalDateTime end, Sort sort);

        List<Booking> findByBookerIdAndStartIsAfter(Long bookerId, LocalDateTime start, Sort sort);

        List<Booking> findByBookerIdAndStartIsBeforeAndEndIsAfter(Long bookerId, LocalDateTime start, LocalDateTime end,
                        Sort sort);

        List<Booking> findByBookerIdAndStatusIs(Long bookerId, Status status, Sort sort);

        List<Booking> findByItemIdIn(List<Long> itemIds, Sort sort);

        List<Booking> findByItemIdInAndEndIsBefore(List<Long> itemIds, LocalDateTime end, Sort sort);

        List<Booking> findByItemIdInAndStartIsAfter(List<Long> itemIds, LocalDateTime start, Sort sort);

        List<Booking> findByItemIdInAndStartIsBeforeAndEndIsAfter(List<Long> itemIds, LocalDateTime start,
                        LocalDateTime end, Sort sort);

        List<Booking> findByItemIdInAndStatusIs(List<Long> itemIds, Status status, Sort sort);

        @Query("select b from Booking b where b.item.id = ?1 and b.start < ?2 and b.status = 'APPROVED' order by b.end desc")
        List<Booking> findLastBookingByItemId(Long itemId, LocalDateTime time);

        @Query("select b from Booking b where b.item.id = ?1 and b.start > ?2 and b.status = 'APPROVED' order by b.start")
        List<Booking> findNextBookingByItemId(Long itemId, LocalDateTime time);

        @Query("select b.id from Booking b where b.booker.id = ?1 and b.item.id = ?2 and b.end < ?3 and b.status = 'APPROVED'")
        List<Long> findByBookerIdAndItemIdAndEndIsBeforeAndStatusIs(Long bookerId, Long itemId, LocalDateTime end,
                        Status status);
}