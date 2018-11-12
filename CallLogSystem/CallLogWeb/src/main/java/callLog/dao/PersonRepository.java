package callLog.dao;

import callLog.domain.Person;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/11/11 20:31
 */
@Repository
public interface PersonRepository extends CrudRepository<Person, Integer> {

    Person findByPhone(String phone);
}
