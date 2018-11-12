package callLog.service;

import callLog.dao.PersonRepository;
import callLog.domain.Person;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/11/11 20:39
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class PersonServiceTest {

    @Autowired
    private PersonService personService;

    @Test
    public void getByPhone() {
        Person person = personService.getByPhone("13147193238");
        System.out.println(person.getName());
    }
}