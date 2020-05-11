package bookrental;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BooklistRepository extends CrudRepository<Booklist, Long> {


}