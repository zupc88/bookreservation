package bookrental;

import org.springframework.data.repository.CrudRepository;
import java.util.Optional;

public interface ReservationRepository extends CrudRepository<Reservation, Long> {
    Optional<Reservation> findBybookid(String BookId);
}