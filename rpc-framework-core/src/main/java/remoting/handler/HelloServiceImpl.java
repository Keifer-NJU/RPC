package remoting.handler;

import com.example.serviceapi.HelloService;
import com.example.serviceapi.Person;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Keifer
 * @createTime 2021/3/9 17:23
 */
@Slf4j
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(Person person) {
        log.info("HelloServiceImpl收到：{}.",person.getDescription());
        String result = "Hello " + person.getName() + " you age is " + person.getAge();
        log.info("HelloServiceImpl 返回了：{}.", result);
        return result;
    }
}

