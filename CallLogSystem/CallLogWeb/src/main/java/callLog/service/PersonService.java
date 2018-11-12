package callLog.service;

import callLog.dao.PersonRepository;
import callLog.domain.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/11/11 20:33
 */
@Service
public class PersonService {

    @Autowired
    private PersonRepository personRepository;

    public Person getByPhone(String phone) {
        return personRepository.findByPhone(phone);
    }

}
