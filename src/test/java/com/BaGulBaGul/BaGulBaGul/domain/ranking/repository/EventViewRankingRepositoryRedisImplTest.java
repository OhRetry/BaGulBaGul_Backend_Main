package com.BaGulBaGul.BaGulBaGul.domain.ranking.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.BaGulBaGul.BaGulBaGul.domain.ranking.dto.service.response.EventViewsRankingItemInfo;
import com.BaGulBaGul.BaGulBaGul.domain.ranking.repository.EventViewRankingRepositoryRedisImpl;
import com.BaGulBaGul.BaGulBaGul.extension.AllTestContainerExtension;
import com.BaGulBaGul.BaGulBaGul.extension.MysqlTestContainerExtension;
import com.BaGulBaGul.BaGulBaGul.extension.RedisTestContainerExtension;
import com.BaGulBaGul.BaGulBaGul.domain.event.constant.EventType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ExtendWith(AllTestContainerExtension.class)
@ActiveProfiles("test")
class EventViewRankingRepositoryRedisImplTest {

    @Autowired
    EventViewRankingRepositoryRedisImpl eventViewRankingRepository;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Nested
    @DisplayName("최근 7일 조회수 랭킹")
    class SevenDays {
        Map<EventType, String> zSetKeys = new HashMap<>();
        @BeforeEach
        void init() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            Method privateMethod = EventViewRankingRepositoryRedisImpl.class
                    .getDeclaredMethod("get7DaysKey", EventType.class);
            privateMethod.setAccessible(true);
            for(EventType eventType : EventType.values()) {
                zSetKeys.put(
                        eventType,
                        (String)privateMethod.invoke(eventViewRankingRepository, eventType)
                );
            }
        }

