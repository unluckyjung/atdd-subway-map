package wooteco.subway.station.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import wooteco.subway.exception.DuplicateNameException;
import wooteco.subway.exception.NotFoundException;
import wooteco.subway.station.domain.Station;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Sql("classpath:tableInit.sql")
public class StationRepositoryTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private StationRepository stationRepository;

    @BeforeEach
    void setUp() {
        stationRepository = new StationRepository(jdbcTemplate);
        String query = "INSERT INTO STATION(name) VALUES (?)";

        jdbcTemplate.update(query, "잠실역");
        jdbcTemplate.update(query, "잠실새내역");
    }

    @DisplayName("역을 DB에 저장하면, id가 생성된 역을 반환한다.")
    @Test
    void saveStation() {
        Station station = new Station("석촌역");

        Station savedStation = stationRepository.save(station);
        assertThat(savedStation.getId()).isEqualTo(3L);
    }

    @DisplayName("DB에 있는 역이름을 찾으면, true를 반환한다.")
    @Test
    void isExist() {
        assertThat(stationRepository.isExistName(new Station("잠실역"))).isTrue();
        assertThat(stationRepository.isExistName(new Station("잠실새내역"))).isTrue();
        assertThat(stationRepository.isExistName(new Station("석촌역"))).isFalse();
    }

    @DisplayName("DB에 있는 역들을 조회하면, 역을 담은 리스트를 반환한다.")
    @Test
    void findAll() {
        List<Station> stations = Arrays.asList(new Station(1L, "잠실역"), new Station(2L, "잠실새내역"));
        assertThat(stationRepository.getStations()).usingRecursiveComparison().isEqualTo(stations);
    }

    @DisplayName("id를 통해 삭제 요청을 하면, DB에 있는 해당 id 역을 삭제한다")
    @Test
    void deleteById() {
        Long id = 1L;

        String query = "SELECT EXISTS(SELECT * FROM station WHERE id = ?)";
        assertThat(jdbcTemplate.queryForObject(query, Boolean.class, id)).isTrue();

        stationRepository.deleteById(id);
        assertThat(jdbcTemplate.queryForObject(query, Boolean.class, id)).isFalse();
    }

    @DisplayName("중복된 name을 가진 Station을 저장하려고 하면, 예외가 발생한다.")
    @Test
    void saveDuplicateName() {
        Station station = new Station("잠실역");
        assertThatThrownBy(() -> stationRepository.save(station))
                .isInstanceOf(DuplicateNameException.class).hasMessageContaining("중복되는 StationName 입니다.");
    }

    @DisplayName("없는 id의 Station을 삭제하려고 하면, 예외가 발생한다.")
    @Test
    void deleteByWrongId() {
        assertThatThrownBy(() -> stationRepository.deleteById(100L))
                .isInstanceOf(NotFoundException.class).hasMessageContaining("존재하지 않는 id 입니다.");
    }
}