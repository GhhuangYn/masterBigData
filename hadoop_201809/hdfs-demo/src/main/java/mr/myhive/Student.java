package mr.myhive;

import java.util.Comparator;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/9 8:28
 */
public class Student implements Comparator<Student> {

    private int age;

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }


    @Override
    public String toString() {
        return "Student{" +
                "age=" + age +
                '}';
    }

    @Override
    public int compare(Student o1, Student o2) {
        return o1.getAge() - o2.getAge();
    }
}