        @AfterEach
        void tearDown() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            for(String key : zSetKeys.values()) {
                redisTemplate.delete(key);
            }
        }

        @ParameterizedTest
        @EnumSource(EventType.class)
        @DisplayName("조회수 증가 감소 테스트")
        void increaseViewCountTest(EventType eventType) {
            //given
            Long eventId = 1L;
            Long amount1 = 2L;
            Long amount2 = 39999999999L;
            Long amount3 = -4L;

            //when
            eventViewRankingRepository.increase7DaysViewCount(eventId, eventType, amount1);
            eventViewRankingRepository.increase7DaysViewCount(eventId, eventType, amount2);
            eventViewRankingRepository.increase7DaysViewCount(eventId, eventType, amount3);

            //then
            String key = zSetKeys.get(eventType);
            double score = redisTemplate.opsForZSet().score(key, String.valueOf(eventId));
            assertThat((long)score).isEqualTo(amount1 + amount2 + amount3);
        }

        @ParameterizedTest
        @EnumSource(EventType.class)
        @DisplayName("조회수 배치 증가 테스트")
        void increaseViewCountsTest(EventType eventType) {
            //given
            List<EventViewsRankingItemInfo> items = new ArrayList<>();
            int eventCount = 100;
            for(int i = 0; i < eventCount; i++) {
                items.add(new EventViewsRankingItemInfo((long) i, (long) i));
            }
            //when
            eventViewRankingRepository.increase7DaysViewCounts(
                    eventType,
                    items
            );
            eventViewRankingRepository.increase7DaysViewCounts(
                    eventType,
                    items
            );

            //then
            List<TypedTuple<String>> typedTuples = redisTemplate.opsForZSet().rangeWithScores(
                    zSetKeys.get(eventType),
                    0,
                    eventCount
            ).stream().collect(Collectors.toList());

            for(int i=0; i<eventCount; i++) {
                assertThat(typedTuples.get(i).getScore()).isEqualTo(i * 2);
            }
        }

        @ParameterizedTest
        @EnumSource(EventType.class)
        @DisplayName("조회수 배치 감소 테스트")
        void decreaseViewCountsTest(EventType eventType) {
            //given
            List<EventViewsRankingItemInfo> items = new ArrayList<>();
            int eventCount = 100;
            for(int i = 0; i < eventCount; i++) {
                items.add(new EventViewsRankingItemInfo((long) i, (long) i));
            }
            //when
            eventViewRankingRepository.decrease7DaysViewCounts(
                    eventType,
                    items
            );
            eventViewRankingRepository.decrease7DaysViewCounts(
                    eventType,
                    items
            );

            //then
            List<TypedTuple<String>> typedTuples = redisTemplate.opsForZSet().reverseRangeWithScores(
                    zSetKeys.get(eventType),
                    0,
                    eventCount
            ).stream().collect(Collectors.toList());

            for(int i=0; i<eventCount; i++) {
                assertThat(typedTuples.get(i).getScore()).isEqualTo(-i * 2);
            }
        }

        @ParameterizedTest
        @EnumSource(EventType.class)
        @DisplayName("조회수 내림차순 조회가 작동하는지 랜덤값으로 테스트")
        void getTopKRankEventTest(EventType eventType) {
            //given
            Random random = new Random();
            Map<Long, Long> eventViewCountMap = new TreeMap<>(Comparator.reverseOrder());
            int eventCount = 100;
            Long maxView = 1000000000L;
            for(int i=0; i<eventCount; i++) {
                eventViewCountMap.put(random.nextLong() % maxView, (long) i);
            }
            for(Map.Entry<Long, Long> entry : eventViewCountMap.entrySet()) {
                eventViewRankingRepository.increase7DaysViewCount(entry.getValue(), eventType, entry.getKey());
            }

            //when
            List<Long> topKRankEventFrom7DaysViewCount = eventViewRankingRepository.getTopKRankEventFrom7DaysViewCount(
                    eventType, eventCount);

            //then
            List<Long> answer = eventViewCountMap.values().stream().collect(Collectors.toList());
            assertThat(answer.equals(topKRankEventFrom7DaysViewCount)).isTrue();
        }

        @ParameterizedTest
        @EnumSource(EventType.class)
        @DisplayName("조회수 삭제 테스트")
        void deleteAll7DaysViewCountTest(EventType targetEventType) {
            //given
            for(EventType eventType : EventType.values()) {
                eventViewRankingRepository.increase7DaysViewCount(1L, eventType, 1L);
            }

            //when
            eventViewRankingRepository.deleteAll7DaysViewCount(targetEventType);

            //then
            for(EventType eventType : EventType.values()) {
                List<Long> topK = eventViewRankingRepository.getTopKRankEventFrom7DaysViewCount(eventType, 10);
                if(eventType == targetEventType) {
                    assertThat(topK.isEmpty()).isTrue();
                } else {
                    assertThat(topK.isEmpty()).isFalse();
                }
            }
        }
    }

    @Nested
    @DisplayName("특정 날짜 조회수")
    class Day {

        LocalDateTime testTime = LocalDateTime.now();
        LocalDateTime testTime2 = LocalDateTime.now().minusDays(1);
        Map<EventType, String> hSetKeys = new HashMap<>();
        Map<EventType, String> hSetKeys2 = new HashMap<>();

        @BeforeEach
        void init() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

            Method privateMethod = EventViewRankingRepositoryRedisImpl.class
                    .getDeclaredMethod("getDayKey", EventType.class, LocalDateTime.class);
            privateMethod.setAccessible(true);
            for(EventType eventType : EventType.values()) {
                hSetKeys.put(
                        eventType,
                        (String)privateMethod.invoke(eventViewRankingRepository, eventType, testTime)
                );
                hSetKeys2.put(
                        eventType,
                        (String)privateMethod.invoke(eventViewRankingRepository, eventType, testTime2)
                );
            }
        }

        @AfterEach
        void tearDown() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            for(String key : hSetKeys.values()) {
                redisTemplate.delete(key);
            }
            for(String key : hSetKeys2.values()) {
                redisTemplate.delete(key);
            }
        }

        @ParameterizedTest
        @EnumSource(EventType.class)
        @DisplayName("조회수 증가 감소 테스트")
        void increaseViewCountTest(EventType eventType) {
            //given
            Long eventId = 1L;
            Long amount1 = 2L;
            Long amount2 = 39999999999L;
            Long amount3 = -4L;

            //when
            eventViewRankingRepository.increaseDayViewCount(eventId, eventType, testTime, amount1);
            eventViewRankingRepository.increaseDayViewCount(eventId, eventType, testTime, amount2);
            eventViewRankingRepository.increaseDayViewCount(eventId, eventType, testTime, amount3);

            //then
            String key = hSetKeys.get(eventType);
            String score = redisTemplate.<String, String>opsForHash().get(key, String.valueOf(eventId));
            assertThat(score).isEqualTo(String.valueOf(amount1 + amount2 + amount3));
        }

        @ParameterizedTest
        @EnumSource(EventType.class)
        @DisplayName("조회수 배치 증가 테스트")
        void increaseViewCountsTest(EventType eventType) {
            //given
            int eventCount = 100;
            List<EventViewsRankingItemInfo> items = new ArrayList<>();
            for(int i=0; i<eventCount; i++) {
                items.add(new EventViewsRankingItemInfo(
                        (long) i,
                        (long) i
                ));
            }

            //when
            eventViewRankingRepository.increaseDayViewCounts(
                    eventType,
                    testTime,
                    items
            );
            eventViewRankingRepository.increaseDayViewCounts(
                    eventType,
                    testTime,
                    items
            );

            //then
            Iterator<EventViewsRankingItemInfo> iterator = eventViewRankingRepository.getDayViewCountIterator(
                    eventType, testTime);
            while(iterator.hasNext()) {
                EventViewsRankingItemInfo item = iterator.next();
                assertThat(item.getViewCount()).isEqualTo(item.getEventId() * 2);
            }

        }

        @ParameterizedTest
        @EnumSource(EventType.class)
        @DisplayName("순회 테스트")
        void getDayViewCountIteratorTest(EventType eventType) {
            //given
            int eventCount = 100;
            Map<Long, Long> eventViewCountMap = new HashMap<>();
            for(int i=0;i<eventCount;i++) {
                eventViewCountMap.put((long) i, (long) i);
            }

            for(Long eventId : eventViewCountMap.keySet()) {
                Long viewCount = eventViewCountMap.get(eventId);
                eventViewRankingRepository.increaseDayViewCount(
                        eventId,
                        eventType,
                        testTime,
                        viewCount
                );
            }

            //when
            Iterator<EventViewsRankingItemInfo> iterator = eventViewRankingRepository.getDayViewCountIterator(
                    eventType,
                    testTime
            );

            //then
            int iterateCount = 0;
            while(iterator.hasNext()) {
                iterateCount++;
                EventViewsRankingItemInfo item = iterator.next();
                assertThat(eventViewCountMap.get(item.getEventId())).isEqualTo(item.getViewCount());
            }
            assertThat(eventCount).isEqualTo(iterateCount);
        }

        @ParameterizedTest
        @EnumSource(EventType.class)
        @DisplayName("특정 날짜의 조회수 전부 삭제 테스트")
        void deleteAllDayViewCountByTimeTest(EventType eventType) {
            //given
            Long eventId = 1L;
            Long amount = 5L;

            eventViewRankingRepository.increaseDayViewCount(
                    eventId,
                    eventType,
                    testTime,
                    amount
            );
            eventViewRankingRepository.increaseDayViewCount(
                    eventId,
                    eventType,
                    testTime2,
                    amount
            );

            //when
            eventViewRankingRepository.deleteAllDayViewCountByTime(eventType, testTime);

            //then
            String deleted = (String) redisTemplate.opsForHash().get(hSetKeys.get(eventType),
                    String.valueOf(eventId));

            String notDeleted = (String) redisTemplate.opsForHash().get(hSetKeys2.get(eventType),
                    String.valueOf(eventId));
            //지워져야 함
            assertThat(deleted).isEqualTo(null);
            //남아있어야 함
            assertThat(Long.parseLong(notDeleted)).isEqualTo(amount);
        }
    }
}